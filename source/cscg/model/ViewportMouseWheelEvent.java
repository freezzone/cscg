package cscg.model;

import java.awt.event.MouseWheelEvent;

/**
 * Událost rotace koleèka myši.
 * @author Tomáš Režnar
 */
public class ViewportMouseWheelEvent extends ViewportMouseEvent
{

	/**
	 * @param evt Zdrojová událost.
	 */
	public ViewportMouseWheelEvent(MouseWheelEvent evt)
	{
		super(evt);
	}

	/**
	 * Zdrojová událost rotace.
	 */
	public MouseWheelEvent getWheelEvt()
	{
		return (MouseWheelEvent) super.getEvt();
	}
}
