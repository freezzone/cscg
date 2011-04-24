package cscg.model.objects;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Z�kladn� implementace bodu.
 * @author Tom� Re�nar
 */
public class Point3f implements IPoint3f{
	/**
	 * Sou�adnice
	 */
	private float x,y,z;


	public Point3f(){}

	/**
	 * Vytvo�en� bodu zad�n�m sou�adnic.
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
	 * Nastaven� sou�adnice x.
	 */
	protected void setX(float x)
	{
		this.x=x;
	}
	/**
	 * Nastaven� sou�adnice y.
	 */
	protected void setY(float y)
	{
		this.y=y;
	}
	/**
	 * Nastaven� sou�adnice z.
	 */
	protected void setZ(float z)
	{
		this.z=z;
	}
	/**
	 * Nastaven� bodu.
	 * @return Vrac� instanci sebe sama.
	 */
	protected IPoint3f set(float x,float y,float z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
		return this;
	}

	/**
	 * Nastav� vlastnosti bodu dle jin�ho.
	 * @return Vrac� instanci sebe sama.
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
		catch (CloneNotSupportedException ex)//nelze pokra�ovat bez klonov�n�
		{
			Logger.getLogger(Point3f.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(1000);
		}
		return null;
	}

}
