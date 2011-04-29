package cscg.model.objects;

/**
 * Interface ploch, které jsou definovány pomocí uzlových vektorů.
 * @author Tomáš Režnar
 */
public interface INonUniformSurface<PointClass extends IPoint3f> extends IDegree,ISurface<PointClass> {
	/**
	 * Získání uzlového vektoru řádků
	 */
	public IKnotVector getRowKnot();
	/**
	 * Získání uzlového vektoru sloupců
	 */
	public IKnotVector getColumnKnot();
}
