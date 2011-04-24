package cscg.model;

import cscg.model.objects.IObject;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;

/**
 * T��da p�edtavuje data jednoho projektu aplikace.
 * @author Tom� Re�nar
 */
public class Project implements Serializable
{

	/**
	 * Poslucha�i zm�n v projektu.
	 */
	private transient ArrayList<ProjectListener> projectListeners;
	/**
	 * Jm�no projektu.
	 */
	private String name;
	/**
	 * Soubor pro ulo�en� projektu.
	 */
	private String file;
	/**
	 * P�p�na� mezi zobrazen�m v jednom viewportu a 4 viewporty.
	 */
	private boolean displayAllViewports;
	/**
	 * Barva pozad� projektu.
	 */
	private Color backgroundColor;
	/**
	 * Objekty v projektu. Objekt s indexem nula je nejvy��� vrstva.
	 */
	private ArrayList<IObject> objects = new ArrayList<IObject>(16);
	/**
	 * Akt�ln� editovan� objekt.
	 */
	private IObject selectedObject;
	/**
	 * Exluzivn� viditelnost pro aktu�ln� editovan� objekt.
	 */
	private boolean exlusiveVisibility = false;
	/**
	 * Vlastnocti projekce viewportu 1.
	 */
	private Projection viewport1Projection;
	/**
	 * Vlastnocti projekce viewportu 2.
	 */
	private Projection viewport2Projection;
	/**
	 * Vlastnocti projekce viewportu 3.
	 */
	private Projection viewport3Projection;
	/**
	 * Vlastnocti projekce viewportu 4.
	 */
	private Projection viewport4Projection;
	/**
	 * Zda je OpenGL okno v re�imu pro editaci objekt�.
	 */
	private boolean editorInEditingMode = true;
	/**
	 * Zda se maj� vykreslit osy.
	 */
	private boolean showAxes = true;
	/**
	 * Zda se m� vykreslit ikona zobrazuj�c� orientaci kamery.
	 */
	private boolean showOrientationIcon = true;
	/**
	 * Zda se maj� vykreslit informa�n� texty - pozice kurzoru a zoom.
	 */
	private boolean showInformationText = true;

	/**
	 * Konstruktorem se vytvo�� z�kladn� pr�zdn� �loha.
	 */
	public Project()
	{
		name = "Nov� projekt";
		displayAllViewports = true;
		backgroundColor = Color.WHITE;
		viewport1Projection = new Projection();
		viewport1Projection.setPerspective(true);
		viewport1Projection.lookFront();
		viewport2Projection = new Projection();
		viewport2Projection.setPerspective(false);
		viewport2Projection.lookFront();
		viewport3Projection = new Projection();
		viewport3Projection.setPerspective(false);
		viewport3Projection.lookProfile();
		viewport4Projection = new Projection();
		viewport4Projection.setPerspective(false);
		viewport4Projection.lookTop();

		initTransients();
	}

	/**
	 * Inicializace transientn�ch vlastnost�.
	 */
	private void initTransients()
	{
		projectListeners = new ArrayList<ProjectListener>();

		viewport1Projection.addListener(new ChangeListener()
		{

			@Override
			public void changeEvent(EventObject e)
			{
				for (ProjectListener l : projectListeners)
				{
					l.eventVieport1Changed(viewport1Projection);
				}
			}
		});
		viewport2Projection.addListener(new ChangeListener()
		{

			@Override
			public void changeEvent(EventObject e)
			{
				for (ProjectListener l : projectListeners)
				{
					l.eventVieport2Changed(viewport2Projection);
				}
			}
		});
		viewport3Projection.addListener(new ChangeListener()
		{

			@Override
			public void changeEvent(EventObject e)
			{
				for (ProjectListener l : projectListeners)
				{
					l.eventVieport3Changed(viewport3Projection);
				}
			}
		});
		viewport4Projection.addListener(new ChangeListener()
		{

			@Override
			public void changeEvent(EventObject e)
			{
				for (ProjectListener l : projectListeners)
				{
					l.eventVieport4Changed(viewport4Projection);
				}
			}
		});
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransients();
	}

