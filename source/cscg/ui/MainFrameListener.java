package cscg.ui;

import cscg.model.Project;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Interface poslucha�� ud�lost� objekt� {@link MainFrame}.
 * @author Tom� Re�nar
 */
public interface MainFrameListener
{

	/**
	 * Ud�lost ukon�en� aplikace.
	 */
	void eventExit();

	/**
	 * Ud�lost zav�en� projektu/souboru.
	 */
	void eventCloseProject(Project project);

	/**
	 * Ud�lost vytvo�en� nov�ho projektu.
	 */
	void eventNewProject();

	/**
	 * Ud�lost volby re�imu zobrazen�.
	 * @param showCombination True pro zobrazen� �ty� viewport�.
	 */
	public void eventSetViewportCombination(Project project, boolean showCombination);

	/**
	 * Ud�lost nastaven� pracovn�ho projektu.
	 * @param index Index pracovn�ho projektu.
	 */
	void eventSetWorkingProject(int index);

	/**
	 * Ud�lost nastaven� vybran�ho projektu.
	 * @param indexOfObject Index vybran�ho objektu.
	 */
	public void eventSetSelectedObject(Project project, int indexOfObject);

	/**
	 * Vyvol�n� ud�losti relativn�ho posunu bodu.
	 * @param point Bod pro p�esun.
	 * @param x Zm�na x.
	 * @param y Zm�na y.
	 * @param z Zm�na z.
	 */
	public void eventMovePointRelative(Project project, IObject object, IPoint3f point, float x, float y, float z);

	/**
	 * Vyvol�n� ud�losti relativn�ho posunu bod�.
	 * @param points Body pro p�esun.
	 * @param x Zm�na x.
	 * @param y Zm�na y.
	 * @param z Zm�na z.
	 */
	public void eventMovePointsRelative(Project project, IObject object, List<IPoint3f> points, float x, float y, float z);

	/**
	 * Vyvol�n� ud�losti posunu bodu na pozici.
	 * @param point Bod pro p�esun.
	 * @param x Zm�na x.
	 * @param y Zm�na y.
	 * @param z Zm�na z.
	 */
	public void eventMovePointAbsolute(Project project, IObject object, IPoint3f point, float x, float y, float z);

	/**
	 * Vyvol�n� ud�losti smaz�n� v�ech vybran�ch bod� z objektu.
	 */
	public void eventDeleteSelectedPointsFromObject(Project project, IObject object);

	/**
	 * Ud�lost p�id�n� nov�ho bodu.
	 * @param point Nov� bod.
	 */
	public void eventAddPoint(Project project, IObject object, IPoint3f point);

	/**
	 * Ud�lost zm�ny n�zvu objektu.
	 */
	public void eventObjectRename(Project project, IObject object, String text);

	/**
	 * Ud�lost p�id�n� nov�ho objektu.
	 * @param className Jm�no t��dy pro vytvo�en�.
	 */
	public void eventAddObject(Project project, String className);

	/**
	 * Ud�lost posunut� vybran�ho objektu nahoru.
	 */
	public void eventMoveUpObject(Project project, IObject object);

	/**
	 * Ud�lost posunut� vybran�ho objektu dol�.
	 */
	public void eventMoveDownObject(Project project, IObject object);

	/**
	 * Ud�lost nastaven� viditelnosti vybran�ho objektu.
	 */
	public void eventSetObjectVisibility(Project project, IObject object, boolean visible);

	/**
	 * Ud�lost nastaven� exluzivn� viditelnosti vybran�ho objektu.
	 */
	public void eventSetObjectExlusiveVisibility(Project project, IObject object, boolean visible);

	/**
	 * Ud�lost odstran�n� objektu z vybraneho projektu.
	 */
	public void eventRemoveObject(Project project, IObject object);

	/**
	 * Ud�lost rotace objektu.
	 * @param pivot St�ed ot��en�.
	 * @param axes Osa ot��en�.
	 * @param angle �hel ve stupn�ch.
	 */
	public void eventRotateObject(Project project, IObject object, IPoint3f pivot, IPoint3f axes, float angle);

	/**
	 * Ud�lost posunu objektu.
	 * @param vector Vektor p�esunu objektu.
	 */
	public void eventMoveObject(Project project, IObject object, IPoint3f vector);

	/**
	 * Ud�lost nastaven� edita�n�ho re�imu pro okno OpenGL.
	 */
	public void eventSetEditingMode(Project project, boolean editingMode);

	/**
	 * Ud�lost exportu obr�zku.
	 */
	public void eventExportImage(Project project, BufferedImage image);

	/**
	 * Ud�lost otev�en� projektu.
	 */
	public void eventOpenProject();

	/**
	 * Ud�lost ulo�en� projektu.
	 */
	public void eventSaveProject(Project project);

	/**
	 * Ud�lost ulo�en� projektu jako.
	 */
	public void eventSaveAsProject(Project project);

	/**
	 * Ud�lost zm�ny nastaven� zobrazov�n� os.
	 */
	public void eventShowAxes(Project project, boolean showAxes);

	/**
	 * Ud�lost editace bodu.
	 * @param object Editovan� objekt.
	 * @param oldPoint P�vodn� bod.
	 * @param newPoint Upraven� bod.
	 */
	public void eventEditPoint(Project project, IObject object, IPoint3f oldPoint, IPoint3f newPoint);

	/**
	 * Ud�lost zm�ny v�b�ru bod�.
	 * @param object Objekt jeho� se t�k� v�b�r bod�.
	 * @param selectedPoints Pole index� vybran�ch bod�.
	 */
	public void eventSetSelectedPointByIndex(Project project, IObject object, int[] selectedPoints);

	/**
	 * Ud�lost zm�ny nastaven� zobrazen� informa�n�ho textu v editoru.
	 */
	public void eventShowInformationText(Project project, boolean showInformationText);

	/**
	 * Ud�lost zm�ny nastaven� zobrazen� ikony orientace kamery v editoru.
	 */
	public void eventShowOrientationIcon(Project project, boolean showOrientationIcon);

	/**
	 * Ud�lost otev�en� okna pro editaci bod� a uzlov�ch vektor� objektu.
	 * @param object Objekt je� bude v okn� editov�n.
	 */
	public void eventOpenNodesFrame(Project project, IObject object);

	/**
	 * Ud�lost otev�en� okna s nastaven�m programu.
	 */
	public void eventOpenSettingsFrame();

	/**
	 * Ud�lost zru�en� v�b�ru bod�.
	 */
	public void eventCancelPointsSelection(Project project, IObject object);

	/**
	 * Ud�lost p�id�n� bodu do v�b�ru.
	 */
	public void eventAddPointToSelection(Project project, IObject object, IPoint3f point);

	/**
	 * Ud�lost nastaven� v�b�ru z bodu.
	 */
	public void eventSetPointAsSelection(Project project, IObject object, IPoint3f point);
}
