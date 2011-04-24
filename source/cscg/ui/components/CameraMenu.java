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
 * Menu pro nastaven� kamery.
 * @author Tom� Re�nar
 */
public class CameraMenu extends JPopupMenu
{

	/**
	 * P�ep�na� zp�sobu zobrazen�.
	 */
	private final JCheckBoxMenuItem perspective;
	/**
	 * Nastav� kameru na pohled ze p�edu.
	 */
	private final JMenuItem front;
	/**
	 * Nastav� kameru na pohled z boku.
	 */
	private final JMenuItem side;
	/**
	 * Nastav� kameru na pohled z vrchu.
	 */
	private final JMenuItem top;
	/**
	 * Projekce kter� je nastavov�na.
	 */
	private Projection projection;

	public CameraMenu()
	{
		super();

		JMenu look = new JMenu("Pohled kamery");
		add(look);

		front = new JMenuItem("zep�edu");
		side = new JMenuItem("zleva");
		top = new JMenuItem("shora");
		look.add(front);
		look.add(side);
		look.add(top);

		perspective = new JCheckBoxMenuItem("Perspektivn� zobrazen�");
		add(perspective);

		initListeners();
	}

	/**
	 * Z�sk�n� projekce kterou menu edituje.
	 */
	public Projection getProjection()
	{
		return projection;
	}

	/**
	 * Nastaven� projekce kterou menu edituje.
	 * Nesm� b�t null.
	 */
	public void setProjection(Projection projection)
	{
		this.projection = projection;

		perspective.setSelected(projection.isPerspective());
	}

	/**
	 * P�ed zobrazen�m menu je nutn� nastavit projekci metodou 
	 * {@link #setProjection(cscg.model.Projection)} na nenull hodnotu.
	 */
	@Override
	public void show(Component invoker, int x, int y)
	{
		if(perspective==null)
		{
			throw new NullPointerException("P�ed zobrazen�m menu je nutn� nastavit projekci metodou "
			  + "setProjection(Projection projection) na nenull hodnotu.");
		}
		super.show(invoker, x, y);
	}



	/**
	 * P�id�n� poslucha�� GUI prvk�.
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
