package cscg.ui.components;

import java.util.EventObject;

/**
 * Událost rotace objektu.
 * @author Tomáš Režnar
 */
public class RotateEvent extends EventObject
{

	/**
	 * Osa rotace je rovnobìžná s osou x.
	 */
	public static final int AXE_BY_X = 1;
	/**
	 * Osa rotace je rovnobìžná s osou y.
	 */
	public static final int AXE_BY_Y = 2;
	/**
	 * Osa rotace je rovnobìžná s osou z.
	 */
	public static final int AXE_BY_Z = 3;
	/**
	 * Støed rotace je v bodì 0.
	 */
	public static final int CENTER_IS_ORIGIN = 1;
	/**
	 * Støed rotace je v bodì støedovém rotovaného objektu.
	 */
	public static final int CENTER_IS_MIDDLE_POINT = 2;
	/**
	 * Støed rotace je v bodì prùmìrném rotovaného objektu.
	 */
	public static final int CENTER_IS_AVERAGE_POINT = 3;
	/**
	 * Kladná rotace.
	 */
	private final boolean positive;
	/**
	 * Osa rotace
	 */
	private final int axe;
	/**
	 * Støed rotace
	 */
	private final int pivot;
	/**
	 * Úhel rotace ve stupních.
	 */
	private final double angle;

	/**
	 * @param source Zdroj události.
	 * @param axe Osa rotace, jedna z konstant: AXE_BY_X,AXE_BY_Y,AXE_BY_Z.
	 * @param positive True pro kladnou rotaci false pro zápornou.
	 * @param pivot Støed rotace, jedna z konstant: CENTER_IS_ORIGIN,CENTER_IS_MIDDLE_POINT,CENTER_IS_AVERAGE_POINT.
	 * @param angle Úhel rotace ve stupních.
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
	 * Vrátí osu rotace.
	 * @return Jedna z konstant: AXE_BY_X,AXE_BY_Y,AXE_BY_Z.
	 */
	public int getAxes()
	{
		return axe;
	}

	/**
	 * Vrátí true pokud je rotace kladná.
	 */
	public boolean isPositive()
	{
		return positive;
	}

	/**
	 * Vrátí støed rotace.
	 * @return Jedna z konstant: CENTER_IS_ORIGIN,CENTER_IS_MIDDLE_POINT,CENTER_IS_AVERAGE_POINT.
	 */
	public int getPivot()
	{
		return pivot;
	}

	/**
	 * Vrátí úhel rotace.
	 * @return Úhel rotace ve stupních.
	 */
	public double getAngle()
	{
		return angle;
	}
}
