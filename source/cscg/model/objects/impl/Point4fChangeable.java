package cscg.model.objects.impl;

import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.Point4f;

/**
 * Rozšíøení bodu o protected metody pro zmìnu bodu.
 * @author Tomáš Režnar
 */
public class Point4fChangeable extends Point4f
{

	@Override
	protected void setW(float w)
	{
		super.setW(w);
	}

	@Override
	protected IPoint4f set(float x, float y, float z)
	{
		return (IPoint4f) super.set(x, y, z);
	}

	@Override
	protected IPoint4f setBy(IPoint4f p)
	{
		return (IPoint4f) super.setBy(p);
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
