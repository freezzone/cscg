package cscg.ui.components;

import java.util.EventObject;

/**
 * Událost pøesunu objektu.
 * @author Tomáš Režnar
 */
public class MoveEvent extends EventObject
{

	/**
	 * Pøesun ve smìru osy x.
	 */
	public static final int MOVE_X = 1;
	/**
	 * Pøesun ve smìru osy y.
	 */
	public static final int MOVE_Y = 2;
	/**
	 * Pøesun ve smìru osy z.
	 */
	public static final int MOVE_Z = 3;
	/**
	 * Pøesun kladným smìrem.
	 */
	private final boolean positive;
	/**
	 * Smìr posunu, jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 */
	private final int direction;

	/**
	 * @param source Zdroj události.
	 * @param direction Smìr posunu, jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 * @param positive True pro posun vpøed.
	 */
	public MoveEvent(Object source, int direction, boolean positive)
	{
		super(source);
		this.positive = positive;
		this.direction = direction;
	}

	/**
	 * Vrátí smìr posunu.
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
