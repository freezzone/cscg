package cscg.ui.components;

import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.Point3f;
import cscg.model.objects.Point4f;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.LinkedList;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel pro úpravu vlastností bodù - {@link IPoint3f} a tøíd zdìdìných.
 * @author Tomáš Režnar
 */
public class PointPanel extends JPanel {

	/**
	 * Gui prvky pro nastavení vlastností.
	 */
	private JFormattedTextField xValue,yValue,zValue,wValue;

	/**
	 * Popisky.
	 */
	private JLabel zLabel,wLabel;

	/**
	 * Editavovaný bod.
	 */
	private IPoint3f editedPoint;

	/**
	 * Posluchaèi zmìn v nastavení bodu.
	 */
	private LinkedList<PointChangedListener> pointListeners=new LinkedList<PointChangedListener>();


	public PointPanel()
	{
		super();
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		NumberFormat formatCoords = NumberFormat.getNumberInstance();//formát èísel
		formatCoords.setMaximumFractionDigits(1);
		formatCoords.setMinimumFractionDigits(1);
		formatCoords.setRoundingMode(RoundingMode.HALF_UP);
		NumberFormat formatWeight = NumberFormat.getNumberInstance();//formát váht
		formatWeight.setMaximumFractionDigits(3);
		formatWeight.setMinimumFractionDigits(3);
		formatWeight.setRoundingMode(RoundingMode.HALF_UP);
		int columns=10;
		xValue=new JFormattedTextField(formatCoords);
		yValue=new JFormattedTextField(formatCoords);
		zValue=new JFormattedTextField(formatCoords);
		wValue=new JFormattedTextField(formatWeight);
		xValue.setColumns(columns);
		yValue.setColumns(columns);
		zValue.setColumns(columns);
		wValue.setColumns(5);
		add(new JLabel("X"));
		add(xValue);
		add(new JLabel("  Y"));
		add(yValue);
		zLabel=new JLabel("  Z");
		add(zLabel);
		add(zValue);
		wLabel=new JLabel("  Váha");
		add(wLabel);
		add(wValue);

		setEnabled(false);

		//nastavení posluchaèù zmìn vlastností bodu
		ActionListener listener=new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				firePointChanged();
			}
		};

		xValue.addActionListener(listener);
		yValue.addActionListener(listener);
		zValue.addActionListener(listener);
		wValue.addActionListener(listener);
	}

	/**
	 * Vyvolání události zmìny bodu.
	 */
	private synchronized void firePointChanged()
	{
		if(editedPoint!=null)
		{
			IPoint3f newPoint;
			if(editedPoint instanceof IPoint4f)
			{
				newPoint=new Point4f(new Float(xValue.getValue().toString()),
					new Float(yValue.getValue().toString()),
					new Float(zValue.getValue().toString()),
					new Float(wValue.getValue().toString()));
			}
			else
			{
				newPoint=new Point3f(new Float(xValue.getValue().toString()),
					new Float(yValue.getValue().toString()),
					new Float(zValue.getValue().toString()));
			}
			//oznámím posluchaèùm
			PointChangedEvent evt=new PointChangedEvent(this, editedPoint, newPoint);
			for(PointChangedListener l:pointListeners)
			{
				l.pointChanged(evt);
			}
		}
	}

	/**
	 * Nastaví GUI na daný bod.
	 */
	public synchronized void setPoint(IPoint3f p)
	{
		editedPoint=p;
		selectProperMode();//vyberu režim zobrazení
	}

	/**
	 * Zablokuje editaci.
	 */
	@Override
	final public synchronized void setEnabled(boolean enabled)
	{
		xValue.setEnabled(enabled);
		yValue.setEnabled(enabled);
		zValue.setEnabled(enabled);
		wValue.setEnabled(enabled);
	}

	/**
	 * Vybrání vhodného režimu editace dle aktuálního bodu.
	 */
	private synchronized void selectProperMode()
	{
		if(editedPoint==null)
		{
			setEnabled(false);
		}
		else
		{
			setEnabled(true);
			//zobrazení pole pro váhu
			if(editedPoint instanceof IPoint4f)
			{
				wValue.setVisible(true);
				wLabel.setVisible(true);
			}
			else
			{
				wValue.setVisible(false);
				wLabel.setVisible(false);
			}

			//nastavení hodnot polí
			xValue.setValue(editedPoint.getX());
			yValue.setValue(editedPoint.getY());
			zValue.setValue(editedPoint.getZ());
			if(editedPoint instanceof IPoint4f)
			{
				wValue.setValue(((IPoint4f)editedPoint).getW());
			}
		}
	}

	/**
	 * Pøidání posluchaèe zmìny bodu.
	 */
	public void AddPointChangedListener(PointChangedListener l)
	{
		pointListeners.add(l);
	}

	/**
	 * Odebrání posluchaèe zmìny bodu.
	 */
	public void RemovePointChangedListener(PointChangedListener l)
	{
		pointListeners.remove(l);
	}

}
