package cscg.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

/**
 * Panel pro nastavení meta vlastností objektů - seřazení a viditelnost.
 * @author Tomáš Režnar
 */
public class ObjectOrderPanel extends JPanel
{

	/**
	 * Tlačítko pro přesun dolů.
	 */
	private final JButton moveDownButton;
	/**
	 * Tlačítko pro přesun nahoru.
	 */
	private final JButton moveUpButton;
	/**
	 * Tlačítko pro smazání.
	 */
	private final JButton deleteButton;
	/**
	 * Tlačítko pro nastavení jestli se objekt vykreslí.
	 */
	private final JToggleButton visibleButton;
	/**
	 * Tlačítko pro aktivaci vykreslení jediného objektu.
	 */
	private final JToggleButton exlusiveVisibleButton;
	/**
	 * Posluchači událostí panelu.
	 */
	private final LinkedList<ObjectOrderPanelListener> listeners = new LinkedList<ObjectOrderPanelListener>();
	private final JButton rotateXPositiveButton;
	private final JButton rotateXNegativeButton;
	private final JButton rotateYNegativeButton;
	private final JButton rotateYPositiveButton;
	private final JButton rotateZNegativeButton;
	private final JButton rotateZPositiveButton;
	private final JButton moveXNegativeButton;
	private final JButton moveXPositiveButton;
	private final JButton moveYNegativeButton;
	private final JButton moveYPositiveButton;
	private final JButton moveZNegativeButton;
	private final JButton moveZPositiveButton;
	private final JComboBox rotateCenterComboBox;
	private final JSpinner rotateAngleSpinBox;

