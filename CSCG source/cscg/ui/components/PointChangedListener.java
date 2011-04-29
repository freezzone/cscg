package cscg.ui.components;

import java.util.EventListener;

/**
 * Posluchači změn bodu.
 * @author Tomáš Režnar
 */
public interface PointChangedListener extends EventListener
{
	/**
	 * Změna bodu.
	 */
	void pointChanged(PointChangedEvent e);
}
