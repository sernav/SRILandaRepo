package es.uclm.sri.cache;

import net.sf.ehcache.Element;
import es.uclm.sri.persistencia.postgre.dao.PesosalbumMapper;
import es.uclm.sri.persistencia.postgre.dao.model.Pesosalbum;
import es.uclm.sri.sis.KSistema;

/**
 * Crea y recupera instancias de <code>CachePesosAlbum</code>. Utiliza el patr�n
 * Singleton. Recoge elementos de la cach� y actualiza la misma.
 * Si el elemento no est�, se recupera de la tabla de base de datos.
 * 
 * @author Sergio Navarro
 * */
public class CachePesosAlbum extends AbstractCache {
	
	private static CachePesosAlbum instance = null;
	public Element[] elements;
	
	private CachePesosAlbum() {
		nombreCache = KSistema.Cache.cacheVPesosAlbum;
		tablaDCache = KSistema.Tablas.albums;
		cache = getCache();
	}
	
	private static void createInstance() {
		if (instance == null) {
			synchronized (CachePesosAlbum.class) {
				if (instance == null)
					instance = new CachePesosAlbum();
			}
		}
	}
	
	public static CachePesosAlbum getInstance() {
		createInstance();
		return instance;
	}
	
	public Element[] getElementosDCacheById(String id) {
		Element[] elements = null;
		// calcularElementosDPesosAlbum(Usuario user);
		for (Element element : elements) {
			loadElementoEnCache(element);
		}
		return elements;
	}
	
	public void actualizarElementoDCache(Element element) {
		String keyOldElement = "ID_ALBUM";
		Element oldElement = cache.get(keyOldElement);
		cache.replace(oldElement, element);
	}
	
	public Element getElementosDTablaById(Integer id) {
		try{
			sqlMapper.openSession();
			PesosalbumMapper mapper = session.getMapper(PesosalbumMapper.class);
			Pesosalbum pesosAlbum = mapper.selectByPrimaryKey(id);
			element = new Element(pesosAlbum.getID_PESOSALBUM(), pesosAlbum);
			return element;
		} finally {
			session.close();
		}	
		
	}
}
