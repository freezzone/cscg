package cscg.model;

import java.awt.event.MouseEvent;

/**
 * Tøída pro uložení informací o poloze kurzoru.
 * @author Tomáš Režnar
 */
public class MouseScreenPosition
{

	int x, y;

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	/**
	 * Kontruktor s ruèním zadáním pozice.
	 * @param x X pozice kurzoru.
	 * @param y Y pozice kurzoru.
	 */
	public MouseScreenPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Konstruktor se zadáním pozice myši z události.
	 * @param e Událost myši.
	 */
	public MouseScreenPosition(MouseEvent e)
	{
		this(e.getX(), e.getComponent().getHeight() - e.getY());
	}

	@Override
	public String toString()
	{
		return "MouseScreenPosition{" + "x=" + x + "y=" + y + '}';
	}
}
