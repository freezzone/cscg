package cscg.model;

import java.util.EventObject;

/**
 * Posluchaè zmìn objektu.
 * @author Tomáš Režnar
 */
public interface ChangeListener
{
	/**
	 * Událost zmìny sledovaného objektu.
	 */
	void changeEvent(EventObject e);
}
