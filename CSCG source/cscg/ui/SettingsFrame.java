package cscg.ui;

import cscg.model.DisplayOptions;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventObject;
import java.util.LinkedList;
import javax.media.opengl.GL2;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Okno pro editaci nastavení.
 * @author Tomáš Režnar
 */
public class SettingsFrame extends JFrame
{

	/**
	 * Posluchači změn nastavení.
	 */
	private LinkedList<SettingsListener> listeners = new LinkedList<SettingsListener>();
	/**
	 * Editované nastavení zobrazení.
	 */
	private DisplayOptions displayOptions = null;
	/**
	 * Původní nastavení zobrazení.
	 */
	private DisplayOptions displayOptionsRevert = null;
	/**
	 * Tlačítko potvrzení změn.
	 */
	private final JButton okButton;
	/**
	 * Tlačítko zrušení změn.
	 */
	private final JButton cancelButton;
	/**
	 * Prvek pro nastaveí antialiasingu bodů.
	 */
	private final JComboBox pointSmooth;
	/**
	 * Prvek pro nastaveí antialiasingu linek.
	 */
	private final JComboBox lineSmooth;
	/**
	 * Prvek pro nastaveí antialiasingu polygonů.
	 */
	private final JComboBox polygonSmooth;
	/**
	 * Prvek pro nastaveí antialiasingu perspeKtivy.
	 */
	private final JComboBox perspectiveSmooth;
	/**
	 * Prvek volby vertikální synchronizace.
	 */
	private final JCheckBox vsync;
	/**
	 * Prvek volby povolení testování hloubky.
	 */
	private final JCheckBox depthTest;
	/**
	 * Prvek volby ambientní složky světla.
	 */
	private final JSlider ambientLight;
	/**
	 * Prvek volby difůzní složky světla.
	 */
	private final JSlider difuseLight;
	/**
	 * Prvek volby odleskové složky světla.
	 */
	private final JSlider specularLight;
	/**
	 * Prvek volby míry odlesků.
	 */
	private final JSlider specularShininessLight;

	public SettingsFrame()
	{
		super("Nastavení");
		setLayout(new GridBagLayout());
		GridBagConstraints gbc;

		//vytvoření GUI

		/*
		 * Zobrazení
		 */
		JPanel displayPanel = new JPanel(new GridBagLayout());
		displayPanel.setBorder(BorderFactory.createTitledBorder("Nastavení zobrazení"));
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;

		//antialiasing bodů
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Antialiasing bodů"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		pointSmooth = new JComboBox(new String[]
		  {
			  "Vypnut", "Rychlý", "Přesný"
		  });
		displayPanel.add(pointSmooth, gbc);

