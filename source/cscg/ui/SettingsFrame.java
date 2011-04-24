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
 * Okno pro editaci nastaven�.
 * @author Tom� Re�nar
 */
public class SettingsFrame extends JFrame
{

	/**
	 * Poslucha�i zm�n nastaven�.
	 */
	private LinkedList<SettingsListener> listeners = new LinkedList<SettingsListener>();
	/**
	 * Editovan� nastaven� zobrazen�.
	 */
	private DisplayOptions displayOptions = null;
	/**
	 * P�vodn� nastaven� zobrazen�.
	 */
	private DisplayOptions displayOptionsRevert = null;
	/**
	 * Tla��tko potvrzen� zm�n.
	 */
	private final JButton okButton;
	/**
	 * Tla��tko zru�en� zm�n.
	 */
	private final JButton cancelButton;
	/**
	 * Prvek pro nastave� antialiasingu bod�.
	 */
	private final JComboBox pointSmooth;
	/**
	 * Prvek pro nastave� antialiasingu linek.
	 */
	private final JComboBox lineSmooth;
	/**
	 * Prvek pro nastave� antialiasingu polygon�.
	 */
	private final JComboBox polygonSmooth;
	/**
	 * Prvek pro nastave� antialiasingu perspeKtivy.
	 */
	private final JComboBox perspectiveSmooth;
	/**
	 * Prvek volby vertik�ln� synchronizace.
	 */
	private final JCheckBox vsync;
	/**
	 * Prvek volby povolen� testov�n� hloubky.
	 */
	private final JCheckBox depthTest;
	/**
	 * Prvek volby ambientn� slo�ky sv�tla.
	 */
	private final JSlider ambientLight;
	/**
	 * Prvek volby dif�zn� slo�ky sv�tla.
	 */
	private final JSlider difuseLight;
	/**
	 * Prvek volby odleskov� slo�ky sv�tla.
	 */
	private final JSlider specularLight;
	/**
	 * Prvek volby m�ry odlesk�.
	 */
	private final JSlider specularShininessLight;

	public SettingsFrame()
	{
		super("Nastaven�");
		setLayout(new GridBagLayout());
		GridBagConstraints gbc;

		//vytvo�en� GUI

		/*
		 * Zobrazen�
		 */
		JPanel displayPanel = new JPanel(new GridBagLayout());
		displayPanel.setBorder(BorderFactory.createTitledBorder("Nastaven� zobrazen�"));
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;

		//antialiasing bod�
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Antialiasing bod�"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		pointSmooth = new JComboBox(new String[]
		  {
			  "Vypnut", "Rychl�", "P�esn�"
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
			  "Vypnut", "Rychl�", "P�esn�"
		  });
		displayPanel.add(lineSmooth, gbc);

		//antialiasing polygon�
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Antialiasing polygon�"), gbc);
		gbc.weightx = 0;
		gbc.gridx = 1;
		polygonSmooth = new JComboBox(new String[]
		  {
			  "Vypnut", "Rychl�", "P�esn�"
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
			  "Vypnut", "Rychl�", "P�esn�"
		  });
		displayPanel.add(perspectiveSmooth, gbc);

		//vsync
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Vertik�ln� synchronizace"), gbc);
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

		//ambientn� sv�tlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Ambientn� slo�ka sv�tla"), gbc);

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

		//dif�zn� sv�tlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Dif�zn� slo�ka sv�tla"), gbc);

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

		//odleskov� sv�tlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("Odleskov� slo�ka sv�tla"), gbc);

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

		//dif�zn� sv�tlo
		gbc.weightx = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		displayPanel.add(new JLabel("M�ra odlesk�"), gbc);

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
		 * Tla��tka
		 */
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		okButton = new JButton("Ulo�it");
		cancelButton = new JButton("Storno");
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(okButton);

		/*
		 * Vlo�en� panel�
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
		 * Poslucha�i
		 */
		initListeners();

		this.pack();//velikost okna
	}

	/**
	 * Z�sk�n� objektu nastaven� kter� je upravov�n oknem.
	 */
	public DisplayOptions getSettingsObject()
	{
		return displayOptions;
	}

	/**
	 * Z�sk�n� kopie p�vodn�ho nastaven� jak� bylo p�ed vol�n�m metody 
	 * {@link #setSettingsObject(cscg.model.DisplayOptions)}.
	 * @return Klon p�vodn�ho objektu nastaven�.
	 */
	public DisplayOptions getSettingsObjectUnchanged()
	{
		return displayOptionsRevert;
	}

	/**
	 * Registrace poslucha�� GUI prvk�.
	 */
	private void initListeners()
	{
		/*
		 * tla��tka
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

		//tla��tko pro zav�en� okna - stejn� funkce jako storno
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
		 * nastaven� zobrazen�
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
	 * P�evede index v comboboxu pro v�b�r antialiasingu na konstantu OpenGL2.
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
	 * P�evede konstantu OpenGL pro v�b�r antialiasingu na index v comboboxu.
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
	 * Nastaven� okna na editov�n� dan�ho objektu.
	 * @param displayOptions Nastaven� zobrazen� je� bude editov�no.
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
	 * Vr�t� aktu�ln� objekt editavn�ho nastaven� zobrazen�.
	 */
	public DisplayOptions getDisplayOptionsSettings()
	{
		return displayOptions;
	}

	/**
	 * P�id�n� poslucha�e nastaven�.
	 */
	public void addListener(SettingsListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e nastaven�.
	 */
	public void removeListener(SettingsListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Nastav� GUI prvky aby reflektovali editovan� objekt nastaven� .
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
