package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Ud�lost uvoln�n� my�i.
 * @author Tom� Re�nar
 */
public class ViewportMouseRelasedEvent extends ViewportMouseEvent
{
	/**
	 * @param evt Zdrojov� ud�lost.
	 */
	public ViewportMouseRelasedEvent(MouseEvent evt)
	{
		super(evt);
	}
}
