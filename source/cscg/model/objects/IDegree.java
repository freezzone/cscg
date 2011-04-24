package cscg.model.objects;

/**
 * Interface objektù, které umožòují nastavit stupeò køivky/plochy.
 * @author Tomáš Režnar
 */
public interface IDegree {

	/**
	 * Získání stupnì.
	 */
	int getDegree();

	/**
	 * Nastavení stupnì.
	 * Pokud zadáte neplatnou hodnotu, použije se nejbližší platná.
	 */
	 void setDegree(int degree);

	/**
	 * Pøidání posluchaèe zmìn stupnì.
	 */
	public void addDegreeListener(DegreeListener degreeListener);

	/**
	 * Odebrání posluchaèe zmìn stupnì.
	 */
	public void removeDegreeListener(DegreeListener degreeListener);

}