	/**
	 * P�id�n� poslucha�e zm�n v projektu.
	 */
	public synchronized void addProjectListener(ProjectListener l)
	{
		projectListeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e zm�n v projektu.
	 */
	public synchronized void removeProjectListener(ProjectListener l)
	{
		projectListeners.remove(l);
	}

	/**
	 * Jm�no projektu.
	 */
	public synchronized String getName()
	{
		return name;
	}

	/**
	 * Nastaven� jm�na projektu je priv�tn� metoda, proto�e je vol�na intern� v�dy se zm�nou souboru
	 * @param name Nov� jm�no projektu.
	 */
	private synchronized void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Cesta k souboru projektu ze kter�ho byl projekt na�ten� nebo kam byl ulo�en�.
	 */
	public synchronized String getFile()
	{
		return file;
	}

	/**
	 * Nastaven� cesty k souboru pro ukl�d�n�.
	 */
	public synchronized void setFile(String file)
	{
		this.file = file;
		File f = new File(file);
		setName(f.getName());
		for (ProjectListener l : projectListeners)
		{
			l.eventFileChanged(name, file);
		}
	}

	/**
	 * Re�im pr�ce se zobrazen�m jedn�m nebo 4 viewporty.
	 */
	public synchronized boolean isDisplayAllViewports()
	{
		return displayAllViewports;
	}

	/**
	 * P�epnut� mezi zobrazen�m jedn�m nebo 4 viewporty.
	 */
	public synchronized void setDisplayAllViewports(boolean displayAllViewports)
	{
		this.displayAllViewports = displayAllViewports;
		for (ProjectListener l : projectListeners)
		{
			l.eventDisplayAllViewportsChanged(displayAllViewports);
		}
	}

	/**
	 * Z�sk�n� barvy pozad�.
	 */
	public synchronized Color getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * P�id�n� objektu do projektu.
	 */
	public synchronized void addObject(IObject o)
	{
		objects.add(0, o);
		for (ProjectListener l : projectListeners)
		{
			l.eventObjectAdded(o, 0);
		}
	}

	/**
	 * Zda je aktu�ln� vybran� objekt exluzivn� viditeln� - pokud ano, ostatn� objekty jsou skryt�.
	 */
	public synchronized boolean isExlusiveVisibility()
	{
		return selectedObject != null && exlusiveVisibility;
	}

	/**
	 * Nastav� exluzivn� viditelnost vybran�ho objektu. Pouze aktu�ln� vybran� objket m��e m�t tento p��znak.
	 * Zp�sob� ud�lost {@link ProjectListener#eventExlusiveVisibilityChanged()}.
	 */
	public synchronized void setExlusiveVisibility(boolean exlusiveVisibility)
	{
		if (this.exlusiveVisibility != exlusiveVisibility)
		{
			this.exlusiveVisibility = exlusiveVisibility;
			for (ProjectListener l : projectListeners)
			{
				l.eventExlusiveVisibilityChanged();
			}
		}
	}

	/**
	 * Odstran�n� objektu z projektu.
	 */
	public synchronized void removeObject(IObject o)
	{
		int deletedIndex = objects.indexOf(o),
		  selectedIndex = getSelectedObjectIndex();
		if (objects.remove(o))
		{
			if (selectedIndex >= deletedIndex)
			{
				setSelectedObject(selectedIndex - 1);
			}
			for (ProjectListener l : projectListeners)
			{
				l.eventObjectRemoved(o, deletedIndex);
			}
		}
	}

	/**
	 * P�esunut� objektu o jednu vrstvu nahoru/dol�. Nen�-li objekt sou��st� projektu, 
	 * nebo nelze li objekt nikam p�esunout nic se neprovede.
	 * @param moveUp Sm�r p�esunu: true - nahoru, false - dol�.
	 */
	public synchronized void moveObject(boolean moveUp, IObject o)
	{
		int index = objects.indexOf(o);
		if (index >= 0)
		{
			boolean change = false;
			if (!moveUp && index < objects.size() - 1)//dol�-znamen� zv��en� indexu
			{
				objects.remove(o);
				objects.add(index + 1, o);
				change = true;
			} else
			{
				if (moveUp && index > 0)//nahoru-znamen� sn�en� indexu
				{
					objects.remove(o);
					objects.add(index - 1, o);
					change = true;
				}
			}

			if (change)
			{
				for (ProjectListener l : projectListeners)
				{
					l.eventObjectsOrderChanged();
				}
			}
		}
	}

	/**
	 * Zisk�n� v�ech objket�. Nejvy��� vrstva je v indexu 0.
	 */
	public synchronized IObject[] getObjects()
	{
		return objects.toArray(new IObject[0]);
	}

	/**
	 * Zisk�n� v�ech objket� reverzn�. Nejni��� vrstva je v indexu 0.
	 * @param onlyVisible Kdy� true, tak budou vr�ceny pouze objekty je� jsou viditeln�
	 */
	public synchronized IObject[] getObjectsReverse(boolean onlyVisible)
	{
		LinkedList<IObject> ret = new LinkedList<IObject>();
		for (IObject o : objects)
		{
			if (onlyVisible && o.isVisible() == false)
			{
				continue;
			} else
			{
				ret.addFirst(o);
			}
		}
		return ret.toArray(new IObject[0]);
	}

	/**
	 * Zisk�n� objektu podle indexu.
	 * @param index Index objektu.
	 * @return Objekt na dan�m indexu, pokud je index neplatn�, pak vr�t� null.
	 */
	public synchronized IObject getObject(int index)
	{
		try
		{
			return objects.get(index);
		} catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Zisk�n� indexu objektu, vr�t� index, nebo -1 pokud objekt nen� sou��st� projektu.
	 */
	public synchronized int indexOf(IObject object)
	{
		return objects.indexOf(object);
	}

	/**
	 * Zjist� jestli je objekt v nejni��� vrstv�.
	 * @return True kdy� je v nejni���.
	 */
	public synchronized boolean isLowest(IObject object)
	{
		return objects.size() > 0 ? objects.get(objects.size() - 1) == object : false;
	}

	/**
	 * Zjist� jestli je objekt nejvy��� vrstv�.
	 * @return True kdy� je v nejvy��� vrstv�.
	 */
	public synchronized boolean isHighest(IObject object)
	{
		return objects.size() > 0 ? objects.get(0) == object : false;
	}

	/**
	 * vr�cen� editovan�ho objektu v projektu.
	 */
	public synchronized IObject getSelectedObject()
	{
		return selectedObject;
	}

	/**
	 * Vr�t� index vybran�ho objektu. Vr�t� -1 pokud nen� ��dn� vybr�n
	 */
	public synchronized int getSelectedObjectIndex()
	{
		return objects.indexOf(selectedObject);
	}

	/**
	 * Nastaven� editovan�ho objektu v projektu.
	 * @param index Index objektu, pokud je index neplatn�, bude nastaven jako pracovn� projekt ��dn�.
	 */
	public synchronized void setSelectedObject(int index)
	{
		this.selectedObject = getObject(index);
		setExlusiveVisibility(false);
		for (ProjectListener l : projectListeners)
		{
			l.eventSelectedObjectChanged(this.selectedObject, index >= 0 ? index : -1);
		}
	}

	/**
	 * Nastaveni editovaneho objektu v projektu. Pokud objekt v projektu neexistuje, nedojde ke zm�n� nastaven�.
	 * @param o Editavany objekt.
	 */
	public void setSelectedObject(IObject o)
	{
		int index = objects.indexOf(o);
		if (index >= 0)
		{
			setSelectedObject(index);
		}
	}

	/**
	 * Z�sk�n� nastaven� projekce vieportu 1.
	 */
	public synchronized Projection getViewport1Projection()
	{
		return viewport1Projection;
	}

	/**
	 * Z�sk�n� nastaven� projekce vieportu 2.
	 */
	public synchronized Projection getViewport2Projection()
	{
		return viewport2Projection;
	}

	/**
	 * Z�sk�n� nastaven� projekce vieportu 3.
	 */
	public synchronized Projection getViewport3Projection()
	{
		return viewport3Projection;
	}

	/**
	 * Z�sk�n� nastaven� projekce vieportu 4.
	 */
	public synchronized Projection getViewport4Projection()
	{
		return viewport4Projection;
	}

	/**
	 * Re�im editace editoru.
	 */
	public synchronized boolean isEditorInEditingMode()
	{
		return editorInEditingMode;
	}

	/**
	 * Nastav� editor do re�imu editace.
	 */
	public synchronized void setEditorInEditingMode(boolean editorInEditingMode)
	{
		if (this.editorInEditingMode == editorInEditingMode)
		{
			return;
		}
		this.editorInEditingMode = editorInEditingMode;
		for (ProjectListener l : projectListeners)
		{
			l.eventEditorInEditingMode(editorInEditingMode);
		}
	}

	/**
	 * Zda se maj� zobrazit osy v editoru.
	 */
	public synchronized boolean isShowAxes()
	{
		return showAxes;
	}

	/**
	 * Zapne zobrazen� os v editoru.
	 */
	public synchronized void setShowAxes(boolean showAxes)
	{
		if (this.showAxes == showAxes)
		{
			return;
		}
		this.showAxes = showAxes;
		for (ProjectListener l : projectListeners)
		{
			l.eventShowAxesChanged(showAxes);
		}
	}

	/**
	 * Zda se m� v editoru zobrazit informa�n� text o poloze kurzoru a zoomu.
	 */
	public boolean isShowInformationText()
	{
		return showInformationText;
	}

	/**
	 * Nastav� zda se m� v editoru zobrazit informa�n� text o poloze kurzoru a zoomu.
	 */
	public void setShowInformationText(boolean showInformationText)
	{
		if (this.showInformationText == showInformationText)
		{
			return;
		}
		this.showInformationText = showInformationText;
		for (ProjectListener l : projectListeners)
		{
			l.eventShowInformationText(showInformationText);
		}
	}

	/**
	 * Zda se m� v editoru vykreslit ikona informuj� o orientaci kamery.
	 */
	public boolean isShowOrientationIcon()
	{
		return showOrientationIcon;
	}

	/**
	 * Nastav� zda se m� v editoru vykreslit ikona informuj� o orientaci kamery.
	 */
	public void setShowOrientationIcon(boolean showOrientationIcon)
	{
		if (this.showOrientationIcon == showOrientationIcon)
		{
			return;
		}
		this.showOrientationIcon = showOrientationIcon;
		for (ProjectListener l : projectListeners)
		{
			l.eventShowOrientationIcon(showOrientationIcon);
		}
	}
}
