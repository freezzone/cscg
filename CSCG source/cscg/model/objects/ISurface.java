package cscg.model.objects;

/**
 * Interface základních metod ploch.
 * @author Tomáš Režnar
 */
public interface ISurface<PointClass extends IPoint3f> extends IObject
{

	/**
	 * Získá bod ze mřížky řídících bodů plochy.
	 * @param row Řádek (row &lt; {@link #rows()} objektu), číslováno od 0.
	 * @param col sloupec (col &lt; {@link #columns()} objektu), číslováno od 0.
	 * @throws IllegalArgumentException Pokud jsou neplatné souřadnice.
	 */
	public PointClass getPointFromGrid(int row, int col) throws IllegalArgumentException;

	/**
	 * Vrátí šířku objektu.
	 * @return Počet sloupců.
	 */
	public int columns();

	/**
	 * Vrátí výšku objektu.
	 * @return Počet řádků.
	 */
	public int rows();
}
