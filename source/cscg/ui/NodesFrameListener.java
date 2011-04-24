package cscg.ui;

import cscg.model.objects.IKnotVector;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import java.util.EventListener;

/**
 * Posluchaè událostí okna editace uzlù.
 * @author Tomáš Režnar
 */
public interface NodesFrameListener extends EventListener
{

	/**
	 * Zmìna bodu.
	 * @param object Objekt jehož bod je mìnìn.
	 * @param oldPoint Pùvodní bod.
	 * @param newPoint Zmìnìný bod.
	 */
	public void eventPointChanged(IObject object, IPoint3f oldPoint, IPoint3f newPoint);

	/**
	 * Zmìna výbìru bodù.
	 * @param object Objekt jehož bod je mìnìn.
	 * @param selectedPoints Pole indexù bodù jež mají být vybrány.
	 */
	public void eventPointSelectionChanged(IObject object, int[] selectedPoints);

	/**
	 * Událost zmìny uzlového vektoru.
	 * @param object Objekt jehož je uzlový vektor.
	 * @param knot Uzlový vektor.
	 * @param index Index uzlu ve vektoru.
	 * @param value Nová hodnota uzlu.
	 */
	public void eventChangeKnotNode(IObject object, IKnotVector knot, int index, float value);
}
