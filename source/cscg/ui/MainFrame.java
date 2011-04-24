package cscg.ui;

import com.jogamp.opengl.util.FPSAnimator;
import cscg.model.Couple;
import cscg.model.Model;
import cscg.model.ModelAdapter;
import cscg.model.objects.IObject;
import cscg.model.objects.IPointsAddable;
import cscg.model.objects.ObjectAdapater;
import cscg.model.Project;
import cscg.model.ProjectAdapter;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.Point3f;
import cscg.ui.components.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Hlavní okno aplikace obsahující hlavní ovládací prvky.
 * @author Tomáš režnar
 */
public class MainFrame extends JFrame
{

	/**
	 * Model dle vzoru MVC.
	 */
	Model model;
	/**
	 * Posluchaè editorù-jedna instance pro všechny editory.
	 */
	private final EditorListenerImpl editorListener;
	/**
	 * PosluchaÈi udalostí.
	 */
	private ArrayList<MainFrameListener> listeners = new ArrayList<MainFrameListener>(1);
	/**
	 * Panel vyplòující celé okno.
	 */
	private JPanel mainPanel;
	/**
	 * Rozdìlení hlavního panelu na 2 svislé èásti.
	 */
	private JSplitPane mainSplitPanel;
	/**
	 * Levá èást GUI obsahující objekty a vlastnosti.
	 */
	private JPanel leftPanel;
	/**
	 * Panel vlastností objektu.
	 */
	private JPanel objectPropertiesPanel;
	/**
	 * Panel se seznamem existujích objektù.
	 */
	private JPanel objectListPanel;
	/**
	 * Pravá èást GUI obsahující OPENGL a vlastnosti bodu.
	 */
	private JPanel rightPanel;
	/**
	 * Panel sdružující v sobì panely pro nastavení bodu a panel pro nastavení bodu generovaný vybraným objektem.
	 */
	private JPanel pointPanel;
	/**
	 * Panel vlastností bodu.
	 */
	private PointPanel pointPropertiesPanel;
	/**
	 * Panel obsahující panel vlastností bodu generovaný objektem.
	 */
	private JPanel pointDependPropertiesPanel;
	/**
	 * Panel obsahující OPENGL editaci objektu v prostoru.
	 */
	private JTabbedPane editorPanel;
	/**
	 * Menu bar s hlavním menu.
	 */
	private JMenuBar mainMenuBar;
	/**
	 * Menu soubor.
	 */
	private JMenu fileMenu;
	/**
	 * Položka menu nový projekt.
	 */
	private JMenuItem menuItemNewProject;
	/**
	 * Položka menu otevøít projekt.
	 */
	private JMenuItem menuItemOpenProject;
	/**
	 * Položka menu zavøít projekt.
	 */
	private JMenuItem menuItemCloseProject;
	/**
	 * Položka menu uložit projekt.
	 */
	private JMenuItem menuItemSaveProject;
	/**
	 * Položka menu uložit jako projekt.
	 */
	private JMenuItem menuItemSaveAsProject;
	/**
	 * Položka menu export do obrázku.
	 */
	private JMenuItem menuItemExportImage;
	/**
	 * Položka menu skonèit program.
	 */
	private JMenuItem menuItemExit;
	/**
	 * Menu volba zobrazení.
	 */
	private JMenu displayModeMenu;
	/**
	 * Položka menu zobrazení kombinace nárysu, pùdorysu, bokorysu a perspektivního pohledu.
	 */
	private JRadioButtonMenuItem menuItemDisplayCombine;
	/**
	 * Položka menu zobrazení nárysu - pohledu zepøedu.
	 */
	private JRadioButtonMenuItem menuItemDisplayFront;
	/**
	 * Položka menu pro nastavení režimu editace.
	 */
	private JCheckBoxMenuItem menuItemEditingMode;
	/**
	 * Položka menu pro volbu zobrazení/skrytí os.
	 */
	private JCheckBoxMenuItem menuItemShowAxes;
	/**
	 * Položka menu pro volbu zobrazení informaèního textu.
	 */
	private JCheckBoxMenuItem menuItemShowInformationText;
	/**
	 * Položka menu pro volbu zobrazení okony orientace kamery.
	 */
	private JCheckBoxMenuItem menuItemShowOrientationIcon;
	/**
	 * Položka menu pro otevøení okna editaèních uzlù.
	 */
	private JMenuItem menuItemShowNodesFrame;
	/**
	 * Seskupení voleb zobrazení.
	 */
	private ButtonGroup displayModeMenuGroup;
	/**
	 * Položka menu pro zobrazení okna s nastavením.
	 */
	private JMenuItem menuItemSettings;
	/**
	 * Seznam menu pro pøidání objektu do projektu.
	 */
	private List<JMenu> menuObjectAdd = new LinkedList<JMenu>();
	/**
	 * Tlaèítko pro zavøení otevøeného souboru.
	 */
	private JButton projectCloseButton;
	/**
	 * Posluchaèi zmìn v projektu. Pro každý projekt je jedna instance.
	 */
	private ArrayList<ProjectListener> projectListeners = new ArrayList<ProjectListener>();
	/**
	 * Pro každý projekt je vytvoøen jeden animátor, k objektu je pøistupováno z více vláken.
	 */
	private final List<FPSAnimator> animators = Collections.synchronizedList(new LinkedList<FPSAnimator>());
	/**
	 * List existujících objektù.
	 */
	private JList objectList;
	/**
	 * Pole pro nastavení jména objektu.
	 */
	private JTextField objectNameField;
	/**
	 * Panel s promnìným GUI, GUI se mìní dle typu vybraného objektu, GUI je umplementováno pøímo v tøídì objektu.
	 */
	private JPanel objectDependPropertiesPanel;
	/**
	 * Label zobrazující informativní hlášky objektù.
	 */
	private JLabel statusMessage;
	/**
	 * Spodní panel pro informativní hlášky.
	 */
	private JPanel statusBar;
	/**
	 * Posluchaè všech menu za úèelem opravy zobrazení menu pøe GLCanvas.
	 */
	private MenusListener menusListener;
	/**
	 * Panel akcí s objekty - seøazení, viditelnost.
	 */
	private ObjectOrderPanel objectOrderPanel;
	/**
	 * Aktuální zobrazený projekt.
	 */
	private Project currentProject;

