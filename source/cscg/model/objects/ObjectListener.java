package cscg.model.objects;

/**
 * Interface posluchaèù událostí z objektu.
 * @author Tomáš Renar
 */
public interface ObjectListener
{

	/**
	 * Událost zmìny jména.
	 * @param o Zdroj události.
	 */
	void eventNameChanged(IObject o);

	/**
	 * Událost pøidání nového bodu.
	 * @param o Objekt ve kterém se bod nachází.
	 * @param p Bod.
	 * @param index Index bodu.
	 */
	void eventPointAdded(IObject o, IPoint3f p, int index);

	/**
	 * Událost odebrání bodu.
	 * @param o Objekt ve kterém se bod nachází.
	 * @param p Bod.
	 * @param index Bıvalı index bodu.
	 */
	void eventPointRemoved(IObject o, IPoint3f p, int index);

	/**
	 * Událost zmìny bodu.
	 * @param o Objekt ve kterém se bod nachází.
	 * @param p Bod.
	 * @param index Index bodu.
	 */
	void eventPointChanged(IObject o, IPoint3f p, int index);

	/**
	 * Událost zmìny více bodù. Událost pøi které mohlo dojít ke zmìnì: poøadí, vlastností jednoho
	 * èi více bodù. Pøi události nedošlo ke zmìnì vıbìru bodù.
	 * @param o Objekt ve kterém se body nachází.
	 */
	void eventPointsChanged(IObject o);

	/**
	 * Událost zmìny barvy èáry.
	 * @param o Zdroj události.
	 */
	void eventColorChanged(IObject o);

	/**
	 * Událost zmìny tloušky èáry.
	 * @param o Zdroj události.
	 */
	void eventLineWidthChanged(IObject o);

	/**
	 * Událost pøi zmìnì vıbìru bodù.
	 * @param o Zdroj události.
	 */
	void eventPointSelectionChanged(IObject o);

	/**
	 * Událost zmìny textové zprávy objektu.
	 * @param o Zdroj události.
	 */
	void eventStateMessageChanged(IObject o);

	/**
	 * Událost zmìny specifickıch nastavení konkrétních implementací objektù. (Napø: stupeò køivky u aproximace).
	 * @param o Zdroj události.
	 */
	void eventSpecificPropertiesChanged(IObject o);

	/**
	 * Událost pøidání èi odebrání jednoho nebo více bodù.
	 */
	void eventSizeChanged(IObject o);
}
