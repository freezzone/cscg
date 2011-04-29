package cscg.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.EventObject;
import java.util.LinkedList;
import javax.media.opengl.GL2;

/**
 * Třída ukládající nastavení parametrů pro vykreslování OpenGL.
 * @author Tomáš Režnar
 */
public class DisplayOptions implements Serializable, Cloneable
{

	/**
	 * Posluchači změn nastavení.
	 */
	private transient LinkedList<ChangeListener> listeners;
	/**
	 * Antialiasing bodů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int pointSmooth = GL2.GL_NICEST;
	/**
	 * Antialiasing linek.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int lineSmooth = GL2.GL_NICEST;
	/**
	 * Antialiasing polygonů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int polygonSmooth = 0;
	/**
	 * Antialiasing perspektivy.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST
	 */
	private int perspectiveCorection = GL2.GL_NICEST;
	/**
	 * Testování hloubky.
	 */
	private boolean depthTest = true;
	/**
	 * Vertikální synchronizace.
	 */
	private boolean vsync = true;
	/**
	 * Ambientní složka světla.
	 */
	private float ambientLight = 0.2f;
	/**
	 * Difůzní složka světla.
	 */
	private float difuseLight = 0.5f;
	/**
	 * Odlesková složka světla.
	 */
	private float specularLight = 0.5f;
	/**
	 * Parametr odlesků objektů.
	 * Musí být v rozmezí 0 až 128, čím vyší číslo, tím odlesk pokryje menší plochu.
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
	 * Inicializace transientních vlastností.
	 */
	private void initTransients()
	{
		listeners = new LinkedList<ChangeListener>();
	}

	/**
	 * Ambientní složka světla.
	 */
	public float getAmbientLight()
	{
		return ambientLight;
	}

	/**
	 * Testování hloubky.
	 */
	public boolean isDepthTest()
	{
		return depthTest;
	}

	/**
	 * Difůzní složka světla.
	 */
	public float getDifuseLight()
	{
		return difuseLight;
	}

	/**
	 * Antialiasing bodů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getLineSmooth()
	{
		return lineSmooth;
	}

	/**
	 * Antialiasing bodů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getPointSmooth()
	{
		return pointSmooth;
	}

	/**
	 * Antialiasing polygonů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getPolygonSmooth()
	{
		return polygonSmooth;
	}

	/**
	 * Odlesková složka světla.
	 */
	public float getSpecularLight()
	{
		return specularLight;
	}

	/**
	 * Parametr odlesků objektů.
	 * Musí být v rozmezí 0 až 128, čím vyší číslo, tím odlesk pokryje menší plochu.
	 */
	public float getSpecularLightShininess()
	{
		return specularLightShininess;
	}

	/**
	 * Vertikální synchronizace.
	 */
	public boolean isVsync()
	{
		return vsync;
	}

	/**
	 * Antialiasing perspektivy.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public int getPerspectiveCorection()
	{
		return perspectiveCorection;
	}

	/**
	 * Ambientní složka světla.
	 */
	public void setAmbientLight(float ambientLight)
	{
		this.ambientLight = ambientLight;
		fireChange();
	}

	/**
	 * Testování hloubky.
	 */
	public void setDepthTest(boolean depthTest)
	{
		this.depthTest = depthTest;
		fireChange();
	}

	/**
	 * Difůzní složka světla.
	 */
	public void setDifuseLight(float difuseLight)
	{
		this.difuseLight = difuseLight;
		fireChange();
	}

	/**
	 * Antialiasing linek.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nemá jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setLineSmooth(int lineSmooth)
	{
		checkSmoothConstant(lineSmooth);
		this.lineSmooth = lineSmooth;
		fireChange();
	}

	/**
	 * Antialiasing perspektivy.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nemá jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setPerspectiveCorection(int perspectiveCorection)
	{
		checkSmoothConstant(lineSmooth);
		this.perspectiveCorection = perspectiveCorection;
		fireChange();
	}

	/**
	 * Antialiasing bodů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nemá jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setPointSmooth(int pointSmooth)
	{
		checkSmoothConstant(lineSmooth);
		this.pointSmooth = pointSmooth;
		fireChange();
	}

	/**
	 * Antialiasing polygonů.
	 * Může mít hodnoty 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 * @throws IllegalArgumentException Pokud parametr nemá jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
	 */
	public void setPolygonSmooth(int polygonSmooth)
	{
		checkSmoothConstant(lineSmooth);
		this.polygonSmooth = polygonSmooth;
		fireChange();
	}

	/**
	 * Odlesková složka světla.
	 */
	public void setSpecularLight(float specularLight)
	{
		this.specularLight = specularLight;
		fireChange();
	}

	/**
	 * Parametr odlesků objektů.
	 * Musí být v rozmezí 0 až 128, čím vyší číslo, tím odlesk pokryje menší plochu.
	 * @throws IllegalArgumentException Pokud není zadáno číslo v rozmezí 0 až 128.
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
	 * Vertikální synchronizace.
	 */
	public void setVsync(boolean vsync)
	{
		this.vsync = vsync;
		fireChange();
	}

	/**
	 * Vytvoření kopie objektu, posluchači nebudou v kopii obsaženi.
	 */
	@Override
	public Object clone()
	{
		DisplayOptions copy = new DisplayOptions();
		copy.set(this);
		return copy;
	}

	/**
	 * Nastavení vlastností dle jiné instance objektu. Posluchači nebudou kopírovaní.
	 * @param setBy Objekt z něhož dojde ke kopírování vlastností.
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
	 * Přidání posluchače změn.
	 */
	public void addListener(ChangeListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchače změn.
	 */
	public void removeListener(ChangeListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Vyvolání události změny objektu.
	 */
	private void fireChange()
	{
		for (ChangeListener l : listeners)
		{
			l.changeEvent(new EventObject(this));
		}
	}

	/**
	 * Kontrola jestli parametr smooth má správnou hodnotu.
	 * @throws IllegalArgumentException pokud parametr smooth nemá jednu z hodnot 0, GL2.GL_FASTEST, GL2.GL_NICEST.
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
