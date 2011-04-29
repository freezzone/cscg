package cscg.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.MaskFormatter;

/**
 * GUI komponenta pro výběr barvy. Komponenta je tlačítko, jež má aktuálně nastavenou barvu a po jeho kliknutí
 * se zobrazí dialog pro výběr barvy.
 * @author Tomáš Režnar
 */
public class ColorSelector extends JPanel {

	/**
	 * Box jehož pozadí zobrazí aktuální barvu
	 */
	Component colorRect;
	/**
	 * Textové pole pro zadáví barvy ve formátu #rrggbb
	 */
	JFormattedTextField htmlValue;
	/**
	 * Posluchači změny barvy
	 */
	protected ArrayList<ActionListener> actionListeners=new ArrayList<ActionListener>();



	/**
	 * Vytvoří výběr barvy s tlačítkem pro volbu barvy a textovým vstupem pro zadání ve formátu #rrggbb.
	 */
	public ColorSelector()
	{
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createLineBorder(SystemColor.controlDkShadow, 1));
		GridBagConstraints gbc = new GridBagConstraints();

		colorRect=Box.createRigidArea(new Dimension(20, 10));
		colorRect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(colorRect,gbc);

		gbc.gridx=1;
		try
		{
			htmlValue = new JFormattedTextField(new MaskFormatter("'#HHHHHH"));
			htmlValue.setBorder(null);
		} catch (ParseException ex)
		{}
		add(htmlValue,gbc);

		//posluchači
		colorRect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Color newColor=JColorChooser.showDialog(ColorSelector.this, "Vyberte barvu objektu", getBackground());
				if(newColor!=null)
				{
					setColor(newColor);
				}
			}
		});

		htmlValue.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(htmlValue.isValid())
				{
					setColor(new Color(Integer.parseInt(htmlValue.getText().substring(1), 16)));
				}
			}
		});

		//nastaveni defaultni barvy
		setColor(Color.black);
	}

	/**
	 * Kontruktor s nastavení defaultní barvy.
	 */
	public ColorSelector(Color color)
	{
		this();
		this.setColor(color);
	}

	/**
	 * Nastaví barvu.
	 * @param color Pokud je null, pak se nastaví barva černá.
	 */
	public final void setColor(Color color)
	{
		try
		{
			if(color==null)
			{
				color=Color.black;
			}
			super.setBackground(color);
			htmlValue.setText("#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase());
			for(ActionListener l:actionListeners)
			{
				l.actionPerformed(new ActionEvent(this, 1, null));
			}
		}
		//kvůli přepsání metody setBackground(Color bg)
		catch(NullPointerException ex)
		{}
	}
	/**
	 * Aktuální nastavená barva.
	 */
	public Color getColor()
	{
		return getBackground();
	}

	@Override
	public void setBackground(Color bg)//přepsána, protože barva pozadí se používa pro uložení nstavené barvy
	{
		setColor(bg);
	}



	/**
	 * Odebere posluchače změny barvy.
	 */
	public void removeActionListener(ActionListener l)
	{
		actionListeners.remove(l);
	}

	/**
	 * Přidá posluchače změny barvy.
	 */
	public void addActionListener(ActionListener l)
	{
		actionListeners.add(l);
	}
}
