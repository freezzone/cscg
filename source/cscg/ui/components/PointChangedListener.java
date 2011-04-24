package cscg.ui.components;

import java.util.EventListener;

/**
 * Poslucha�i zm�n bodu.
 * @author Tom� Re�nar
 */
public interface PointChangedListener extends EventListener
{
	/**
	 * Zm�na bodu.
	 */
	void pointChanged(PointChangedEvent e);
}
