package cscg.model.objects;

import cscg.model.Projection;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import javax.media.opengl.GL2;

/**
 * Rozhran� objekt� projektu. Samotn� objekt m��e b�t nap��klad k�ivka nebo plocha. Objekt je vykresliteln�
 * komponenta, s vlastn�m GUI.
 * @author Tom� Re�nar
 */
public interface IObject extends Serializable
{

	/**
	 * Nastaven� jm�na objektu.
	 * @param name Jm�no objektu.
	 */
	void setName(String name);

	/**
	 * Z�sk�n� jm�na objektu.
	 * @return Jm�no objektu.
	 */
	String getName();

	/**
	 * P�id�n� poslucha�e objektu.
	 */
	void addObjectListener(ObjectListener l);

	/**
	 * Odebr�n� poslucha�e objektu.
	 */
	void removeObjectListener(ObjectListener l);

	/**
	 * Z�sk�n� hranic objektu.
	 */
	Bounds getBounds();

	/**
	 * Z�sk�n� v�ech ��d�c�ch bod� tvo��c�ch objekt.
	 */
	List<IPoint3f> getPoints();

	/**
	 * Vr�cen� po�tu ��d�c�ch bod� v objektu.
	 */
	public int getPointsCount();

	/**
	 * Z�sk�n� jednoho bodu objektu dle indexu.
	 * @throws IndexOutOfBoundsException P�i zad�n� neplatn�ho indexu.
	 */
	IPoint3f getPoint(int index);

