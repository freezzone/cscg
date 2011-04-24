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
 * GUI komponenta pro v�b�r barvy. Komponenta je tla��tko, je� m� aktu�ln� nastavenou barvu a po jeho kliknut�
 * se zobraz� dialog pro v�b�r barvy.
 * @author Tom� Re�nar
 */
public class ColorSelector extends JPanel {

	/**
	 * Box jeho� pozad� zobraz� aktu�ln� barvu
	 */
	Component colorRect;
	/**
	 * Textov� pole pro zad�v� barvy ve form�tu #rrggbb
	 */
	JFormattedTextField htmlValue;
	/**
	 * Poslucha�i zm�ny barvy
	 */
	protected ArrayList<ActionListener> actionListeners=new ArrayList<ActionListener>();



	/**
	 * Vytvo�� v�b�r barvy s tla��tkem pro volbu barvy a textov�m vstupem pro zad�n� ve form�tu #rrggbb.
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

		//poslucha�i
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
	 * Kontruktor s nastaven� defaultn� barvy.
	 */
	public ColorSelector(Color color)
	{
		this();
		this.setColor(color);
	}

	/**
	 * Nastav� barvu.
	 * @param color Pokud je null, pak se nastav� barva �ern�.
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
		//kv�li p�eps�n� metody setBackground(Color bg)
		catch(NullPointerException ex)
		{}
	}
	/**
	 * Aktu�ln� nastaven� barva.
	 */
	public Color getColor()
	{
		return getBackground();
	}

	@Override
	public void setBackground(Color bg)//p�eps�na, proto�e barva pozad� se pou��va pro ulo�en� nstaven� barvy
	{
		setColor(bg);
	}



	/**
	 * Odebere poslucha�e zm�ny barvy.
	 */
	public void removeActionListener(ActionListener l)
	{
		actionListeners.remove(l);
	}

	/**
	 * P�id� poslucha�e zm�ny barvy.
	 */
	public void addActionListener(ActionListener l)
	{
		actionListeners.add(l);
	}
}
