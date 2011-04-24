package cscg.model.objects;

/**
 * Interface ploch, které jsou definovány pomocí uzlových vektorù.
 * @author Tomáš Režnar
 */
public interface INonUniformSurface<PointClass extends IPoint3f> extends IDegree,ISurface<PointClass> {
	/**
	 * Získání uzlového vektoru øádkù
	 */
	public IKnotVector getRowKnot();
	/**
	 * Získání uzlového vektoru sloupcù
	 */
	public IKnotVector getColumnKnot();
}
