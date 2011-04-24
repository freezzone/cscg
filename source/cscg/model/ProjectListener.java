package cscg.model;

import cscg.model.objects.IObject;

/**
 * Interface poslucha�� projktu.
 * @author Tom� Re�nar
 */
public interface ProjectListener {
	/**
	 * Ud�lost zm�ny n�zvu projektu a souboru pro ukl�d�n� projektu.
	 */
	public void eventFileChanged(String name, String file);
	/**
	 * Ud�lost zm�ny mezi zobrazen�m jednoho a �ty� viewport�.
	 * @param displayAllViewports true kdy� je pr�ce v 3D
	 */
	public void eventDisplayAllViewportsChanged(boolean displayAllViewports);
	/**
	 * Ud�lost p�i p�id�n� objektu do projektu.
	 * @param newObject P�idan� objekt.
	 * @param index Index objektu.
	 */
	public void eventObjectAdded(IObject newObject,int index);
	/**
	 * Ud�lost p�i odebr�n� objektu z projektu.
	 * @param index Index objektu.
	 * @param oldObject Odebran� objekt.
	 */
	public void eventObjectRemoved(IObject oldObject,int index);
	/**
	 * Ud�lost zm�ny po�ad� objekt�.
	 */
	public void eventObjectsOrderChanged();
	/**
	 * Ud�lost zm�ny vybran�ho objektu.
	 * @param selectedObject Nov� vybran� objekt (null pokud ��dn�).
	 * @param index Index nov� vybran�ho objektu (-1 pokud ��dn�).
	 */
	public void eventSelectedObjectChanged(IObject selectedObject,int index);
	/**
	 * Ud�lost zm�ny zobrazen� v vieportu 1.
	 */
	public void eventVieport1Changed(Projection p);
	/**
	 * Ud�lost zm�ny zobrazen� v vieportu 2.
	 */
	public void eventVieport2Changed(Projection p);
	/**
	 * Ud�lost zm�ny zobrazen� v vieportu 3.
	 */
	public void eventVieport3Changed(Projection p);
	/**
	 * Ud�lost zm�ny zobrazen� v vieportu 4.
	 */
	public void eventVieport4Changed(Projection p);
	/**
	 * Ud�lost zm�ny zapnut� exluzivn�ho zobrazen� vybran�ho objektu.
	 */
	public void eventExlusiveVisibilityChanged();
	/**
	 * Ud�lost zm�ny re�imu re�imu editoru.
	 */
	void eventEditorInEditingMode(boolean editorInEditingMode);
	/**
	 * Ud�lost zm�ny zobrazen� os v editoru.
	 */
	public void eventShowAxesChanged(boolean showAxes);
	/**
	 * Ud�lost zm�ny zobrazen� informa�n�ho textu v editoru.
	 */
	public void eventShowInformationText(boolean showInformationText);
	/**
	 * Ud�lost zm�ny zobrazen� orienta�n� ikony kamery v editoru.
	 */
	public void eventShowOrientationIcon(boolean showOrientationIcon);
}
