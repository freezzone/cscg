package cscg.model.objects;

/**
 * Interface poslucha�� ud�lost� z objektu.
 * @author Tom� Re�nar
 */
public interface ObjectListener
{

	/**
	 * Ud�lost zm�ny jm�na.
	 * @param o Zdroj ud�losti.
	 */
	void eventNameChanged(IObject o);

	/**
	 * Ud�lost p�id�n� nov�ho bodu.
	 * @param o Objekt ve kter�m se bod nach�z�.
	 * @param p Bod.
	 * @param index Index bodu.
	 */
	void eventPointAdded(IObject o, IPoint3f p, int index);

	/**
	 * Ud�lost odebr�n� bodu.
	 * @param o Objekt ve kter�m se bod nach�z�.
	 * @param p Bod.
	 * @param index B�val� index bodu.
	 */
	void eventPointRemoved(IObject o, IPoint3f p, int index);

	/**
	 * Ud�lost zm�ny bodu.
	 * @param o Objekt ve kter�m se bod nach�z�.
	 * @param p Bod.
	 * @param index Index bodu.
	 */
	void eventPointChanged(IObject o, IPoint3f p, int index);

	/**
	 * Ud�lost zm�ny v�ce bod�. Ud�lost p�i kter� mohlo doj�t ke zm�n�: po�ad�, vlastnost� jednoho
	 * �i v�ce bod�. P�i ud�losti nedo�lo ke zm�n� v�b�ru bod�.
	 * @param o Objekt ve kter�m se body nach�z�.
	 */
	void eventPointsChanged(IObject o);

	/**
	 * Ud�lost zm�ny barvy ��ry.
	 * @param o Zdroj ud�losti.
	 */
	void eventColorChanged(IObject o);

	/**
	 * Ud�lost zm�ny tlou��ky ��ry.
	 * @param o Zdroj ud�losti.
	 */
	void eventLineWidthChanged(IObject o);

	/**
	 * Ud�lost p�i zm�n� v�b�ru bod�.
	 * @param o Zdroj ud�losti.
	 */
	void eventPointSelectionChanged(IObject o);

	/**
	 * Ud�lost zm�ny textov� zpr�vy objektu.
	 * @param o Zdroj ud�losti.
	 */
	void eventStateMessageChanged(IObject o);

	/**
	 * Ud�lost zm�ny specifick�ch nastaven� konkr�tn�ch implementac� objekt�. (Nap�: stupe� k�ivky u aproximace).
	 * @param o Zdroj ud�losti.
	 */
	void eventSpecificPropertiesChanged(IObject o);

	/**
	 * Ud�lost p�id�n� �i odebr�n� jednoho nebo v�ce bod�.
	 */
	void eventSizeChanged(IObject o);
}
