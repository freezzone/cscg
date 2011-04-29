package cscg.model;

/**
 * Generická třída pro uložení dvojice objektů.
 * Třída se využívá v modelu {@link Model} pro uložení struktůry menu pro přidání objektu do projektu,
 * kde se ukládají dvojice hodnot text položky menu a název třídy objektu.
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
