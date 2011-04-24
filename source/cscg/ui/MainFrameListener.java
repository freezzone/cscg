package cscg.ui;

import cscg.model.Project;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Interface posluchaèù událostí objektù {@link MainFrame}.
 * @author Tomáš Režnar
 */
public interface MainFrameListener
{

	/**
	 * Událost ukonèení aplikace.
	 */
	void eventExit();

	/**
	 * Událost zavøení projektu/souboru.
	 */
	void eventCloseProject(Project project);

	/**
	 * Událost vytvoøení nového projektu.
	 */
	void eventNewProject();

	/**
	 * Událost volby režimu zobrazení.
	 * @param showCombination True pro zobrazení ètyø viewportù.
	 */
	public void eventSetViewportCombination(Project project, boolean showCombination);

	/**
	 * Událost nastavení pracovního projektu.
	 * @param index Index pracovního projektu.
	 */
	void eventSetWorkingProject(int index);

	/**
	 * Událost nastavení vybraného projektu.
	 * @param indexOfObject Index vybraného objektu.
	 */
	public void eventSetSelectedObject(Project project, int indexOfObject);

	/**
	 * Vyvolání události relativního posunu bodu.
	 * @param point Bod pro pøesun.
	 * @param x Zmìna x.
	 * @param y Zmìna y.
	 * @param z Zmìna z.
	 */
	public void eventMovePointRelative(Project project, IObject object, IPoint3f point, float x, float y, float z);

	/**
	 * Vyvolání události relativního posunu bodù.
	 * @param points Body pro pøesun.
	 * @param x Zmìna x.
	 * @param y Zmìna y.
	 * @param z Zmìna z.
	 */
	public void eventMovePointsRelative(Project project, IObject object, List<IPoint3f> points, float x, float y, float z);

	/**
	 * Vyvolání události posunu bodu na pozici.
	 * @param point Bod pro pøesun.
	 * @param x Zmìna x.
	 * @param y Zmìna y.
	 * @param z Zmìna z.
	 */
	public void eventMovePointAbsolute(Project project, IObject object, IPoint3f point, float x, float y, float z);

	/**
	 * Vyvolání události smazání všech vybraných bodù z objektu.
	 */
	public void eventDeleteSelectedPointsFromObject(Project project, IObject object);

	/**
	 * Událost pøidání nového bodu.
	 * @param point Nový bod.
	 */
	public void eventAddPoint(Project project, IObject object, IPoint3f point);

	/**
	 * Událost zmìny názvu objektu.
	 */
	public void eventObjectRename(Project project, IObject object, String text);

	/**
	 * Událost pøidání nového objektu.
	 * @param className Jméno tøídy pro vytvoøení.
	 */
	public void eventAddObject(Project project, String className);

	/**
	 * Událost posunutí vybraného objektu nahoru.
	 */
	public void eventMoveUpObject(Project project, IObject object);

	/**
	 * Událost posunutí vybraného objektu dolù.
	 */
	public void eventMoveDownObject(Project project, IObject object);

	/**
	 * Událost nastavení viditelnosti vybraného objektu.
	 */
	public void eventSetObjectVisibility(Project project, IObject object, boolean visible);

	/**
	 * Událost nastavení exluzivní viditelnosti vybraného objektu.
	 */
	public void eventSetObjectExlusiveVisibility(Project project, IObject object, boolean visible);

	/**
	 * Událost odstranìní objektu z vybraneho projektu.
	 */
	public void eventRemoveObject(Project project, IObject object);

	/**
	 * Událost rotace objektu.
	 * @param pivot Støed otáèení.
	 * @param axes Osa otáèení.
	 * @param angle Úhel ve stupních.
	 */
	public void eventRotateObject(Project project, IObject object, IPoint3f pivot, IPoint3f axes, float angle);

	/**
	 * Událost posunu objektu.
	 * @param vector Vektor pøesunu objektu.
	 */
	public void eventMoveObject(Project project, IObject object, IPoint3f vector);

	/**
	 * Událost nastavení editaèního režimu pro okno OpenGL.
	 */
	public void eventSetEditingMode(Project project, boolean editingMode);

	/**
	 * Událost exportu obrázku.
	 */
	public void eventExportImage(Project project, BufferedImage image);

	/**
	 * Událost otevøení projektu.
	 */
	public void eventOpenProject();

	/**
	 * Událost uložení projektu.
	 */
	public void eventSaveProject(Project project);

	/**
	 * Událost uložení projektu jako.
	 */
	public void eventSaveAsProject(Project project);

	/**
	 * Událost zmìny nastavení zobrazování os.
	 */
	public void eventShowAxes(Project project, boolean showAxes);

	/**
	 * Událost editace bodu.
	 * @param object Editovaný objekt.
	 * @param oldPoint Pùvodní bod.
	 * @param newPoint Upravený bod.
	 */
	public void eventEditPoint(Project project, IObject object, IPoint3f oldPoint, IPoint3f newPoint);

	/**
	 * Událost zmìny výbìru bodù.
	 * @param object Objekt jehož se týká výbìr bodù.
	 * @param selectedPoints Pole indexù vybraných bodù.
	 */
	public void eventSetSelectedPointByIndex(Project project, IObject object, int[] selectedPoints);

	/**
	 * Událost zmìny nastavení zobrazení informaèního textu v editoru.
	 */
	public void eventShowInformationText(Project project, boolean showInformationText);

	/**
	 * Událost zmìny nastavení zobrazení ikony orientace kamery v editoru.
	 */
	public void eventShowOrientationIcon(Project project, boolean showOrientationIcon);

	/**
	 * Událost otevøení okna pro editaci bodù a uzlových vektorù objektu.
	 * @param object Objekt jež bude v oknì editován.
	 */
	public void eventOpenNodesFrame(Project project, IObject object);

	/**
	 * Událost otevøení okna s nastavením programu.
	 */
	public void eventOpenSettingsFrame();

	/**
	 * Událost zrušení výbìru bodù.
	 */
	public void eventCancelPointsSelection(Project project, IObject object);

	/**
	 * Událost pøidání bodu do výbìru.
	 */
	public void eventAddPointToSelection(Project project, IObject object, IPoint3f point);

	/**
	 * Událost nastavení výbìru z bodu.
	 */
	public void eventSetPointAsSelection(Project project, IObject object, IPoint3f point);
}
