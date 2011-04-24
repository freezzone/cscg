package cscg.ui.components;

import cscg.model.Projection;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Menu pro nastavení kamery.
 * @author Tomáš Režnar
 */
public class CameraMenu extends JPopupMenu
{

	/**
	 * Pøepínaè zpùsobu zobrazení.
	 */
	private final JCheckBoxMenuItem perspective;
	/**
	 * Nastaví kameru na pohled ze pøedu.
	 */
	private final JMenuItem front;
	/**
	 * Nastaví kameru na pohled z boku.
	 */
	private final JMenuItem side;
	/**
	 * Nastaví kameru na pohled z vrchu.
	 */
	private final JMenuItem top;
	/**
	 * Projekce která je nastavována.
	 */
	private Projection projection;

	public CameraMenu()
	{
		super();

		JMenu look = new JMenu("Pohled kamery");
		add(look);

		front = new JMenuItem("zepøedu");
		side = new JMenuItem("zleva");
		top = new JMenuItem("shora");
		look.add(front);
		look.add(side);
		look.add(top);

		perspective = new JCheckBoxMenuItem("Perspektivní zobrazení");
		add(perspective);

		initListeners();
	}

	/**
	 * Získání projekce kterou menu edituje.
	 */
	public Projection getProjection()
	{
		return projection;
	}

	/**
	 * Nastavení projekce kterou menu edituje.
	 * Nesmí být null.
	 */
	public void setProjection(Projection projection)
	{
		this.projection = projection;

		perspective.setSelected(projection.isPerspective());
	}

	/**
	 * Pøed zobrazením menu je nutné nastavit projekci metodou 
	 * {@link #setProjection(cscg.model.Projection)} na nenull hodnotu.
	 */
	@Override
	public void show(Component invoker, int x, int y)
	{
		if(perspective==null)
		{
			throw new NullPointerException("Pøed zobrazením menu je nutné nastavit projekci metodou "
			  + "setProjection(Projection projection) na nenull hodnotu.");
		}
		super.show(invoker, x, y);
	}



	/**
	 * Pøidání posluchaèù GUI prvkù.
	 */
	private void initListeners()
	{
		front.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				projection.lookFront();
			}
		});
		side.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				projection.lookProfile();
			}
		});
		top.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				projection.lookTop();
			}
		});

		perspective.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				projection.setPerspective(perspective.isSelected());
			}
		});
	}
}
