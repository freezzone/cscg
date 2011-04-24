package cscg.ui.components;

/**
 * T��da p�edstavuj�c� hranice pohledu v r�mci obrazovky OpenGL
 * @author Tom� Re�nar
 */
public class ViewportBounds
{

	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private float center[] = new float[2];

	/**
	 * Vytvo�� hranice.
	 * @param x1 Men�� x hranice.
	 * @param x2 V�t�� x hranice.
	 * @param y1 Men�� y hranice.
	 * @param y2 V�t�� y hranice.
	 */
	public ViewportBounds(int x1, int x2, int y1, int y2)
	{
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		center[0] = ((float) (x1 + x2)) / 2f;
		center[1] = ((float) (y1 + y2)) / 2f;
	}

	/**
	 * @return Men�� x hranice.
	 */
	public int getX1()
	{
		return x1;
	}

	/**
	 * @return V�t�� x hranice.
	 */
	public int getX2()
	{
		return x2;
	}

	/**
	 * @return Men�� y hranice.
	 */
	public int getY1()
	{
		return y1;
	}

	/**
	 * @return V�t�� y hranice.
	 */
	public int getY2()
	{
		return y2;
	}

	/**
	 * @return Vr�t� x sou�adnici bodu nach�zej�c�ho se uprost�ed pohledu.
	 */
	public float getCenterX()
	{
		return center[0];
	}

	/**
	 * @return Vr�t� z sou�adnici bodu nach�zej�c�ho se uprost�ed pohledu.
	 */
	public float getCenterY()
	{
		return center[1];
	}

	/**
	 * Vr�t� ���ku prostoru.
	 * @return ���ka (x2-x1).
	 */
	public int getWidth()
	{
		return (x2 - x1);
	}

	/**
	 * Vr�t� v��ku prostoru.
	 * @return V��ka (y2-y1)
	 */
	public int getHeight()
	{
		return (y2 - y1);
	}

	/**
	 * Zjist� zda dan� sou�adnice jsou <= hranic�m.
	 * @return True pokud jsou obsa�eny.
	 */
	public boolean contains(int x, int y)
	{
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	@Override
	public String toString()
	{
		return "ViewportBounds: [" + x1 + ";" + x2 + "],[" + y1 + ";" + y2 + "]";
	}
}
