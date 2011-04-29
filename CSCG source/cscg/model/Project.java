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
 * Třída představuje data jednoho projektu aplikace.
 * @author Tomáš Režnar
 */
public class Project implements Serializable
{

	/**
	 * Posluchači změn v projektu.
	 */
	private transient ArrayList<ProjectListener> projectListeners;
	/**
	 * Jméno projektu.
	 */
	private String name;
	/**
	 * Soubor pro uložení projektu.
	 */
	private String file;
	/**
	 * Přpínač mezi zobrazením v jednom viewportu a 4 viewporty.
	 */
	private boolean displayAllViewports;
	/**
	 * Barva pozadí projektu.
	 */
	private Color backgroundColor;
	/**
	 * Objekty v projektu. Objekt s indexem nula je nejvyšší vrstva.
	 */
	private ArrayList<IObject> objects = new ArrayList<IObject>(16);
	/**
	 * Aktálně editovaný objekt.
	 */
	private IObject selectedObject;
	/**
	 * Exluzivní viditelnost pro aktuálně editovaný objekt.
	 */
	private boolean exlusiveVisibility = false;
	/**
	 * Vlastnosti projekce viewportu 1.
	 */
	private Projection viewport1Projection;
	/**
	 * Vlastnosti projekce viewportu 2.
	 */
	private Projection viewport2Projection;
	/**
	 * Vlastnosti projekce viewportu 3.
	 */
	private Projection viewport3Projection;
	/**
	 * Vlastnosti projekce viewportu 4.
	 */
	private Projection viewport4Projection;
	/**
	 * Zda je OpenGL okno v režimu pro editaci objektů.
	 */
	private boolean editorInEditingMode = true;
	/**
	 * Zda se mají vykreslit osy.
	 */
	private boolean showAxes = true;
	/**
	 * Zda se má vykreslit ikona zobrazující orientaci kamery.
	 */
	private boolean showOrientationIcon = true;
	/**
	 * Zda se mají vykreslit informační texty - pozice kurzoru a zoom.
	 */
	private boolean showInformationText = true;

