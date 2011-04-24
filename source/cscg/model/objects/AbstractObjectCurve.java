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
 * Z·kladnÌ abstraktnÌ implementace k¯ivky.
 * Tento objekt slouûÌ jako z·klad pro konkrÈtnÌ implementace.
 * @author Tom·ö Reûnar
 */
public abstract class AbstractObjectCurve<PointClass extends Point3f> extends AbstractObject<PointClass> implements IPointsAddable
{

	/**
	 * Tlouöùka Ë·ry.
	 */
	protected volatile float lineWidth;
	/**
	 * GUI pro volbu tlouöùky Ë·ry.
	 */
	private transient JSpinner guiLineWidth;
	/**
	 * GUI panel s nastavenÌm objektu.
	 */
	private transient JPanel guiPanel;

	/**
	 * Z·kladnÌ kontruktor.
	 */
	public AbstractObjectCurve()
	{
		super();
		lineWidth = 2;
		initTransients();
		setLightOn(false);
	}

	/**
	 * Inicializace transientnÌch vlastnostÌ.
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
	 * Vytvo¯enÌ GUI.
	 */
	private void initGUI()
	{
		guiPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 0, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		guiPanel.add(new JLabel("Tlouöùka"), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		guiLineWidth = new JSpinner(new SpinnerNumberModel(lineWidth, 1, EditorLogic.getLineMaxWidth(), 1));
		guiPanel.add(guiLineWidth, gbc);

		//poslouch·nÌ ud·lostÌ z gui prvk˘
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
		setState(ObjectState.notCounted);//oznamim zmÏnu objektu
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
		setState(ObjectState.notCounted);//oznamim zmÏnu objektu
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
		setState(ObjectState.notCounted); //oznamim zmÏnu objektu
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
	 * ZÌsk·nÌ tlouöùky Ëar.
	 * @return Tlouöùka v pixelech.
	 */
	public synchronized float getLineWidth()
	{
		return lineWidth;
	}

	/**
	 * NastavenÌ tlouöùky Ëar
	 * @param lineWidth Tlouöùka v pixelech.
	 */
	public synchronized void setLineWidth(float lineWidth)
	{
		if (lineWidth <= 0)
		{
			throw new IllegalArgumentException("lineWidth musÌ b˝t kladnÈ ËÌslo");
		}
		this.lineWidth = lineWidth;
		guiLineWidth.setValue((double) lineWidth);
		for (ObjectListener l : listeners)
		{
			l.eventColorChanged(this);
		}
	}
}
