package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Obecná událost myši.
 * @author Tomáš Režnar
 */
public class ViewportMouseEvent
{

	/**
	 * Zdrojová událost myši.
	 */
	protected MouseEvent evt;
	/**
	 * Pozice kde k události došlo.
	 */
	protected MouseScreenPosition cursorPosition;

	public ViewportMouseEvent(MouseEvent evt)
	{
		this.evt = evt;
		this.cursorPosition = new MouseScreenPosition(evt);
	}

	/**
	 * Zjistí zda bylo pøi události stiknuto tlaèítko 1.
	 */
	public boolean isButton1()
	{
		return evt.getButton() == MouseEvent.BUTTON1;
	}

	/**
	 * Zjistí zda bylo pøi události stiknuto tlaèítko 2.
	 */
	public boolean isButton2()
	{
		return evt.getButton() == MouseEvent.BUTTON2;
	}

	/**
	 * Zjistí zda bylo pøi události stiknuto tlaèítko 3.
	 */
	public boolean isButton3()
	{
		return evt.getButton() == MouseEvent.BUTTON3;
	}

	/**
	 * Vrátí pùvodní zdrojový event.
	 */
	public MouseEvent getEvt()
	{
		return evt;
	}

	/**
	 * Pozice myši kde k události došlo.
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
