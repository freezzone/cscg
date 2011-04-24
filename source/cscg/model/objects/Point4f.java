package cscg.model.objects;

/**
 * Implementace z�kladn�ho bodu s vahou.
 * @author Tom� Re�nar
 */
public class Point4f extends Point3f implements IPoint4f
{

	/**
	 * V�ha bodu
	 */
	private float w = 1;

	public Point4f()
	{
		super();
	}

	/**
	 * Vytvo�en� bodu zad�n�m sou�adnic.
	 * @param w V�ha bodu.
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
	 * Nastaven� v�hy bodu.
	 */
	protected void setW(float w)
	{
		this.w = w;
	}

	/**
	 * Nastaven� bodu.
	 * @return Vrac� instanci sebe sama.
	 */
	protected IPoint4f set(float x, float y, float z, float w)
	{
		super.set(x, y, z);
		this.w = w;
		return this;
	}

	/**
	 * Nastav� vlastnosti bodu dle jin�ho.
	 * @return Vrac� instanci sebe sama.
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
