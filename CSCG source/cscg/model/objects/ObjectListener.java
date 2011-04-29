package cscg.model.objects;

/**
 * Interface posluchačů událostí z objektu {@link IObject}.
 * @author Tomáš Režnar
 */
public interface ObjectListener
{

	/**
	 * Událost změny jména.
	 * @param o Zdroj události.
	 */
	void eventNameChanged(IObject o);

	/**
	 * Událost přidání nového bodu.
	 * @param o Objekt ve kterém se bod nachází.
	 * @param p Bod.
	 * @param index Index bodu.
	 */
	void eventPointAdded(IObject o, IPoint3f p, int index);

	/**
	 * Událost odebrání bodu.
	 * @param o Objekt ve kterém se bod nachází.
	 * @param p Bod.
	 * @param index Bývalý index bodu.
	 */
	void eventPointRemoved(IObject o, IPoint3f p, int index);

	/**
	 * Událost změny bodu.
	 * @param o Objekt ve kterém se bod nachází.
	 * @param p Bod.
	 * @param index Index bodu.
	 */
	void eventPointChanged(IObject o, IPoint3f p, int index);

	/**
	 * Událost změny více bodů. Událost při které mohlo dojít ke změně: pořadí, vlastností jednoho
	 * či více bodů. Při události nedošlo ke změně výběru bodů.
	 * @param o Objekt ve kterém se body nachází.
	 */
	void eventPointsChanged(IObject o);

	/**
	 * Událost změny barvy čáry.
	 * @param o Zdroj události.
	 */
	void eventColorChanged(IObject o);

	/**
	 * Událost změny tloušťky čáry.
	 * @param o Zdroj události.
	 */
	void eventLineWidthChanged(IObject o);

	/**
	 * Událost při změně výběru bodů.
	 * @param o Zdroj události.
	 */
	void eventPointSelectionChanged(IObject o);

	/**
	 * Událost změny textové zprávy objektu.
	 * @param o Zdroj události.
	 */
	void eventStateMessageChanged(IObject o);

	/**
	 * Událost změny specifických nastavení konkrétních implementací objektů. (Např: stupeň křivky u aproximace).
	 * @param o Zdroj události.
	 */
	void eventSpecificPropertiesChanged(IObject o);

	/**
	 * Událost přidání či odebrání jednoho nebo více bodů.
	 */
	void eventSizeChanged(IObject o);
}
