package cscg.model;

import cscg.model.objects.IObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

/**
 * Kořenová třída modelu (dle vzoru MVC), přes její instanci se lze dostat ke všem ostatním aktuálním
 * třídám modelu (projekty, nastavení). Třída je podle vzoru singleton, její aktuální instance se ukládá
 * v serializované podobě do kořenového adresáře aplikace. Instanci lze získat metodou {@link #getInstance()}.
 * @author Tomáš Režnar
 */
public class Model implements Serializable {

	/**
	 * Instance modelu.
	 */
	private static Model instance=null;

	static{
		/*
		 * Načtení modelu ze souboru.
		 */
		ObjectInputStream s = null;
		try
		{
			s = new ObjectInputStream(new FileInputStream(new File("./state.bin")));
			Model m = (Model) s.readObject();
			instance = m;
		}
		catch (Exception ex)
		{
			instance = new Model();
			Logger.getLogger(Model.class.getName()).log(Level.INFO, null, ex);
		}
		finally
		{
			try
			{
				s.close();
			} catch (Exception ex)
			{}
		}
	}

	/**
	 * V dubug módu se pro vstup poslaný pomoci {@link #debugPrintLn(String)} vyvolá event
	 * {@link ModelListener#eventDebugPrintLn(String)}.
	 */
	private static boolean debug=false;
	/**
	 * Posluchači na změny modelu.
	 */
	private transient ArrayList<ModelListener> modelListeners;
	/**
	 * Všechny otevřené projekty.
	 */
	private ArrayList<Project> projects=new ArrayList<Project>();
	/**
	 * Nastavení zobrazení
	 */
	private DisplayOptions programOptions=new DisplayOptions();
	/**
	 * Projekt se kterým se aktuálně pracuje.
	 */
	private Project workingProject;
	/**
	 * Seznam položek v hlavním menu pro přidání objektů, vygenerovaný na základě konfiguračního souboru.
	 */
	private transient List<Couple<String,List<Couple<String,String>>>> menuObjects;

