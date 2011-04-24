package cscg.model.objects;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Základní implementace bodu.
 * @author Tomáš Režnar
 */
public class Point3f implements IPoint3f{
	/**
	 * Souøadnice
	 */
	private float x,y,z;


	public Point3f(){}

	/**
	 * Vytvoøení bodu zadáním souøadnic.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Point3f(float x,float y,float z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	/**
	 * @param vector Pole velikosti 3.
	 */
	public Point3f(float[] vector)
	{
		this(vector[0],vector[1],vector[2]);
	}

	@Override
	public float getX()
	{
		return x;
	}

	@Override
	public float getY()
	{
		return y;
	}

	@Override
	public float getZ()
	{
		return z;
	}
	/**
	 * Nastavení souøadnice x.
	 */
	protected void setX(float x)
	{
		this.x=x;
	}
	/**
	 * Nastavení souøadnice y.
	 */
	protected void setY(float y)
	{
		this.y=y;
	}
	/**
	 * Nastavení souøadnice z.
	 */
	protected void setZ(float z)
	{
		this.z=z;
	}
	/**
	 * Nastavení bodu.
	 * @return Vrací instanci sebe sama.
	 */
	protected IPoint3f set(float x,float y,float z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
		return this;
	}

	/**
	 * Nastaví vlastnosti bodu dle jiného.
	 * @return Vrací instanci sebe sama.
	 */
	protected IPoint3f setBy(IPoint3f p)
	{
		x=p.getX();
		y=p.getY();
		z=p.getZ();
		return this;
	}

	@Override
	public String toString()
	{
		return "x: "+getX()+"; y:"+getY()+"; z:"+getZ();
	}

	@Override
	public Object clone()
	{
		try
		{
			return super.clone();
		} 
		catch (CloneNotSupportedException ex)//nelze pokraèovat bez klonování
		{
			Logger.getLogger(Point3f.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(1000);
		}
		return null;
	}

}
