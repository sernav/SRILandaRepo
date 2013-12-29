package es.uclm.sri.sis.ponderacion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import es.uclm.sri.sis.entidades.Album;
import es.uclm.sri.sis.entidades.AlbumPonderado;
import es.uclm.sri.sis.entidades.Genero;
import es.uclm.sri.sis.utilidades.FicheroDPropiedades;
import es.uclm.sri.sis.utilidades.UtilsDAlbum;
import es.uclm.sri.sis.utilidades.UtilsDLastfm;

/**
 * <code>PonderacionDAlbum</code> recoge los tags de cada uno de los discos
 * y los translada a g�neros estandar del sistema. Calcula el peso de cada
 * g�nero estandar para cada disco.
 * 
 * La suma de todos los pesos de un album es igual a 1.
 * 
 * 
 * @author Sergio Navarro
 * @date Dic, 2013
 * */
public class PonderacionDAlbum implements IPonderacion {
    
    private Album album;
    private de.umass.lastfm.Album albumLastfm;
    private AlbumPonderado albumPonderado;
    private boolean isAlbumLastfm;
    
    private static Collection<String> listaTags;
    private static Collection<Genero> listaGeneros;
    
    private static FicheroDPropiedades properties;
    private static final Logger logger = Logger.getLogger(PonderacionDAlbum.class);
    
    private final String PATH_GENEROS_STD = "src/es/uclm/sri/properties/generoEstandar.properties";
    
    public PonderacionDAlbum(Album album) {
        this.album = album;
        this.albumLastfm = null;
        this.isAlbumLastfm = false;
        
        this.listaTags = new ArrayList<String>();
        
        cargarGenerosEstandar();
    }
    
    public PonderacionDAlbum(de.umass.lastfm.Album albumLastfm) {
        this.albumLastfm = albumLastfm;
        this.album = null;
        this.isAlbumLastfm = true;
        
        this.listaTags = new ArrayList<String>();
        
        cargarGenerosEstandar();
    }
    
    /**
     * Funci�n que agrupa las operaciones para estandarizar y ponderar
     * un album. Primero se estandariza y despu�s calcula sus pesos.
     * 
     * @param
     * @return AlbumPonderado
     * */
    public AlbumPonderado procesar() {
        this.listaTags = estandarizarTags();
        this.listaGeneros = convertirTags();
        
        calcularPesos();
        
        if (this.isAlbumLastfm) {
            this.album = new Album();
            this.album.setArtista(this.albumLastfm.getArtist());
            this.album.setTitulo(this.albumLastfm.getName());
            this.album.setFecha("");
            this.album.setPais("");
            this.album.setNumTemas(this.albumLastfm.getTracks().size());
        }
        
        crearAlbumPonderado();
        
        return this.albumPonderado;
    }
    
    protected void crearAlbumPonderado() {
        Double[] pesosAlbum = null;
        this.albumPonderado = new AlbumPonderado(this.album, construirVectorDGeneros());
    }
    
    private Double[] construirVectorDGeneros() {
        ArrayList<String> listKeyProp = properties.getPropiedades();
        Double[] generos = new Double[18];
        iniciarVectorGeneros(generos);
        
        for(int i=0; i < listKeyProp.size(); i++) {
            for (int j=0; j < this.listaGeneros.size(); j++) {
                if (listKeyProp.get(i).equals(((ArrayList<Genero>)this.getListaGeneros()).get(j).getTipo())) {
                    Double valor = new Double(((ArrayList<Genero>)this.getListaGeneros()).get(j).getValorPonderado());
                    generos[i] = valor;
                }
            }
        }
        return generos;
    }
    
    private Double[] iniciarVectorGeneros(Double[] generos) {
        for(int i=0; i < generos.length; i++) {
            Double cero = new Double(0);
            generos[i] = cero;
        }
        return generos;
    }
    
    /**
     * Recoge los tags de un album, bien desde el propio album, 
     * desde los tracks del album o, en �ltima instancia, del artista.
     * 
     * Descompone en tags simples y lo transforma en un tag estandar
     * de la aplicaci�n.
     * 
     * @param
     * @return ArrayList<String>
     * */
    public ArrayList<String> estandarizarTags() {
        Collection<String> tags = null;
        if (this.isAlbumLastfm) {
            tags = UtilsDLastfm.extraerTagsLastfm(this.albumLastfm);
        } else {
            tags = this.album.getEtiquetas();
        }
        ArrayList<String> listaTagsStnd = new ArrayList<String>();
        
        Iterator<String> it = tags.iterator();
        while(it.hasNext()) {
            String tag = it.next();
            if (UtilsDAlbum.isEtiquetaCompuesta(tag)) {
                String[] etqSimples = UtilsDAlbum.descomponerEtiquetaCompuesta(tag);
                for (int i = 0; i < etqSimples.length; i++) {
                    String etqStdr = estandarizaGeneroSimple(etqSimples[i]);
                    if (etqStdr != "") {
                        listaTagsStnd.add(etqStdr);
                    }
                }
            } else {
                String etqStdr = estandarizaGeneroSimple(tag);
                if (etqStdr != "") {
                    listaTagsStnd.add(etqStdr);
                }
            }
        }
        return listaTagsStnd;
    }
    
