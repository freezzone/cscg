package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Obecn� ud�lost my�i.
 * @author Tom� Re�nar
 */
public class ViewportMouseEvent
{

	/**
	 * Zdrojov� ud�lost my�i.
	 */
	protected MouseEvent evt;
	/**
	 * Pozice kde k ud�losti do�lo.
	 */
	protected MouseScreenPosition cursorPosition;

	public ViewportMouseEvent(MouseEvent evt)
	{
		this.evt = evt;
		this.cursorPosition = new MouseScreenPosition(evt);
	}

	/**
	 * Zjist� zda bylo p�i ud�losti stiknuto tla��tko 1.
	 */
	public boolean isButton1()
	{
		return evt.getButton() == MouseEvent.BUTTON1;
	}

	/**
	 * Zjist� zda bylo p�i ud�losti stiknuto tla��tko 2.
	 */
	public boolean isButton2()
	{
		return evt.getButton() == MouseEvent.BUTTON2;
	}

	/**
	 * Zjist� zda bylo p�i ud�losti stiknuto tla��tko 3.
	 */
	public boolean isButton3()
	{
		return evt.getButton() == MouseEvent.BUTTON3;
	}

	/**
	 * Vr�t� p�vodn� zdrojov� event.
	 */
	public MouseEvent getEvt()
	{
		return evt;
	}

	/**
	 * Pozice my�i kde k ud�losti do�lo.
	 */
	public MouseScreenPosition getCursorPosition()
	{
		return cursorPosition;
	}

	@Override
	public String toString()
	{
		return "ViewportMouseEvent{" + "cursorPosition=" + cursorPosition + '}';
	}
}
