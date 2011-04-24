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
 * Ko�enov� t��da modelu (dle vzoru MVC), p�es jej� instanci se lze dostat ke v�em ostatn�m aktu�ln�m
 * t��d�m modelu (projekty, nastaven�). T��da je podle vzoru singleton, jej� aktu�ln� instance se ukl�d�
 * v serializovan� podob� do ko�enov�ho adres��e aplikace. Instanci lze z�skat metodou {@link #getInstance()}.
 * @author Tom� Re�nar
 */
public class Model implements Serializable {

	/**
	 * Instance modelu.
	 */
	private static Model instance=null;

	static{
		/*
		 * Na�ten� modelu ze souboru.
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
	 * V dubug m�du se pro vstup poslan� pomoci {@link #debugPrintLn(String)} vyvol� event
	 * {@link ModelListener#eventDebugPrintLn(String)}.
	 */
	private static boolean debug=false;
	/**
	 * Poslucha�i na zm�ny modelu.
	 */
	private transient ArrayList<ModelListener> modelListeners;
	/**
	 * V�echny otev�en� projekty.
	 */
	private ArrayList<Project> projects=new ArrayList<Project>();
	/**
	 * Nastaven� zobrazen�
	 */
	private DisplayOptions programOptions=new DisplayOptions();
	/**
	 * Projekt se kter�m se aktu�ln� pracuje.
	 */
	private Project workingProject;
	/**
	 * Seznam polo�ek v hlavn�m menu pro p�id�n� objekt�, vygenerovan� na z�klad� konfigura�n�ho souboru.
	 */
	private transient List<Couple<String,List<Couple<String,String>>>> menuObjects;

	/**
	 * T��da je podle vzoru singleton, jej� aktu�ln� instance se ukl�d�
	 * v serializovan� podob� do ko�enov�ho adres��e aplikace. Instanci lze z�skat metodou {@link #getInstance()}.
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
	 * Inicializace transientn�ch vlastnost�.
	 */
	private void initTransients()
	{
		modelListeners=new ArrayList<ModelListener>();
		menuObjects=new LinkedList();
		parseMenuObjectsFile();
	}

	/**
	 * Vytvo�en� seznamu objekt� pro menu.
	 */
	private void parseMenuObjectsFile()
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expressRoot = "/menu";
		String expressSubmenus = "submenu[@title]";//submenu mus� m�t nastaven titulek
		//polo�ky menu, mus� b�t bud separatory nebo objekty s nastaven�m typem a titulkem
		String expressItems = "separator | object[@title!='' and @class!='']";
		InputStream is = getClass().getResourceAsStream("/menu.xml");
		if(is!=null)
		{
			try
			{
				InputSource inputSource = new InputSource(is);
				Couple<String,List<Couple<String,String>>> section=null;//aktu�ln� parsovan� sekce objekt�
				List<Couple<String,String>> sectionObjects=null;//seznam objekt� aktu�ln� sekce
				Node submenu,nodeMenuItem;
				NamedNodeMap attrs;
				String objectClassName,objectTitle;
				//na�ten� ko�ene <menu>
				NodeList nodes = (NodeList) xpath.evaluate(expressRoot, inputSource, XPathConstants.NODESET);
				if(nodes.getLength()<1)
				{
					throw new Exception("Chyb� element ko�enov� uzel <menu>.");
				}
				Node nodeMenu=nodes.item(0);
				//na�ten� submenus <submenu>
				nodes = (NodeList) xpath.evaluate(expressSubmenus, nodeMenu, XPathConstants.NODESET);
				for(int i=0;i<nodes.getLength();i++)//proch�zen� submenus
				{
					submenu=nodes.item(i);
					sectionObjects=new LinkedList<Couple<String, String>>();
					section=new Couple<String, List<Couple<String, String>>>(
					  submenu.getAttributes().getNamedItem("title").getTextContent(), //titulek submenu
					  sectionObjects);
					menuObjects.add(section);
					//polo�ky v submenu
					NodeList items = (NodeList) xpath.evaluate(expressItems, submenu, XPathConstants.NODESET);
					for(int j=0;j<items.getLength();j++)//proch�zen� polo�ek
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
									System.out.println("T��da \""+objectClassName+"\" neimplementuje interface IPObject"
									  + " v konfigura�n�m souboru menu.xml.");
									continue;
								}
							} catch (ClassNotFoundException ex)//neexistuj�c� t��da
							{
								System.out.println("Neexistuj�c� datov� typ \""+objectClassName+"\" v konfigura�n�m souboru menu.xml.");
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
				  "Chyba p�i �ten� konfigura�n�ho souboru menu.xml.", "Chyba",
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
			  "Chyb� konfigura�n� soubor menu.xml.", "Chyba",
			  JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Z�sk�n� seznamu objekt� pro menu.
	 */
	public List<Couple<String, List<Couple<String, String>>>> getMenuObjects()
	{
		return menuObjects;
	}

	/**
	 * Z�sk�n� instace modelu.
	 */
	public static Model getInstance()
	{
		return instance;
	}

	/**
	 * Ulo�� model do �lo�i�t�.
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
	 * Debug m�d.
	 * V dubug m�du se pro vstup poslan� pomoci {@link #debugPrintLn(String)} vyvol� event
	 * {@link ModelListener#eventDebugPrintLn(String)}.
	 */
	public static boolean isDebug()
	{
		return debug;
	}
	/**
	 * Nastavi debug m�d.
	 */
	public static void setDebug(boolean debug)
	{
		Model.debug = debug;
	}
	/**
	 * Posl�n� �et�zce na debug v�stup.
	 * @param text Zpr�va.
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
	 * Vytisknut� chyby na v�stup v debug m�du.
	 * @param ex Vyj�mka.
	 */
	public void debugPrintException(Throwable ex)
	{
		if(isDebug())
		{
			//sestaven� v�pisu vyj�mky
			StringBuilder sb=new StringBuilder();
			sb.append(ex.toString());
			for(StackTraceElement x:ex.getStackTrace())
			{
				sb.append("\n");
				sb.append(x.toString());
			}
			String text=sb.toString();
			//ozn�men� ud�losti
			for(ModelListener l:modelListeners)
			{
				l.eventDebugPrintLn(text);
			}
		}
	}
	/**
	 * P�id�n� poslucha�e zm�n v modelu.
	 */
	public void addModelListener(ModelListener l)
	{
		modelListeners.add(l);
	}
	/**
	 * Odebr�n� poslucha�e zm�n v modelu.
	 */
	public void removeModelListener(ModelListener l)
	{
		modelListeners.remove(l);
	}
	/**
	 * P�id�n� projektu.
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
	 * Odebr�n� projektu.
	 */
	public void removeProject(Project project)
	{
		int index=projects.indexOf(project);
		if(index>=0)
		{
			//nastav�m nov� pracovn� projekt
			if(project==workingProject)
			{
				if(projects.size()==1)
				{
					setWorkingProject(null);
				}
				else
				{
					//nastaven� pracovn�ho projektu kter� se nach�z� za mazan�m projektem
					if(index==projects.size()-1)//pokud je mazan� projekt posledn�
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
	 * Z�sk�n� v�ech projekt�.
	 */
	public Project[] getProjects() {
		return projects.toArray(new Project[0]);
	}
	/**
	 * Odebr�n� projektu.
	 * @param index Index odeb�ran�ho projektu, neplatn� index bude ignorov�n.
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
	 * Nastaven� zobrazen�.
	 */
	public DisplayOptions getDisplayOptions() {
		return programOptions;
	}

	/**
	 * Z�sk�n� pracovn�ho projektu.
	 * @return Pracovn� projekt.
	 */
	public Project getWorkingProject()
	{
		return workingProject;
	}

	/**
	 * Z�sk�n� indexu pracovn�ho projektu.
	 */
	public int getWorkingProjectIndex()
	{
		return projects.indexOf(workingProject);
	}

	/**
	 * Z�sk�n� indexu projektu.
	 * @return Vr�t� index projektu nebo -1 pokud nen� projekt sou��st� modelu.
	 */
	public int getIndexOfProject(Project project)
	{
		return projects.indexOf(project);
	}

	/**
	 * Nastaven� pracovn�ho projektu
	 * @param projectIndex Index pracovn�ho projektu (index dle po�ad� vytv��en� projekt�). Pokud je index neplatn�,
	 * bude nastaven jako pracovn� projekt ��dn�.
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
	 * Nastaven� pracovn�ho projektu.
	 * @param project Pracovn� projekt, pokud nen� projekt sou��st� indexu, bude nastaven ��dn� projekt
	 * jako pracovn�.
	 */
	public void setWorkingProject(Project project)
	{
		int index = projects.indexOf(project);
		setWorkingProject(index);
	}


	
}
