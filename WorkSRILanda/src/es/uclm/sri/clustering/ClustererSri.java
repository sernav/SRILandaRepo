package es.uclm.sri.clustering;

import java.io.IOException;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import es.uclm.sri.clustering.weka.AnalysisFactory;
import es.uclm.sri.clustering.weka.WekaDatosCluster;
import es.uclm.sri.clustering.weka.WekaSRIInstance;
import es.uclm.sri.clustering.weka.WekaSimpleKMeansCluster;
import es.uclm.sri.sis.KSistema;
import es.uclm.sri.sis.log.Log;
import es.uclm.sri.sis.utilidades.FicheroDPropiedades;
import es.uclm.sri.sis.utilidades.Utils;

/**
 * 
 * @author Sergio Navarro
 * @date Dic, 2013
 * */
public class ClustererSri {
    
    private static Instances data = null;
    private static SimpleKMeans clusterer = null;
    private static WekaSimpleKMeansCluster wekaKMeans = null;
    
    private static FicheroDPropiedades properties = null;
    
    private static ClustererSri instance;
    
    private ClustererSri() {
        cargarPropiedadesBD();
        
        InstanceQuery query;
        try {
            Log.log("Instanciando base de datos Postgresql. PESOSALBUM", 1);
            query = new InstanceQuery();
//            query.setUsername(properties.getValorPropiedad("username"));
//            query.setPassword(properties.getValorPropiedad("password"));
            query.setUsername("postgres");
            query.setPassword("root");
            query.setQuery("SELECT * FROM \"PESOSALBUM\"");
            
            Log.log("Extrayendo instancias de la base de datos", 1);
            data = query.retrieveInstances();
            
            construirCluterer(data);
        } catch (WekaException e) {
            e.printStackTrace();
            Log.log(e, "(" + ClustererSri.class.getSimpleName() + ") Weka Exception! " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.log(e, "(" + ClustererSri.class.getSimpleName() + ") IO Exception! " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.log(e, "(" + ClustererSri.class.getSimpleName() + ") General Exception! " + e.getMessage());
        }
    }
    
    public static ClustererSri getInstance() {
        Log.log("====== INVOCANDO A SISTEMA DE CLUSTERING WEKA ======", 2);
        createInstance();
        return instance;
    }
    
    private static void createInstance() {
        if (instance == null) {
            synchronized (ClustererSri.class) {
                if (instance == null) {
                    instance = new ClustererSri();
                }
            }
        }
    }
    
    protected void construirCluterer(Instances data) throws Exception {
        Log.log("Construyendo clustering con Weka para los par�metros configurados", 1);
        Instances dataAux = null;
        String[] options = new String[2];
        options[0] = "-I"; // max. iterations
        options[1] = "500";

        // Algoritmo SimpleKMeans de Weka
        clusterer = new SimpleKMeans();
        clusterer.setOptions(options);
        clusterer.setNumClusters(8);
        clusterer.setSeed(10);
        
        /*
         * Filtro para los atributos nombre de album y artista
         * 
         * */
        int[] attributes = {0, 1, 2, 21};
        Remove rm = new Remove();
        
        rm.setAttributeIndicesArray(attributes);
        rm.setInputFormat(data);
        dataAux = Filter.useFilter(data, rm);

        // Funci�n de c�lculo de distancias: Euclidea
        EuclideanDistance df = new EuclideanDistance(dataAux);
        df.setAttributeIndices("first-last");
        df.setDontNormalize(false);
        df.setInvertSelection(false);

        clusterer.setDistanceFunction(df);
        
        Log.log("-Algoritmo: SimpleKMeans", 1);
        Log.log("-Funci�n de c�lculo de distancias: D. Euclidea", 1);
        Log.log("-N�mero de clusters: 8", 1);
        Log.log("-N�mero de iteraciones: 500", 1);

        // Construcci�n del clusterer con las instancias traidas
        Log.log("Construcci�n del clusterer con las instancias traidas...", 1);
        clusterer.buildClusterer(dataAux);
        
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
    }
    
    public WekaSRIInstance[] generarRecomendacionesWeka(WekaSRIInstance inst) throws Exception {
        Log.log("Generando recomendaciones del clustering", 1);
        AnalysisFactory.buildFactory();

        WekaDatosCluster wekaDatosCluster = (WekaDatosCluster) AnalysisFactory
                .createRawData(data);
        wekaKMeans = new WekaSimpleKMeansCluster();
        wekaKMeans.setInputData(wekaDatosCluster);
        wekaKMeans.setK(clusterer.numberOfClusters());
        wekaKMeans.setSimpleKMeans(clusterer);

        int iCluster = clasificarInstanciaWeka(inst);
        Log.log("Cluster #" + iCluster, 1);
        Instance centroide = clusterer.getClusterCentroids().instance(iCluster);
        
        WekaSRIInstance[] instCluster = wekaKMeans.getWekaSRIInstancesDCluster(data.enumerateInstances(), iCluster);
        WekaSRIInstance[] resultados = wekaDatosCluster.getSimiliarWekaSRIInstance(instCluster, inst.getInstance(), 10);
        Log.log("Recomendaciones del clustering: ", 1);
        Log.log(toStringRecomendaciones(resultados), 1);
        return resultados;
    }
    
    public WekaSRIInstance[] generarRecomendacionesWeka(Double[] attValues) throws Exception {
        Log.log("Generando recomendaciones del clustering", 1);
        WekaSRIInstance inst = new WekaSRIInstance(1.0, attValues);

        AnalysisFactory.buildFactory();

        WekaDatosCluster wekaDatosCluster = (WekaDatosCluster) AnalysisFactory
                .createRawData(data);
        wekaKMeans = new WekaSimpleKMeansCluster();
        wekaKMeans.setInputData(wekaDatosCluster);
        wekaKMeans.setK(clusterer.numberOfClusters());
        wekaKMeans.setSimpleKMeans(clusterer);

        int iCluster = clasificarInstanciaWeka(inst);

        Instance centroide = clusterer.getClusterCentroids().instance(iCluster);

        double dEuclidea = wekaDatosCluster.getDistanciaEuclideaCentroide(
                centroide.toDoubleArray(), Utils.toDoubleArray(attValues));
        
        WekaSRIInstance[] instCluster = wekaKMeans.getWekaSRIInstancesDCluster(data.enumerateInstances(), iCluster);
        return wekaDatosCluster.getSimiliarWekaSRIInstance(instCluster, inst.getInstance(), 10);
        
    }
    
    protected void generarWekaSimpleKMeans(Instances data) {
        WekaDatosCluster wekaDatosCluster = new WekaDatosCluster(data);
        wekaKMeans = new WekaSimpleKMeansCluster();
        wekaKMeans.setInputData(wekaDatosCluster);
    }

    protected int clasificarInstanciaWeka(WekaSRIInstance wekaInst)
            throws Exception {
        int cluster = wekaKMeans.clusterInstance(wekaInst.getInstance());

        return cluster;
    }

    protected String evaluadorClusterer() {
        ClusterEvaluation eval = new ClusterEvaluation();
        if (this.clusterer != null)
            eval.setClusterer(this.clusterer);

        return eval.clusterResultsToString();
    }
    
    private void cargarPropiedadesBD() {
        try {
            properties = new FicheroDPropiedades(KSistema.Recursos.PATH_DATABASE_PROPERTIES);
            properties.cargarPropiedades();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String toStringRecomendaciones(WekaSRIInstance[] resultados) {
        StringBuffer sb = new StringBuffer();
        int cont = 1;
        for (int i = 0; i < resultados.length; i++) {
            String artista = resultados[i].getArtita();
            String album = resultados[i].getTitulo();
            sb.append("#" + cont + " ");
            sb.append(artista);
            sb.append(" -- ");
            sb.append(album);
            sb.append(" | ");
            cont++;
        }
        return sb.toString();
    }

}