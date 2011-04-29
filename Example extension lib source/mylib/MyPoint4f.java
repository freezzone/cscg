package mylib;

import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.Point4f;

/**
 * Ukázka implementace vlastního bodu s vahou.
 * @author Tomáš Režnar
 */
public class MyPoint4f extends Point4f{

	@Override
	protected IPoint4f set(float x, float y, float z, float w)
	{
		return super.set(x, y, z, w);
	}

	@Override
	protected IPoint4f setBy(IPoint4f p)
	{
		return super.setBy(p);
	}

	@Override
	protected void setW(float w)
	{
		super.setW(w);
	}

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