	/**
	 * Relativn� posunut� ��d�c�ho bodu.
	 * Pokud zadan� bod nen� ��d�c� bod t�to instance objektu, pak nedojde k ��dn� zm�n�.
	 * @param point Bod kter� budeme posouvat.
	 * @param xOffset Posunut� v ose x.
	 * @param yOffset Posunut� v ose y.
	 * @param zOffset Posunut� v ose z.
	 */
	public void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset);

	/**
	 * Absolutn� posunut� bodu.
	 * Pokud zadan� bod nen� ��d�c� bod t�to instance objektu, pak nedojde k ��dn� zm�n�.
	 * @param point Bod kter� budeme posouvat.
	 * @param x Sou�adnice v ose x.
	 * @param y Sou�adnice v ose y.
	 * @param z Sou�adnice v ose z.
	 */
	public void movePointTo(IPoint3f point, float x, float y, float z);

	/**
	 * Uprav� nastaven� bodu dle nastaven� jin�ho bodu,
	 * Pokud zadan� bod nen� ��d�c� bod t�to instance objektu, pak nedojde k ��dn� zm�n�.
	 * @param editedPoint Editovan� bod.
	 * @param setBy Bod, jeho� vlastnosti budou ulo�eny do editovan�ho bodu.
	 */
	public void editPoint(IPoint3f editedPoint, IPoint3f setBy);

	/**
	 * Vykreslen� objektu pomoc� OpenGL2.
	 * @param gl OpenGL objekt vyu�it� pro kreslen�.
	 */
	public abstract void draw(GL2 gl);

	/**
	 * Vykreslen� ��d�c� s�t� objektu: ��d�ch bod�, vektor� a ostatn�ch prvk� ur�en�ch k editaci objektu.
	 * @param gl OpenGL objekt vyu�it� pro kreslen�.
	 * @param projection Aktu�ln� nastaven� projekce ve kter� se ��d�c� s� vykresluje.
	 */
	public abstract void drawNodes(GL2 gl, Projection projection);

	/**
	 * Z�sk�n� pole vybran�ch ��d�c�ch bod�.
	 * @return Pole vybran�ch bod�.
	 */
	public List<IPoint3f> getSelectedPoints();

	/**
	 * Z�sk�n� pole index� vybran�ch ��d�c�ch bod�.
	 * @return Pole index� vybran�ch bod�.
	 */
	public int[] getSelectedPointsIndexes();

	/**
	 * Vr�cen� po�tu vybran�ch ��d�c�ch bod�.
	 * @return po�et vybran�ch bod�.
	 */
	public int getSelectedPointsCount();

	/**
	 * Vr�t� naposledy p�idan� bod v seznamu vybran�ch ��d�c�ch bod�.
	 * @return Vybran� bod nebo null.
	 */
	public IPoint3f getLastSelectedPoint();

	/**
	 * Nastaven� vybran�ch ��d�c�ch bod�.
	 * @param selectedPoints Seznam bod� je� maj� b�t vybr�ny, body kter� nejsou ��d�c� body dan�ho objektu budou
	 * ignorov�ny.
	 */
	public void setSelectedPoints(List<? extends IPoint3f> selectedPoints);

	/**
	 * Nastaven� vybran�ch ��d�c�ch bod�.
	 * @param selectedPoints Pole index� ��d�c�ch bod� je� maj� b�t vybr�ny. Neplatn� indexy budou ignorov�ny.
	 */
	public void setSelectedPoints(int[] selectedPoints);

	/**
	 * Nastav� ��d�c� bod jako vybran�.
	 * @param selectedPoint ��d�c� bod, pokud je bod neplatn�, nebude vybr�n ��dn� bod.
	 */
	public void setSelectedPoint(IPoint3f selectedPoint);

	/**
	 * P�id�n� ��d�c�ho bodu mezi vybran� body.
	 * @param selectedPoint ��d�c� bod, pokud je bod neplatn�, nebude p�id�n.
	 */
	public void addSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Odebr�n� ��d�c�ho bodu z vybran�ch bod�.
	 * @param selectedPoint Bod kter� ji� nebude vybr�n. Pokud bod nen� vybr�n nebo nen� platn� ��d�c� bod objektu,
	 * nedojde k ��dn� zm�n�.
	 */
	public void cancelSelectedPoint(IPoint3f selectedPoint);

	/**
	 * Zru�en� v�b�ru ��d�c�ch bod�.
	 */
	public void cancelSelectedPoints();

	/**
	 * Vygenerov�n� GUI pro nastaven� vlastnost� objektu.
	 * M��e vracet null pokud objekt ��dn� nastavovac� GUI nepodporuje.
	 */
	public Component getObjectGUI();

	/**
	 * Vygenerov�n� GUI pro nastaven�/zobrazen� vlastnost� vybran�ho bodu.
	 * M��e vracet null pokud objekt ��dn� nastavovac� GUI nepodporuje
	 */
	public Component getPointGUI();

	/**
	 * Zji�t�n� stavu objektu
	 * @return Aktu�ln� stav objektu. Stav se m��e m�nit na z�klad� vol�n� metod (p�id�n� bodu, zm�na bodu, vykreslen�
	 * objektu).
	 */
	public ObjectState getState();

	/**
	 * Vr�t� popis stavu-nap�. pro� nen� objekt vykreslen... Nebo null pokud nen� zpr�va.
	 */
	public String getStateMessage();

	/**
	 * Nastav� zobrazov�n� objektu.
	 */
	public void setVisible(boolean visible);

	/**
	 * Vr�t� true pokud je objekt viditeln� a m� b�t vykreslen.
	 */
	public boolean isVisible();

	/**
	 * Rotace objektu.
	 * @param pivot St�edov� bod rotace.
	 * @param axesVector Vektor rotace. Osa rotace je pot� d�na bodem pivot a vektorem axesVector.
	 * @param angle �hel rotace ve stupn�ch podle pravidla lev� ruky.
	 */
	public void rotate(IPoint3f pivot, IPoint3f axesVector, double angle);

	/**
	 * P�esun cel�ho objektu na sou�adnice.
	 * @param to Sou�adnice kam p�en�st objekt. Po p�enosu se na dan�ch sou�adnic�ch bude nach�zet st�ed objektu.
	 */
	public void translateTo(IPoint3f to);

	/**
	 * Relativn� p�esun cel�ho objektu.
	 * @param vector Vektor ur�uj�c� dr�hu pohybu.
	 */
	public void translateBy(IPoint3f vector);

	/**
	 * Z�sk� sou�adnice st�edu objektu.
	 * @return Bod nach�zej�c� se ve st�edu objektu vypo�ten� jako st�ed prostoru vymezen�ho nevzd�len�j��my ��d�c�my
	 * body objektu.
	 */
	public IPoint3f getCenter();

	/**
	 * Z�sk� pr�m�rn� st�ed objektu - aritmetick� pr�m�r v�ech ��d�c�ch bod� ze kter�ch se objekt skl�d�.
	 */
	public IPoint3f getAverageCenter();
}
