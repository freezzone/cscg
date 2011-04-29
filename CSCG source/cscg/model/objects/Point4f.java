package cscg.model.objects;

/**
 * Implementace základního bodu s vahou.
 * @author Tomáš Režnar
 */
public class Point4f extends Point3f implements IPoint4f
{

	/**
	 * Váha bodu
	 */
	private float w = 1;

	public Point4f()
	{
		super();
	}

	/**
	 * Vytvoření bodu zadáním souřadnic.
	 * @param w Váha bodu.
	 */
	public Point4f(float x, float y, float z, float w)
	{
		super(x, y, z);
		this.w = w;
	}

	/**
	 * @param vector Pole velikosti 4.
	 */
	public Point4f(float[] vector)
	{
		this(vector[0], vector[1], vector[2], vector[3]);
	}

	@Override
	public float getW()
	{
		return w;
	}

	/**
	 * Nastavení váhy bodu.
	 */
	protected void setW(float w)
	{
		this.w = w;
	}

	/**
	 * Nastavení bodu.
	 * @return Vrací instanci sebe sama.
	 */
	protected IPoint4f set(float x, float y, float z, float w)
	{
		super.set(x, y, z);
		this.w = w;
		return this;
	}

	/**
	 * Nastaví vlastnosti bodu dle jiného.
	 * @return Vrací instanci sebe sama.
	 */
	protected IPoint4f setBy(IPoint4f p)
	{
		super.setBy(p);
		w = p.getW();
		return this;
	}

	@Override
	public String toString()
	{
		return super.toString() + "; w=" + w;
	}

	@Override
	public Object clone()
	{
		return super.clone();
	}
}
