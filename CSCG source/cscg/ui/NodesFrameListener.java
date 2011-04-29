package cscg.ui;

import cscg.model.objects.IKnotVector;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import java.util.EventListener;

/**
 * Posluchač událostí okna editace uzlů.
 * @author Tomáš Režnar
 */
public interface NodesFrameListener extends EventListener
{

	/**
	 * Změna bodu.
	 * @param object Objekt jehož bod je měněn.
	 * @param oldPoint Původní bod.
	 * @param newPoint Změněný bod.
	 */
	public void eventPointChanged(IObject object, IPoint3f oldPoint, IPoint3f newPoint);

	/**
	 * Změna výběru bodů.
	 * @param object Objekt jehož bod je měněn.
	 * @param selectedPoints Pole indexů bodů jež mají být vybrány.
	 */
	public void eventPointSelectionChanged(IObject object, int[] selectedPoints);

	/**
	 * Událost změny uzlového vektoru.
	 * @param object Objekt jehož je uzlový vektor.
	 * @param knot Uzlový vektor.
	 * @param index Index uzlu ve vektoru.
	 * @param value Nová hodnota uzlu.
	 */
	public void eventChangeKnotNode(IObject object, IKnotVector knot, int index, float value);
}
