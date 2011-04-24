package cscg.ui;

import cscg.model.Model;
import cscg.model.objects.IObject;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Pohled dle vzoru MVC.
 * @author Tomáš Režnar
 */
public class View
{

	/**
	 * Model dle MVC.
	 */
	protected final Model model;
	/**
	 * Dialog volby souboru pro export do obrázku.
	 */
	private final JFileChooser exportImageFS;
	/**
	 * Dialog volby souboru pro uložení projektu.
	 */
	private final JFileChooser projectSaveFS;
	/**
	 * Dialog volby souboru pro otevøení projektu.
	 */
	private final JFileChooser projectOpenFS;

	;
	/**
	 * Okno pro zobrazení editaèních uzlù.
	 */
	protected final NodesFrame nodesFrame;
	/**
	 * Okno nastavení programu.
	 */
	protected final SettingsFrame settingsFrame;
	/**
	 * Hlavní frame.
	 */
	private final MainFrame mainFrame;

	public View(final Model model)
	{
		this.model = model;

		//hlavní okno
		mainFrame = new MainFrame(model);

		//vlastnosti export dialogu pro uložení obrázku
		exportImageFS = new JFileChooser();
		exportImageFS.setDialogTitle("Exportovat scénu do obrázku");
		exportImageFS.setDialogType(JFileChooser.SAVE_DIALOG);
		exportImageFS.setFileFilter(new FileNameExtensionFilter("PNG obrázky \".png\"", "png"));

		//vlastnosti dialogu pro výbìr souboru pro uložení
		projectSaveFS = new JFileChooser();
		projectSaveFS.setDialogTitle("Uložit projekt");
		projectSaveFS.setDialogType(JFileChooser.SAVE_DIALOG);
		projectSaveFS.setFileFilter(new FileNameExtensionFilter("projekt \".cscg\"", "cscg"));

		//vlastnosti dialogu pro výbìr souboru pro uložení
		projectOpenFS = new JFileChooser();
		projectOpenFS.setDialogTitle("Otevøít projekt");
		projectOpenFS.setDialogType(JFileChooser.OPEN_DIALOG);
		projectOpenFS.setMultiSelectionEnabled(false);
		projectOpenFS.setFileFilter(new FileNameExtensionFilter("projekt \".cscg\"", "cscg"));

		//okno pro editaci bodù a uzlového vektoru objektu
		nodesFrame = new NodesFrame();

		//okna pro nastavení
		settingsFrame = new SettingsFrame();
	}

	/**
	 * Nastaví okno pro zobrazení editaèních uzlù.
	 * @param o Objekt, který má být v oknì editován.
	 */
	public void setNodesFrameTo(IObject o)
	{
		nodesFrame.setObject(o);
	}

	/**
	 * Získání instance hlavního okna.
	 */
	public MainFrame getMainFrame()
	{
		return mainFrame;
	}

	/**
	 * Získání instance okna pro editaci bodù a uzlových vektorù.
	 */
	public NodesFrame getNodesFrame()
	{
		return nodesFrame;
	}

	/**
	 * Získání instance okna pro nastavení programu.
	 */
	public SettingsFrame getSettingsFrame()
	{
		return settingsFrame;
	}

	/**
	 * Otevøe dialog pro výbìr souboru pro export do obrázku.
	 * @return Vrátí soubor nebo null pokud nedojde k výbìru.
	 */
	public File askForImageExportFile()
	{
		if (exportImageFS.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
		{
			if (exportImageFS.getSelectedFile().getName().indexOf(".") == -1)//pøidám pøíponu pokud ji uživatel nezadal
			{
				exportImageFS.setSelectedFile(
				  new File(exportImageFS.getSelectedFile().getAbsolutePath() + ".png"));
			}
			return exportImageFS.getSelectedFile();
		}
		return null;
	}

	/**
	 * Otevøe dialog pro výbìr souboru pro uložení projektu.
	 * @param defaultFile pøednastavená cesta, mùže být null, pak bude cesta poslední použitá.
	 * @return Vybraný soubor nebo null.
	 */
	public File askForProjectSaveFile(File defaultFile)
	{
		if (defaultFile != null)
		{
			projectSaveFS.setSelectedFile(defaultFile);
		}
		if (projectSaveFS.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
		{
			if (projectSaveFS.getSelectedFile().getName().indexOf(".") == -1)//pøidám pøíponu pokud ji uživatel nezadal
			{
				projectSaveFS.setSelectedFile(
				  new File(projectSaveFS.getSelectedFile().getAbsolutePath() + ".cscg"));
			}
			return projectSaveFS.getSelectedFile();
		}
		return null;
	}

	/**
	 * Otevøe dialog pro výbìr souboru pro otevøení projektu.
	 * @param defaultDirectory Pøednastavená cesta, mùže být null, pak bude cesta poslední použitá.
	 * @return Vybraný soubor nebo null.
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
	 * Ukonèení a zavøení celého GUI.
	 */
	public void destroy()
	{
		mainFrame.setVisible(false);
		mainFrame.dispose();
		nodesFrame.dispose();
		settingsFrame.dispose();
	}
}
