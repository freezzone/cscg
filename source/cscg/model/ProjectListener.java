package cscg.model;

import cscg.model.objects.IObject;

/**
 * Interface posluchaèù projktu.
 * @author Tomáš Režnar
 */
public interface ProjectListener {
	/**
	 * Událost zmìny názvu projektu a souboru pro ukládání projektu.
	 */
	public void eventFileChanged(String name, String file);
	/**
	 * Událost zmìny mezi zobrazením jednoho a ètyø viewportù.
	 * @param displayAllViewports true když je práce v 3D
	 */
	public void eventDisplayAllViewportsChanged(boolean displayAllViewports);
	/**
	 * Událost pøi pøidání objektu do projektu.
	 * @param newObject Pøidaný objekt.
	 * @param index Index objektu.
	 */
	public void eventObjectAdded(IObject newObject,int index);
	/**
	 * Událost pøi odebrání objektu z projektu.
	 * @param index Index objektu.
	 * @param oldObject Odebraný objekt.
	 */
	public void eventObjectRemoved(IObject oldObject,int index);
	/**
	 * Událost zmìny poøadí objektù.
	 */
	public void eventObjectsOrderChanged();
	/**
	 * Událost zmìny vybraného objektu.
	 * @param selectedObject Novì vybraný objekt (null pokud žádný).
	 * @param index Index novì vybraného objektu (-1 pokud žádný).
	 */
	public void eventSelectedObjectChanged(IObject selectedObject,int index);
	/**
	 * Událost zmìny zobrazení v vieportu 1.
	 */
	public void eventVieport1Changed(Projection p);
	/**
	 * Událost zmìny zobrazení v vieportu 2.
	 */
	public void eventVieport2Changed(Projection p);
	/**
	 * Událost zmìny zobrazení v vieportu 3.
	 */
	public void eventVieport3Changed(Projection p);
	/**
	 * Událost zmìny zobrazení v vieportu 4.
	 */
	public void eventVieport4Changed(Projection p);
	/**
	 * Událost zmìny zapnutí exluzivního zobrazení vybraného objektu.
	 */
	public void eventExlusiveVisibilityChanged();
	/**
	 * Událost zmìny režimu režimu editoru.
	 */
	void eventEditorInEditingMode(boolean editorInEditingMode);
	/**
	 * Událost zmìny zobrazení os v editoru.
	 */
	public void eventShowAxesChanged(boolean showAxes);
	/**
	 * Událost zmìny zobrazení informaèního textu v editoru.
	 */
	public void eventShowInformationText(boolean showInformationText);
	/**
	 * Událost zmìny zobrazení orientaèní ikony kamery v editoru.
	 */
	public void eventShowOrientationIcon(boolean showOrientationIcon);
}
