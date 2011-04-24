package cscg.model.objects;

import cscg.ui.GLUtils;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Z�kladn� abstraktn� implementace ploch.
 * @author Tom� Re�nar
 */
public abstract class AbstractObjectSurface<PointClass extends Point3f>
  extends AbstractObject<PointClass>
  implements IObject, ISurface<PointClass>
{

	/**
	 * GUI panel s nastaven�m objektu.
	 */
	private transient JPanel guiPanel;
	/**
	 * GUI prvek pro v�b�r re�imu vykreslen�.
	 */
	private transient JComboBox guiMode;
	/**
	 * GUI prvek pro nastaven� ���ky plochy - v sloupc�ch.
	 */
	private transient JSpinner guiWidth;
	/**
	 * GUI prvek pro nastaven� v��ky plochy - v sloupc�ch.
	 */
	private transient JSpinner guiHeight;
	/**
	 * Re�im vykreslen�. M��e m�t hodnotu jednu z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL.
	 */
	private int mode = GL2.GL_FILL;
	/**
	 * ���ka plochy ve sloupc�ch.
	 */
	private int width;
	/**
	 * V��ka plochy v ��dc�ch.
	 */
	private int height;

	public AbstractObjectSurface()
	{
		super();
		setColor(Color.RED);
		initTransients();
	}

	/**
	 * Inicializace transientn�ch vlastnost�.
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
	 * Inicializace GUI.
	 */
	private synchronized void initGUI()
	{
		guiPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3, 0, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 0;
		guiPanel.add(new JLabel("Vykreslit"), gbc);
		gbc.gridy = 1;
		guiPanel.add(new JLabel("���ka [bod�]"), gbc);
		gbc.gridy = 2;
		guiPanel.add(new JLabel("V��ka [bod�]"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridy = 0;
		guiMode = new JComboBox(new String[]
		  {
			  "V�pl�",
			  "S�",
			  "Body"
		  });
		guiMode.setPrototypeDisplayValue("V�pl�  ");
		guiPanel.add(guiMode, gbc);
		gbc.gridy = 1;
		guiWidth = new JSpinner(getWidthSpinnerModel());
		guiPanel.add(guiWidth, gbc);
		gbc.gridy = 2;
		guiHeight = new JSpinner(getHeightSpinnerModel());
		guiPanel.add(guiHeight, gbc);

		try
		{
			//nutno nastavit zde kv�li deserializaci, av�ak p�i vol�n� v kontruktoru m��e vyvolat vyj�mku
			//proto�e width a height m��ou m�t hodnotu je� nen� validn� a spinner model ji nedovol� nastavit
			guiWidth.setValue(width);
			guiHeight.setValue(height);
		} catch (Exception ex)
		{
		}

		//nastavim mod GUI prvku - prvn�m mus�m zajistit aby v mode byla odli�n� hodnota
		int modeTmp = mode;
		mode++;
		setMode(modeTmp);

		//poslouch�n� ud�lost� z gui prvk�
		guiMode.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				switch (guiMode.getSelectedIndex())
				{
					case 0:
						setMode(GL2.GL_FILL);
						break;
					case 1:
						setMode(GL2.GL_LINE);
						break;
					case 2:
						setMode(GL2.GL_POINT);
						break;
				}
			}
		});

		guiWidth.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (eventHandlerWidthChange((Integer) guiWidth.getValue()) == false)
				{
					guiWidth.setValue(width);
				} else
				{
					width = (Integer) guiWidth.getValue();
					setState(ObjectState.notCounted);
					firePointsChange();
				}
			}
		});

		guiHeight.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (eventHandlerHeightChange((Integer) guiHeight.getValue()) == false)
				{
					guiHeight.setValue(height);
				} else
				{
					height = (Integer) guiHeight.getValue();
					setState(ObjectState.notCounted);
					firePointsChange();
				}
			}
		});

		super.setSpecificGUI(guiPanel);
	}

	/**
	 * Vr�t� model pro ur�en� jak�ch hodnot m��e nab�vat ���ka plochy.
	 */
	abstract protected SpinnerModel getWidthSpinnerModel();

	/**
	 * Vr�t� model pro ur�en� jak�ch hodnot m��e nab�vat v��ka plochy.
	 */
	abstract protected SpinnerModel getHeightSpinnerModel();

	/**
	 * Obsluha ud�losti zm�ny volby ���ky plochy v GUI.
	 * @return Mus� vr�tit true pokud je zm�na potvrzena, pokud vr�t� false, zm�na se zru��.
	 * P�i potvrzen� zm�ny dojde automaticky k vyvol�n� ud�lost� souvisej�ch se zm�nou.
	 */
	abstract protected boolean eventHandlerWidthChange(int newWidth);

	/**
	 * Obsluha ud�losti zm�ny volby v��ky plochy v GUI
	 * @return Mus� vr�tit true pokud je zm�na potvrzena, pokud vr�t� false, zm�na se zru��.
	 * P�i potvrzen� zm�ny dojde automaticky k vyvol�n� ud�lost� souvisej�ch se zm�nou.
	 */
	abstract protected boolean eventHandlerHeightChange(int newHeight);

	/**
	 * Nastav� plochu na �tverec s 4 body a se st�edem v aktu�ln�m st�edu.
	 */
	protected void makeSquare()
	{
		IPoint3f center = getCenter();
		setState(ObjectState.notCounted);
		points.clear(); //vyma�u p�edchoz� tvar
		points.add((PointClass) createNewPoint().set(-25, -25, 0));
		points.add((PointClass) createNewPoint().set(25, -25, 0));
		points.add((PointClass) createNewPoint().set(-25, 25, 0));
		points.add((PointClass) createNewPoint().set(25, 25, 0));
		width = 2;
		height = 2;
		updateGUIForSize();
		translateTo(center); //vycentruji na p�vodn� st�ed
	}

	/**
	 * Z�kladn� implementace pro vykreslen� edita�n�ch uzl� ploch - vykresl� body (viditeln� v�dy)
	 * a k nim m��ku kter� je vid�t pouze kdy� nen� p�ekryt�.
	 */
	protected synchronized void drawSimpleGridNodes(GL2 gl)
	{
		gl.glLineWidth(1f);
		GLUtils.glSetColor(gl, supportColor);
		gl.glDepthFunc(gl.GL_ALWAYS);
		GLUtils.drawGrid(gl, points, columns(), rows());
		drawSimpleNodes(gl);
	}

	/**
	 * Nastaven� rozm�r� plochy.
	 * @param width Po�et sloupc� na ���ku.
	 * @param height Po�et sloupc� na v��ku.
	 */
	protected void setSize(int width, int height)
	{
		setState(ObjectState.notCounted);

		//p�vodn� hodnoty
		int oldWidth = columns(),
		  oldHeight = rows();
		IPoint3f oldCenter = getCenter();
		//vytvo��m m��ku pro body, prvn� index znamen� �adek, druh� znamen� sloupec
		Point3f grid[][] = new Point3f[height][width];

		//napln�m m��ku body je� jsou ji� vytvo�en�
		for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//proch�z�m ��dky
		{
			for (int indexWidth = 0; indexWidth < oldWidth; indexWidth++)//proch�z�m sloupce
			{
				if (indexHeight < height && indexWidth < width)//pokud nedoch�z� ke zmen�en�
				{
					grid[indexHeight][indexWidth] = getPointFromGrid(indexHeight, indexWidth);
				}
			}
		}

		IPoint3f first, last;
		float[] vector;
		Point3f newPoint;

		//ka�d�mu st�vaj�c�mu ��dku p�id�m nov� sloupce
		if (width > oldWidth)//pokud doch�z� ke zv�t�en� po�tu sloupc�
		{
			/*
			 * v�b�r algoritmu dle po�tu p�vodn�ch sloupc�
			 */
			if (oldWidth == 0)
			{
				vector = new float[]
				  {
					  50f, 0, 0
				  };
				for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//proch�z�m ��dky
				{
					last = new Point3f(0, 0, 0);
					for (int indexWidth = 0; indexWidth < width; indexWidth++)//p�id�m sloupce
					{
						newPoint = (createNewPoint());
						newPoint.setBy(PointOperations.move(last, vector));
						grid[indexHeight][indexWidth] = newPoint;//k posledn�mu bodu p�i�tu posun
						last = grid[indexHeight][indexWidth];
					}
				}
			} else
			{
				if (oldWidth == 1)
				{
					vector = new float[]
					  {
						  50f, 0, 0
					  };
					for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//proch�z�m ��dky
					{
						last = grid[indexHeight][oldWidth - 1];
						for (int indexWidth = oldWidth; indexWidth < width; indexWidth++)//p�id�m sloupce
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k posledn�mu bodu p�i�tu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				} else
				{
					for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//proch�z�m ��dky
					{
						//na�tu koncov� body a vektor z prvn�ho bodu k posledn�mu
						first = grid[indexHeight][0];
						last = grid[indexHeight][oldWidth - 1];
						vector = PointOperations.directionVector(first, last);
						vector = PointOperations.scaleVector3(vector, 1f / (oldWidth - 1));//zm�na velikosti vektoru na velikost pr�m�rn�
						for (int indexWidth = oldWidth; indexWidth < width; indexWidth++)//p�id�m sloupce
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k posledn�mu bodu p�i�tu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				}
			}
		}

		//vytvo��m nov� ��dky
		if (height > oldHeight)//pokud doch�z� ke zv�t�en�
		{
			/*
			 * v�b�r algoritmu dle po�tu p�vodn�ch ��dk�
			 */
			if (oldHeight == 0)
			{
				vector = new float[]
				  {
					  0, 50f, 0
				  };
				for (int indexWidth = 0; indexWidth < width; indexWidth++)//proch�z�m sloupce
				{
					last = new Point3f(0, 0, 0);
					for (int indexHeight = 0; indexHeight < height; indexHeight++)//p�id�m ��dky
					{
						newPoint = (createNewPoint());
						newPoint.setBy(PointOperations.move(last, vector));
						grid[indexHeight][indexWidth] = newPoint;//k posledn�mu bodu p�i�tu posun
						last = grid[indexHeight][indexWidth];
					}
				}
			} else
			{
				if (oldHeight == 1)
				{
					vector = new float[]
					  {
						  50f, 0, 0
					  };
					for (int indexWidth = 0; indexWidth < width; indexWidth++)//proch�z�m sloupce
					{
						last = new Point3f(0, 0, 0);
						for (int indexHeight = oldHeight; indexHeight < height; indexHeight++)//p�id�m ��dky
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k posledn�mu bodu p�i�tu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				} else
				{
					for (int indexWidth = 0; indexWidth < width; indexWidth++)//proch�z�m sloupce
					{
						//na�tu koncov� body a vektor z prvn�ho bodu k posledn�mu
						first = grid[0][indexWidth];
						last = grid[oldHeight - 1][indexWidth];
						vector = PointOperations.directionVector(first, last);
						vector = PointOperations.scaleVector3(vector, 1f / (oldHeight - 1));//zm�na velikosti vektoru na velikost pr�m�rn�
						for (int indexHeight = oldHeight; indexHeight < height; indexHeight++)//p�id�m ��dky
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k posledn�mu bodu p�i�tu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				}
			}
		}

		//ulo��m nov� uspo��d�n� bod�
		points.clear();
		points.ensureCapacity(width * height);
		for (int indexHeight = 0; indexHeight < height; indexHeight++)//proch�z�m ��dky
		{
			for (int indexWidth = 0; indexWidth < width; indexWidth++)//proch�z�m sloupce
			{
				points.add((PointClass) grid[indexHeight][indexWidth]);//vlo��m bod
			}
		}

		//ulo��m nov� rozm�ry
		this.width = width;
		this.height = height;
		updateGUIForSize();

		//ud�lost zm�ny bod�
		for (ObjectListener l : listeners)
		{
			l.eventSizeChanged(this);
		}

		//vycentruju na p�vodn� m�sto
		translateTo(oldCenter);
	}

	/**
	 * Aktualizuje prvky pro nastaven� rozm�r�.
	 */
	protected void updateGUIForSize()
	{
		try
		{
			if (guiWidth.getValue().equals(width) == false)
			{
				guiWidth.setValue(width);
			}
			if (guiHeight.getValue().equals(height) == false)
			{
				guiHeight.setValue(height);
			}
		} catch (NullPointerException ex)
		{
		}//fce je vol�na v situac�ch kdy nejsou GUI prvky je�t� vytvo�eny
	}

	@Override
	public synchronized PointClass getPointFromGrid(int row, int col) throws IllegalArgumentException
	{
		//kontrola sou�adnic
		if (row < 0 || row >= rows()
		  || col < 0 || col >= columns())
		{
			throw new IllegalArgumentException("Neplatn� sou�adnice:" + row + ";" + col);
		}
		return points.get(row * columns() + col);
	}

	@Override
	public synchronized int columns()
	{
		return width;
	}

	@Override
	public synchronized int rows()
	{
		return height;
	}

	/**
	 * Zji�t�n� re�imu vykreslen�.
	 * @return Vr�t� jednu z kontstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL.
	 */
	public synchronized int getMode()
	{
		return mode;
	}

	/**
	 * Nastaven� re�imu vykreslen�.
	 * @param mode Jedna z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL, pro vykreslen�:
	 * body, s�t�, vypln�n� plochy.
	 */
	public synchronized void setMode(int mode)
	{
		if (mode != GL2.GL_POINT && mode != GL2.GL_LINE && mode != GL2.GL_FILL)
		{
			throw new IllegalArgumentException("Neplatn� hodnota argumentu.");
		}
		if (this.mode != mode)
		{
			this.mode = mode;
			switch (mode)
			{
				case GL2.GL_FILL:
					guiMode.setSelectedIndex(0);
					break;
				case GL2.GL_LINE:
					guiMode.setSelectedIndex(1);
					break;
				case GL2.GL_POINT:
					guiMode.setSelectedIndex(2);
					break;
			}
			for (ObjectListener l : listeners)
			{
				l.eventSpecificPropertiesChanged(this);
			}
		}
	}

	@Override
	protected synchronized void setSpecificGUI(JComponent gui)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		guiPanel.add(gui, gbc);
	}
}
