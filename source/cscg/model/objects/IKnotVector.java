package cscg.model.objects;

import java.io.Serializable;

/**
 * Interface uzlov�ho vektoru.
 * @author Tom� Re�nar
 */
public interface IKnotVector<Parent> extends Serializable{
	/**
	 * Z�sk�n� objektu ke kter�mu se v�e uzlov� vektor.
	 */
	public Parent getParent();

	/**
	 * Z�sk�n� cel�ho knot vektoru.
	 */
	public Float[] getValues();

	/**
	 * Z�sk�n� cel�ho knot vektoru transformovan�ho do intervalu <0,1>.
	 */
	public float[] getTransformedValues();

	/**
	 * Z�sk�n� prvku knot vektoru
	 * @throws IndexOutOfBoundsException Neplatn� index.
	 */
	public Float getValue(int index);

	/**
	 * Po�et prvk� v uzlov�m vektoru.
	 */
	public int length();

	/**
	 * Nastaven� prvku knot vektoru.
	 * @param value Nov� hodnota prvku knot vektoru. Mus� spl�ovat po�adavky dle definice knot vektoru k�ivky.
	 * @throws IndexOutOfBoundsException Neplatn� index.
	 * @throws IllegalArgumentException Pokud je zadan� hodnota neplatn�.
	 */
	public void setValue(int index, float value);

	/**
	 * P�id�n� poslucha�e zm�n knot vektoru.
	 */
	public void addKnotListener(KnotListener knotListener);

	/**
	 * Odebr�n� poslucha�e zm�n knot vektoru.
	 */
	public void removeKnotListener(KnotListener knotListener);
}
