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
 * Hlavn� okno aplikace obsahuj�c� hlavn� ovl�dac� prvky.
 * @author Tom� re�nar
 */
public class MainFrame extends JFrame
{

	/**
	 * Model dle vzoru MVC.
	 */
	Model model;
	/**
	 * Poslucha� editor�-jedna instance pro v�echny editory.
	 */
	private final EditorListenerImpl editorListener;
	/**
	 * Poslucha�i udalost�.
	 */
	private ArrayList<MainFrameListener> listeners = new ArrayList<MainFrameListener>(1);
	/**
	 * Panel vypl�uj�c� cel� okno.
	 */
	private JPanel mainPanel;
	/**
	 * Rozd�len� hlavn�ho panelu na 2 svisl� ��sti.
	 */
	private JSplitPane mainSplitPanel;
	/**
	 * Lev� ��st GUI obsahuj�c� objekty a vlastnosti.
	 */
	private JPanel leftPanel;
	/**
	 * Panel vlastnost� objektu.
	 */
	private JPanel objectPropertiesPanel;
	/**
	 * Panel se seznamem existuj�ch objekt�.
	 */
	private JPanel objectListPanel;
	/**
	 * Prav� ��st GUI obsahuj�c� OPENGL a vlastnosti bodu.
	 */
	private JPanel rightPanel;
	/**
	 * Panel sdru�uj�c� v sob� panely pro nastaven� bodu a panel pro nastaven� bodu generovan� vybran�m objektem.
	 */
	private JPanel pointPanel;
	/**
	 * Panel vlastnost� bodu.
	 */
	private PointPanel pointPropertiesPanel;
	/**
	 * Panel obsahuj�c� panel vlastnost� bodu generovan� objektem.
	 */
	private JPanel pointDependPropertiesPanel;
	/**
	 * Panel obsahuj�c� OPENGL editaci objektu v prostoru.
	 */
	private JTabbedPane editorPanel;
	/**
	 * Menu bar s hlavn�m menu.
	 */
	private JMenuBar mainMenuBar;
	/**
	 * Menu soubor.
	 */
	private JMenu fileMenu;
	/**
	 * Polo�ka menu nov� projekt.
	 */
	private JMenuItem menuItemNewProject;
	/**
	 * Polo�ka menu otev��t projekt.
	 */
	private JMenuItem menuItemOpenProject;
	/**
	 * Polo�ka menu zav��t projekt.
	 */
	private JMenuItem menuItemCloseProject;
	/**
	 * Polo�ka menu ulo�it projekt.
	 */
	private JMenuItem menuItemSaveProject;
	/**
	 * Polo�ka menu ulo�it jako projekt.
	 */
	private JMenuItem menuItemSaveAsProject;
	/**
	 * Polo�ka menu export do obr�zku.
	 */
	private JMenuItem menuItemExportImage;
	/**
	 * Polo�ka menu skon�it program.
	 */
	private JMenuItem menuItemExit;
	/**
	 * Menu volba zobrazen�.
	 */
	private JMenu displayModeMenu;
	/**
	 * Polo�ka menu zobrazen� kombinace n�rysu, p�dorysu, bokorysu a perspektivn�ho pohledu.
	 */
	private JRadioButtonMenuItem menuItemDisplayCombine;
	/**
	 * Polo�ka menu zobrazen� n�rysu - pohledu zep�edu.
	 */
	private JRadioButtonMenuItem menuItemDisplayFront;
	/**
	 * Polo�ka menu pro nastaven� re�imu editace.
	 */
	private JCheckBoxMenuItem menuItemEditingMode;
	/**
	 * Polo�ka menu pro volbu zobrazen�/skryt� os.
	 */
	private JCheckBoxMenuItem menuItemShowAxes;
	/**
	 * Polo�ka menu pro volbu zobrazen� informa�n�ho textu.
	 */
	private JCheckBoxMenuItem menuItemShowInformationText;
	/**
	 * Polo�ka menu pro volbu zobrazen� okony orientace kamery.
	 */
	private JCheckBoxMenuItem menuItemShowOrientationIcon;
	/**
	 * Polo�ka menu pro otev�en� okna edita�n�ch uzl�.
	 */
	private JMenuItem menuItemShowNodesFrame;
	/**
	 * Seskupen� voleb zobrazen�.
	 */
	private ButtonGroup displayModeMenuGroup;
	/**
	 * Polo�ka menu pro zobrazen� okna s nastaven�m.
	 */
	private JMenuItem menuItemSettings;
	/**
	 * Seznam menu pro p�id�n� objektu do projektu.
	 */
	private List<JMenu> menuObjectAdd = new LinkedList<JMenu>();
	/**
	 * Tla��tko pro zav�en� otev�en�ho souboru.
	 */
	private JButton projectCloseButton;
	/**
	 * Poslucha�i zm�n v projektu. Pro ka�d� projekt je jedna instance.
	 */
	private ArrayList<ProjectListener> projectListeners = new ArrayList<ProjectListener>();
	/**
	 * Pro ka�d� projekt je vytvo�en jeden anim�tor, k objektu je p�istupov�no z v�ce vl�ken.
	 */
	private final List<FPSAnimator> animators = Collections.synchronizedList(new LinkedList<FPSAnimator>());
	/**
	 * List existuj�c�ch objekt�.
	 */
	private JList objectList;
	/**
	 * Pole pro nastaven� jm�na objektu.
	 */
	private JTextField objectNameField;
	/**
	 * Panel s promn�n�m GUI, GUI se m�n� dle typu vybran�ho objektu, GUI je umplementov�no p��mo v t��d� objektu.
	 */
	private JPanel objectDependPropertiesPanel;
	/**
	 * Label zobrazuj�c� informativn� hl�ky objekt�.
	 */
	private JLabel statusMessage;
	/**
	 * Spodn� panel pro informativn� hl�ky.
	 */
	private JPanel statusBar;
	/**
	 * Poslucha� v�ech menu za ��elem opravy zobrazen� menu p�e GLCanvas.
	 */
	private MenusListener menusListener;
	/**
	 * Panel akc� s objekty - se�azen�, viditelnost.
	 */
	private ObjectOrderPanel objectOrderPanel;
	/**
	 * Aktu�ln� zobrazen� projekt.
	 */
	private Project currentProject;

