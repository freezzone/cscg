package cscg.model.objects;

/**
 * Interface ploch, kter� jsou definov�ny pomoc� uzlov�ch vektor�.
 * @author Tom� Re�nar
 */
public interface INonUniformSurface<PointClass extends IPoint3f> extends IDegree,ISurface<PointClass> {
	/**
	 * Z�sk�n� uzlov�ho vektoru ��dk�
	 */
	public IKnotVector getRowKnot();
	/**
	 * Z�sk�n� uzlov�ho vektoru sloupc�
	 */
	public IKnotVector getColumnKnot();
}
