package cscg.model;

import java.util.EventObject;

/**
 * Poslucha� zm�n objektu.
 * @author Tom� Re�nar
 */
public interface ChangeListener
{
	/**
	 * Ud�lost zm�ny sledovan�ho objektu.
	 */
	void changeEvent(EventObject e);
}
