package cscg.model.objects;

/**
 * Interface z�kladn�ch metod ploch.
 * @author Tom� Re�nar
 */
public interface ISurface<PointClass extends IPoint3f> extends IObject
{

	/**
	 * Z�sk� bod ze m��ky ��d�c�ch bod� plochy.
	 * @param row ��dek (row < rows objektu), ��slov�no od 0.
	 * @param col sloupec (col<columns), ��slov�no od 0.
	 * @throws IllegalArgumentException Pokud jsou neplatn� sou�adnice.
	 */
	public PointClass getPointFromGrid(int row, int col) throws IllegalArgumentException;

	/**
	 * Vr�t� ���ku objektu.
	 * @return Po�et sloupc�.
	 */
	public int columns();

	/**
	 * Vr�t� v��ku objektu.
	 * @return Po�et ��dk�.
	 */
	public int rows();
}
