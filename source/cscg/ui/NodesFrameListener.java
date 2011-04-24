package cscg.ui;

import cscg.model.objects.IKnotVector;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import java.util.EventListener;

/**
 * Poslucha� ud�lost� okna editace uzl�.
 * @author Tom� Re�nar
 */
public interface NodesFrameListener extends EventListener
{

	/**
	 * Zm�na bodu.
	 * @param object Objekt jeho� bod je m�n�n.
	 * @param oldPoint P�vodn� bod.
	 * @param newPoint Zm�n�n� bod.
	 */
	public void eventPointChanged(IObject object, IPoint3f oldPoint, IPoint3f newPoint);

	/**
	 * Zm�na v�b�ru bod�.
	 * @param object Objekt jeho� bod je m�n�n.
	 * @param selectedPoints Pole index� bod� je� maj� b�t vybr�ny.
	 */
	public void eventPointSelectionChanged(IObject object, int[] selectedPoints);

	/**
	 * Ud�lost zm�ny uzlov�ho vektoru.
	 * @param object Objekt jeho� je uzlov� vektor.
	 * @param knot Uzlov� vektor.
	 * @param index Index uzlu ve vektoru.
	 * @param value Nov� hodnota uzlu.
	 */
	public void eventChangeKnotNode(IObject object, IKnotVector knot, int index, float value);
}