	public MainFrame(Model model)
	{
		this.model = model;
		this.editorListener = new EditorListenerImpl();//jeden poslucha� pro v�echny editory
		Project[] projects = model.getProjects();//z�sk�m projekty
		Project workingProject = model.getWorkingProject();
		//odstran�m star� projety
		for (Project p : projects)
		{
			model.removeProject(p);
		}

		model.addModelListener(new ModelListener());//p�id�n� poslucha�e ud�lost� v modelu
		init();

		//znovu vlo��m star� projety
		for (Project p : projects)
		{
			model.addProject(p);
		}

		model.setWorkingProject(workingProject);

		if (editorPanel.getSelectedIndex() != -1)//zajist�m aby zobrazen� projekt byl opravdu vybran�
		{
			model.setWorkingProject(editorPanel.getSelectedIndex());
		}
	}

	/**
	 * P�id�n� poslucha�e okna.
	 */
	public void addListener(MainFrameListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e okna.
	 */
	public void removeListener(MainFrameListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Inicializace okna, vytvo�en� komponent GUI.
	 */
	private void init()
	{
		setTitle("K�ivky a plochy");
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
		mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);//vertik�ln� rozd�len� hlavn�ho panelu
		mainPanel.add(mainSplitPanel);
		initLeftColumn();
		initRightColumn();
		initStatusBar();
	}

	/**
	 * Vytvo�en� ��sti GUI lev�ho sloupce obsahuj�c� prvky pro nastaven� vlastnost� objektu.
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
	 *  Vytvo�en� ��sti GUI prav�ho sloupce obsahuj�c� p�edev��m OpenGL okno.
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
	 * Vytvo�en� status baru - obsahuje chybov� hl�ky.
	 */
	private void initStatusBar()
	{
		statusMessage = new JLabel();
		statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SystemColor.windowBorder));
		statusBar.add(new JLabel(" "));//slou�� aby se status bar nesmrskl kdy� statusMessage nem� text
		statusBar.add(statusMessage);
		mainPanel.add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * Vytvo�en� GUI vlastnost� objektu.
	 */
	private void initObjectPropertiesPanel()
	{
		objectPropertiesPanel = new JPanel(new GridBagLayout());
		objectPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Vlastnosti objektu"));
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		objectPropertiesPanel.add(new JLabel("Jm�no "), gbc);

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

		//poslucha� zm�ny textu v poli n�zvu objektu
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
	 * Vytvo�en� GUI seznamu existuj�c�ch objekt�.
	 */
	private void initObjectListPanel()
	{
		objectListPanel = new JPanel(new GridBagLayout());
		objectListPanel.setBorder(BorderFactory.createTitledBorder("Seznam objekt�"));
		//seznam objekt�
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
	 * Vytvo�en� GUI vlastnost� bodu.
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
	 * Vytvo�en� GUI pro zobrazen� a editaci objektu v prostoru.
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
	 * Vytvo�en� hlavn�ho menu.
	 */
	private void initMainMenu()
	{
		mainMenuBar = new JMenuBar();
		setJMenuBar(mainMenuBar);
		//menu soubor
		fileMenu = new JMenu("Projekt");
		mainMenuBar.add(fileMenu);
		menuItemNewProject = new JMenuItem("Nov� projekt");
		menuItemOpenProject = new JMenuItem("Otev��t projekt");
		menuItemCloseProject = new JMenuItem("Zav��t projekt");
		menuItemSaveProject = new JMenuItem("Ulo�it projekt");
		menuItemSaveAsProject = new JMenuItem("Ulo�it jako");
		menuItemExportImage = new JMenuItem("Exportovat obr�zek");
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
		displayModeMenu = new JMenu("Zobrazen�");
		mainMenuBar.add(displayModeMenu);
		menuItemDisplayCombine = new JRadioButtonMenuItem("Kombinace");
		menuItemDisplayFront = new JRadioButtonMenuItem("Jeden pohled");
		menuItemEditingMode = new JCheckBoxMenuItem("Re�im editace");
		menuItemShowAxes = new JCheckBoxMenuItem("Vykreslit osy");
		menuItemShowInformationText = new JCheckBoxMenuItem("Vykreslit informa�n� text");
		menuItemShowOrientationIcon = new JCheckBoxMenuItem("Vykreslit orienta�n� ikonu");
		menuItemShowNodesFrame = new JMenuItem("Otev��t seznam bod�");
		menuItemSettings = new JMenuItem("Nastaven�");

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


		//menu pro p�id�n� objekt�
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
					//poslucha�
					submenuItemGUI.addActionListener(new MenuObjectAddActionListener(submenuItem.second));
				}
			}
			mainMenuBar.add(submenuGUI);
			menuObjectAdd.add(submenuGUI);
		}

