package cscg.model.objects.impl;

import cscg.model.objects.IPoint3f;
import cscg.model.objects.Point3f;

/**
 * Roz���en� bodu o protected metody pro zm�nu bodu.
 * @author Tom� Re�nar
 */
public class Point3fChangeable extends Point3f
{

	@Override
	protected IPoint3f set(float x, float y, float z)
	{
		return super.set(x, y, z);
	}

	@Override
	protected IPoint3f setBy(IPoint3f p)
	{
		return super.setBy(p);
	}

	@Override
	protected void setX(float x)
	{
		super.setX(x);
	}

	@Override
	protected void setY(float y)
	{
		super.setY(y);
	}

	@Override
	protected void setZ(float z)
	{
		super.setZ(z);
	}
}
