package cscg.model;

import cscg.model.objects.IObject;

/**
 * Interface posluchačů projektu {@link Project}.
 * @author Tomáš Režnar
 */
public interface ProjectListener {
	/**
	 * Událost změny názvu projektu a souboru pro ukládání projektu.
	 */
	public void eventFileChanged(String name, String file);
	/**
	 * Událost změny mezi zobrazením jednoho a čtyř viewportů.
	 * @param displayAllViewports true když je práce v 3D
	 */
	public void eventDisplayAllViewportsChanged(boolean displayAllViewports);
	/**
	 * Událost při přidání objektu do projektu.
	 * @param newObject Přidaný objekt.
	 * @param index Index objektu.
	 */
	public void eventObjectAdded(IObject newObject,int index);
	/**
	 * Událost při odebrání objektu z projektu.
	 * @param index Index objektu.
	 * @param oldObject Odebraný objekt.
	 */
	public void eventObjectRemoved(IObject oldObject,int index);
	/**
	 * Událost změny pořadí objektů.
	 */
	public void eventObjectsOrderChanged();
	/**
	 * Událost změny vybraného objektu.
	 * @param selectedObject Nově vybraný objekt (null pokud žádný).
	 * @param index Index nově vybraného objektu (-1 pokud žádný).
	 */
	public void eventSelectedObjectChanged(IObject selectedObject,int index);
	/**
	 * Událost změny zobrazení v viewportu 1.
	 */
	public void eventVieport1Changed(Projection p);
	/**
	 * Událost změny zobrazení v viewportu 2.
	 */
	public void eventVieport2Changed(Projection p);
	/**
	 * Událost změny zobrazení v viewportu 3.
	 */
	public void eventVieport3Changed(Projection p);
	/**
	 * Událost změny zobrazení v viewportu 4.
	 */
	public void eventVieport4Changed(Projection p);
	/**
	 * Událost změny zapnutí exluzivního zobrazení vybraného objektu.
	 */
	public void eventExlusiveVisibilityChanged();
	/**
	 * Událost změny režimu editoru.
	 */
	void eventEditorInEditingMode(boolean editorInEditingMode);
	/**
	 * Událost změny zobrazení os v editoru.
	 */
	public void eventShowAxesChanged(boolean showAxes);
	/**
	 * Událost změny zobrazení informačního textu v editoru.
	 */
	public void eventShowInformationText(boolean showInformationText);
	/**
	 * Událost změny zobrazení orientační ikony kamery v editoru.
	 */
	public void eventShowOrientationIcon(boolean showOrientationIcon);
}