	public ObjectOrderPanel()
	{
		super();
		//vrchní část
		Dimension buttonSize = new Dimension(32, 24);//velikost tlačítek
		JPanel topPanel = new JPanel(new GridLayout(1, 5, 0, 0));
		moveUpButton = new JButton(new ImageIcon(this.getClass().getResource("up.png")));
		moveUpButton.setToolTipText("Přesunout objekt nahoru.");
		moveUpButton.setPreferredSize(buttonSize);
		topPanel.add(moveUpButton);
		moveDownButton = new JButton(new ImageIcon(this.getClass().getResource("down.png")));
		moveDownButton.setToolTipText("Přesunout objekt dolů.");
		moveDownButton.setPreferredSize(buttonSize);
		topPanel.add(moveDownButton);
		deleteButton = new JButton(new ImageIcon(this.getClass().getResource("delete.png")));
		deleteButton.setToolTipText("Smazat objekt.");
		deleteButton.setPreferredSize(buttonSize);
		topPanel.add(deleteButton);
		visibleButton = new JToggleButton(new ImageIcon(this.getClass().getResource("visibilityNon.png")));
		visibleButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("visibility.png")));
		visibleButton.setToolTipText("Přepnout viditelnost objektu.");
		visibleButton.setPreferredSize(buttonSize);
		topPanel.add(visibleButton);
		exlusiveVisibleButton = new JToggleButton(new ImageIcon(this.getClass().getResource("exlusiveVisibilityNon.png")));
		exlusiveVisibleButton.setSelectedIcon(new ImageIcon(this.getClass().getResource("exlusiveVisibility.png")));
		exlusiveVisibleButton.setToolTipText("Zobrazit pouze tento objekt.");
		exlusiveVisibleButton.setPreferredSize(buttonSize);
		topPanel.add(exlusiveVisibleButton);

		JPanel xPanel, yPanel, zPanel, bPanel;
		Insets buttonMargin = new Insets(1, 1, 1, 1);
		//spodní část


		//rotace
		JPanel rotatePanel = new JPanel(new GridLayout(3, 1, 2, 0));
		rotatePanel.setBorder(BorderFactory.createTitledBorder("Rotace vybraného objektu"));
		//volba středu
		JPanel axesPanel = new JPanel(new BorderLayout(1, 0));
		axesPanel.add(new JLabel("Střed"), BorderLayout.WEST);
		axesPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
		rotateCenterComboBox = new JComboBox(new String[]
		  {
			  "průměrný bod",
			  "střed objektu",
			  "počátek"
		  });
		axesPanel.add(rotateCenterComboBox, BorderLayout.EAST);
		//úhel
		JPanel anglePanel = new JPanel(new BorderLayout(1, 0));
		anglePanel.add(new JLabel("Úhel [°]"), BorderLayout.WEST);
		anglePanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
		rotateAngleSpinBox = new JSpinner(new SpinnerNumberModel(22.5, 0., 360., 0.5));
		anglePanel.add(rotateAngleSpinBox, BorderLayout.EAST);
		//tlačítka rotace
		JPanel rotateButtonsPanel = new JPanel(new GridLayout(1, 3, 2, 0));

		xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		xPanel.add(new JLabel("x"));
		bPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		rotateXNegativeButton = new JButton("-");
		rotateXNegativeButton.setMargin(buttonMargin);
		rotateXPositiveButton = new JButton("+");
		rotateXPositiveButton.setMargin(buttonMargin);
		bPanel.add(rotateXNegativeButton);
		bPanel.add(rotateXPositiveButton);
		xPanel.add(bPanel);

		yPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		yPanel.add(new JLabel("y"));
		bPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		rotateYNegativeButton = new JButton("-");
		rotateYNegativeButton.setMargin(buttonMargin);
		rotateYPositiveButton = new JButton("+");
		rotateYPositiveButton.setMargin(buttonMargin);
		bPanel.add(rotateYNegativeButton);
		bPanel.add(rotateYPositiveButton);
		yPanel.add(bPanel);

		zPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		zPanel.add(new JLabel("z"));
		bPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		rotateZNegativeButton = new JButton("-");
		rotateZNegativeButton.setMargin(buttonMargin);
		rotateZPositiveButton = new JButton("+");
		rotateZPositiveButton.setMargin(buttonMargin);
		bPanel.add(rotateZNegativeButton);
		bPanel.add(rotateZPositiveButton);
		zPanel.add(bPanel);

		rotateButtonsPanel.add(xPanel);
		rotateButtonsPanel.add(yPanel);
		rotateButtonsPanel.add(zPanel);

		rotatePanel.add(anglePanel);
		rotatePanel.add(axesPanel);
		rotatePanel.add(rotateButtonsPanel);

		//přesun
		JPanel movePanel = new JPanel(new GridLayout(1, 3, 2, 0));
		movePanel.setBorder(BorderFactory.createTitledBorder("Přesun vybraného objektu"));

		xPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		xPanel.add(new JLabel("x"));
		bPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		moveXNegativeButton = new JButton("-");
		moveXNegativeButton.setMargin(buttonMargin);
		moveXPositiveButton = new JButton("+");
		moveXPositiveButton.setMargin(buttonMargin);
		bPanel.add(moveXNegativeButton);
		bPanel.add(moveXPositiveButton);
		xPanel.add(bPanel);

		yPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		yPanel.add(new JLabel("y"));
		bPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		moveYNegativeButton = new JButton("-");
		moveYNegativeButton.setMargin(buttonMargin);
		moveYPositiveButton = new JButton("+");
		moveYPositiveButton.setMargin(buttonMargin);
		bPanel.add(moveYNegativeButton);
		bPanel.add(moveYPositiveButton);
		yPanel.add(bPanel);

		zPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		zPanel.add(new JLabel("z"));
		bPanel = new JPanel(new GridLayout(1, 2, 0, 0));
		moveZNegativeButton = new JButton("-");
		moveZNegativeButton.setMargin(buttonMargin);
		moveZPositiveButton = new JButton("+");
		moveZPositiveButton.setMargin(buttonMargin);
		bPanel.add(moveZNegativeButton);
		bPanel.add(moveZPositiveButton);
		zPanel.add(bPanel);

		movePanel.add(xPanel);
		movePanel.add(yPanel);
		movePanel.add(zPanel);


		//spojení
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.gridy = 0;
		this.add(topPanel, gbc);
		gbc.gridy = 1;
		this.add(rotatePanel, gbc);
		gbc.gridy = 2;
		this.add(movePanel, gbc);

		setEnabled(false);

		//posluchači
		moveUpButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveUpEvent(new EventObject(ObjectOrderPanel.this));
				}
			}
		});
		moveDownButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveDownEvent(new EventObject(ObjectOrderPanel.this));
				}
			}
		});
		deleteButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.deleteEvent(new EventObject(ObjectOrderPanel.this));
				}
			}
		});
		visibleButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.visibilityChangeEvent(new EventObject(ObjectOrderPanel.this));
				}
			}
		});
		exlusiveVisibleButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.exlusiveVisibilityChangeEvent(new EventObject(ObjectOrderPanel.this));
				}
			}
		});

		//potsluchači tlačítek rotace
		rotateXNegativeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.rotateEvent(new RotateEvent(ObjectOrderPanel.this, RotateEvent.AXE_BY_X, false,
					  getSelectedRotationCenter(), (Double) rotateAngleSpinBox.getValue()));
				}
			}
		});
		rotateXPositiveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.rotateEvent(new RotateEvent(ObjectOrderPanel.this, RotateEvent.AXE_BY_X, true,
					  getSelectedRotationCenter(), (Double) rotateAngleSpinBox.getValue()));
				}
			}
		});
		rotateYNegativeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.rotateEvent(new RotateEvent(ObjectOrderPanel.this, RotateEvent.AXE_BY_Y, false,
					  getSelectedRotationCenter(), (Double) rotateAngleSpinBox.getValue()));
				}
			}
		});
		rotateYPositiveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.rotateEvent(new RotateEvent(ObjectOrderPanel.this, RotateEvent.AXE_BY_Y, true,
					  getSelectedRotationCenter(), (Double) rotateAngleSpinBox.getValue()));
				}
			}
		});
		rotateZNegativeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.rotateEvent(new RotateEvent(ObjectOrderPanel.this, RotateEvent.AXE_BY_Z, false,
					  getSelectedRotationCenter(), (Double) rotateAngleSpinBox.getValue()));
				}
			}
		});
		rotateZPositiveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.rotateEvent(new RotateEvent(ObjectOrderPanel.this, RotateEvent.AXE_BY_Z, true,
					  getSelectedRotationCenter(), (Double) rotateAngleSpinBox.getValue()));
				}
			}
		});

		//posluchači tlačítek pro přesun
		moveXNegativeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveEvent(new MoveEvent(ObjectOrderPanel.this, MoveEvent.MOVE_X, false));
				}
			}
		});
		moveXPositiveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveEvent(new MoveEvent(ObjectOrderPanel.this, MoveEvent.MOVE_X, true));
				}
			}
		});
		moveYNegativeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveEvent(new MoveEvent(ObjectOrderPanel.this, MoveEvent.MOVE_Y, false));
				}
			}
		});
		moveYPositiveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveEvent(new MoveEvent(ObjectOrderPanel.this, MoveEvent.MOVE_Y, true));
				}
			}
		});
		moveZNegativeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveEvent(new MoveEvent(ObjectOrderPanel.this, MoveEvent.MOVE_Z, false));
				}
			}
		});
		moveZPositiveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (ObjectOrderPanelListener l : listeners)
				{
					l.moveEvent(new MoveEvent(ObjectOrderPanel.this, MoveEvent.MOVE_Z, true));
				}
			}
		});
	}

	/**
	 * Vrátí vybraný střed rotace.
	 * @return Jedna z konstant RotateEvent.CENTER_IS_AVERAGE_POINT, RotateEvent.CENTER_IS_MIDDLE_POINT,
	 * RotateEvent.CENTER_IS_ORIGIN.
	 */
	private int getSelectedRotationCenter()
	{
		int selected = rotateCenterComboBox.getSelectedIndex();
		if (selected == 0)
		{
			return RotateEvent.CENTER_IS_AVERAGE_POINT;
		}
		if (selected == 1)
		{
			return RotateEvent.CENTER_IS_MIDDLE_POINT;
		}
		return RotateEvent.CENTER_IS_ORIGIN;
	}

	/**
	 * Nastavení zablokování/povolení panelu.
	 */
	@Override
	public final void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		moveUpButton.setEnabled(enabled);
		moveDownButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
		visibleButton.setEnabled(enabled);
		exlusiveVisibleButton.setEnabled(enabled);

		//tlačítka rotace a přesunu
		rotateXNegativeButton.setEnabled(enabled);
		rotateXPositiveButton.setEnabled(enabled);
		rotateYNegativeButton.setEnabled(enabled);
		rotateYPositiveButton.setEnabled(enabled);
		rotateZNegativeButton.setEnabled(enabled);
		rotateZPositiveButton.setEnabled(enabled);
		rotateAngleSpinBox.setEnabled(enabled);
		rotateCenterComboBox.setEnabled(enabled);

		moveXNegativeButton.setEnabled(enabled);
		moveXPositiveButton.setEnabled(enabled);
		moveYNegativeButton.setEnabled(enabled);
		moveYPositiveButton.setEnabled(enabled);
		moveZNegativeButton.setEnabled(enabled);
		moveZPositiveButton.setEnabled(enabled);
	}

	/**
	 * Povolení tlačítka pro přesun nahoru.
	 */
	public void setUpButtonEnabled(boolean enabled)
	{
		moveUpButton.setEnabled(enabled);
	}

	/**
	 * Povolení tlačítka pro přesun dolů.
	 */
	public void setDownButtonEnabled(boolean enabled)
	{
		moveDownButton.setEnabled(enabled);
	}

	/**
	 * Zjistí jestli je aktivováno tlačítko pro nastavení zobrazení.
	 */
	public boolean isVisibilityButtonSelected()
	{
		return visibleButton.isSelected();
	}

	/**
	 * Přepne aktivování tlačítka pro volbu zobrazení.
	 */
	public void setVisibilityButtonSelected(boolean selected)
	{
		visibleButton.setSelected(selected);
	}

	/**
	 * Zjistí jestli je aktivováno tlačítko pro nastavení exluzivního zobrazení.
	 */
	public boolean isExlusiveVisibilityButtonSelected()
	{
		return exlusiveVisibleButton.isSelected();
	}

	/**
	 * Přepne aktivování tlačítka pro volbu zobrazení.
	 */
	public void setExlusiveVisibilityButtonSelected(boolean selected)
	{
		exlusiveVisibleButton.setSelected(selected);
	}

	/**
	 * Přidání posluchače událostí panelu.
	 */
	public void addEventListener(ObjectOrderPanelListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchače událostí panelu.
	 */
	public void removeEventListener(ObjectOrderPanelListener l)
	{
		listeners.remove(l);
	}
}
