package cscg.model.objects;

import cscg.model.Projection;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import javax.media.opengl.GL2;

/**
 * Rozhraní objektù projektu. Samotnı objekt mùe bıt napøíklad køivka nebo plocha. Objekt je vykreslitelná
 * komponenta, s vlastním GUI.
 * @author Tomáš Renar
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
	 * Pøidání posluchaèe objektu.
	 */
	void addObjectListener(ObjectListener l);

	/**
	 * Odebrání posluchaèe objektu.
	 */
	void removeObjectListener(ObjectListener l);

	/**
	 * Získání hranic objektu.
	 */
	Bounds getBounds();

	/**
	 * Získání všech øídících bodù tvoøících objekt.
	 */
	List<IPoint3f> getPoints();

	/**
	 * Vrácení poètu øídících bodù v objektu.
	 */
	public int getPointsCount();

	/**
	 * Získání jednoho bodu objektu dle indexu.
	 * @throws IndexOutOfBoundsException Pøi zadání neplatného indexu.
	 */
	IPoint3f getPoint(int index);

	/**
	 * Relativní posunutí øídícího bodu.
	 * Pokud zadanı bod není øídící bod této instance objektu, pak nedojde k ádné zmìnì.
	 * @param point Bod kterı budeme posouvat.
	 * @param xOffset Posunutí v ose x.
	 * @param yOffset Posunutí v ose y.
	 * @param zOffset Posunutí v ose z.
	 */
	public void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset);

	/**
	 * Absolutní posunutí bodu.
	 * Pokud zadanı bod není øídící bod této instance objektu, pak nedojde k ádné zmìnì.
	 * @param point Bod kterı budeme posouvat.
	 * @param x Souøadnice v ose x.
	 * @param y Souøadnice v ose y.
	 * @param z Souøadnice v ose z.
	 */
	public void movePointTo(IPoint3f point, float x, float y, float z);

	/**
	 * Upraví nastavení bodu dle nastavení jiného bodu,
	 * Pokud zadanı bod není øídící bod této instance objektu, pak nedojde k ádné zmìnì.
	 * @param editedPoint Editovanı bod.
	 * @param setBy Bod, jeho vlastnosti budou uloeny do editovaného bodu.
	 */
	public void editPoint(IPoint3f editedPoint, IPoint3f setBy);

	/**
	 * Vykreslení objektu pomocí OpenGL2.
	 * @param gl OpenGL objekt vyuitı pro kreslení.
	 */
	public abstract void draw(GL2 gl);

	/**
	 * Vykreslení øídící sítì objektu: øídích bodù, vektorù a ostatních prvkù urèenıch k editaci objektu.
	 * @param gl OpenGL objekt vyuitı pro kreslení.
	 * @param projection Aktuální nastavená projekce ve které se øídící sí vykresluje.
	 */
	public abstract void drawNodes(GL2 gl, Projection projection);

	/**
	 * Získání pole vybranıch øídících bodù.
	 * @return Pole vybranıch bodù.
	 */
	public List<IPoint3f> getSelectedPoints();

	/**
	 * Získání pole indexù vybranıch øídících bodù.
	 * @return Pole indexù vybranıch bodù.
	 */
	public int[] getSelectedPointsIndexes();

	/**
	 * Vrácení poètu vybranıch øídících bodù.
	 * @return poèet vybranıch bodù.
	 */
	public int getSelectedPointsCount();

	/**
	 * Vrátí naposledy pøidanı bod v seznamu vybranıch øídících bodù.
	 * @return Vybranı bod nebo null.
	 */
	public IPoint3f getLastSelectedPoint();

	/**
	 * Nastavení vybranıch øídících bodù.
	 * @param selectedPoints Seznam bodù je mají bıt vybrány, body které nejsou øídící body daného objektu budou
	 * ignorovány.
	 */
	public void setSelectedPoints(List<? extends IPoint3f> selectedPoints);

	/**
	 * Nastavení vybranıch øídících bodù.
	 * @param selectedPoints Pole indexù øídících bodù je mají bıt vybrány. Neplatné indexy budou ignorovány.
	 */
	public void setSelectedPoints(int[] selectedPoints);

	/**
	 * Nastaví øídící bod jako vybranı.
	 * @param selectedPoint Øídící bod, pokud je bod neplatnı, nebude vybrán ádnı bod.
	 */
	public void setSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Pøidání øídícího bodu mezi vybrané body.
	 * @param selectedPoint Øídící bod, pokud je bod neplatnı, nebude pøidán.
	 */
	public void addSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Odebrání øídícího bodu z vybranıch bodù.
	 * @param selectedPoint Bod kterı ji nebude vybrán. Pokud bod není vybrán nebo není platnı øídící bod objektu,
	 * nedojde k ádné zmìnì.
	 */
	public void cancelSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Zrušení vıbìru øídících bodù.
	 */
	public void cancelSelectedPoints();

	/**
	 * Vygenerování GUI pro nastavení vlastností objektu.
	 * Mùe vracet null pokud objekt ádné nastavovací GUI nepodporuje.
	 */
	public Component getObjectGUI();

	/**
	 * Vygenerování GUI pro nastavení/zobrazení vlastností vybraného bodu.
	 * Mùe vracet null pokud objekt ádné nastavovací GUI nepodporuje
	 */
	public Component getPointGUI();

	/**
	 * Zjištìní stavu objektu
	 * @return Aktuální stav objektu. Stav se mùe mìnit na základì volání metod (pøidání bodu, zmìna bodu, vykreslení
	 * objektu).
	 */
	public ObjectState getState();

	/**
	 * Vrátí popis stavu-napø. proè není objekt vykreslen... Nebo null pokud není zpráva.
	 */
	public String getStateMessage();

	/**
	 * Nastaví zobrazování objektu.
	 */
	public void setVisible(boolean visible);

	/**
	 * Vrátí true pokud je objekt viditelnı a má bıt vykreslen.
	 */
	public boolean isVisible();

	/**
	 * Rotace objektu.
	 * @param pivot Støedovı bod rotace.
	 * @param axesVector Vektor rotace. Osa rotace je poté dána bodem pivot a vektorem axesVector.
	 * @param angle Úhel rotace ve stupních podle pravidla levé ruky.
	 */
	public void rotate(IPoint3f pivot, IPoint3f axesVector, double angle);

	/**
	 * Pøesun celého objektu na souøadnice.
	 * @param to Souøadnice kam pøenést objekt. Po pøenosu se na danıch souøadnicích bude nacházet støed objektu.
	 */
	public void translateTo(IPoint3f to);

	/**
	 * Relativní pøesun celého objektu.
	 * @param vector Vektor urèující dráhu pohybu.
	 */
	public void translateBy(IPoint3f vector);

	/**
	 * Získá souøadnice støedu objektu.
	 * @return Bod nacházející se ve støedu objektu vypoètenı jako støed prostoru vymezeného nevzdálenìjšímy øídícímy
	 * body objektu.
	 */
	public IPoint3f getCenter();

	/**
	 * Získá prùmìrnı støed objektu - aritmetickı prùmìr všech øídících bodù ze kterıch se objekt skládá.
	 */
	public IPoint3f getAverageCenter();
}
