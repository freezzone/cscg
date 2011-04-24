package cscg.model.objects;

/**
 * Interface základních metod ploch.
 * @author Tomáš Režnar
 */
public interface ISurface<PointClass extends IPoint3f> extends IObject
{

	/**
	 * Získá bod ze møížky øídících bodù plochy.
	 * @param row Øádek (row < rows objektu), èíslováno od 0.
	 * @param col sloupec (col<columns), èíslováno od 0.
	 * @throws IllegalArgumentException Pokud jsou neplatné souøadnice.
	 */
	public PointClass getPointFromGrid(int row, int col) throws IllegalArgumentException;

	/**
	 * Vrátí šíøku objektu.
	 * @return Poèet sloupcù.
	 */
	public int columns();

	/**
	 * Vrátí výšku objektu.
	 * @return Poèet øádkù.
	 */
	public int rows();
}