	/**
	 * Třída je podle vzoru singleton, její aktuální instance se ukládá
	 * v serializované podobě do kořenového adresáře aplikace. Instanci lze získat metodou {@link #getInstance()}.
	 */
	private Model()
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
		modelListeners=new ArrayList<ModelListener>();
		menuObjects=new LinkedList();
		parseMenuObjectsFile();
	}

	/**
	 * Vytvoření seznamu objektů pro menu.
	 */
	private void parseMenuObjectsFile()
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expressRoot = "/menu";
		String expressSubmenus = "submenu[@title]";//submenu musí mít nastaven titulek
		//položky menu, musí být bud separatory nebo objekty s nastaveným typem a titulkem
		String expressItems = "separator | object[@title!='' and @class!='']";
		InputStream is = getClass().getResourceAsStream("/menu.xml");
		if(is!=null)
		{
			try
			{
				InputSource inputSource = new InputSource(is);
				Couple<String,List<Couple<String,String>>> section=null;//aktuální parsovaná sekce objektů
				List<Couple<String,String>> sectionObjects=null;//seznam objektů aktuální sekce
				Node submenu,nodeMenuItem;
				NamedNodeMap attrs;
				String objectClassName,objectTitle;
				//načtení kořene <menu>
				NodeList nodes = (NodeList) xpath.evaluate(expressRoot, inputSource, XPathConstants.NODESET);
				if(nodes.getLength()<1)
				{
					throw new Exception("Chybí element kořenový uzel <menu>.");
				}
				Node nodeMenu=nodes.item(0);
				//načtení submenus <submenu>
				nodes = (NodeList) xpath.evaluate(expressSubmenus, nodeMenu, XPathConstants.NODESET);
				for(int i=0;i<nodes.getLength();i++)//procházení submenus
				{
					submenu=nodes.item(i);
					sectionObjects=new LinkedList<Couple<String, String>>();
					section=new Couple<String, List<Couple<String, String>>>(
					  submenu.getAttributes().getNamedItem("title").getTextContent(), //titulek submenu
					  sectionObjects);
					menuObjects.add(section);
					//položky v submenu
					NodeList items = (NodeList) xpath.evaluate(expressItems, submenu, XPathConstants.NODESET);
					for(int j=0;j<items.getLength();j++)//procházení položek
					{
						nodeMenuItem=items.item(j);
						attrs=nodeMenuItem.getAttributes();
						if(nodeMenuItem.getNodeName().equals("separator"))//typ separator
						{
							sectionObjects.add(new Couple<String, String>(null, null));
						}
						else//typ object
						{
							objectClassName=attrs.getNamedItem("class").getTextContent();
							//kontrola existence typu
							try
							{
								Class cl=Class.forName(objectClassName);
								//kontrola implementace interfacu IObject
								try
								{
									cl.asSubclass(IObject.class);
								}
								catch(ClassCastException ex)//neimplementuje z IObject
								{
									System.out.println("Třída \""+objectClassName+"\" neimplementuje interface IPObject"
									  + " v konfiguračním souboru menu.xml.");
									continue;
								}
							} catch (ClassNotFoundException ex)//neexistující třída
							{
								System.out.println("Neexistující datový typ \""+objectClassName+"\" v konfiguračním souboru menu.xml.");
								continue;
							}
							objectTitle=attrs.getNamedItem("title").getTextContent();
							sectionObjects.add(new Couple<String, String>(objectTitle, objectClassName));
						}
					}
				}
			}
			catch (Exception ex)
			{
				Logger.getLogger(Model.class.getName()).log(Level.WARNING, null, ex);
				JOptionPane.showMessageDialog(null,
				  "Chyba při čtení konfiguračního souboru menu.xml.", "Chyba",
				  JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				try
				{
					is.close();
				} catch (IOException ex)
				{}
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null,
			  "Chybí konfigurační soubor menu.xml.", "Chyba",
			  JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Získání seznamu objektů pro menu.
	 */
	public List<Couple<String, List<Couple<String, String>>>> getMenuObjects()
	{
		return menuObjects;
	}

	/**
	 * Získání instace modelu.
	 */
	public static Model getInstance()
	{
		return instance;
	}

	/**
	 * Uloží model do úložiště.
	 */
	public static void persistInstance() throws IOException
	{
		ObjectOutputStream s = null;
		FileOutputStream file = null;
		try
		{
			file=new FileOutputStream(new File("./state.bin"));
			s = new ObjectOutputStream(file);
			s.writeObject(instance);
		}
		finally
		{
			try
			{
				s.close();
				file.close();
			} catch (Exception ex)
			{}
		}
	}

	/**
	 * Debug mód.
	 * V dubug módu se pro vstup poslaný pomoci {@link #debugPrintLn(String)} vyvolá event
	 * {@link ModelListener#eventDebugPrintLn(String)}.
	 */
	public static boolean isDebug()
	{
		return debug;
	}
	/**
	 * Nastaví debug mód.
	 */
	public static void setDebug(boolean debug)
	{
		Model.debug = debug;
	}
	/**
	 * Poslání řetězce na debug výstup.
	 * @param text Zpráva.
	 */
	public void debugPrintLn(String text)
	{
		if(isDebug())
		{
			for(ModelListener l:modelListeners)
			{
				l.eventDebugPrintLn(text);
			}
		}
	}
	/**
	 * Vytisknutí chyby na výstup v debug módu.
	 * @param ex Vyjímka.
	 */
	public void debugPrintException(Throwable ex)
	{
		if(isDebug())
		{
			//sestavení výpisu vyjímky
			StringBuilder sb=new StringBuilder();
			sb.append(ex.toString());
			for(StackTraceElement x:ex.getStackTrace())
			{
				sb.append("\n");
				sb.append(x.toString());
			}
			String text=sb.toString();
			//oznámení události
			for(ModelListener l:modelListeners)
			{
				l.eventDebugPrintLn(text);
			}
		}
	}
	/**
	 * Přidání posluchače změn v modelu.
	 */
	public void addModelListener(ModelListener l)
	{
		modelListeners.add(l);
	}
	/**
	 * Odebrání posluchače změn v modelu.
	 */
	public void removeModelListener(ModelListener l)
	{
		modelListeners.remove(l);
	}
	/**
	 * Přidání projektu.
	 */
	public void addProject(Project project)
	{
		projects.add(project);
		for(ModelListener l:modelListeners)
		{
			l.eventProjectAdd(project);
		}
	}
	/**
	 * Odebrání projektu.
	 */
	public void removeProject(Project project)
	{
		int index=projects.indexOf(project);
		if(index>=0)
		{
			//nastavím nový pracovní projekt
			if(project==workingProject)
			{
				if(projects.size()==1)
				{
					setWorkingProject(null);
				}
				else
				{
					//nastavení pracovního projektu který se nachází za mazaným projektem
					if(index==projects.size()-1)//pokud je mazaný projekt poslední
					{
						setWorkingProject(projects.get(index-1));
					}
					else
					{
						setWorkingProject(projects.get(index+1));
					}
				}
			}
			//odeberu projekt
			if(projects.remove(project))
			{
				for(ModelListener l:modelListeners)
				{
					l.eventProjectRemove(project,index);
				}
			}
		}
	}
	/**
	 * Získání všech projektů.
	 */
	public Project[] getProjects() {
		return projects.toArray(new Project[0]);
	}
	/**
	 * Odebrání projektu.
	 * @param index Index odebíraného projektu, neplatný index bude ignorován.
	 */
	public void removeProject(int index)
	{
		try
		{
			Project project = projects.get(index);
			removeProject(project);
		}
		catch(IndexOutOfBoundsException e){}
	}

	/**
	 * Nastavení zobrazení.
	 */
	public DisplayOptions getDisplayOptions() {
		return programOptions;
	}

	/**
	 * Získání pracovního projektu.
	 * @return Pracovní projekt.
	 */
	public Project getWorkingProject()
	{
		return workingProject;
	}

	/**
	 * Získání indexu pracovního projektu.
	 */
	public int getWorkingProjectIndex()
	{
		return projects.indexOf(workingProject);
	}

	/**
	 * Získání indexu projektu.
	 * @return Vrátí index projektu nebo -1 pokud není projekt součástí modelu.
	 */
	public int getIndexOfProject(Project project)
	{
		return projects.indexOf(project);
	}

	/**
	 * Nastavení pracovního projektu
	 * @param projectIndex Index pracovního projektu (index dle pořadí vytváření projektů). Pokud je index neplatný,
	 * bude nastaven jako pracovní projekt žádný.
	 */
	public void setWorkingProject(int projectIndex)
	{
		Project previous=workingProject;
		try
		{
			this.workingProject = projects.get(projectIndex);
		}
		catch(IndexOutOfBoundsException e)
		{
			this.workingProject=null;
		}
		finally
		{
			for(ModelListener l:modelListeners)
			{
				l.eventSetWorkingProject(previous,workingProject);
			}
		}
	}

	/**
	 * Nastavení pracovního projektu.
	 * @param project Pracovní projekt, pokud není projekt součástí indexu, bude nastaven žádný projekt
	 * jako pracovní.
	 */
	public void setWorkingProject(Project project)
	{
		int index = projects.indexOf(project);
		setWorkingProject(index);
	}


	
}
