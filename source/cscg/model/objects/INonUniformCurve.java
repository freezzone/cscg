package cscg.model.objects;

/**
 * Interface k�ivek, kter� jsou definov�ny pomoc� uzlov�ho vektoru.
 * @author Tom� Re�nar
 */
public interface INonUniformCurve extends IDegree, IObject
{

	/**
	 * Z�sk�n� uzlov�ho vektoru.
	 */
	public IKnotVector getKnot();
}
