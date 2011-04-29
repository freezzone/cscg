package cscg.model.objects;

/**
 * Interface křivek, které jsou definovány pomocí uzlového vektoru.
 * @author Tomáš Režnar
 */
public interface INonUniformCurve extends IDegree, IObject
{

	/**
	 * Získání uzlového vektoru.
	 */
	public IKnotVector getKnot();
}
