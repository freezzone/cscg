package cscg.model.objects;

/**
 * Interface objekt�, kter� umo��uj� nastavit stupe� k�ivky/plochy.
 * @author Tom� Re�nar
 */
public interface IDegree {

	/**
	 * Z�sk�n� stupn�.
	 */
	int getDegree();

	/**
	 * Nastaven� stupn�.
	 * Pokud zad�te neplatnou hodnotu, pou�ije se nejbli��� platn�.
	 */
	 void setDegree(int degree);

	/**
	 * P�id�n� poslucha�e zm�n stupn�.
	 */
	public void addDegreeListener(DegreeListener degreeListener);

	/**
	 * Odebr�n� poslucha�e zm�n stupn�.
	 */
	public void removeDegreeListener(DegreeListener degreeListener);

}
