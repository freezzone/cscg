package cscg.model;

/**
 * Generick� t��da pro ulo�en� dvojice objekt�.
 * T��da se vyu��v� v modelu {@link Model} pro ulo�en� strukt�ry menu pro p�id�n� objektu do projektu,
 * kde se ukl�daj� dvojice hodnot text polo�ky menu a n�zev t��dy objektu.
 * @author Tom� Re�nar
 */
public class Couple<U,V> {
	public final U first;
	public final V second;

	/**
	 * Konstruktor.
	 * @param first Prvn� z dvojice.
	 * @param second Druh� z dvojice.
	 */
	public Couple(U first, V second)
	{
		this.first = first;
		this.second = second;
	}
	
}
