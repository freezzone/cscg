package cscg.ui.components;

/**
 * Třída představující hranice pohledu v rámci obrazovky OpenGL
 * @author Tomáš Režnar
 */
public class ViewportBounds
{

	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private float center[] = new float[2];

	/**
	 * Vytvoří hranice.
	 * @param x1 Menší x hranice.
	 * @param x2 Větší x hranice.
	 * @param y1 Menší y hranice.
	 * @param y2 Větší y hranice.
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
	 * @return Menší x hranice.
	 */
	public int getX1()
	{
		return x1;
	}

	/**
	 * @return Větší x hranice.
	 */
	public int getX2()
	{
		return x2;
	}

	/**
	 * @return Menší y hranice.
	 */
	public int getY1()
	{
		return y1;
	}

	/**
	 * @return Větší y hranice.
	 */
	public int getY2()
	{
		return y2;
	}

	/**
	 * @return Vrátí x souřadnici bodu nacházejícího se uprostřed pohledu.
	 */
	public float getCenterX()
	{
		return center[0];
	}

	/**
	 * @return Vrátí z souřadnici bodu nacházejícího se uprostřed pohledu.
	 */
	public float getCenterY()
	{
		return center[1];
	}

	/**
	 * Vrátí šířku prostoru.
	 * @return Šířka (x2-x1).
	 */
	public int getWidth()
	{
		return (x2 - x1);
	}

	/**
	 * Vrátí výšku prostoru.
	 * @return Výška (y2-y1)
	 */
	public int getHeight()
	{
		return (y2 - y1);
	}

	/**
	 * Zjistí zda dané souřadnice jsou <= hranicím.
	 * @return True pokud jsou obsaženy.
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
