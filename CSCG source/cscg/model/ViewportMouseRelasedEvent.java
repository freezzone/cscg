package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Událost uvolnění myši.
 * @author Tomáš Režnar
 */
public class ViewportMouseRelasedEvent extends ViewportMouseEvent
{
	/**
	 * @param evt Zdrojová událost.
	 */
	public ViewportMouseRelasedEvent(MouseEvent evt)
	{
		super(evt);
	}
}
