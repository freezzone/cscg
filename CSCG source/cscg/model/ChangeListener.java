package cscg.model;

import java.util.EventObject;

/**
 * Posluchač změn objektu.
 * @author Tomáš Režnar
 */
public interface ChangeListener
{
	/**
	 * Událost změny sledovaného objektu.
	 */
	void changeEvent(EventObject e);
}
