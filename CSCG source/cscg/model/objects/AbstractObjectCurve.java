package cscg.model.objects;

import cscg.ui.components.EditorLogic;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Základní abstraktní implementace křivky.
 * Tento objekt slouží jako základ pro konkrétní implementace.
 * @author Tomáš Režnar
 */
public abstract class AbstractObjectCurve<PointClass extends Point3f> extends AbstractObject<PointClass> implements IPointsAddable
{

	/**
	 * Tloušťka čáry.
	 */
	protected volatile float lineWidth;
	/**
	 * GUI pro volbu tloušťky čáry.
	 */
	private transient JSpinner guiLineWidth;
	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiPanel;

	/**
	 * Základní konstruktor.
	 */
	public AbstractObjectCurve()
	{
		super();
		lineWidth = 2;
		initTransients();
		setLightOn(false);
	}

	/**
	 * Inicializace transientních vlastností.
	 */
	private void initTransients()
	{
		initGUI();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransients();
	}

	/**
	 * Vytvoření GUI.
	 */
	private void initGUI()
	{
		guiPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 0, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		guiPanel.add(new JLabel("Tloušťka"), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		guiLineWidth = new JSpinner(new SpinnerNumberModel(lineWidth, 1, EditorLogic.getLineMaxWidth(), 1));
		guiPanel.add(guiLineWidth, gbc);

		//poslouchání událostí z gui prvků
		guiLineWidth.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setLineWidth(((Double) guiLineWidth.getValue()).floatValue());
			}
		});

		super.setSpecificGUI(guiPanel);
	}

	@Override
	protected synchronized void setSpecificGUI(JComponent gui)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		guiPanel.add(gui, gbc);
	}

	@Override
	public synchronized IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint)
	{
		if (newPoint == null)
		{
			throw new NullPointerException();
		}
		setState(ObjectState.notCounted);//oznamim změnu objektu
		int index = -1;
		if (beforePoint != null)
		{
			index = points.indexOf(beforePoint);
		}
		PointClass addPoint = (PointClass) createNewPoint().setBy(newPoint);
		if (index < 0)
		{
			index = points.size();
			points.add(addPoint);
		} else
		{
			points.add(index, addPoint);
		}
		reportAnyPointChange();
		for (ObjectListener l : listeners)
		{
			l.eventPointAdded(this, addPoint, index);
		}
		return addPoint;
	}

	@Override
	public synchronized IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint)
	{
		if (newPoint == null)
		{
			throw new NullPointerException();
		}
		setState(ObjectState.notCounted);//oznamim změnu objektu
		int index = -1;
		if (afterPoint != null)
		{
			index = points.indexOf(afterPoint);
			index = index == -1 ? -1 : index + 1;
		}
		PointClass addPoint = (PointClass) createNewPoint().setBy(newPoint);
		if (index < 0)
		{
			index = 0;
			points.add(0, addPoint);
		} else
		{
			points.add(index, addPoint);
		}
		reportAnyPointChange();
		for (ObjectListener l : listeners)
		{
			l.eventPointAdded(this, addPoint, index);
		}
		return addPoint;
	}

	@Override
	public synchronized void removePoint(IPoint3f point)
	{
		setState(ObjectState.notCounted); //oznamim změnu objektu
		int index = points.indexOf(point);
		cancelSelectedPoint(point);
		if (points.remove(point))
		{
			reportAnyPointChange();
			for (ObjectListener l : listeners)
			{
				l.eventPointRemoved(this, point, index);
			}
		}
	}

	/**
	 * Získání tloušťky čar.
	 * @return Tloušťka v pixelech.
	 */
	public synchronized float getLineWidth()
	{
		return lineWidth;
	}

	/**
	 * Nastavení tloušťky čar
	 * @param lineWidth Tloušťka v pixelech.
	 */
	public synchronized void setLineWidth(float lineWidth)
	{
		if (lineWidth <= 0)
		{
			throw new IllegalArgumentException("lineWidth musí být kladné číslo");
		}
		this.lineWidth = lineWidth;
		guiLineWidth.setValue((double) lineWidth);
		for (ObjectListener l : listeners)
		{
			l.eventColorChanged(this);
		}
	}
}
