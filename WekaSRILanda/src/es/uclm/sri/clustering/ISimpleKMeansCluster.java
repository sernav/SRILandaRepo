package es.uclm.sri.clustering;

/**
 * Intefaz para definir las funciones b�sicas del algor�tmo Simple K Means
 * 
 * @author sernav
 * @version 0.1
 * @since 0.1
 * 
 * */
public interface ISimpleKMeansCluster {

	public void doPCA(boolean doPCA);

	/**
	 * Set valor K (n�mero de clusters)
	 * 
	 * @param k
	 */
	public void setK(int k);

	/**
	 * Get valor K (n�mero de clusters)
	 * 
	 * @return
	 */
	public int getK();

}
