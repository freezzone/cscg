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
 * Základní abstraktní implementace ploch.
 * @author Tomáš Renar
 */
public abstract class AbstractObjectSurface<PointClass extends Point3f>
  extends AbstractObject<PointClass>
  implements IObject, ISurface<PointClass>
{

	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiPanel;
	/**
	 * GUI prvek pro vıbìr reimu vykreslení.
	 */
	private transient JComboBox guiMode;
	/**
	 * GUI prvek pro nastavení šíøky plochy - v sloupcích.
	 */
	private transient JSpinner guiWidth;
	/**
	 * GUI prvek pro nastavení vıšky plochy - v sloupcích.
	 */
	private transient JSpinner guiHeight;
	/**
	 * Reim vykreslení. Mùe mít hodnotu jednu z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL.
	 */
	private int mode = GL2.GL_FILL;
	/**
	 * Šíøka plochy ve sloupcích.
	 */
	private int width;
	/**
	 * Vıška plochy v øádcích.
	 */
	private int height;

	public AbstractObjectSurface()
	{
		super();
		setColor(Color.RED);
		initTransients();
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
		guiPanel.add(new JLabel("Šíøka [bodù]"), gbc);
		gbc.gridy = 2;
		guiPanel.add(new JLabel("Vıška [bodù]"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridy = 0;
		guiMode = new JComboBox(new String[]
		  {
			  "Vıplò",
			  "Sí",
			  "Body"
		  });
		guiMode.setPrototypeDisplayValue("Vıplò  ");
		guiPanel.add(guiMode, gbc);
		gbc.gridy = 1;
		guiWidth = new JSpinner(getWidthSpinnerModel());
		guiPanel.add(guiWidth, gbc);
		gbc.gridy = 2;
		guiHeight = new JSpinner(getHeightSpinnerModel());
		guiPanel.add(guiHeight, gbc);

		try
		{
			//nutno nastavit zde kvùli deserializaci, avšak pøi volání v kontruktoru mùe vyvolat vyjímku
			//protoe width a height mùou mít hodnotu je není validní a spinner model ji nedovolí nastavit
			guiWidth.setValue(width);
			guiHeight.setValue(height);
		} catch (Exception ex)
		{
		}

		//nastavim mod GUI prvku - prvním musím zajistit aby v mode byla odlišná hodnota
		int modeTmp = mode;
		mode++;
		setMode(modeTmp);

		//poslouchání událostí z gui prvkù
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
	 * Vrátí model pro urèení jakıch hodnot mùe nabıvat šíøka plochy.
	 */
	abstract protected SpinnerModel getWidthSpinnerModel();

	/**
	 * Vrátí model pro urèení jakıch hodnot mùe nabıvat vıška plochy.
	 */
	abstract protected SpinnerModel getHeightSpinnerModel();

	/**
	 * Obsluha události zmìny volby šíøky plochy v GUI.
	 * @return Musí vrátit true pokud je zmìna potvrzena, pokud vrátí false, zmìna se zruší.
	 * Pøi potvrzení zmìny dojde automaticky k vyvolání událostí souvisejích se zmìnou.
	 */
	abstract protected boolean eventHandlerWidthChange(int newWidth);

	/**
	 * Obsluha události zmìny volby vıšky plochy v GUI
	 * @return Musí vrátit true pokud je zmìna potvrzena, pokud vrátí false, zmìna se zruší.
	 * Pøi potvrzení zmìny dojde automaticky k vyvolání událostí souvisejích se zmìnou.
	 */
	abstract protected boolean eventHandlerHeightChange(int newHeight);

	/**
	 * Nastaví plochu na ètverec s 4 body a se støedem v aktuálním støedu.
	 */
	protected void makeSquare()
	{
		IPoint3f center = getCenter();
		setState(ObjectState.notCounted);
		points.clear(); //vymau pøedchozí tvar
		points.add((PointClass) createNewPoint().set(-25, -25, 0));
		points.add((PointClass) createNewPoint().set(25, -25, 0));
		points.add((PointClass) createNewPoint().set(-25, 25, 0));
		points.add((PointClass) createNewPoint().set(25, 25, 0));
		width = 2;
		height = 2;
		updateGUIForSize();
		translateTo(center); //vycentruji na pùvodní støed
	}

	/**
	 * Základní implementace pro vykreslení editaèních uzlù ploch - vykreslí body (viditelné vdy)
	 * a k nim møíku která je vidìt pouze kdy není pøekrytá.
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
	 * Nastavení rozmìrù plochy.
	 * @param width Poèet sloupcù na šíøku.
	 * @param height Poèet sloupcù na vıšku.
	 */
	protected void setSize(int width, int height)
	{
		setState(ObjectState.notCounted);

		//pùvodní hodnoty
		int oldWidth = columns(),
		  oldHeight = rows();
		IPoint3f oldCenter = getCenter();
		//vytvoøím møíku pro body, první index znamená øadek, druhı znamená sloupec
		Point3f grid[][] = new Point3f[height][width];

		//naplním møíku body je jsou ji vytvoøené
		for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//procházím øádky
		{
			for (int indexWidth = 0; indexWidth < oldWidth; indexWidth++)//procházím sloupce
			{
				if (indexHeight < height && indexWidth < width)//pokud nedochází ke zmenšení
				{
					grid[indexHeight][indexWidth] = getPointFromGrid(indexHeight, indexWidth);
				}
			}
		}

		IPoint3f first, last;
		float[] vector;
		Point3f newPoint;

		//kadému stávajícímu øádku pøidám nové sloupce
		if (width > oldWidth)//pokud dochází ke zvìtšení poètu sloupcù
		{
			/*
			 * vıbìr algoritmu dle poètu pùvodních sloupcù
			 */
			if (oldWidth == 0)
			{
				vector = new float[]
				  {
					  50f, 0, 0
				  };
				for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//procházím øádky
				{
					last = new Point3f(0, 0, 0);
					for (int indexWidth = 0; indexWidth < width; indexWidth++)//pøidám sloupce
					{
						newPoint = (createNewPoint());
						newPoint.setBy(PointOperations.move(last, vector));
						grid[indexHeight][indexWidth] = newPoint;//k poslednímu bodu pøiètu posun
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
					for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//procházím øádky
					{
						last = grid[indexHeight][oldWidth - 1];
						for (int indexWidth = oldWidth; indexWidth < width; indexWidth++)//pøidám sloupce
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k poslednímu bodu pøiètu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				} else
				{
					for (int indexHeight = 0; indexHeight < oldHeight; indexHeight++)//procházím øádky
					{
						//naètu koncové body a vektor z prvního bodu k poslednímu
						first = grid[indexHeight][0];
						last = grid[indexHeight][oldWidth - 1];
						vector = PointOperations.directionVector(first, last);
						vector = PointOperations.scaleVector3(vector, 1f / (oldWidth - 1));//zmìna velikosti vektoru na velikost prùmìrné
						for (int indexWidth = oldWidth; indexWidth < width; indexWidth++)//pøidám sloupce
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k poslednímu bodu pøiètu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				}
			}
		}

		//vytvoøím nové øádky
		if (height > oldHeight)//pokud dochází ke zvìtšení
		{
			/*
			 * vıbìr algoritmu dle poètu pùvodních øádkù
			 */
			if (oldHeight == 0)
			{
				vector = new float[]
				  {
					  0, 50f, 0
				  };
				for (int indexWidth = 0; indexWidth < width; indexWidth++)//procházím sloupce
				{
					last = new Point3f(0, 0, 0);
					for (int indexHeight = 0; indexHeight < height; indexHeight++)//pøidám øádky
					{
						newPoint = (createNewPoint());
						newPoint.setBy(PointOperations.move(last, vector));
						grid[indexHeight][indexWidth] = newPoint;//k poslednímu bodu pøiètu posun
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
					for (int indexWidth = 0; indexWidth < width; indexWidth++)//procházím sloupce
					{
						last = new Point3f(0, 0, 0);
						for (int indexHeight = oldHeight; indexHeight < height; indexHeight++)//pøidám øádky
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k poslednímu bodu pøiètu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				} else
				{
					for (int indexWidth = 0; indexWidth < width; indexWidth++)//procházím sloupce
					{
						//naètu koncové body a vektor z prvního bodu k poslednímu
						first = grid[0][indexWidth];
						last = grid[oldHeight - 1][indexWidth];
						vector = PointOperations.directionVector(first, last);
						vector = PointOperations.scaleVector3(vector, 1f / (oldHeight - 1));//zmìna velikosti vektoru na velikost prùmìrné
						for (int indexHeight = oldHeight; indexHeight < height; indexHeight++)//pøidám øádky
						{
							newPoint = (createNewPoint());
							newPoint.setBy(PointOperations.move(last, vector));
							grid[indexHeight][indexWidth] = newPoint;//k poslednímu bodu pøiètu posun
							last = grid[indexHeight][indexWidth];
						}
					}
				}
			}
		}

		//uloím nové uspoøádání bodù
		points.clear();
		points.ensureCapacity(width * height);
		for (int indexHeight = 0; indexHeight < height; indexHeight++)//procházím øádky
		{
			for (int indexWidth = 0; indexWidth < width; indexWidth++)//procházím sloupce
			{
				points.add((PointClass) grid[indexHeight][indexWidth]);//vloím bod
			}
		}

		//uloím nové rozmìry
		this.width = width;
		this.height = height;
		updateGUIForSize();

		//událost zmìny bodù
		for (ObjectListener l : listeners)
		{
			l.eventSizeChanged(this);
		}

		//vycentruju na pùvodní místo
		translateTo(oldCenter);
	}

	/**
	 * Aktualizuje prvky pro nastavení rozmìrù.
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
		}//fce je volána v situacích kdy nejsou GUI prvky ještì vytvoøeny
	}

	@Override
	public synchronized PointClass getPointFromGrid(int row, int col) throws IllegalArgumentException
	{
		//kontrola souøadnic
		if (row < 0 || row >= rows()
		  || col < 0 || col >= columns())
		{
			throw new IllegalArgumentException("Neplatné souøadnice:" + row + ";" + col);
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
	 * Zjištìní reimu vykreslení.
	 * @return Vrátí jednu z kontstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL.
	 */
	public synchronized int getMode()
	{
		return mode;
	}

	/**
	 * Nastavení reimu vykreslení.
	 * @param mode Jedna z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL, pro vykreslení:
	 * body, sítì, vyplnìné plochy.
	 */
	public synchronized void setMode(int mode)
	{
		if (mode != GL2.GL_POINT && mode != GL2.GL_LINE && mode != GL2.GL_FILL)
		{
			throw new IllegalArgumentException("Neplatná hodnota argumentu.");
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
