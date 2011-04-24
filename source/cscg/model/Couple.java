package cscg.model;

/**
 * Generická tøída pro uložení dvojice objektù.
 * Tøída se využívá v modelu {@link Model} pro uložení struktùry menu pro pøidání objektu do projektu,
 * kde se ukládají dvojice hodnot text položky menu a název tøídy objektu.
 * @author Tomáš Režnar
 */
public class Couple<U,V> {
	public final U first;
	public final V second;

	/**
	 * Konstruktor.
	 * @param first První z dvojice.
	 * @param second Druhý z dvojice.
	 */
	public Couple(U first, V second)
	{
		this.first = first;
		this.second = second;
	}
	
}
