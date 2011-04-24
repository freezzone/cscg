package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Ud�lost ta�en� my��.
 * @author Tom� Re�nar
 */
public class ViewportMouseDragEvent extends ViewportMouseEvent
{

	/**
	 * Poloha odkud byla my� p�esunuta.
	 */
	protected MouseScreenPosition fromCursorPosition;

	/**
	 * @param evt ud�lost my�i (z ud�lasti se zjist� kam byla my� p�esunuta).
	 * @param fromCursorPosition sou�adnice odkud byla my� p�esunuta.
	 */
	public ViewportMouseDragEvent(MouseEvent evt, MouseScreenPosition fromCursorPosition)
	{
		super(evt);
		this.fromCursorPosition = fromCursorPosition;
	}

	/**
	 * Poloha odkud byla my� p�esunuta.
	 */
	public MouseScreenPosition getFromCursorPosition()
	{
		return fromCursorPosition;
	}

	@Override
	public boolean isButton1()
	{
		return (evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK;
	}

	@Override
	public boolean isButton2()
	{
		return (evt.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK;
	}

	@Override
	public boolean isButton3()
	{
		return (evt.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK;
	}

	@Override
	public String toString()
	{
		return "ViewportMouseDragEvent{" + "fromCursorPosition=" + fromCursorPosition + "; toCursorPosition=" + super.getCursorPosition() + "}";
	}
}
