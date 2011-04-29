package cscg.model.objects;

import cscg.model.Projection;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import javax.media.opengl.GL2;

/**
 * Rozhraní objektů projektu. Samotný objekt může být například křivka nebo plocha. Objekt je vykreslitelná
 * komponenta, s vlastním GUI.
 * @author Tomáš Režnar
 */
public interface IObject extends Serializable
{

	/**
	 * Nastavení jména objektu.
	 * @param name Jméno objektu.
	 */
	void setName(String name);

	/**
	 * Získání jména objektu.
	 * @return Jméno objektu.
	 */
	String getName();

	/**
	 * Přidání posluchače objektu.
	 */
	void addObjectListener(ObjectListener l);

	/**
	 * Odebrání posluchače objektu.
	 */
	void removeObjectListener(ObjectListener l);

	/**
	 * Získání hranic objektu.
	 */
	Bounds getBounds();

	/**
	 * Získání všech řídících bodů tvořících objekt.
	 */
	List<IPoint3f> getPoints();

	/**
	 * Vrácení počtu řídících bodů v objektu.
	 */
	public int getPointsCount();

	/**
	 * Získání jednoho bodu objektu dle indexu.
	 * @throws IndexOutOfBoundsException Při zadání neplatného indexu.
	 */
	IPoint3f getPoint(int index);

	/**
	 * Relativní posunutí řídícího bodu.
	 * Pokud zadaný bod není řídící bod této instance objektu, pak nedojde k žádné změně.
	 * @param point Bod který budeme posouvat.
	 * @param xOffset Posunutí v ose x.
	 * @param yOffset Posunutí v ose y.
	 * @param zOffset Posunutí v ose z.
	 */
	public void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset);

	/**
	 * Absolutní posunutí bodu.
	 * Pokud zadaný bod není řídící bod této instance objektu, pak nedojde k žádné změně.
	 * @param point Bod který budeme posouvat.
	 * @param x Souřadnice v ose x.
	 * @param y Souřadnice v ose y.
	 * @param z Souřadnice v ose z.
	 */
	public void movePointTo(IPoint3f point, float x, float y, float z);

	/**
	 * Upraví nastavení bodu dle nastavení jiného bodu,
	 * Pokud zadaný bod není řídící bod této instance objektu, pak nedojde k žádné změně.
	 * @param editedPoint Editovaný bod.
	 * @param setBy Bod, jehož vlastnosti budou uloženy do editovaného bodu.
	 */
	public void editPoint(IPoint3f editedPoint, IPoint3f setBy);

	/**
	 * Vykreslení objektu pomocí OpenGL2.
	 * @param gl OpenGL objekt využitý pro kreslení.
	 */
	public abstract void draw(GL2 gl);

	/**
	 * Vykreslení řídící sítě objektu: řídících bodů, vektorů a ostatních prvků určených k editaci objektu.
	 * @param gl OpenGL objekt využitý pro kreslení.
	 * @param projection Aktuální nastavená projekce ve které se řídící síť vykresluje.
	 */
	public abstract void drawNodes(GL2 gl, Projection projection);

	/**
	 * Získání pole vybraných řídících bodů.
	 * @return Pole vybraných bodů.
	 */
	public List<IPoint3f> getSelectedPoints();

	/**
	 * Získání pole indexů vybraných řídících bodů.
	 * @return Pole indexů vybraných bodů.
	 */
	public int[] getSelectedPointsIndexes();

	/**
	 * Vrácení počtu vybraných řídících bodů.
	 * @return počet vybraných bodů.
	 */
	public int getSelectedPointsCount();

	/**
	 * Vrátí naposledy přidaný bod v seznamu vybraných řídících bodů.
	 * @return Vybraný bod nebo null.
	 */
	public IPoint3f getLastSelectedPoint();

	/**
	 * Nastavení vybraných řídících bodů.
	 * @param selectedPoints Seznam bodů jež mají být vybrány, body které nejsou řídící body daného objektu budou
	 * ignorovány.
	 */
	public void setSelectedPoints(List<? extends IPoint3f> selectedPoints);

	/**
	 * Nastavení vybraných řídících bodů.
	 * @param selectedPoints Pole indexů řídících bodů jež mají být vybrány. Neplatné indexy budou ignorovány.
	 */
	public void setSelectedPoints(int[] selectedPoints);

	/**
	 * Nastaví řídící bod jako vybraný.
	 * @param selectedPoint Řídící bod, pokud je bod neplatný, nebude vybrán žádný bod.
	 */
	public void setSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Přidání řídícího bodu mezi vybrané body.
	 * @param selectedPoint Řídící bod, pokud je bod neplatný, nebude přidán.
	 */
	public void addSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Odebrání řídícího bodu z vybraných bodů.
	 * @param selectedPoint Bod který již nebude vybrán. Pokud bod není vybrán nebo není platný řídící bod objektu,
	 * nedojde k žádné změně.
	 */
	public void cancelSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Zrušení výběru řídících bodů.
	 */
	public void cancelSelectedPoints();

	/**
	 * Vygenerování GUI pro nastavení vlastností objektu.
	 * Může vracet null pokud objekt žádné nastavovací GUI nepodporuje.
	 */
	public Component getObjectGUI();

	/**
	 * Vygenerování GUI pro nastavení/zobrazení vlastností vybraného bodu.
	 * Může vracet null pokud objekt žádné nastavovací GUI nepodporuje
	 */
	public Component getPointGUI();

	/**
	 * Zjištění stavu objektu
	 * @return Aktuální stav objektu. Stav se může měnit na základě volání metod (přidání bodu, změna bodu, vykreslení
	 * objektu).
	 */
	public ObjectState getState();

	/**
	 * Vrátí popis stavu-např. proč není objekt vykreslen... Nebo null pokud není zpráva.
	 */
	public String getStateMessage();

	/**
	 * Nastaví zobrazování objektu.
	 */
	public void setVisible(boolean visible);

	/**
	 * Vrátí true pokud je objekt viditelný a má být vykreslen.
	 */
	public boolean isVisible();

	/**
	 * Rotace objektu.
	 * @param pivot Středový bod rotace.
	 * @param axesVector Vektor rotace. Osa rotace je poté dána bodem pivot a vektorem axesVector.
	 * @param angle Úhel rotace ve stupních podle pravidla levé ruky.
	 */
	public void rotate(IPoint3f pivot, IPoint3f axesVector, double angle);

	/**
	 * Přesun celého objektu na souřadnice.
	 * @param to Souřadnice kam přenést objekt. Po přenosu se na daných souřadnicích bude nacházet střed objektu.
	 */
	public void translateTo(IPoint3f to);

	/**
	 * Relativní přesun celého objektu.
	 * @param vector Vektor určující dráhu pohybu.
	 */
	public void translateBy(IPoint3f vector);

	/**
	 * Získá souřadnice středu objektu.
	 * @return Bod nacházející se ve středu objektu vypočtený jako střed prostoru vymezeného nevzdálenějšímy řídícímy
	 * body objektu.
	 */
	public IPoint3f getCenter();

	/**
	 * Získá průměrný střed objektu - aritmetický průměr všech řídících bodů ze kterých se objekt skládá.
	 */
	public IPoint3f getAverageCenter();
}
