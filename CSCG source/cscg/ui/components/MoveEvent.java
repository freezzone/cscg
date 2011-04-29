package cscg.ui.components;

import java.util.EventObject;

/**
 * Událost přesunu objektu.
 * @author Tomáš Režnar
 */
public class MoveEvent extends EventObject
{

	/**
	 * Přesun ve směru osy x.
	 */
	public static final int MOVE_X = 1;
	/**
	 * Přesun ve směru osy y.
	 */
	public static final int MOVE_Y = 2;
	/**
	 * Přesun ve směru osy z.
	 */
	public static final int MOVE_Z = 3;
	/**
	 * Přesun kladným směrem.
	 */
	private final boolean positive;
	/**
	 * Směr posunu, jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 */
	private final int direction;

	/**
	 * @param source Zdroj události.
	 * @param direction Směr posunu, jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 * @param positive True pro posun vpřed.
	 */
	public MoveEvent(Object source, int direction, boolean positive)
	{
		super(source);
		this.positive = positive;
		this.direction = direction;
	}

	/**
	 * Vrátí směr posunu.
	 * @return jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 */
	public int getDirection()
	{
		return direction;
	}

	/**
	 * Vrátí true pokud je posun kladný.
	 */
	public boolean isPositive()
	{
		return positive;
	}
}
