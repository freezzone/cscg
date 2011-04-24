package cscg.ui;

import cscg.model.Model;
import cscg.model.objects.IObject;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Pohled dle vzoru MVC.
 * @author Tom� Re�nar
 */
public class View
{

	/**
	 * Model dle MVC.
	 */
	protected final Model model;
	/**
	 * Dialog volby souboru pro export do obr�zku.
	 */
	private final JFileChooser exportImageFS;
	/**
	 * Dialog volby souboru pro ulo�en� projektu.
	 */
	private final JFileChooser projectSaveFS;
	/**
	 * Dialog volby souboru pro otev�en� projektu.
	 */
	private final JFileChooser projectOpenFS;

	;
	/**
	 * Okno pro zobrazen� edita�n�ch uzl�.
	 */
	protected final NodesFrame nodesFrame;
	/**
	 * Okno nastaven� programu.
	 */
	protected final SettingsFrame settingsFrame;
	/**
	 * Hlavn� frame.
	 */
	private final MainFrame mainFrame;

	public View(final Model model)
	{
		this.model = model;

		//hlavn� okno
		mainFrame = new MainFrame(model);

		//vlastnosti export dialogu pro ulo�en� obr�zku
		exportImageFS = new JFileChooser();
		exportImageFS.setDialogTitle("Exportovat sc�nu do obr�zku");
		exportImageFS.setDialogType(JFileChooser.SAVE_DIALOG);
		exportImageFS.setFileFilter(new FileNameExtensionFilter("PNG obr�zky \".png\"", "png"));

		//vlastnosti dialogu pro v�b�r souboru pro ulo�en�
		projectSaveFS = new JFileChooser();
		projectSaveFS.setDialogTitle("Ulo�it projekt");
		projectSaveFS.setDialogType(JFileChooser.SAVE_DIALOG);
		projectSaveFS.setFileFilter(new FileNameExtensionFilter("projekt \".cscg\"", "cscg"));

		//vlastnosti dialogu pro v�b�r souboru pro ulo�en�
		projectOpenFS = new JFileChooser();
		projectOpenFS.setDialogTitle("Otev��t projekt");
		projectOpenFS.setDialogType(JFileChooser.OPEN_DIALOG);
		projectOpenFS.setMultiSelectionEnabled(false);
		projectOpenFS.setFileFilter(new FileNameExtensionFilter("projekt \".cscg\"", "cscg"));

		//okno pro editaci bod� a uzlov�ho vektoru objektu
		nodesFrame = new NodesFrame();

		//okna pro nastaven�
		settingsFrame = new SettingsFrame();
	}

	/**
	 * Nastav� okno pro zobrazen� edita�n�ch uzl�.
	 * @param o Objekt, kter� m� b�t v okn� editov�n.
	 */
	public void setNodesFrameTo(IObject o)
	{
		nodesFrame.setObject(o);
	}

	/**
	 * Z�sk�n� instance hlavn�ho okna.
	 */
	public MainFrame getMainFrame()
	{
		return mainFrame;
	}

	/**
	 * Z�sk�n� instance okna pro editaci bod� a uzlov�ch vektor�.
	 */
	public NodesFrame getNodesFrame()
	{
		return nodesFrame;
	}

	/**
	 * Z�sk�n� instance okna pro nastaven� programu.
	 */
	public SettingsFrame getSettingsFrame()
	{
		return settingsFrame;
	}

	/**
	 * Otev�e dialog pro v�b�r souboru pro export do obr�zku.
	 * @return Vr�t� soubor nebo null pokud nedojde k v�b�ru.
	 */
	public File askForImageExportFile()
	{
		if (exportImageFS.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
		{
			if (exportImageFS.getSelectedFile().getName().indexOf(".") == -1)//p�id�m p��ponu pokud ji u�ivatel nezadal
			{
				exportImageFS.setSelectedFile(
				  new File(exportImageFS.getSelectedFile().getAbsolutePath() + ".png"));
			}
			return exportImageFS.getSelectedFile();
		}
		return null;
	}

	/**
	 * Otev�e dialog pro v�b�r souboru pro ulo�en� projektu.
	 * @param defaultFile p�ednastaven� cesta, m��e b�t null, pak bude cesta posledn� pou�it�.
	 * @return Vybran� soubor nebo null.
	 */
	public File askForProjectSaveFile(File defaultFile)
	{
		if (defaultFile != null)
		{
			projectSaveFS.setSelectedFile(defaultFile);
		}
		if (projectSaveFS.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
		{
			if (projectSaveFS.getSelectedFile().getName().indexOf(".") == -1)//p�id�m p��ponu pokud ji u�ivatel nezadal
			{
				projectSaveFS.setSelectedFile(
				  new File(projectSaveFS.getSelectedFile().getAbsolutePath() + ".cscg"));
			}
			return projectSaveFS.getSelectedFile();
		}
		return null;
	}

	/**
	 * Otev�e dialog pro v�b�r souboru pro otev�en� projektu.
	 * @param defaultDirectory P�ednastaven� cesta, m��e b�t null, pak bude cesta posledn� pou�it�.
	 * @return Vybran� soubor nebo null.
	 */
	public File askForProjectOpenFile(File defaultDirectory)
	{
		if (defaultDirectory != null)
		{
			projectOpenFS.setCurrentDirectory(defaultDirectory);
		}
		if (projectOpenFS.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
		{
			return projectOpenFS.getSelectedFile();
		}
		return null;
	}

	/**
	 * Ukon�en� a zav�en� cel�ho GUI.
	 */
	public void destroy()
	{
		mainFrame.setVisible(false);
		mainFrame.dispose();
		nodesFrame.dispose();
		settingsFrame.dispose();
	}
}