		//antialiasing linek
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Antialiasing linek"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		lineSmooth = new JComboBox(new String[]
		  {
			  "Vypnut", "Rychlý", "Přesný"
		  });
		displayPanel.add(lineSmooth, gbc);

		//antialiasing polygonů
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Antialiasing polygonů"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		polygonSmooth = new JComboBox(new String[]
		  {
			  "Vypnut", "Rychlý", "Přesný"
		  });
		displayPanel.add(polygonSmooth, gbc);

		//antialiasing perspektivy
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Antialiasing perspektivy"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		perspectiveSmooth = new JComboBox(new String[]
		  {
			  "Vypnut", "Rychlý", "Přesný"
		  });
		displayPanel.add(perspectiveSmooth, gbc);

		//vsync
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Vertikální synchronizace"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		vsync = new JCheckBox();
		displayPanel.add(vsync, gbc);

		//test hloubky
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Test hloubky"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		depthTest = new JCheckBox();
		displayPanel.add(depthTest, gbc);

		//ambientní světlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Ambientní složka světla"), gbc);

		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		ambientLight = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		ambientLight.setMajorTickSpacing(20);
		ambientLight.setMinorTickSpacing(5);
		ambientLight.createStandardLabels(20);
		ambientLight.setPaintLabels(true);
		ambientLight.setPaintTicks(true);
		displayPanel.add(ambientLight, gbc);

		//difůzní světlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Difůzní složka světla"), gbc);

		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		difuseLight = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		difuseLight.setMajorTickSpacing(20);
		difuseLight.setMinorTickSpacing(5);
		difuseLight.createStandardLabels(20);
		difuseLight.setPaintLabels(true);
		difuseLight.setPaintTicks(true);
		displayPanel.add(difuseLight, gbc);

		//odleskové světlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Odlesková složka světla"), gbc);

		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		specularLight = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		specularLight.setMajorTickSpacing(20);
		specularLight.setMinorTickSpacing(5);
		specularLight.createStandardLabels(20);
		specularLight.setPaintLabels(true);
		specularLight.setPaintTicks(true);
		displayPanel.add(specularLight, gbc);

		//difůzní světlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Míra odlesků"), gbc);

		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		specularShininessLight = new JSlider(JSlider.HORIZONTAL, 0, 128, 0);
		specularShininessLight.setMajorTickSpacing(32);
		specularShininessLight.setMinorTickSpacing(16);
		specularShininessLight.createStandardLabels(32);
		specularShininessLight.setPaintLabels(true);
		specularShininessLight.setPaintTicks(true);
		displayPanel.add(specularShininessLight, gbc);


		/*
		 * Tlačítka
		 */
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		okButton = new JButton("Uložit");
		cancelButton = new JButton("Storno");
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(okButton);

		/*
		 * Vložení panelů
		 */
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		add(displayPanel, gbc);

		gbc.gridy = 1;
		gbc.weighty = 0;
		add(buttonsPanel, gbc);

		/*
		 * Posluchači
		 */
		initListeners();

		this.pack();//velikost okna
	}

	/**
	 * Získání objektu nastavení který je upravován oknem.
	 */
	public DisplayOptions getSettingsObject()
	{
		return displayOptions;
	}

	/**
	 * Získání kopie původního nastavení jaké bylo před voláním metody 
	 * {@link #setSettingsObject(cscg.model.DisplayOptions)}.
	 * @return Klon původního objektu nastavení.
	 */
	public DisplayOptions getSettingsObjectUnchanged()
	{
		return displayOptionsRevert;
	}

	/**
	 * Registrace posluchačů GUI prvků.
	 */
	private void initListeners()
	{
		/*
		 * tlačítka
		 */
		okButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (SettingsListener l : listeners)
				{
					l.eventConfirmed(new EventObject(SettingsFrame.this));
				}
			}
		});

		cancelButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (SettingsListener l : listeners)
				{
					l.eventCanceled(new EventObject(SettingsFrame.this));
				}
			}
		});

		//tlačítko pro zavření okna - stejná funkce jako storno
		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(WindowEvent e)
			{
				for (SettingsListener l : listeners)
				{
					l.eventCanceled(new EventObject(SettingsFrame.this));
				}
			}
		});

		/*
		 * nastavení zobrazení
		 */
		pointSmooth.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayOptions.setPointSmooth(smoothIndexToConstant(pointSmooth.getSelectedIndex()));
			}
		});
		lineSmooth.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayOptions.setLineSmooth(smoothIndexToConstant(lineSmooth.getSelectedIndex()));
			}
		});
		polygonSmooth.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayOptions.setPolygonSmooth(smoothIndexToConstant(polygonSmooth.getSelectedIndex()));
			}
		});
		perspectiveSmooth.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayOptions.setPerspectiveCorection(smoothIndexToConstant(perspectiveSmooth.getSelectedIndex()));
			}
		});
		vsync.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayOptions.setVsync(vsync.isSelected());
			}
		});
		depthTest.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayOptions.setDepthTest(depthTest.isSelected());
			}
		});
		ambientLight.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				displayOptions.setAmbientLight(ambientLight.getValue() * 0.01f);
			}
		});
		difuseLight.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				displayOptions.setDifuseLight(difuseLight.getValue() * 0.01f);
			}
		});
		specularLight.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				displayOptions.setSpecularLight(specularLight.getValue() * 0.01f);
			}
		});
		specularShininessLight.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				displayOptions.setSpecularLightShininess(specularShininessLight.getValue());
			}
		});
	}

	/**
	 * Převede index v comboboxu pro výběr antialiasingu na konstantu OpenGL2.
	 * 0=>0, 1=>GL2.GL_FASTEST, 2=>GL2.GL_NICEST.
	 */
	private int smoothIndexToConstant(int index)
	{
		switch (index)
		{
			case 2:
				return GL2.GL_NICEST;
			case 1:
				return GL2.GL_FASTEST;
			case 0:
			default:
				return 0;
		}
	}

	/**
	 * Převede konstantu OpenGL pro výběr antialiasingu na index v comboboxu.
	 * 0=>0, GL2.GL_FASTEST=>1, GL2.GL_NICEST=>2.
	 */
	private int smoothConstantToIndex(int constant)
	{
		switch (constant)
		{
			case GL2.GL_NICEST:
				return 2;
			case GL2.GL_FASTEST:
				return 1;
			case 0:
			default:
				return 0;
		}
	}

	/**
	 * Nastavení okna na editování daného objektu.
	 * @param displayOptions Nastavení zobrazení jež bude editováno.
	 */
	public void setSettingsObject(DisplayOptions displayOptions)
	{
		if (displayOptions == null)
		{
			throw new NullPointerException();
		}
		this.displayOptions = displayOptions;
		this.displayOptionsRevert = (DisplayOptions) displayOptions.clone();
		updateGUI();
	}

	/**
	 * Vrátí aktuální objekt editavného nastavení zobrazení.
	 */
	public DisplayOptions getDisplayOptionsSettings()
	{
		return displayOptions;
	}

	/**
	 * Přidání posluchače nastavení.
	 */
	public void addListener(SettingsListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchače nastavení.
	 */
	public void removeListener(SettingsListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Nastaví GUI prvky aby reflektovali editovaný objekt nastavení .
	 */
	private void updateGUI()
	{
		pointSmooth.setSelectedIndex(smoothConstantToIndex(displayOptions.getPointSmooth()));
		lineSmooth.setSelectedIndex(smoothConstantToIndex(displayOptions.getLineSmooth()));
		polygonSmooth.setSelectedIndex(smoothConstantToIndex(displayOptions.getPolygonSmooth()));
		perspectiveSmooth.setSelectedIndex(smoothConstantToIndex(displayOptions.getPerspectiveCorection()));
		vsync.setSelected(displayOptions.isVsync());
		depthTest.setSelected(displayOptions.isDepthTest());
		ambientLight.setValue((int) (100 * displayOptions.getAmbientLight()));
		difuseLight.setValue((int) (100 * displayOptions.getDifuseLight()));
		specularLight.setValue((int) (100 * displayOptions.getSpecularLight()));
		specularShininessLight.setValue((int) displayOptions.getSpecularLightShininess());
	}
}
