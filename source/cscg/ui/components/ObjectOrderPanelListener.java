package cscg.ui.components;

import java.util.EventObject;

/**
 * Poslucha� ud�lost� v panelu pro nastaven� hiearchie objekt�.
 * @author Tom� Re�nar
 */
public interface ObjectOrderPanelListener
{
	/**
	 * P�esun onjektu nahoru.
	 */
	void moveUpEvent(EventObject e);

	/**
	 * P�esun objektu dol�.
	 */
	void moveDownEvent(EventObject e);

	/**
	 * Zm�na viditelnosti objektu.
	 */
	void visibilityChangeEvent(EventObject e);

	/**
	 * Zm�na exkluzivn� viditelnosti.
	 */
	void exlusiveVisibilityChangeEvent(EventObject e);

	/**
	 * Smaz�n� objektu.
	 */
	void deleteEvent(EventObject e);

	/**
	 * Rotace objektu.
	 */
	void rotateEvent(RotateEvent e);

	/**
	 * Posun objektu.
	 */
	void moveEvent(MoveEvent e);
}