	/**
	 * Konstruktorem se vytvoří základní prázdná úloha.
	 */
	public Project()
	{
		name = "Nový projekt";
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
	 * Inicializace transientních vlastností.
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
	 * Přidání posluchače změn v projektu.
	 */
	public synchronized void addProjectListener(ProjectListener l)
	{
		projectListeners.add(l);
	}

	/**
	 * Odebrání posluchače změn v projektu.
	 */
	public synchronized void removeProjectListener(ProjectListener l)
	{
		projectListeners.remove(l);
	}

	/**
	 * Jméno projektu.
	 */
	public synchronized String getName()
	{
		return name;
	}

	/**
	 * Nastavení jména projektu je privátní metoda, protože je volána interně vždy se změnou souboru
	 * @param name Nové jméno projektu.
	 */
	private synchronized void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Cesta k souboru projektu ze kterého byl projekt načtený nebo kam byl uložený.
	 */
	public synchronized String getFile()
	{
		return file;
	}

	/**
	 * Nastavení cesty k souboru pro ukládání.
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
	 * Režim práce se zobrazeným jedním nebo 4 viewporty.
	 */
	public synchronized boolean isDisplayAllViewports()
	{
		return displayAllViewports;
	}

	/**
	 * Přepnutí mezi zobrazením jedním nebo 4 viewporty.
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
	 * Získání barvy pozadí.
	 */
	public synchronized Color getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * Přidání objektu do projektu.
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
	 * Zda je aktuálně vybraný objekt exluzivně viditelný - pokud ano, ostatní objekty jsou skryté.
	 */
	public synchronized boolean isExlusiveVisibility()
	{
		return selectedObject != null && exlusiveVisibility;
	}

	/**
	 * Nastaví exluzivní viditelnost vybraného objektu. Pouze aktuálně vybraný objket může mít tento příznak.
	 * Způsobí událost {@link ProjectListener#eventExlusiveVisibilityChanged()}.
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
	 * Odstranění objektu z projektu.
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
	 * Přesunutí objektu o jednu vrstvu nahoru/dolů. Není-li objekt součástí projektu, 
	 * nebo nelze li objekt nikam přesunout nic se neprovede.
	 * @param moveUp Směr přesunu: true - nahoru, false - dolů.
	 */
	public synchronized void moveObject(boolean moveUp, IObject o)
	{
		int index = objects.indexOf(o);
		if (index >= 0)
		{
			boolean change = false;
			if (!moveUp && index < objects.size() - 1)//dolů-znamená zvýšení indexu
			{
				objects.remove(o);
				objects.add(index + 1, o);
				change = true;
			} else
			{
				if (moveUp && index > 0)//nahoru-znamená snížení indexu
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
	 * Ziskání všech objketů. Nejvyšší vrstva je v indexu 0.
	 */
	public synchronized IObject[] getObjects()
	{
		return objects.toArray(new IObject[0]);
	}

	/**
	 * Ziskání všech objketů reverzně. Nejnižší vrstva je v indexu 0.
	 * @param onlyVisible Když true, tak budou vráceny pouze objekty jež jsou viditelné
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
	 * Ziskání objektu podle indexu.
	 * @param index Index objektu.
	 * @return Objekt na daném indexu, pokud je index neplatný, pak vrátí null.
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
	 * Ziskání indexu objektu, vrátí index, nebo -1 pokud objekt není součástí projektu.
	 */
	public synchronized int indexOf(IObject object)
	{
		return objects.indexOf(object);
	}

	/**
	 * Zjistí jestli je objekt v nejnišší vrstvě.
	 * @return True když je v nejnižší.
	 */
	public synchronized boolean isLowest(IObject object)
	{
		return objects.size() > 0 ? objects.get(objects.size() - 1) == object : false;
	}

	/**
	 * Zjistí jestli je objekt nejvyšší vrstvě.
	 * @return True když je v nejvyšší vrstvě.
	 */
	public synchronized boolean isHighest(IObject object)
	{
		return objects.size() > 0 ? objects.get(0) == object : false;
	}

	/**
	 * Vrácení editovaného objektu v projektu.
	 */
	public synchronized IObject getSelectedObject()
	{
		return selectedObject;
	}

	/**
	 * Vrátí index vybraného objektu. Vrátí -1 pokud není žádný vybrán
	 */
	public synchronized int getSelectedObjectIndex()
	{
		return objects.indexOf(selectedObject);
	}

	/**
	 * Nastavení editovaného objektu v projektu.
	 * @param index Index objektu, pokud je index neplatný, bude nastaven jako pracovní projekt žádný.
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
	 * Nastaveni editovaneho objektu v projektu. Pokud objekt v projektu neexistuje, nedojde ke změně nastavení.
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
	 * Získání nastavení projekce vieportu 1.
	 */
	public synchronized Projection getViewport1Projection()
	{
		return viewport1Projection;
	}

	/**
	 * Získání nastavení projekce vieportu 2.
	 */
	public synchronized Projection getViewport2Projection()
	{
		return viewport2Projection;
	}

	/**
	 * Získání nastavení projekce vieportu 3.
	 */
	public synchronized Projection getViewport3Projection()
	{
		return viewport3Projection;
	}

	/**
	 * Získání nastavení projekce vieportu 4.
	 */
	public synchronized Projection getViewport4Projection()
	{
		return viewport4Projection;
	}

	/**
	 * Režim editace editoru.
	 */
	public synchronized boolean isEditorInEditingMode()
	{
		return editorInEditingMode;
	}

	/**
	 * Nastaví editor do režimu editace.
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
	 * Zda se mají zobrazit osy v editoru.
	 */
	public synchronized boolean isShowAxes()
	{
		return showAxes;
	}

	/**
	 * Zapne zobrazení os v editoru.
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
	 * Zda se má v editoru zobrazit informační text o poloze kurzoru a zoomu.
	 */
	public boolean isShowInformationText()
	{
		return showInformationText;
	}

	/**
	 * Nastaví zda se má v editoru zobrazit informační text o poloze kurzoru a zoomu.
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
	 * Zda se má v editoru vykreslit ikona informují o orientaci kamery.
	 */
	public boolean isShowOrientationIcon()
	{
		return showOrientationIcon;
	}

	/**
	 * Nastaví zda se má v editoru vykreslit ikona informují o orientaci kamery.
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