    /**
     * Estandariza un tag simple. Comprueba que el tag est� en la lista
     * de g�neros del archivo de propiedades y lo devuelve.
     * 
     * @param String
     * @return String
     * */
    private String estandarizaGeneroSimple(String tag) {
        String generoStdr = "";
        StringTokenizer crud = null;
        ArrayList<String> listKeyProp = new ArrayList<String>();
        listKeyProp = properties.getPropiedades();
        
        boolean isGeneroStd = false;
        
        for(int i = 0; i < listKeyProp.size() && !isGeneroStd; i++) {
            String valorProp = properties.getValorPropiedad(listKeyProp.get(i));
            crud = new StringTokenizer(valorProp, ",");
            if (isGeneroDListaEstandar(crud, tag)) {
                String alistKeyProp = listKeyProp.get(i);
                alistKeyProp.split("\\.");
                generoStdr = listKeyProp.get(i);
                isGeneroStd = true;
            }
        }
        return generoStdr;
    }
    
    /**
     * El tag est� en los elementos de g�nero.
     * 
     * @param StringTokenizer
     * @param String
     * @return boolean
     * */
    private static boolean isGeneroDListaEstandar(StringTokenizer crud, String tag) {
        while(crud.hasMoreElements()) {
            if(tag.equals(crud.nextToken())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Genera objetos de la entidad <code>Genero</code> desde los tag
     * estandarizados. No tiene en cuenta repeticiones.
     * 
     * @param
     * @return ArrayList<Genero>
     * */
    public ArrayList<Genero> convertirTags() {
        ArrayList<Genero> listGeneros = new ArrayList<Genero>();
        HashMap<String, Genero> hashGeneros = new HashMap<String, Genero>();
        
        ArrayList<String> etiquetas = (ArrayList<String>) listaTags;
        
        for (int i = 0; i < etiquetas.size(); i++) {
            if (!hashGeneros.containsKey(etiquetas.get(i))) {
                int numOcurGenero = 1;
                Genero genero = new Genero();
                genero.setTipo(etiquetas.get(i));
                for (int j=0; j < etiquetas.size(); j++) {
                    if (i != j && !hashGeneros.containsKey(etiquetas.get(j))) {
                        if (genero.getTipo().equals(etiquetas.get(j))) {
                            numOcurGenero++;
                        }
                    }
                }
                genero.setNumOcurrencias(numOcurGenero);
                hashGeneros.put(genero.getTipo(), genero);
                listGeneros.add(genero);
            }
        }
        return listGeneros;
    }
    
    /**
     * Calcula los pesos de los g�neros.
     * El peso de cada g�nero se recalcula en funci�n del n�mero de
     * g�neros que tiene el album.
     * 
     * Ng = N�mero de g�neros
     * Na = N�mero apariciones de un g�nero en el album
     * Pu = Peso unidad de aparici�n
     * Pg = Peso de g�nero
     * 
     * Pu = 1 / Ng
     * Pg = Na * Pu
     * 
     * El valor se setea al g�nero correspondiente.
     * 
     * @param
     * @return
     * */
    public void calcularPesos() {
        double numGenerosGlobal = numGenerosDAlbumGlobal((ArrayList<Genero>)this.listaGeneros);
        if (numGenerosGlobal > 0) {
            double pesoUnidad = 1 / numGenerosGlobal;
            
            for(int i = 0; i < this.listaGeneros.size(); i++) {
                double numOcurr = ((ArrayList<Genero>)this.listaGeneros).get(i).getNumOcurrencias();
                ((ArrayList<Genero>)this.listaGeneros).get(i).setValorPonderado(numOcurr*pesoUnidad);
            }
        }
        
    }
    
    /**
     * N�mero de ocurrencias de un g�nero
     * 
     * @param ArrayList<Genero>
     * @return int
     * */
    private static int numGenerosDAlbumGlobal(ArrayList<Genero> generos) {
        int numGenerosGlobal = 0;
        
        for (int i=0; i < generos.size(); i++) {
            numGenerosGlobal += generos.get(i).getNumOcurrencias();
        }
        
        return numGenerosGlobal;
    }
    
    public Album getAlbum() {
        return this.album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public de.umass.lastfm.Album getAlbumLastfm() {
        return this.albumLastfm;
    }

    public void setAlbumLastfm(de.umass.lastfm.Album albumLastfm) {
        this.albumLastfm = albumLastfm;
    }
    
    public AlbumPonderado getAlbumPonderado() {
        return this.albumPonderado;
    }

    public boolean isAlbumLastfm() {
        return isAlbumLastfm;
    }
    
    public Collection<String> getListaTags() {
        return listaTags;
    }
    
    public Collection<Genero> getListaGeneros() {
        return listaGeneros;
    }
    
    private void cargarGenerosEstandar() {
        try {
            properties = new FicheroDPropiedades(PATH_GENEROS_STD);
            properties.cargarPropiedades();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ordenar(String tipoOrden) {
        
    }

}
