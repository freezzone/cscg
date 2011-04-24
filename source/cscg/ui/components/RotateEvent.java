package cscg.ui.components;

import java.util.EventObject;

/**
 * Ud�lost rotace objektu.
 * @author Tom� Re�nar
 */
public class RotateEvent extends EventObject
{

	/**
	 * Osa rotace je rovnob�n� s osou x.
	 */
	public static final int AXE_BY_X = 1;
	/**
	 * Osa rotace je rovnob�n� s osou y.
	 */
	public static final int AXE_BY_Y = 2;
	/**
	 * Osa rotace je rovnob�n� s osou z.
	 */
	public static final int AXE_BY_Z = 3;
	/**
	 * St�ed rotace je v bod� 0.
	 */
	public static final int CENTER_IS_ORIGIN = 1;
	/**
	 * St�ed rotace je v bod� st�edov�m rotovan�ho objektu.
	 */
	public static final int CENTER_IS_MIDDLE_POINT = 2;
	/**
	 * St�ed rotace je v bod� pr�m�rn�m rotovan�ho objektu.
	 */
	public static final int CENTER_IS_AVERAGE_POINT = 3;
	/**
	 * Kladn� rotace.
	 */
	private final boolean positive;
	/**
	 * Osa rotace
	 */
	private final int axe;
	/**
	 * St�ed rotace
	 */
	private final int pivot;
	/**
	 * �hel rotace ve stupn�ch.
	 */
	private final double angle;

	/**
	 * @param source Zdroj ud�losti.
	 * @param axe Osa rotace, jedna z konstant: AXE_BY_X,AXE_BY_Y,AXE_BY_Z.
	 * @param positive True pro kladnou rotaci false pro z�pornou.
	 * @param pivot St�ed rotace, jedna z konstant: CENTER_IS_ORIGIN,CENTER_IS_MIDDLE_POINT,CENTER_IS_AVERAGE_POINT.
	 * @param angle �hel rotace ve stupn�ch.
	 */
	public RotateEvent(Object source, int axe, boolean positive, int pivot, double angle)
	{
		super(source);
		this.positive = positive;
		this.axe = axe;
		this.pivot = pivot;
		this.angle = angle;
	}

	/**
	 * Vr�t� osu rotace.
	 * @return Jedna z konstant: AXE_BY_X,AXE_BY_Y,AXE_BY_Z.
	 */
	public int getAxes()
	{
		return axe;
	}

	/**
	 * Vr�t� true pokud je rotace kladn�.
	 */
	public boolean isPositive()
	{
		return positive;
	}

	/**
	 * Vr�t� st�ed rotace.
	 * @return Jedna z konstant: CENTER_IS_ORIGIN,CENTER_IS_MIDDLE_POINT,CENTER_IS_AVERAGE_POINT.
	 */
	public int getPivot()
	{
		return pivot;
	}

	/**
	 * Vr�t� �hel rotace.
	 * @return �hel rotace ve stupn�ch.
	 */
	public double getAngle()
	{
		return angle;
	}
}
