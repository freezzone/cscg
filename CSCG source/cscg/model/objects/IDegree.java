package cscg.model.objects;

/**
 * Interface objektů, které umožňují nastavit stupeň křivky/plochy.
 * @author Tomáš Režnar
 */
public interface IDegree {

	/**
	 * Získání stupně.
	 */
	int getDegree();

	/**
	 * Nastavení stupně.
	 * Pokud zadáte neplatnou hodnotu, použije se nejbližší platná.
	 */
	 void setDegree(int degree);

	/**
	 * Přidání posluchače změn stupně.
	 */
	public void addDegreeListener(DegreeListener degreeListener);

	/**
	 * Odebrání posluchače změn stupně.
	 */
	public void removeDegreeListener(DegreeListener degreeListener);

}
