package cscg.model.objects;

import java.io.Serializable;

/**
 * Interface uzlového vektoru.
 * @author Tomáš Režnar
 */
public interface IKnotVector<Parent> extends Serializable{
	/**
	 * Získání objektu ke kterému se váže uzlový vektor.
	 */
	public Parent getParent();

	/**
	 * Získání celého knot vektoru.
	 */
	public Float[] getValues();

	/**
	 * Získání celého knot vektoru transformovaného do intervalu <0,1>.
	 */
	public float[] getTransformedValues();

	/**
	 * Získání prvku knot vektoru
	 * @throws IndexOutOfBoundsException Neplatný index.
	 */
	public Float getValue(int index);

	/**
	 * Počet prvků v uzlovém vektoru.
	 */
	public int length();

	/**
	 * Nastavení prvku knot vektoru.
	 * @param value Nová hodnota prvku knot vektoru. Musí splňovat požadavky dle definice knot vektoru křivky.
	 * @throws IndexOutOfBoundsException Neplatný index.
	 * @throws IllegalArgumentException Pokud je zadaná hodnota neplatná.
	 */
	public void setValue(int index, float value);

	/**
	 * Přidání posluchače změn knot vektoru.
	 */
	public void addKnotListener(KnotListener knotListener);

	/**
	 * Odebrání posluchače změn knot vektoru.
	 */
	public void removeKnotListener(KnotListener knotListener);
}