		//tla��tko zav�en� projektu
		mainMenuBar.add(Box.createHorizontalGlue());
		projectCloseButton = new JButton(new ImageIcon(getClass().getResource("close.png")));
		projectCloseButton.setToolTipText("Zav��t projekt");
		mainMenuBar.add(projectCloseButton);

		//p�id�n� poslucha��
		initMainMenuListeners();
	}

	/**
	 * P�id�n� poslucha�� k tla��tk� v hlavn�m menu
	 */
	private void initMainMenuListeners()
	{
		//zav�en� projektu
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
		//nov� projekt
		menuItemNewProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireNewProject();
			}
		});
		//export do obr�zku
		menuItemExportImage.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{

				final Editor editor = (Editor) editorPanel.getSelectedComponent();
				//vl�kno je� po�k� na vygenerov�n� obr�zku
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
		//ulo�en� projektu
		menuItemSaveProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSaveProject(currentProject);
			}
		});
		//ulo�en� projektu
		menuItemSaveAsProject.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSaveAsProject(currentProject);
			}
		});
		//otev�en� projektu
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
		//volba zobrazen�
		menuItemDisplayCombine.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				fireSetViewportCombination(currentProject, menuItemDisplayCombine.isSelected());
			}
		});
		//re�im editace
		menuItemEditingMode.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetEditingMode(currentProject, menuItemEditingMode.isSelected());
				repairGLShow();
			}
		});
		//zobrazen� os
		menuItemShowAxes.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetShowAxes(currentProject, menuItemShowAxes.isSelected());
				repairGLShow();
			}
		});
		//zobrazen� inf. textu
		menuItemShowInformationText.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetShowInformationText(currentProject, menuItemShowInformationText.isSelected());
				repairGLShow();
			}
		});
		//zobrazen� orientace kamery
		menuItemShowOrientationIcon.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireSetOrientationIcon(currentProject, menuItemShowOrientationIcon.isSelected());
				repairGLShow();
			}
		});
		//zobrazen� okna editace bod�
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
		//zobrazen� okna s nastaven�m
		menuItemSettings.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				fireOpenSettingsFrame();
				repairGLShow();
			}
		});
		//p�id�m poslucha�e v�ech submenu-rekurzivn� proch�zen�
		menusListener = new MenusListener();
		menuAddListeners(mainMenuBar, menusListener);
	}

	/**
	 * Rekurzivn� p�id�n� poslucha�� k menu.
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
	 * Hromadn� povol� v�echny GUI prvky kter� se t�kaj� projektu:<br />
	 * -vlastnosti bodu, vlastnosti objektu, objekty seznam, tla��tko pro zav�en� projektu, prvky v menu.
	 * @param enabled True pro povolen�
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
		//submenu pro p�id�n� objektu
		for (JMenu menu : menuObjectAdd)
		{
			menu.setEnabled(enabled);
		}
	}

	/**
	 * Nastav� prvky GUI pro projekt.
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
	 * Nastav� GUI prvky t�kaj�c� se seznamu objekt� v projektu.
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
	 * Nastaven� panelu pro p�esun objekt� projektu.
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
	 * Nastav� prvky GUI pro objekt.
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
			//GUI pro nastaven� bodu
			setPointGUIto(project);
		} else
		{
			//setStatusMessage(selectedObject.getStateMessage());
			objectDependPropertiesPanel.setLayout(new BorderLayout());
			objectNameField.setEnabled(true);
			String name = selectedObject.getName();
			if (!objectNameField.getText().equals(name))//pokud do�lo ke zm�n� n�zvu
			{
				objectNameField.setText(name);
			}
			//GUI generovane p��mo objektem
			if (selectedObject.getObjectGUI() != null)
			{
				objectDependPropertiesPanel.add(selectedObject.getObjectGUI(), BorderLayout.CENTER);
			}
			//GUI pro nastaven� bodu
			setPointGUIto(project);
			objectDependPropertiesPanel.updateUI();
		}
		setObjectOrderGUIto(project);
		repairGLShow();
	}

	/**
	 * Nastaven� GUI pro editaci bodu pro vybran� body.
	 */
	private void setPointGUIto(Project project)
	{
		List<IPoint3f> selectedPoints = null;
		IObject selectedObject = null;
		pointDependPropertiesPanel.removeAll();//odeberu panel vlastnost� bodu generovan� objektem
		if (project != null && (selectedObject = project.getSelectedObject()) != null)
		{
			selectedPoints = selectedObject.getSelectedPoints();
			Component pointPorpertiesPnael = selectedObject.getPointGUI();
			if (pointPorpertiesPnael != null)
			{
				//p�id�m panel vlastnost� bodu generovan� objektem
				pointDependPropertiesPanel.add(pointPorpertiesPnael, BorderLayout.CENTER);
			}
		}
		pointPropertiesPanel.setPoint((selectedPoints == null || selectedPoints.size() != 1) ? null : selectedPoints.get(0));
		objectDependPropertiesPanel.updateUI();
	}

	/**
	 * Nastaven� status textu.
	 * @param stateMessage Pokud zad�te null, pak se status text vynuluje
	 */
	private void setStatusMessage(String stateMessage)
	{
		statusMessage.setText(stateMessage);
		repairGLShow();
	}

	/**
	 * Odstran� chybu, kdy GLCanvas komponenta p�ekresluje menu, a menu nelze vid�t.
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
		//zastav�m v�echny OpenGL panely
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
	 * Vyvol�n� ud�losti ukon�en� aplikace vyvolan� u�ivatelem v GUI.
	 */
	protected void fireExit()
	{
		for (MainFrameListener l : listeners)
		{
			l.eventExit();
		}
	}

	/**
	 * Vyvol�n� ud�losti zav�en� projektu.
	 */
	protected void fireCloseProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventCloseProject(project);
		}
	}

	/**
	 * Vyvol�n� ud�losti vytvo�en� projektu.
	 */
	protected void fireNewProject()
	{
		for (MainFrameListener l : listeners)
		{
			l.eventNewProject();
		}
	}

	/**
	 * Vyvol�n� ud�losti nastaven� re�imu zobrazen� mezi 4 pohledy a jedn�m.
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
	 * Vyvol�n� ud�losti nastaven� pracovn�ho projektu.
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
	 * Vyvol�n� ud�losti nastaven� vybran�ho objektu.
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
	 * Vyvol�n� ud�losti relativn�ho posunu bodu.
	 * @param project Projekt ve kter�m se nach�z� bod.
	 * @param point Bod pro p�esun.
	 * @param x Zm�na x.
	 * @param y Zm�na y.
	 * @param z Zm�na z.
	 */
	protected void fireMovePointRelative(Project project, IObject object, IPoint3f point, float x, float y, float z)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMovePointRelative(project, object, point, x, y, z);
		}
	}

	/**
	 * Vyvol�n� ud�losti relativn�ho posunu bod�.
	 * @param project Projekt ve kter�m se nach�z� bod.
	 * @param points Body pro p�esun.
	 * @param x Zm�na x.
	 * @param y Zm�na y.
	 * @param z Zm�na z.
	 */
	private void fireMovePointsRelative(Project project, IObject object, List<IPoint3f> points, float x, float y, float z)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMovePointsRelative(project, object, points, x, y, z);
		}
	}

	/**
	 * Vyvol�n� ud�losti posunu bodu na pozici.
	 * @param project Projekt ve kter�m se nach�z� bod.
	 * @param point Bod pro p�esun.
	 */
	protected void fireMovePointAbsolute(Project project, IObject object, IPoint3f point, float x, float y, float z)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMovePointAbsolute(project, object, point, x, y, z);
		}
	}

	/**
	 * Vyvol�n� ud�losti odebr�n� ��d�c�ch bod� objektu.
	 */
	protected void fireDeleteSelectedPointsFromObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventDeleteSelectedPointsFromObject(project, object);
		}
	}

	/**
	 * Ud�lost p�id�n� nov�ho bodu.
	 */
	protected void fireAddPoint(Project project, IObject object, IPoint3f point)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventAddPoint(project, object, point);
		}
	}

	/**
	 * Ud�lost nastaven� jm�na objektu.
	 */
	protected void fireSetObjectName(Project project, IObject object, String text)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventObjectRename(project, object, text);
		}
	}

	/**
	 * Ud�lost p�id�n� nov�ho objektu.
	 */
	protected void fireAddObject(Project project, String className)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventAddObject(project, className);
		}
	}

	/**
	 * Ud�lost posunut� vybran�ho objektu nahoru.
	 */
	protected void fireMoveUpObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMoveUpObject(project, object);
		}
	}

	/**
	 * Ud�lost posunut� vybran�ho objektu dol�.
	 */
	protected void fireMoveDownObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMoveDownObject(project, object);
		}
	}

	/**
	 * Ud�lost nastaven� viditelnosti vybran�ho objektu.
	 */
	protected void fireSetObjectVisibility(Project project, IObject object, boolean visible)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetObjectVisibility(project, object, visible);
		}
	}

	/**
	 * Ud�lost nastaven� exluzivn� viditelnosti vybran�ho objektu.
	 */
	protected void fireSetObjectExlusiveVisibility(Project project, IObject object, boolean visible)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetObjectExlusiveVisibility(project, object, visible);
		}
	}

	/**
	 * Ud�lost odstran�n� objektu.
	 */
	protected void fireRemoveObject(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventRemoveObject(project, object);
		}
	}

	/**
	 * Ud�lost rotace objektu.
	 * @param pivot st�ed ot��en�.
	 * @param axes osa ot��en�.
	 * @param angle �hel ve stupn�ch.
	 */
	protected void fireRotateObject(Project project, IObject object, IPoint3f pivot, IPoint3f axes, float angle)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventRotateObject(project, object, pivot, axes, angle);
		}
	}

	/**
	 * Ud�lost posunu objektu.
	 * @param vector Vektor p�esunu objektu.
	 */
	protected void fireMoveObject(Project project, IObject object, IPoint3f vector)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventMoveObject(project, object, vector);
		}
	}

	/**
	 * Ud�lost nastaven� edita�n�ho re�imu.
	 */
	protected void fireSetEditingMode(Project project, boolean editingMode)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetEditingMode(project, editingMode);
		}
	}

	/**
	 * Ud�lost exportu do obr�zku.
	 */
	protected void fireExportImage(Project project, BufferedImage image)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventExportImage(project, image);
		}
	}

	/**
	 * Ud�lost otev�en� projektu..
	 */
	protected void fireOpenProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventOpenProject();
		}
	}

	/**
	 * Ud�lost ulo�en� projektu.
	 */
	protected void fireSaveProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSaveProject(project);
		}
	}

	/**
	 * Ud�lost ulo�en� projektu jako.
	 */
	protected void fireSaveAsProject(Project project)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSaveAsProject(project);
		}
	}

	/**
	 * Ud�lost nastaven� zobrazen� os.
	 */
	protected void fireSetShowAxes(Project project, boolean showAxes)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventShowAxes(project, showAxes);
		}
	}

	/**
	 * Ud�lost editace bodu.
	 */
	protected void fireEditPoint(Project project, IObject object, IPoint3f oldPoint, IPoint3f newPoint)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventEditPoint(project, object, oldPoint, newPoint);
		}
	}

	/**
	 * Ud�lost ud�lost nastaven� v�b�ru bod�.
	 */
	protected void fireSetPointsSelectionByIndex(Project project, IObject object, int[] selectedPoints)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetSelectedPointByIndex(project, object, selectedPoints);
		}
	}

	/**
	 * Ud�lost zru�en� v�b�ru bod�.
	 */
	private void fireCancelSelectedPoints(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventCancelPointsSelection(project, object);
		}
	}

	/**
	 * Ud�lost p�id�n� bodu do v�b�ru.
	 */
	private void fireAddPointToSelection(Project project, IObject object, IPoint3f point)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventAddPointToSelection(project, object, point);
		}
	}

	/**
	 * Ud�lost nastaven� bodu do v�b�ru.
	 */
	private void fireSetPointAsSelection(Project project, IObject object, IPoint3f point)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventSetPointAsSelection(project, object, point);
		}
	}

	/**
	 * Ud�lost nastaven� zobrazen� informa�n�ho textu.
	 */
	protected void fireSetShowInformationText(Project project, boolean showInformationText)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventShowInformationText(project, showInformationText);
		}
	}

	/**
	 * Ud�lost nastaven� orienta�n� ikony kamery.
	 */
	protected void fireSetOrientationIcon(Project project, boolean showOrientationIcon)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventShowOrientationIcon(project, showOrientationIcon);
		}
	}

	/**
	 * Ud�lost otev�en� okna editace ��d�c�ch bod�.
	 */
	private void fireOpenNodesFrame(Project project, IObject object)
	{
		for (MainFrameListener l : listeners)
		{
			l.eventOpenNodesFrame(project, object);
		}
	}

	/**
	 * Ud�lost otev�en� okna nastaven�.
	 */
	private void fireOpenSettingsFrame()
	{
		for (MainFrameListener l : listeners)
		{
			l.eventOpenSettingsFrame();
		}
	}

	/**
	 * T��da obsluhuj�c� ud�losti zm�n v projektech. Pro ka�d� projekt je vytvo�ena jedna instance t�to t��dy.
	 */
	private class ProjectListener extends ProjectAdapter
	{

		/**
		 * Projekt kter� je sledov�n
		 */
		protected Project project;
		/**
		 * Index projektu v z�lo�k�ch
		 */
		protected int index;
		/**
		 * Poslucha� zm�n v objektech tohoto projektu, jeden pro v�echny
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
		 * Z�sk�n� poslucha�e objekt� v tomto projektu.
		 */
		public ObjectListener getObjectListener()
		{
			return objectListener;
		}
	}

	/**
	 * T��da obsluj�c� ud�losti v modelu.
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
				//p�epnu na z�lo�ku s nov�m projektem
				editorPanel.setSelectedIndex(editorPanel.getTabCount() - 1);
				//nastavim poskucha�e v�ech objekt� v projektu
				for (IObject o : project.getObjects())
				{
					o.addObjectListener(projectListener.getObjectListener());
				}
				//skryju a znovu zobrazim hlavn� menu p�i ka�d� zm�n� velikosti editoru,
				//kvuli chyb� v po�ad� vykreslen�-editor by jinak p�ekr�val menu
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
				  "Nejsou dostupn� knihovny OpenGL pro v� syst�m. Program bude ukon�en.",
				  "Chyba",
				  JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			} catch (GLException ex)
			{
				model.debugPrintException(ex);
				JOptionPane.showMessageDialog(MainFrame.this,
				  "Nelze inicializovat OpenGL. Program bude ukon�en.",
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
			//zastaven� a odebr�n� anim�toru
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
			//spu�t�n� anim�toru pouze zobrazen�ho projektu
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
	 * T��da obsluhuj�c� zm�ny v objektu, pou��v� se jedna instance pro v�echny objekty z projektu.
	 */
	private class ObjectListener extends ObjectAdapater
	{

		/**
		 * Projekt jeho� objekty poslouch�m.
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
	 * Poslucha� v�ech menu, jeho ukolem je p�ed zobrazen�m menu zajistit aby menu bylo vid�t p�es GLJPanel.
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
	 * Poslucha� menu polo�ek pro vytvo�en� nov�ho objektu.
	 * P�i akci vyvolan� sledovanou polo�kou, dojde k vytvo�en� nov�ho objektu dan� t��dy.
	 */
	private class MenuObjectAddActionListener implements ActionListener
	{

		private String className;

		/**
		 * @param className N�zev t��dy objektu je� m� b�t vytvo�en.
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
	 * Poslucha� editor�.
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