	public MainFrame(Model model)
	{
		this.model = model;
		this.editorListener = new EditorListenerImpl();//jeden posluchaè pro všechny editory
		Project[] projects = model.getProjects();//získám projekty
		Project workingProject = model.getWorkingProject();
		//odstraním staré projety
		for (Project p : projects)
		{
			model.removeProject(p);
		}

		model.addModelListener(new ModelListener());//pøidání posluchaèe událostí v modelu
		init();

		//znovu vložím staré projety
		for (Project p : projects)
		{
			model.addProject(p);
		}

		model.setWorkingProject(workingProject);

		if (editorPanel.getSelectedIndex() != -1)//zajistím aby zobrazený projekt byl opravdu vybraný
		{
			model.setWorkingProject(editorPanel.getSelectedIndex());
		}
	}

	/**
	 * Pøidání posluchaèe okna.
	 */
	public void addListener(MainFrameListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchaèe okna.
	 */
	public void removeListener(MainFrameListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Inicializace okna, vytvoøení komponent GUI.
	 */
	private void init()
	{
		setTitle("Køivky a plochy");
		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(WindowEvent e)
			{
				fireExit();
			}
		});
		setSize(800, 700);
		setMinimumSize(new Dimension(800, 600));
		initMainMenu();
		setLayout(new BorderLayout());
		//hlavni panel
		mainPanel = new JPanel(new BorderLayout());
		add(mainPanel);
		mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);//vertikální rozdìlení hlavního panelu
		mainPanel.add(mainSplitPanel);
		initLeftColumn();
		initRightColumn();
		initStatusBar();
	}

	/**
	 * Vytvoøení èásti GUI levého sloupce obsahující prvky pro nastavení vlastností objektu.
	 */
	private void initLeftColumn()
	{
		leftPanel = new JPanel(new GridBagLayout());
		mainSplitPanel.setLeftComponent(leftPanel);
		initObjectPropertiesPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		leftPanel.add(objectPropertiesPanel, gbc);
		initObjectListPanel();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		leftPanel.add(objectListPanel, gbc);

		leftPanel.setMinimumSize(leftPanel.getPreferredSize());
	}

	/**
	 *  Vytvoøení èásti GUI pravého sloupce obsahující pøedevším OpenGL okno.
	 */
	private void initRightColumn()
	{
		rightPanel = new JPanel(new GridBagLayout());
		rightPanel.setMinimumSize(new Dimension(600, 500));
		mainSplitPanel.setRightComponent(rightPanel);
		initPointPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		pointPanel = new JPanel(new GridBagLayout());
		JScrollPane pointScroll = new JScrollPane(pointPanel);
		pointScroll.setBorder(BorderFactory.createTitledBorder("Vlastnosti bodu"));
		pointPanel.add(pointPropertiesPanel, gbc);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 1;
		gbc2.weightx = 1;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.insets = new Insets(0, 5, 0, 0);
		pointPanel.add(pointDependPropertiesPanel, gbc2);
		rightPanel.add(pointScroll, gbc);
		pointPropertiesPanel.setSize(pointPropertiesPanel.getPreferredSize());
		initEditor();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		rightPanel.add(editorPanel, gbc);
	}

	/**
	 * Vytvoøení status baru - obsahuje chybové hlášky.
	 */
	private void initStatusBar()
	{
		statusMessage = new JLabel();
		statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.windowBorder));
		statusBar.add(new JLabel(" "));//slouží aby se status bar nesmrskl když statusMessage nemá text
		statusBar.add(statusMessage);
		mainPanel.add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * Vytvoøení GUI vlastností objektu.
	 */
	private void initObjectPropertiesPanel()
	{
		objectPropertiesPanel = new JPanel(new GridBagLayout());
		objectPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Vlastnosti objektu"));
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		objectPropertiesPanel.add(new JLabel("Jméno "), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		objectNameField = new JTextField();
		objectPropertiesPanel.add(objectNameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 0, 0, 0);
		objectDependPropertiesPanel = new JPanel(new BorderLayout());
		objectPropertiesPanel.add(objectDependPropertiesPanel, gbc);

		//posluchaè zmìny textu v poli názvu objektu
		objectNameField.addCaretListener(new CaretListener()
		{

			@Override
			public void caretUpdate(CaretEvent e)
			{
				try
				{
					fireSetObjectName(currentProject, currentProject.getSelectedObject(), objectNameField.getText());
				} catch (Exception ex)
				{
				}
			}
		});
	}

	/**
	 * Vytvoøení GUI seznamu existujících objektù.
	 */
	private void initObjectListPanel()
	{
		objectListPanel = new JPanel(new GridBagLayout());
		objectListPanel.setBorder(BorderFactory.createTitledBorder("Seznam objektù"));
		//seznam objektù
		objectList = new JList();
		JScrollPane objectListScroll = new JScrollPane(objectList);
		objectListScroll.setPreferredSize(new Dimension(50, 50));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		objectListPanel.add(objectListScroll, gbc);
		objectList.addListSelectionListener(new ListSelectionListener()
		{

			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				fireSetSelectedObject(currentProject, objectList.getSelectedIndex());
			}
		});
		//akce nad objektem
		objectOrderPanel = new ObjectOrderPanel();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		objectListPanel.add(objectOrderPanel, gbc);
		objectOrderPanel.addEventListener(new ObjectOrderPanelListener()
		{

			@Override
			public void moveUpEvent(EventObject e)
			{
				try
				{
					Project p = currentProject;
					IObject o = currentProject.getSelectedObject();
					fireMoveUpObject(p, o);
				} catch (NullPointerException ex)
				{
				}
			}

			@Override
			public void moveDownEvent(EventObject e)
			{
				try
				{
					Project p = currentProject;
					IObject o = p.getSelectedObject();
					fireMoveDownObject(p, o);
				} catch (NullPointerException ex)
				{
				}
			}

			@Override
			public void visibilityChangeEvent(EventObject e)
			{
				try
				{
					Project p = currentProject;
					IObject o = p.getSelectedObject();
					fireSetObjectVisibility(p, o, objectOrderPanel.isVisibilityButtonSelected());
				} catch (NullPointerException ex)
				{
				}
			}

			@Override
			public void exlusiveVisibilityChangeEvent(EventObject e)
			{
				try
				{
					Project p = currentProject;
					IObject o = p.getSelectedObject();
					fireSetObjectExlusiveVisibility(p, o, objectOrderPanel.isExlusiveVisibilityButtonSelected());
				} catch (NullPointerException ex)
				{
				}
			}

			@Override
			public void deleteEvent(EventObject e)
			{
				try
				{
					Project p = currentProject;
					IObject o = p.getSelectedObject();
					fireRemoveObject(p, o);
				} catch (NullPointerException ex)
				{
				}
			}

			@Override
			public void rotateEvent(RotateEvent e)
			{
				try
				{
					Project p = currentProject;
					IObject o = p.getSelectedObject();
					IPoint3f axes;
					IPoint3f pivot;
					switch (e.getAxes())
					{
						case RotateEvent.AXE_BY_X:
							axes = new Point3f(1, 0, 0);
							break;

						case RotateEvent.AXE_BY_Y:
							axes = new Point3f(0, 1, 0);
							break;

						case RotateEvent.AXE_BY_Z:
						default:
							axes = new Point3f(0, 0, 1);
							break;
					}

					switch (e.getPivot())
					{
						case RotateEvent.CENTER_IS_AVERAGE_POINT:
							pivot = o.getAverageCenter();
							break;
						case RotateEvent.CENTER_IS_MIDDLE_POINT:
							pivot = o.getCenter();
							break;
						case RotateEvent.CENTER_IS_ORIGIN:
						default:
							pivot = new Point3f(0, 0, 0);
							break;
					}

					float angle = (float) e.getAngle();
					if (e.isPositive() == false)
					{
						angle *= -1f;
					}

					fireRotateObject(p, o, pivot, axes, angle);
				} catch (NullPointerException ex)
				{
				}
			}

			@Override
			public void moveEvent(MoveEvent e)
			{
				try
				{
					Project p = currentProject;
					IObject o = p.getSelectedObject();

					float change = 10f / p.getViewport1Projection().getZoom();
					if (e.isPositive() == false)
					{
						change *= -1;
					}

					IPoint3f vector;
					switch (e.getDirection())
					{
						case MoveEvent.MOVE_X:
							vector = new Point3f(change, 0, 0);
							break;

						case MoveEvent.MOVE_Y:
							vector = new Point3f(0, change, 0);
							break;

						case MoveEvent.MOVE_Z:
						default:
							vector = new Point3f(0, 0, change);
							break;
					}

					fireMoveObject(p, o, vector);
				} catch (NullPointerException ex)
				{
				}
			}
		});
	}

	/**
	 * Vytvoøení GUI vlastností bodu.
	 */
	private void initPointPanel()
	{
		pointPropertiesPanel = new PointPanel();
		pointPropertiesPanel.AddPointChangedListener(new PointChangedListener()
		{

			@Override
			public void pointChanged(PointChangedEvent e)
			{
				IObject o = currentProject.getSelectedObject();
				if (o != null)
				{
					fireEditPoint(currentProject, o, e.getOldPoint(), e.getNewPoint());
				}
			}
		});

		pointDependPropertiesPanel = new JPanel(new BorderLayout(0, 0));
	}

	/**
	 * Vytvoøení GUI pro zobrazení a editaci objektu v prostoru.
	 */
	private void initEditor()
	{
		editorPanel = new JTabbedPane(JTabbedPane.BOTTOM);
		editorPanel.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				fireSetWorkingProject(editorPanel.getSelectedIndex());
			}
		});
	}

	/**
	 * Vytvoøení hlavního menu.
	 */
	private void initMainMenu()
	{
		mainMenuBar = new JMenuBar();
		setJMenuBar(mainMenuBar);
		//menu soubor
		fileMenu = new JMenu("Projekt");
		mainMenuBar.add(fileMenu);
		menuItemNewProject = new JMenuItem("Nový projekt");
		menuItemOpenProject = new JMenuItem("Otevøít projekt");
		menuItemCloseProject = new JMenuItem("Zavøít projekt");
		menuItemSaveProject = new JMenuItem("Uložit projekt");
		menuItemSaveAsProject = new JMenuItem("Uložit jako");
		menuItemExportImage = new JMenuItem("Exportovat obrázek");
		menuItemExit = new JMenuItem("Konec");
		fileMenu.add(menuItemNewProject);
		fileMenu.add(menuItemOpenProject);
		fileMenu.add(menuItemSaveProject);
		fileMenu.add(menuItemSaveAsProject);
		fileMenu.add(menuItemExportImage);
		fileMenu.add(menuItemCloseProject);
		fileMenu.addSeparator();
		fileMenu.add(menuItemExit);


		//menu volba zobrazoeni
		displayModeMenu = new JMenu("Zobrazení");
		mainMenuBar.add(displayModeMenu);
		menuItemDisplayCombine = new JRadioButtonMenuItem("Kombinace");
		menuItemDisplayFront = new JRadioButtonMenuItem("Jeden pohled");
		menuItemEditingMode = new JCheckBoxMenuItem("Režim editace");
		menuItemShowAxes = new JCheckBoxMenuItem("Vykreslit osy");
		menuItemShowInformationText = new JCheckBoxMenuItem("Vykreslit informaèní text");
		menuItemShowOrientationIcon = new JCheckBoxMenuItem("Vykreslit orientaèní ikonu");
		menuItemShowNodesFrame = new JMenuItem("Otevøít seznam bodù");
		menuItemSettings = new JMenuItem("Nastavení");

		displayModeMenuGroup = new ButtonGroup();
		displayModeMenuGroup.add(menuItemDisplayCombine);
		displayModeMenuGroup.add(menuItemDisplayFront);

		displayModeMenu.add(menuItemDisplayFront);
		displayModeMenu.add(menuItemDisplayCombine);
		displayModeMenu.addSeparator();
		displayModeMenu.add(menuItemEditingMode);
		displayModeMenu.add(menuItemShowAxes);
		displayModeMenu.add(menuItemShowInformationText);
		displayModeMenu.add(menuItemShowOrientationIcon);
		displayModeMenu.add(menuItemShowNodesFrame);
		displayModeMenu.addSeparator();
		displayModeMenu.add(menuItemSettings);


		//menu pro pøidání objektù
		JMenu submenuGUI;
		JMenuItem submenuItemGUI;
		for (Couple<String, List<Couple<String, String>>> submenu : model.getMenuObjects())
		{
			submenuGUI = new JMenu(submenu.first);
			for (Couple<String, String> submenuItem : submenu.second)
			{
				if (submenuItem.first == null)
				{
					submenuGUI.addSeparator();
				} else
				{
					submenuItemGUI = new JMenuItem(submenuItem.first);
					submenuGUI.add(submenuItemGUI);
					//posluchaè
					submenuItemGUI.addActionListener(new MenuObjectAddActionListener(submenuItem.second));
				}
			}
			mainMenuBar.add(submenuGUI);
			menuObjectAdd.add(submenuGUI);
		}

		//tlaèítko zavøení projektu
		mainMenuBar.add(Box.createHorizontalGlue());
		projectCloseButton = new JButton(new ImageIcon(getClass().getResource("close.png")));
		projectCloseButton.setToolTipText("Zavøít projekt");
		mainMenuBar.add(projectCloseButton);

		//pøidání posluchaèù
		initMainMenuListeners();
	}

	/**
	 * Pøidání posluchaèù k tlaèítkù v hlavním menu
	 */
	private void initMainMenuListeners()
	{
		//zavøení projektu
		projectCloseButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireCloseProject(currentProject);
			}
		});
		menuItemCloseProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireCloseProject(currentProject);
			}
		});
		//nový projekt
		menuItemNewProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireNewProject();
			}
		});
		//export do obrázku
		menuItemExportImage.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{

				final Editor editor = (Editor) editorPanel.getSelectedComponent();
				//vlákno jež poèká na vygenerování obrázku
				new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						fireExportImage(currentProject, editor.getImage());
					}
				}).start();
			}
		});
		//uložení projektu
		menuItemSaveProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSaveProject(currentProject);
			}
		});
		//uložení projektu
		menuItemSaveAsProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSaveAsProject(currentProject);
			}
		});
		//otevøení projektu
		menuItemOpenProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireOpenProject(currentProject);
			}
		});
		//konec aplikace
		menuItemExit.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireExit();
			}
		});
		//volba zobrazení
		menuItemDisplayCombine.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				fireSetViewportCombination(currentProject, menuItemDisplayCombine.isSelected());
			}
		});
		//režim editace
		menuItemEditingMode.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetEditingMode(currentProject, menuItemEditingMode.isSelected());
				repairGLShow();
			}
		});
		//zobrazení os
		menuItemShowAxes.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetShowAxes(currentProject, menuItemShowAxes.isSelected());
				repairGLShow();
			}
		});
		//zobrazení inf. textu
		menuItemShowInformationText.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetShowInformationText(currentProject, menuItemShowInformationText.isSelected());
				repairGLShow();
			}
		});
		//zobrazení orientace kamery
		menuItemShowOrientationIcon.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetOrientationIcon(currentProject, menuItemShowOrientationIcon.isSelected());
				repairGLShow();
			}
		});
		//zobrazení okna editace bodù
		menuItemShowNodesFrame.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Project p = currentProject;
				if (p != null)
				{
					fireOpenNodesFrame(p, p.getSelectedObject());
				}
				repairGLShow();
			}
		});
		//zobrazení okna s nastavením
		menuItemSettings.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireOpenSettingsFrame();
				repairGLShow();
			}
		});
		//pøidám posluchaèe všech submenu-rekurzivní procházení
		menusListener = new MenusListener();
		menuAddListeners(mainMenuBar, menusListener);
	}

	/**
	 * Rekurzivní pøidání posluchaèù k menu.
	 */
	private void menuAddListeners(JComponent item, MenusListener l)
	{
		if (item == null)
		{
			return;
		}
		for (Component c : item.getComponents())
		{
			c.addMouseListener(l);
			c.addKeyListener(l);
			if (c instanceof JMenuItem)
			{
				((JMenuItem) c).addActionListener(l);
			}
			if (c instanceof JComponent)
			{
				menuAddListeners((JComponent) c, l);
			}
		}
	}

	/**
	 * Hromadnì povolí všechny GUI prvky které se týkají projektu:<br />
	 * -vlastnosti bodu, vlastnosti objektu, objekty seznam, tlaèítko pro zavøení projektu, prvky v menu.
	 * @param enabled True pro povolení
	 */
	private void setProjectGUIEnabled(boolean enabled)
	{
		projectCloseButton.setVisible(enabled);
		menuItemCloseProject.setEnabled(enabled);
		menuItemSaveProject.setEnabled(enabled);
		menuItemExportImage.setEnabled(enabled);
		displayModeMenu.setEnabled(enabled);
		objectListPanel.setEnabled(enabled);
		objectPropertiesPanel.setEnabled(enabled);
		pointPropertiesPanel.setEnabled(enabled);
		//submenu pro pøidání objektu
		for (JMenu menu : menuObjectAdd)
		{
			menu.setEnabled(enabled);
		}
	}

	/**
	 * Nastaví prvky GUI pro projekt.
	 */
	private void setProjectGUIto(Project project)
	{
		if (project == null)
		{
			setProjectGUIEnabled(false);
			setObjectsListGUIto(project);
			setObjectGUIto(project);
			return;
		}
		editorPanel.setSelectedIndex(model.getIndexOfProject(project));
		setProjectGUIEnabled(true);
		if (project.isDisplayAllViewports())
		{
			menuItemDisplayCombine.setSelected(true);
		} else
		{
			menuItemDisplayFront.setSelected(true);
		}
		setObjectsListGUIto(project);
		setObjectGUIto(project);
	}

	/**
	 * Nastaví GUI prvky týkající se seznamu objektù v projektu.
	 */
	private void setObjectsListGUIto(Project project)
	{
		IObject objects[];
		if (project == null)
		{
			objectList.setListData(new Object[0]);
		} else
		{
			int selectedIndex = project.getSelectedObjectIndex();
			objects = project.getObjects();
			objectList.setListData(objects);
			if (selectedIndex >= 0)
			{
				objectList.setSelectedIndex(selectedIndex);
			} else
			{
				objectList.clearSelection();
			}
		}
		repairGLShow();
	}

	/**
	 * Nastavení panelu pro pøesun objektù projektu.
	 */
	private void setObjectOrderGUIto(Project project)
	{
		IObject selectedObject = null;
		if (project != null)
		{
			selectedObject = project.getSelectedObject();
			objectOrderPanel.setEnabled(false);
		}
		if (selectedObject == null)
		{
			objectOrderPanel.setEnabled(false);
		} else
		{
			objectOrderPanel.setEnabled(true);
			objectOrderPanel.setUpButtonEnabled(!project.isHighest(selectedObject));
			objectOrderPanel.setDownButtonEnabled(!project.isLowest(selectedObject));
			objectOrderPanel.setVisibilityButtonSelected(selectedObject.isVisible());
			objectOrderPanel.setExlusiveVisibilityButtonSelected(project.isExlusiveVisibility());
		}
		repairGLShow();
	}

	/**
	 * Nastaví prvky GUI pro objekt.
	 */
	private void setObjectGUIto(Project project)
	{
		IObject selectedObject = null;
		if (project != null)
		{
			selectedObject = project.getSelectedObject();
			menuItemEditingMode.setSelected(project.isEditorInEditingMode());
			menuItemShowAxes.setSelected(project.isShowAxes());
			menuItemShowInformationText.setSelected(project.isShowInformationText());
			menuItemShowOrientationIcon.setSelected(project.isShowOrientationIcon());
		}
		objectDependPropertiesPanel.removeAll();
		if (selectedObject == null)
		{
			objectNameField.setEnabled(false);
			objectNameField.setText("");
			setStatusMessage(null);
			//GUI pro nastavení bodu
			setPointGUIto(project);
		} else
		{
			//setStatusMessage(selectedObject.getStateMessage());
			objectDependPropertiesPanel.setLayout(new BorderLayout());
			objectNameField.setEnabled(true);
			String name = selectedObject.getName();
			if (!objectNameField.getText().equals(name))//pokud došlo ke zmìnì názvu
			{
				objectNameField.setText(name);
			}
			//GUI generovane pøímo objektem
			if (selectedObject.getObjectGUI() != null)
			{
				objectDependPropertiesPanel.add(selectedObject.getObjectGUI(), BorderLayout.CENTER);
			}
			//GUI pro nastavení bodu
			setPointGUIto(project);
			objectDependPropertiesPanel.updateUI();
		}
		setObjectOrderGUIto(project);
		repairGLShow();
	}

	/**
	 * Nastavení GUI pro editaci bodu pro vybrané body.
	 */
	private void setPointGUIto(Project project)
	{
		List<IPoint3f> selectedPoints = null;
		IObject selectedObject = null;
		pointDependPropertiesPanel.removeAll();//odeberu panel vlastností bodu generovaný objektem
		if (project != null && (selectedObject = project.getSelectedObject()) != null)
		{
			selectedPoints = selectedObject.getSelectedPoints();
			Component pointPorpertiesPnael = selectedObject.getPointGUI();
			if (pointPorpertiesPnael != null)
			{
				//pøidám panel vlastností bodu generovaný objektem
				pointDependPropertiesPanel.add(pointPorpertiesPnael, BorderLayout.CENTER);
			}
		}
		pointPropertiesPanel.setPoint((selectedPoints == null || selectedPoints.size() != 1) ? null : selectedPoints.get(0));
		objectDependPropertiesPanel.updateUI();
	}

	/**
	 * Nastavení status textu.
	 * @param stateMessage Pokud zadáte null, pak se status text vynuluje
	 */
	private void setStatusMessage(String stateMessage)
	{
		statusMessage.setText(stateMessage);
		repairGLShow();
	}

	/**
	 * Odstraní chybu, kdy GLCanvas komponenta pøekresluje menu, a menu nelze vidìt.
	 */
	protected void repairGLShow()
	{
		try
		{
			mainMenuBar.setVisible(false);
			mainMenuBar.setVisible(true);
		} catch (Exception e)
		{
		}
	}

	@Override
	public void dispose()
	{
		//zastavím všechny OpenGL panely
		synchronized(animators)
		{
			for (final FPSAnimator a : animators)
			{
				new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							a.stop();
						} catch (Exception e)
						{
						}
					}
				}).start();
			}
		}
		super.dispose();
	}

	/**
	 * Vyvolání události ukonèení aplikace vyvolané uživatelem v GUI.
	 */
	protected void fireExit()
	{
		for (MainFrameListener l : listeners)
		{
			l.eventExit();
		}
	}

	/**
	 * Vyvolání události zavøení projektu.
	 */
	protected void fireCloseProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventCloseProject(project);
		}
	}

	/**
	 * Vyvolání události vytvoøení projektu.
	 */
	protected void fireNewProject()
	{
		for (MainFrameListener l : listeners)
		{
			l.eventNewProject();
		}
	}

	/**
	 * Vyvolání události nastavení režimu zobrazení mezi 4 pohledy a jedním.
	 * @param combination True pro 4 pohledy.
	 */
	protected void fireSetViewportCombination(Project project, boolean combination)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetViewportCombination(project, combination);
		}
	}

	/**
	 * Vyvolání události nastavení pracovního projektu.
	 * @param index Index projektu.
	 */
	protected void fireSetWorkingProject(int index)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetWorkingProject(index);
		}
	}

	/**
	 * Vyvolání události nastavení vybraného objektu.
	 * @param index Index objektu.
	 */
	protected void fireSetSelectedObject(Project project, int index)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetSelectedObject(project, index);
		}
	}

	/**
	 * Vyvolání události relativního posunu bodu.
	 * @param project Projekt ve kterém se nachází bod.
	 * @param point Bod pro pøesun.
	 * @param x Zmìna x.
	 * @param y Zmìna y.
	 * @param z Zmìna z.
	 */
	protected void fireMovePointRelative(Project project, IObject object, IPoint3f point, float x, float y, float z)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMovePointRelative(project, object, point, x, y, z);
		}
	}

	/**
	 * Vyvolání události relativního posunu bodù.
	 * @param project Projekt ve kterém se nachází bod.
	 * @param points Body pro pøesun.
	 * @param x Zmìna x.
	 * @param y Zmìna y.
	 * @param z Zmìna z.
	 */
	private void fireMovePointsRelative(Project project, IObject object, List<IPoint3f> points, float x, float y, float z)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMovePointsRelative(project, object, points, x, y, z);
		}
	}

	/**
	 * Vyvolání události posunu bodu na pozici.
	 * @param project Projekt ve kterém se nachází bod.
	 * @param point Bod pro pøesun.
	 */
	protected void fireMovePointAbsolute(Project project, IObject object, IPoint3f point, float x, float y, float z)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMovePointAbsolute(project, object, point, x, y, z);
		}
	}

	/**
	 * Vyvolání události odebrání øídících bodù objektu.
	 */
	protected void fireDeleteSelectedPointsFromObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventDeleteSelectedPointsFromObject(project, object);
		}
	}

	/**
	 * Událost pøidání nového bodu.
	 */
	protected void fireAddPoint(Project project, IObject object, IPoint3f point)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventAddPoint(project, object, point);
		}
	}

	/**
	 * Událost nastavení jména objektu.
	 */
	protected void fireSetObjectName(Project project, IObject object, String text)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventObjectRename(project, object, text);
		}
	}

	/**
	 * Událost pøidání nového objektu.
	 */
	protected void fireAddObject(Project project, String className)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventAddObject(project, className);
		}
	}

	/**
	 * Událost posunutí vybraného objektu nahoru.
	 */
	protected void fireMoveUpObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMoveUpObject(project, object);
		}
	}

	/**
	 * Událost posunutí vybraného objektu dolù.
	 */
	protected void fireMoveDownObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMoveDownObject(project, object);
		}
	}

	/**
	 * Událost nastavení viditelnosti vybraného objektu.
	 */
	protected void fireSetObjectVisibility(Project project, IObject object, boolean visible)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetObjectVisibility(project, object, visible);
		}
	}

	/**
	 * Událost nastavení exluzivní viditelnosti vybraného objektu.
	 */
	protected void fireSetObjectExlusiveVisibility(Project project, IObject object, boolean visible)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetObjectExlusiveVisibility(project, object, visible);
		}
	}

	/**
	 * Událost odstranìní objektu.
	 */
	protected void fireRemoveObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventRemoveObject(project, object);
		}
	}

	/**
	 * Událost rotace objektu.
	 * @param pivot støed otáèení.
	 * @param axes osa otáèení.
	 * @param angle úhel ve stupních.
	 */
	protected void fireRotateObject(Project project, IObject object, IPoint3f pivot, IPoint3f axes, float angle)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventRotateObject(project, object, pivot, axes, angle);
		}
	}

	/**
	 * Událost posunu objektu.
	 * @param vector Vektor pøesunu objektu.
	 */
	protected void fireMoveObject(Project project, IObject object, IPoint3f vector)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMoveObject(project, object, vector);
		}
	}

	/**
	 * Událost nastavení editaèního režimu.
	 */
	protected void fireSetEditingMode(Project project, boolean editingMode)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetEditingMode(project, editingMode);
		}
	}

	/**
	 * Událost exportu do obrázku.
	 */
	protected void fireExportImage(Project project, BufferedImage image)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventExportImage(project, image);
		}
	}

	/**
	 * Událost otevøení projektu..
	 */
	protected void fireOpenProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventOpenProject();
		}
	}

	/**
	 * Událost uložení projektu.
	 */
	protected void fireSaveProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSaveProject(project);
		}
	}

	/**
	 * Událost uložení projektu jako.
	 */
	protected void fireSaveAsProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSaveAsProject(project);
		}
	}

	/**
	 * Událost nastavení zobrazení os.
	 */
	protected void fireSetShowAxes(Project project, boolean showAxes)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventShowAxes(project, showAxes);
		}
	}

	/**
	 * Událost editace bodu.
	 */
	protected void fireEditPoint(Project project, IObject object, IPoint3f oldPoint, IPoint3f newPoint)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventEditPoint(project, object, oldPoint, newPoint);
		}
	}

	/**
	 * Událost událost nastavení výbìru bodù.
	 */
	protected void fireSetPointsSelectionByIndex(Project project, IObject object, int[] selectedPoints)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetSelectedPointByIndex(project, object, selectedPoints);
		}
	}

	/**
	 * Událost zrušení výbìru bodù.
	 */
	private void fireCancelSelectedPoints(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventCancelPointsSelection(project, object);
		}
	}

	/**
	 * Událost pøidání bodu do výbìru.
	 */
	private void fireAddPointToSelection(Project project, IObject object, IPoint3f point)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventAddPointToSelection(project, object, point);
		}
	}

	/**
	 * Událost nastavení bodu do výbìru.
	 */
	private void fireSetPointAsSelection(Project project, IObject object, IPoint3f point)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetPointAsSelection(project, object, point);
		}
	}

	/**
	 * Událost nastavení zobrazení informaèního textu.
	 */
	protected void fireSetShowInformationText(Project project, boolean showInformationText)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventShowInformationText(project, showInformationText);
		}
	}

	/**
	 * Událost nastavení orientaèní ikony kamery.
	 */
	protected void fireSetOrientationIcon(Project project, boolean showOrientationIcon)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventShowOrientationIcon(project, showOrientationIcon);
		}
	}

	/**
	 * Událost otevøení okna editace øídících bodù.
	 */
	private void fireOpenNodesFrame(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventOpenNodesFrame(project, object);
		}
	}

	/**
	 * Událost otevøení okna nastavení.
	 */
	private void fireOpenSettingsFrame()
	{
		for (MainFrameListener l : listeners)
		{
			l.eventOpenSettingsFrame();
		}
	}

	/**
	 * Tøída obsluhující události zmìn v projektech. Pro každý projekt je vytvoøena jedna instance této tøídy.
	 */
	private class ProjectListener extends ProjectAdapter
	{

		/**
		 * Projekt který je sledován
		 */
		protected Project project;
		/**
		 * Index projektu v záložkách
		 */
		protected int index;
		/**
		 * Posluchaè zmìn v objektech tohoto projektu, jeden pro všechny
		 */
		private final ObjectListener objectListener;

		public ProjectListener(Project project)
		{
			this.project = project;
			this.objectListener = new ObjectListener(project);
		}

		@Override
		public void eventFileChanged(String name, String file)
		{
			int listenerIndex = projectListeners.indexOf(this);
			editorPanel.setTitleAt(listenerIndex, name);
			editorPanel.setToolTipTextAt(listenerIndex, file);
		}

		@Override
		public void eventObjectAdded(IObject newObject, int index)
		{
			newObject.addObjectListener(objectListener);
			setObjectsListGUIto(currentProject);
			setObjectGUIto(currentProject);
		}

		@Override
		public void eventObjectRemoved(IObject oldObject, int index)
		{
			setObjectsListGUIto(currentProject);
			setObjectGUIto(currentProject);
		}

		@Override
		public void eventSelectedObjectChanged(IObject selectedObject, int index)
		{
			if (project == currentProject)
			{
				objectList.setSelectedIndex(project.getSelectedObjectIndex());
				setObjectGUIto(currentProject);
			}
		}

		@Override
		public void eventDisplayAllViewportsChanged(boolean displayAllViewports)
		{
			setPointGUIto(currentProject);
		}

		@Override
		public void eventObjectsOrderChanged()
		{
			if (project == currentProject)
			{
				setObjectsListGUIto(currentProject);
				setObjectGUIto(currentProject);
			}
		}

		@Override
		public void eventExlusiveVisibilityChanged()
		{
			if (project == currentProject)
			{
				setObjectGUIto(currentProject);
			}
		}

		@Override
		public void eventEditorInEditingMode(boolean editorInEditingMode)
		{
			if (project == currentProject)
			{
				menuItemEditingMode.setSelected(editorInEditingMode);
			}
		}

		@Override
		public void eventShowAxesChanged(boolean showAxes)
		{
			if (project == currentProject)
			{
				menuItemShowAxes.setSelected(showAxes);
			}
		}

		@Override
		public void eventShowInformationText(boolean showInformationText)
		{
			if (project == currentProject)
			{
				menuItemShowInformationText.setSelected(showInformationText);
			}
		}

		@Override
		public void eventShowOrientationIcon(boolean showOrientationIcon)
		{
			if (project == currentProject)
			{
				menuItemShowOrientationIcon.setSelected(showOrientationIcon);
			}
		}

		/**
		 * Získání posluchaèe objektù v tomto projektu.
		 */
		public ObjectListener getObjectListener()
		{
			return objectListener;
		}
	}

	/**
	 * Tøída obslující události v modelu.
	 */
	private class ModelListener extends ModelAdapter
	{

		@Override
		public synchronized void eventProjectAdd(Project project)
		{
			try
			{
				Editor editor = new Editor(project, model);
				editor.addListener(editorListener);
				final FPSAnimator animator = new FPSAnimator(editor, 25, true);
				animators.add(animator);
				animator.setIgnoreExceptions(true);
				animator.setPrintExceptions(Model.isDebug());
				animator.start();
				editorPanel.insertTab(project.getName(), null, editor, project.getFile(), editorPanel.getTabCount());
				ProjectListener projectListener = new ProjectListener(project);
				projectListeners.add(projectListener);
				project.addProjectListener(projectListener);
				//pøepnu na záložku s novým projektem
				editorPanel.setSelectedIndex(editorPanel.getTabCount() - 1);
				//nastavim poskuchaèe všech objektù v projektu
				for (IObject o : project.getObjects())
				{
					o.addObjectListener(projectListener.getObjectListener());
				}
				//skryju a znovu zobrazim hlavní menu pøi každé zmìnì velikosti editoru,
				//kvuli chybì v poøadí vykreslení-editor by jinak pøekrýval menu
				editor.addGLEventListener(new GLEventListener()
				{

					@Override
					public void init(GLAutoDrawable drawable)
					{
					}

					@Override
					public void display(GLAutoDrawable drawable)
					{
					}

					@Override
					public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
					{
						repairGLShow();
					}

					public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
					{
					}

					@Override
					public void dispose(GLAutoDrawable glad)
					{
					}
				});
			} catch (UnsatisfiedLinkError ex)
			{
				model.debugPrintException(ex);
				JOptionPane.showMessageDialog(MainFrame.this,
				  "Nejsou dostupné knihovny OpenGL pro váš systém. Program bude ukonèen.",
				  "Chyba",
				  JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			} catch (GLException ex)
			{
				model.debugPrintException(ex);
				JOptionPane.showMessageDialog(MainFrame.this,
				  "Nelze inicializovat OpenGL. Program bude ukonèen.",
				  "Chyba",
				  JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}

		@Override
		public synchronized void eventProjectRemove(Project project, int index)
		{
			project.removeProjectListener(projectListeners.get(index));
			projectListeners.remove(index);
			//zastavení a odebrání animátoru
			final FPSAnimator animator = animators.remove(index);
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					if (animator.isAnimating())
					{
						animator.stop();
					}
				}
			}).start();
			editorPanel.remove(index);
		}

		@Override
		public synchronized void eventSetWorkingProject(Project previous, Project current)
		{
			MainFrame.this.currentProject = current;
			setProjectGUIto(current);
			if(current==null)
			{
				return;
			}
			//spuštìní animátoru pouze zobrazeného projektu
			final FPSAnimator animator = animators.get(model.getIndexOfProject(current));
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						synchronized(animators)
						{
							for (FPSAnimator a : animators)
							{
								if (a.isAnimating())
								{
									a.stop();
								}
							}
						}
					} catch (Exception ex)
					{
						model.debugPrintException(ex);
					}

					try
					{
						animator.start();
					} catch (Exception ex)
					{
						model.debugPrintException(ex);
					}
				}
			}).start();
		}
	}

	/**
	 * Tøída obsluhující zmìny v objektu, používá se jedna instance pro všechny objekty z projektu.
	 */
	private class ObjectListener extends ObjectAdapater
	{

		/**
		 * Projekt jehož objekty poslouchám.
		 */
		private final Project project;

		public ObjectListener(Project project)
		{
			this.project = project;
		}

		@Override
		public void eventNameChanged(IObject o)
		{
			if (project == currentProject)
			{
				setObjectGUIto(project);
			}
			repairGLShow();
		}

		@Override
		public void eventStateMessageChanged(IObject o)
		{
			setStatusMessage(o.getStateMessage());
			repairGLShow();
		}

		@Override
		public void eventColorChanged(IObject o)
		{
			repairGLShow();
		}

		@Override
		public void eventLineWidthChanged(IObject o)
		{
			repairGLShow();
		}

		@Override
		public void eventSpecificPropertiesChanged(IObject o)
		{
			setObjectOrderGUIto(currentProject);
			repairGLShow();
		}

		@Override
		public void eventPointSelectionChanged(IObject o)
		{
			setPointGUIto(currentProject);
		}

		@Override
		public void eventPointChanged(IObject o, IPoint3f p, int index)
		{
			setPointGUIto(currentProject);
		}

		@Override
		public void eventPointsChanged(IObject o)
		{
			setPointGUIto(currentProject);
		}
	}

	/**
	 * Posluchaè všech menu, jeho ukolem je pøed zobrazením menu zajistit aby menu bylo vidìt pøes GLJPanel.
	 */
	private class MenusListener extends MouseAdapter implements KeyListener, ActionListener
	{

		@Override
		public void mousePressed(MouseEvent e)
		{
			repairGLShow();
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			repairGLShow();
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			repairGLShow();
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			repairGLShow();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			repairGLShow();
		}
	}

	/**
	 * Posluchaè menu položek pro vytvoøení nového objektu.
	 * Pøi akci vyvolané sledovanou položkou, dojde k vytvoøení nového objektu dané tøídy.
	 */
	private class MenuObjectAddActionListener implements ActionListener
	{

		private String className;

		/**
		 * @param className Název tøídy objektu jež má být vytvoøen.
		 */
		private MenuObjectAddActionListener(String className)
		{
			this.className = className;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			fireAddObject(currentProject, className);
		}
	}

	/**
	 * Posluchaè editorù.
	 */
	private class EditorListenerImpl implements EditorListener
	{

		@Override
		public void evenAddPoint(Editor source, IObject object, IPoint3f newPoint)
		{
			fireAddPoint(source.getProject(), object, newPoint);
		}

		@Override
		public void eventMovePointRelative(Editor source, IObject object, IPoint3f p, float x, float y, float z)
		{
			fireMovePointRelative(source.getProject(), object, p, x, y, z);
		}

		@Override
		public void eventMovePointsRelative(Editor source, IObject object, List<IPoint3f> points, float x, float y, float z)
		{
			fireMovePointsRelative(source.getProject(), object, points, x, y, z);
		}

		@Override
		public void eventDeleteSelectedPoints(Editor source, IPointsAddable object)
		{
			fireDeleteSelectedPointsFromObject(source.getProject(), object);
		}

		@Override
		public void eventCancelSelectedPoints(Editor source, IObject object)
		{
			fireCancelSelectedPoints(source.getProject(), object);
		}

		@Override
		public void eventAddSelectedPoint(Editor source, IObject object, IPoint3f point)
		{
			fireAddPointToSelection(source.getProject(), object, point);
		}

		@Override
		public void eventSetSelectedPoint(Editor source, IObject object, IPoint3f point)
		{
			fireSetPointAsSelection(source.getProject(), object, point);
		}
	}
}
