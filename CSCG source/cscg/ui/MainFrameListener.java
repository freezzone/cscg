package cscg.ui;

import cscg.model.Project;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Interface posluchačů událostí objektů {@link MainFrame}.
 * @author Tomáš Režnar
 */
public interface MainFrameListener
{

	/**
	 * Událost ukončení aplikace.
	 */
	void eventExit();

	/**
	 * Událost zavření projektu/souboru.
	 */
	void eventCloseProject(Project project);

	/**
	 * Událost vytvoření nového projektu.
	 */
	void eventNewProject();

	/**
	 * Událost volby režimu zobrazení.
	 * @param showCombination True pro zobrazení čtyř viewportů.
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
	 * @param point Bod pro přesun.
	 * @param x Změna x.
	 * @param y Změna y.
	 * @param z Změna z.
	 */
	public void eventMovePointRelative(Project project, IObject object, IPoint3f point, float x, float y, float z);

	/**
	 * Vyvolání události relativního posunu bodů.
	 * @param points Body pro přesun.
	 * @param x Změna x.
	 * @param y Změna y.
	 * @param z Změna z.
	 */
	public void eventMovePointsRelative(Project project, IObject object, List<IPoint3f> points, float x, float y, float z);

	/**
	 * Vyvolání události posunu bodu na pozici.
	 * @param point Bod pro přesun.
	 * @param x Změna x.
	 * @param y Změna y.
	 * @param z Změna z.
	 */
	public void eventMovePointAbsolute(Project project, IObject object, IPoint3f point, float x, float y, float z);

	/**
	 * Vyvolání události smazání všech vybraných bodů z objektu.
	 */
	public void eventDeleteSelectedPointsFromObject(Project project, IObject object);

	/**
	 * Událost přidání nového bodu.
	 * @param point Nový bod.
	 */
	public void eventAddPoint(Project project, IObject object, IPoint3f point);

	/**
	 * Událost změny názvu objektu.
	 */
	public void eventObjectRename(Project project, IObject object, String text);

	/**
	 * Událost přidání nového objektu.
	 * @param className Jméno třídy pro vytvoření.
	 */
	public void eventAddObject(Project project, String className);

	/**
	 * Událost posunutí vybraného objektu nahoru.
	 */
	public void eventMoveUpObject(Project project, IObject object);

	/**
	 * Událost posunutí vybraného objektu dolů.
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
	 * Událost odstranění objektu z vybraneho projektu.
	 */
	public void eventRemoveObject(Project project, IObject object);

	/**
	 * Událost rotace objektu.
	 * @param pivot Střed otáčení.
	 * @param axes Osa otáčení.
	 * @param angle Úhel ve stupních.
	 */
	public void eventRotateObject(Project project, IObject object, IPoint3f pivot, IPoint3f axes, float angle);

	/**
	 * Událost posunu objektu.
	 * @param vector Vektor přesunu objektu.
	 */
	public void eventMoveObject(Project project, IObject object, IPoint3f vector);

	/**
	 * Událost nastavení editačního režimu pro okno OpenGL.
	 */
	public void eventSetEditingMode(Project project, boolean editingMode);

	/**
	 * Událost exportu obrázku.
	 */
	public void eventExportImage(Project project, BufferedImage image);

	/**
	 * Událost otevření projektu.
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
	 * Událost změny nastavení zobrazování os.
	 */
	public void eventShowAxes(Project project, boolean showAxes);

	/**
	 * Událost editace bodu.
	 * @param object Editovaný objekt.
	 * @param oldPoint Původní bod.
	 * @param newPoint Upravený bod.
	 */
	public void eventEditPoint(Project project, IObject object, IPoint3f oldPoint, IPoint3f newPoint);

	/**
	 * Událost změny výběru bodů.
	 * @param object Objekt jehož se týká výběr bodů.
	 * @param selectedPoints Pole indexů vybraných bodů.
	 */
	public void eventSetSelectedPointByIndex(Project project, IObject object, int[] selectedPoints);

	/**
	 * Událost změny nastavení zobrazení informačního textu v editoru.
	 */
	public void eventShowInformationText(Project project, boolean showInformationText);

	/**
	 * Událost změny nastavení zobrazení ikony orientace kamery v editoru.
	 */
	public void eventShowOrientationIcon(Project project, boolean showOrientationIcon);

	/**
	 * Událost otevření okna pro editaci bodů a uzlových vektorů objektu.
	 * @param object Objekt jež bude v okně editován.
	 */
	public void eventOpenNodesFrame(Project project, IObject object);

	/**
	 * Událost otevření okna s nastavením programu.
	 */
	public void eventOpenSettingsFrame();

	/**
	 * Událost zrušení výběru bodů.
	 */
	public void eventCancelPointsSelection(Project project, IObject object);

	/**
	 * Událost přidání bodu do výběru.
	 */
	public void eventAddPointToSelection(Project project, IObject object, IPoint3f point);

	/**
	 * Událost nastavení výběru z bodu.
	 */
	public void eventSetPointAsSelection(Project project, IObject object, IPoint3f point);
}
