package cscg.ui.components;

import java.util.EventListener;

/**
 * Posluchaèi zmìn bodu.
 * @author Tomáš Režnar
 */
public interface PointChangedListener extends EventListener
{
	/**
	 * Zmìna bodu.
	 */
	void pointChanged(PointChangedEvent e);
}
