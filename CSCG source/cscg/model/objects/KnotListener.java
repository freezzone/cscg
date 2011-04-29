package cscg.model.objects;

/**
 * Posluchač změn knot vektoru {@link IKnotVector} objektů.
 * @author Tomáš Režnar
 */
public interface KnotListener
{

	public void eventKnotChanged(IKnotVector source);
}
