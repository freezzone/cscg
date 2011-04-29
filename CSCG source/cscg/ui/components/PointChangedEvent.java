package cscg.ui.components;

import cscg.model.objects.IPoint3f;
import java.util.EventObject;

/**
 * Objekt události změny bodu.
 * @author Tomáš Režnar
 */
public class PointChangedEvent extends EventObject
{

	/**
	 * Původní bod.
	 */
	protected IPoint3f oldPoint;

	/**
	 * Původní bod.
	 */
	public IPoint3f getOldPoint()
	{
		return oldPoint;
	}

	/**
	 * Změněný bod
	 */
	protected IPoint3f newPoint;

	/**
	 * Změněný bod
	 */
	public IPoint3f getNewPoint()
	{
		return newPoint;
	}

	public PointChangedEvent(Object source, IPoint3f old, IPoint3f changedPoint)
	{
		super(source);
		oldPoint = old;
		this.newPoint = changedPoint;
	}
}
