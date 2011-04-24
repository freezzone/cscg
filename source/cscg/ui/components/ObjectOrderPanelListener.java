package cscg.ui.components;

import java.util.EventObject;

/**
 * Posluchaè událostí v panelu pro nastavení hiearchie objektù.
 * @author Tomáš Režnar
 */
public interface ObjectOrderPanelListener
{
	/**
	 * Pøesun onjektu nahoru.
	 */
	void moveUpEvent(EventObject e);

	/**
	 * Pøesun objektu dolù.
	 */
	void moveDownEvent(EventObject e);

	/**
	 * Zmìna viditelnosti objektu.
	 */
	void visibilityChangeEvent(EventObject e);

	/**
	 * Zmìna exkluzivní viditelnosti.
	 */
	void exlusiveVisibilityChangeEvent(EventObject e);

	/**
	 * Smazání objektu.
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
