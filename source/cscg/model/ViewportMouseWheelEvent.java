package cscg.model;

import java.awt.event.MouseWheelEvent;

/**
 * Ud�lost rotace kole�ka my�i.
 * @author Tom� Re�nar
 */
public class ViewportMouseWheelEvent extends ViewportMouseEvent
{

	/**
	 * @param evt Zdrojov� ud�lost.
	 */
	public ViewportMouseWheelEvent(MouseWheelEvent evt)
	{
		super(evt);
	}

	/**
	 * Zdrojov� ud�lost rotace.
	 */
	public MouseWheelEvent getWheelEvt()
	{
		return (MouseWheelEvent) super.getEvt();
	}
}
