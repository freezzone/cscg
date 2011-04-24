package cscg.ui.components;

import cscg.model.objects.IPoint3f;
import java.util.EventObject;

/**
 * Objekt ud�losti zm�ny bodu.
 * @author Tom� Re�nar
 */
public class PointChangedEvent extends EventObject
{

	/**
	 * P�vodn� bod.
	 */
	protected IPoint3f oldPoint;

	/**
	 * P�vodn� bod.
	 */
	public IPoint3f getOldPoint()
	{
		return oldPoint;
	}

	/**
	 * Zm�n�n� bod
	 */
	protected IPoint3f newPoint;

	/**
	 * Zm�n�n� bod
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
