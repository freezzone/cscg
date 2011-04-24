package cscg.model.objects;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Tøída pøedstavující hranici v prostoru.
 * @author Tomáš Režnar
 */
public class Bounds implements Serializable {
	private float x1;
	private float x2;
	private float y1;
	private float y2;
	private float z1;
	private float z2;
	/**
	 * Vytvoøí hranice.
	 * @param x1 Menší x hranice.
	 * @param x2 Vìtší x hranice.
	 * @param y1 Menší y hranice.
	 * @param y2 Vìtší y hranice.
	 * @param z1 Menší z hranice.
	 * @param z2 Vìtší z hranice.
	 */
	public Bounds(float x1,float x2,float y1,float y2,float z1,float z2)
	{
		this.x1=x1<x2?x1:x2;
		this.x2=x2>x1?x2:x1;
		this.y1=y1<y2?y1:y2;
		this.y2=y2>y1?y2:y1;
		this.z1=z1<z2?z1:z2;
		this.z2=z2>z1?z2:z1;
	}

	/**
	 * @return Menší x hranice.
	 */
	public float getX1() {
		return x1;
	}

	/**
	 * @return Vìtší x hranice.
	 */
	public float getX2() {
		return x2;
	}

	/**
	 * @return Menší y hranice.
	 */
	public float getY1() {
		return y1;
	}

	/**
	 * @return Vìtší y hranice.
	 */
	public float getY2() {
		return y2;
	}

	/**
	 * @return Menší z hranice.
	 */
	public float getZ1() {
		return z1;
	}

	/**
	 * @return Vìtší z hranice.
	 */
	public float getZ2() {
		return z2;
	}

	/**
	 * Vrátí šíøku prostoru.
	 * @return Šíøka (x2-x1).
	 */
	public float getWidth()
	{
		return x2-x1;
	}

	/**
	 * Vrátí výšku prostoru.
	 * @return Šíøka (y2-y1).
	 */
	public float getHeight()
	{
		return y2-y1;
	}

	/**
	 * Vrátí hloubku prostoru.
	 * @return Šíøka (z2-z1).
	 */
	public float getDeep()
	{
		return z2-z1;
	}

	/**
	 * Vypoèítání bodu nacházejícího se ve støedu prostoru vyhrazeného hranicemi.
	 */
	public IPoint3f getCenterPoint()
	{
		return new Point3f(
			(x1+x2)/2,
			(y1+y2)/2,
			(z1+z2)/2
		);
	}

	@Override
	public String toString()
	{
		return "Bounds: ["+x1+";"+x2+"],["+y1+";"+y2+"],["+z1+";"+z2+"]";
	}

	/**
	 * Nalezne hranice prostoru potøebného pro umístìní bodù.
	 */
	public static Bounds findBounds(Collection<? extends IPoint3f> points)
	{
		//pokud nejsou body
		if(points.isEmpty())
		{
			return new Bounds(0, 0, 0, 0, 0, 0);
		}
		//hledání okrajových bodù
		Iterator<? extends IPoint3f> i=points.iterator();
		IPoint3f p=i.next();
		float xMin,xMax,yMin,yMax,zMin,zMax;
		xMin=xMax=p.getX();
		yMin=yMax=p.getY();
		zMin=zMax=p.getZ();
		while(i.hasNext())
		{
			p=i.next();
			xMin=xMin>p.getX()?p.getX():xMin;
			yMin=yMin>p.getY()?p.getY():yMin;
			zMin=zMin>p.getZ()?p.getZ():zMin;
			xMax=xMax<p.getX()?p.getX():xMax;
			yMax=yMax<p.getY()?p.getY():yMax;
			zMax=zMax<p.getZ()?p.getZ():zMax;
		}
		return new Bounds(xMin, xMax, yMin, yMax, zMin, zMax);
	}
}
