package cscg.ui.components;

import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPointsAddable;
import java.util.List;

/**
 * Interface posluchačů panelu editoru {@link Editor}.
 * @author Tomáš Režnar
 */
public interface EditorListener
{

	/**
	 * Událost přidání bodu k objektu.
	 * @param object Objekt ke kterému je bod přidáván.
	 * @param newPoint Nový bod.
	 */
	public void evenAddPoint(Editor source, IObject object, IPoint3f newPoint);

	/**
	 * Událost relativního posunu bodu objektu.
	 * @param object Objekt jež obsahuje bod.
	 * @param p Posunovaný bod.
	 * @param x Posun ve směru x.
	 * @param y Posun ve směru y.
	 * @param z Posun ve směru z.
	 */
	public void eventMovePointRelative(Editor source, IObject object, IPoint3f p, float x, float y, float z);

	/**
	 * Událost relativního posunu bodů.
	 * @param object Měněný objekt.
	 * @param points Měněné body.
	 * @param x Posun x.
	 * @param y Posun y.
	 * @param z Posun z.
	 */
	public void eventMovePointsRelative(Editor aThis, IObject object, List<IPoint3f> points, float x, float y, float z);

	/**
	 * Událost smazání bodů jež jsou v aktuálním výběru.
	 * @param object Objekt jehož body budou smazány.
	 */
	public void eventDeleteSelectedPoints(Editor source, IPointsAddable object);

	/**
	 * Událost zrušení výběru bodů.
	 * @param object Objekt jehož výběr se ruší.
	 */
	public void eventCancelSelectedPoints(Editor source, IObject object);

	/**
	 * Přidání bodu do výběru objektu.
	 * @param object Objekt jehož výběr se mění.
	 * @param point Nově vybraný bod.
	 */
	public void eventAddSelectedPoint(Editor source, IObject object, IPoint3f point);

	/**
	 * Nastavení bodu do výběru objektu.
	 * @param object Objekt jehož výběr se mění.
	 * @param point Nově vybraný bod.
	 */
	public void eventSetSelectedPoint(Editor source, IObject object, IPoint3f point);
}
