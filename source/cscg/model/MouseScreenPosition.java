package cscg.model;

import java.awt.event.MouseEvent;

/**
 * T��da pro ulo�en� informac� o poloze kurzoru.
 * @author Tom� Re�nar
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
	 * Kontruktor s ru�n�m zad�n�m pozice.
	 * @param x X pozice kurzoru.
	 * @param y Y pozice kurzoru.
	 */
	public MouseScreenPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Konstruktor se zad�n�m pozice my�i z ud�losti.
	 * @param e Ud�lost my�i.
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
