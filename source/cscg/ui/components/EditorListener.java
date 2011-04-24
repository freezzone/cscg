package cscg.ui.components;

import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPointsAddable;
import java.util.List;

/**
 * Interface poslucha�� panelu editoru {@link Editor}.
 * @author Tom� Re�nar
 */
public interface EditorListener
{

	/**
	 * Ud�lost p�id�n� bodu k objektu.
	 * @param object Objekt ke kter�mu je bod p�id�v�n.
	 * @param newPoint Nov� bod.
	 */
	public void evenAddPoint(Editor source, IObject object, IPoint3f newPoint);

	/**
	 * Ud�lost relativn�ho posunu bodu objektu.
	 * @param object Objekt je� obsahuje bod.
	 * @param p Posunovan� bod.
	 * @param x Posun ve sm�ru x.
	 * @param y Posun ve sm�ru y.
	 * @param z Posun ve sm�ru z.
	 */
	public void eventMovePointRelative(Editor source, IObject object, IPoint3f p, float x, float y, float z);

	/**
	 * Ud�lost relativn�ho posunu bod�.
	 * @param object M�n�n� objekt.
	 * @param points M�n�n� body.
	 * @param x Posun x.
	 * @param y Posun y.
	 * @param z Posun z.
	 */
	public void eventMovePointsRelative(Editor aThis, IObject object, List<IPoint3f> points, float x, float y, float z);

	/**
	 * Ud�lost smaz�n� bod� je� jsou v aktu�ln�m v�b�ru.
	 * @param object Objekt jeho� body budou smaz�ny.
	 */
	public void eventDeleteSelectedPoints(Editor source, IPointsAddable object);

	/**
	 * Ud�lost zru�en� v�b�ru bod�.
	 * @param object Objekt jeho� v�b�r se ru��.
	 */
	public void eventCancelSelectedPoints(Editor source, IObject object);

	/**
	 * P�id�n� bodu do v�b�ru objektu.
	 * @param object Objekt jeho� v�b�r se m�n�.
	 * @param point Nov� vybran� bod.
	 */
	public void eventAddSelectedPoint(Editor source, IObject object, IPoint3f point);

	/**
	 * Nastaven� bodu do v�b�ru objektu.
	 * @param object Objekt jeho� v�b�r se m�n�.
	 * @param point Nov� vybran� bod.
	 */
	public void eventSetSelectedPoint(Editor source, IObject object, IPoint3f point);
}
