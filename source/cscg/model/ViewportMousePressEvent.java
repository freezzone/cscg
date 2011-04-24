package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Událost stisku myši.
 * @author Tomáš Režnar
 */
public class ViewportMousePressEvent extends ViewportMouseEvent
{
	/**
	 * @param evt Zdrojová událost.
	 */
	public ViewportMousePressEvent(MouseEvent evt)
	{
		super(evt);
	}
}
