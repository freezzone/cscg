package mylib;

import cscg.model.objects.IPoint3f;
import cscg.model.objects.Point3f;

/**
 * Ukázka implementace vlastního bodu.
 * @author Tomáš Režnar
 */
public class MyPoint3f extends Point3f {

	@Override
	protected Point3f set(float x, float y, float z)
	{
		return (Point3f) super.set(x+10, y, z);
	}

	@Override
	protected Point3f setBy(IPoint3f p)
	{
		super.setBy(p);
		setX(p.getX());
		return this;
	}

	@Override
	protected void setX(float x)
	{
		super.setX(x+10);
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
