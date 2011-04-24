package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Událost tažení myší.
 * @author Tomáš Režnar
 */
public class ViewportMouseDragEvent extends ViewportMouseEvent
{

	/**
	 * Poloha odkud byla myš pøesunuta.
	 */
	protected MouseScreenPosition fromCursorPosition;

	/**
	 * @param evt událost myši (z událasti se zjistí kam byla myš pøesunuta).
	 * @param fromCursorPosition souøadnice odkud byla myš pøesunuta.
	 */
	public ViewportMouseDragEvent(MouseEvent evt, MouseScreenPosition fromCursorPosition)
	{
		super(evt);
		this.fromCursorPosition = fromCursorPosition;
	}

	/**
	 * Poloha odkud byla myš pøesunuta.
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
