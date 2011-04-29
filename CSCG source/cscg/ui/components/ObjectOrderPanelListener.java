package cscg.ui.components;

import java.util.EventObject;

/**
 * Posluchač událostí v panelu pro nastavení hiearchie objektů.
 * @author Tomáš Režnar
 */
public interface ObjectOrderPanelListener
{
	/**
	 * Přesun objektu nahoru.
	 */
	void moveUpEvent(EventObject e);

	/**
	 * Přesun objektu dolů.
	 */
	void moveDownEvent(EventObject e);

	/**
	 * Změna viditelnosti objektu.
	 */
	void visibilityChangeEvent(EventObject e);

	/**
	 * Změna exkluzivní viditelnosti.
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
