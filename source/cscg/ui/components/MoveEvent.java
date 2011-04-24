package cscg.ui.components;

import java.util.EventObject;

/**
 * Ud�lost p�esunu objektu.
 * @author Tom� Re�nar
 */
public class MoveEvent extends EventObject
{

	/**
	 * P�esun ve sm�ru osy x.
	 */
	public static final int MOVE_X = 1;
	/**
	 * P�esun ve sm�ru osy y.
	 */
	public static final int MOVE_Y = 2;
	/**
	 * P�esun ve sm�ru osy z.
	 */
	public static final int MOVE_Z = 3;
	/**
	 * P�esun kladn�m sm�rem.
	 */
	private final boolean positive;
	/**
	 * Sm�r posunu, jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 */
	private final int direction;

	/**
	 * @param source Zdroj ud�losti.
	 * @param direction Sm�r posunu, jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 * @param positive True pro posun vp�ed.
	 */
	public MoveEvent(Object source, int direction, boolean positive)
	{
		super(source);
		this.positive = positive;
		this.direction = direction;
	}

	/**
	 * Vr�t� sm�r posunu.
	 * @return jedna z konstant: MOVE_X,MOVE_Y,MOVE_Z.
	 */
	public int getDirection()
	{
		return direction;
	}

	/**
	 * Vr�t� true pokud je posun kladn�.
	 */
	public boolean isPositive()
	{
		return positive;
	}
}
