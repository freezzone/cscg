package cscg.model.objects;

/**
 * Posluchaè zmìn knot vektoru objektù.
 * @author Tomáš Režnar
 */
public interface KnotListener
{

	public void eventKnotChanged(IKnotVector source);
}
