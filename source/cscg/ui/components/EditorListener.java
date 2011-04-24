package cscg.ui.components;

import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPointsAddable;
import java.util.List;

/**
 * Interface posluchaèù panelu editoru {@link Editor}.
 * @author Tomáš Režnar
 */
public interface EditorListener
{

	/**
	 * Událost pøidání bodu k objektu.
	 * @param object Objekt ke kterému je bod pøidáván.
	 * @param newPoint Nový bod.
	 */
	public void evenAddPoint(Editor source, IObject object, IPoint3f newPoint);

	/**
	 * Událost relativního posunu bodu objektu.
	 * @param object Objekt jež obsahuje bod.
	 * @param p Posunovaný bod.
	 * @param x Posun ve smìru x.
	 * @param y Posun ve smìru y.
	 * @param z Posun ve smìru z.
	 */
	public void eventMovePointRelative(Editor source, IObject object, IPoint3f p, float x, float y, float z);

	/**
	 * Událost relativního posunu bodù.
	 * @param object Mìnìný objekt.
	 * @param points Mìnìné body.
	 * @param x Posun x.
	 * @param y Posun y.
	 * @param z Posun z.
	 */
	public void eventMovePointsRelative(Editor aThis, IObject object, List<IPoint3f> points, float x, float y, float z);

	/**
	 * Událost smazání bodù jež jsou v aktuálním výbìru.
	 * @param object Objekt jehož body budou smazány.
	 */
	public void eventDeleteSelectedPoints(Editor source, IPointsAddable object);

	/**
	 * Událost zrušení výbìru bodù.
	 * @param object Objekt jehož výbìr se ruší.
	 */
	public void eventCancelSelectedPoints(Editor source, IObject object);

	/**
	 * Pøidání bodu do výbìru objektu.
	 * @param object Objekt jehož výbìr se mìní.
	 * @param point Novì vybraný bod.
	 */
	public void eventAddSelectedPoint(Editor source, IObject object, IPoint3f point);

	/**
	 * Nastavení bodu do výbìru objektu.
	 * @param object Objekt jehož výbìr se mìní.
	 * @param point Novì vybraný bod.
	 */
	public void eventSetSelectedPoint(Editor source, IObject object, IPoint3f point);
}
