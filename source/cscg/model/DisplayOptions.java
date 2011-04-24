package cscg.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.EventObject;
import java.util.LinkedList;
import javax.media.opengl.GL2;

/**
 * T��da ukl�daj�c� nastaven� parametr� pro vykreslov�n� OpenGL.
 * @author Tom� Re�nar
 */
public class DisplayOptions implements Serializable, Cloneable
{

	/**
	 * Poslucha�i zm�n nastaven�.
	 */
	private transient LinkedList<ChangeListener> listeners;
	/**
	 * Antialiasing bod�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int pointSmooth = GL2.GL_NICEST;
	/**
	 * Antialiasing linek.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int lineSmooth = GL2.GL_NICEST;
	/**
	 * Antialiasing polygon�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int polygonSmooth = 0;
	/**
	 * Antialiasing perspektivy.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int perspectiveCorection = GL2.GL_NICEST;
	/**
	 * Testov�n� hloubky.
	 */
	private boolean depthTest = true;
	/**
	 * Vertik�ln� synchronizace.
	 */
	private boolean vsync = true;
	/**
	 * Ambientn� slo�ka sv�tla.
	 */
	private float ambientLight = 0.2f;
	/**
	 * Dif�zn� slo�ka sv�tla.
	 */
	private float difuseLight = 0.5f;
	/**
	 * Odleskov� slo�ka sv�tla.
	 */
	private float specularLight = 0.5f;
	/**
	 * Parametr odlesk� objekt�.
	 * Mus� b�t v rozmez� 0 a� 128, ��m vy�� ��slo, t�m odlesk pokryje men�� plochu.
	 */
	private float specularLightShininess = 128f;

	public DisplayOptions()
	{
		initTransients();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransients();
	}

	/**
	 * Inicializace transientn�ch vlastnost�.
	 */
	private void initTransients()
	{
		listeners = new LinkedList<ChangeListener>();
	}

	/**
	 * Ambientn� slo�ka sv�tla.
	 */
	public float getAmbientLight()
	{
		return ambientLight;
	}

	/**
	 * Testov�n� hloubky.
	 */
	public boolean isDepthTest()
	{
		return depthTest;
	}

	/**
	 * Dif�zn� slo�ka sv�tla.
	 */
	public float getDifuseLight()
	{
		return difuseLight;
	}

	/**
	 * Antialiasing bod�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getLineSmooth()
	{
		return lineSmooth;
	}

	/**
	 * Antialiasing bod�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getPointSmooth()
	{
		return pointSmooth;
	}

	/**
	 * Antialiasing polygon�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getPolygonSmooth()
	{
		return polygonSmooth;
	}

	/**
	 * Odleskov� slo�ka sv�tla.
	 */
	public float getSpecularLight()
	{
		return specularLight;
	}

	/**
	 * Parametr odlesk� objekt�.
	 * Mus� b�t v rozmez� 0 a� 128, ��m vy�� ��slo, t�m odlesk pokryje men�� plochu.
	 */
	public float getSpecularLightShininess()
	{
		return specularLightShininess;
	}

	/**
	 * Vertik�ln� synchronizace.
	 */
	public boolean isVsync()
	{
		return vsync;
	}

	/**
	 * Antialiasing perspektivy.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getPerspectiveCorection()
	{
		return perspectiveCorection;
	}

	/**
	 * Ambientn� slo�ka sv�tla.
	 */
	public void setAmbientLight(float ambientLight)
	{
		this.ambientLight = ambientLight;
		fireChange();
	}

	/**
	 * Testov�n� hloubky.
	 */
	public void setDepthTest(boolean depthTest)
	{
		this.depthTest = depthTest;
		fireChange();
	}

	/**
	 * Dif�zn� slo�ka sv�tla.
	 */
	public void setDifuseLight(float difuseLight)
	{
		this.difuseLight = difuseLight;
		fireChange();
	}

	/**
	 * Antialiasing linek.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nem� jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setLineSmooth(int lineSmooth)
	{
		checkSmoothConstant(lineSmooth);
		this.lineSmooth = lineSmooth;
		fireChange();
	}

	/**
	 * Antialiasing perspektivy.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nem� jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setPerspectiveCorection(int perspectiveCorection)
	{
		checkSmoothConstant(lineSmooth);
		this.perspectiveCorection = perspectiveCorection;
		fireChange();
	}

	/**
	 * Antialiasing bod�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nem� jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setPointSmooth(int pointSmooth)
	{
		checkSmoothConstant(lineSmooth);
		this.pointSmooth = pointSmooth;
		fireChange();
	}

	/**
	 * Antialiasing polygon�.
	 * M��e m�t hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nem� jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setPolygonSmooth(int polygonSmooth)
	{
		checkSmoothConstant(lineSmooth);
		this.polygonSmooth = polygonSmooth;
		fireChange();
	}

	/**
	 * Odleskov� slo�ka sv�tla.
	 */
	public void setSpecularLight(float specularLight)
	{
		this.specularLight = specularLight;
		fireChange();
	}

	/**
	 * Parametr odlesk� objekt�.
	 * Mus� b�t v rozmez� 0 a� 128, ��m vy�� ��slo, t�m odlesk pokryje men�� plochu.
	 * @throws IllegalArgumentException Pokud nen� zad�no ��slo v rozmez� 0 a� 128.
	 */
	public void setSpecularLightShininess(float specularLightShininess)
	{
		if (specularLightShininess < 0 || specularLightShininess > 128)
		{
			throw new IllegalArgumentException(specularLightShininess + "");
		}
		this.specularLightShininess = specularLightShininess;
		fireChange();
	}

	/**
	 * Vertik�ln� synchronizace.
	 */
	public void setVsync(boolean vsync)
	{
		this.vsync = vsync;
		fireChange();
	}

	/**
	 * Vytvo�en� kopie objektu, poslucha�i nebudou v kopii obsa�eni.
	 */
	@Override
	public Object clone()
	{
		DisplayOptions copy = new DisplayOptions();
		copy.set(this);
		return copy;
	}

	/**
	 * Nastaven� vlastnost� dle jin� instance objektu. Poslucha�i nebudou kop�rovan�.
	 * @param setBy Objekt z n�ho� dojde ke kop�rov�n� vlastnost�.
	 */
	public void set(DisplayOptions setBy)
	{
		pointSmooth = setBy.pointSmooth;
		lineSmooth = setBy.lineSmooth;
		polygonSmooth = setBy.polygonSmooth;
		perspectiveCorection = setBy.perspectiveCorection;
		depthTest = setBy.depthTest;
		ambientLight = setBy.ambientLight;
		difuseLight = setBy.difuseLight;
		specularLight = setBy.specularLight;
		specularLightShininess = setBy.specularLightShininess;
		vsync = setBy.vsync;

		fireChange();
	}

	/**
	 * P�id�n� poslucha�e zm�n.
	 */
	public void addListener(ChangeListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e zm�n.
	 */
	public void removeListener(ChangeListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Vyvol�n� ud�losti zm�ny objektu.
	 */
	private void fireChange()
	{
		for (ChangeListener l : listeners)
		{
			l.changeEvent(new EventObject(this));
		}
	}

	/**
	 * Kontrola jestli parametr smooth m� spr�vnou hodnotu.
	 * @throws IllegalArgumentException pokud parametr smooth nem� jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	private void checkSmoothConstant(int smooth)
	{
		if (smooth != 0
		  && smooth != GL2.GL_FASTEST
		  && smooth != GL2.GL_NICEST)
		{
			throw new IllegalArgumentException();
		}
	}
}
