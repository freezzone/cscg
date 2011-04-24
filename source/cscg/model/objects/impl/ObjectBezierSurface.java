package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectSurface;
import cscg.model.objects.EnumNumberSpinnerModel;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.Joining;
import cscg.model.objects.MathUtils;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import cscg.model.objects.PointOperations;
import cscg.ui.GLUtils;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implementace Bézierovy plochy.
 * @author Tomáš Režnar
 */
public class ObjectBezierSurface extends AbstractObjectSurface<Point3fChangeable>{

	/**
	 * Maximální možný stupeò køivky.
	 */
	public final int MAX_DEGREE = 50;
	/**
	 * Maximální možný nastavitelný poèet krokù pro vykreslení.
	 */
	public final int MAX_STEPS = 100;
	/**
	 * Poèítadlo instancí.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * GUI prvek pro nastavení stupnì plochy.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastavení poètu krokù vykreslení.
	 */
	private transient JSpinner guiSteps;
	/**
	 * Poèet krokù pro vykreslení køivky. 0=automatické urèení.
	 */
	private volatile int steps = 0;
	/**
	 * Stupeò plochy.
	 * Musí být < poèet bodù každým smìrem.
	 */
	private volatile int degree = 3;
	/**
	 * Povolení použití grafické karty pro urychlení vykreslení.
	 */
	private volatile boolean useHWAcceleration = true;
	/**
	 * Napojování ploch.
	 */
	private volatile Joining joining=Joining.G0C0;
	/**
	 * GUI prvek pro automatickou volbu poètu krokù.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * GUI prvek pro povolení HW akcelerace.
	 */
	private transient JCheckBox guiUseHWAcceleration;
	/**
	 * GUI prvek pro výbìr napojování jednotlivých køivek.
	 */
	private transient JComboBox guiJoining;
	/**
	 * Pomocná promnìná, jež má hodnotu true bìhem zmìny hodnoty poètu krokù, kdy nechci aby došlo k opìtovnému zavolání
	 * metody pro zmìnu hodnoty.
	 */
	private boolean guiChangingValue = false;

	/**
	 * Konstruktor.
	 */
	public ObjectBezierSurface()
	{
		super();
		pointPrototype=new Point3fChangeable();
		setName("Bézierova plocha " + selfCounter++);
		setState(ObjectState.notCounted);
		//základní tvar plochy
		makeSquare();
		setSize(4, 4);

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
	 * Vytvoøení GUI.
	 */
	private void initGUI()
	{
		//inicializace gui prvkù nastavení objektu
		guiDegree = new JSpinner(new SpinnerNumberModel(degree, 1, MAX_DEGREE, 1));
		guiSetings = new JPanel(new GridBagLayout());
		guiSteps = new JSpinner(new SpinnerNumberModel(10, 0, MAX_STEPS, 1));
		guiStepsAuto = new JCheckBox("", true);
		guiUseHWAcceleration = new JCheckBox("", useHWAcceleration);
		guiJoining = new JComboBox(new String[]{
			"<html>G<sub>0</sub></html>",
			"<html>G<sub>1</sub></html>"
		});
		guiJoining.setPrototypeDisplayValue("<html>G<sub>0</sub>G</html>");
		GridBagConstraints gbc = new GridBagConstraints();

		//stupeò
		gbc.weightx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupeò plochy"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiDegree, gbc);

		//napojování
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
		guiSetings.add(new JLabel("Napojování"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiJoining, gbc);

		//pøesnost
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiSteps, gbc);

		//povolení hw akcelerace
		gbc.gridy = 4;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("HW akcelerace"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiUseHWAcceleration, gbc);

		//vložím do nadøazeného gui
		super.setSpecificGUI(guiSetings);

		//posluchaèi zmìn nastavení
		guiDegree.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setDegree((Integer) guiDegree.getValue());
			}
		});

		guiJoining.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Joining newJoining=null;
				switch(guiJoining.getSelectedIndex())
				{
					case 0:
						newJoining=Joining.G0C0;
						break;

					case 1:
						newJoining=Joining.C1;
						break;

				}
				if(joining!=newJoining)
				{
					setJoining(newJoining);
					if(checkJoining())
					{
						firePointsChange();
					}
				}
			}
		});

		guiSteps.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (guiChangingValue == false)//obrana pred zacyklením
				{
					setSteps((Integer) guiSteps.getValue());
				}
			}
		});

		guiStepsAuto.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (guiChangingValue == true)//obrana pred zacyklením
				{
					return;
				}
				if (guiStepsAuto.isSelected())
				{
					setSteps(0);
				} else
				{
					setSteps((Integer) guiSteps.getValue());
				}
			}
		});

		guiUseHWAcceleration.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				setUseHWAcceleration(guiUseHWAcceleration.isSelected());
			}
		});

		setSteps(steps);//nastavim presnost
		setJoining(joining);//nastavim napojovani

	}

	@Override
	protected synchronized SpinnerModel getWidthSpinnerModel()
	{
		return new EnumNumberSpinnerModel(2, 2, 1, 100);
	}

	@Override
	protected synchronized SpinnerModel getHeightSpinnerModel()
	{
		return new EnumNumberSpinnerModel(2, 2, 1, 100);
	}

	@Override
	protected boolean eventHandlerWidthChange(int newWidth)
	{
		setSize(newWidth,rows());
		return true;
	}

	@Override
	protected boolean eventHandlerHeightChange(int newHeight)
	{
		setSize(columns(),newHeight);
		return true;
	}

	@Override
	public synchronized void editPoint(IPoint3f editedPoint, IPoint3f setBy)
	{
		super.editPoint(editedPoint, setBy);
		if(checkJoining())
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset)
	{
		super.movePointRelative(point, xOffset, yOffset, zOffset);
		if(checkJoining())
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void movePointTo(IPoint3f point, float x, float y, float z)
	{
		super.movePointTo(point, x, y, z);
		if(checkJoining())
		{
			firePointsChange();
		}
	}

	/**
	 * Vykreslení jedné bézierovy plochy pomocí grafické karty.
	 * @param gl
	 * @param order stupeò plochy+1
	 * @param steps poèet krokù pro vykreslení každé plochy (pro každý parametr)
	 * @param points pole bodù, kde jsou souøadnice x,y,z uloženy v øadì za sebou
	 * @param pointOffset offset v poli bodù vydìlený 3
	 */
	private synchronized void hwBezierSurface(GL2 gl, int order, int steps, float[] points, int pointOffset)
	{
		gl.glMap2f(gl.GL_MAP2_VERTEX_3,
			0, 1, 3, order,
			0, 1, 3*order, order,
			points, pointOffset * 3);
		gl.glMapGrid2f(
			steps, 0, 1,
			steps, 0, 1
		);
		gl.glEvalMesh2(getMode(),
			0, steps,
			0, steps
		);
	}

	/**
	 * Vykreslení jedné bézierovy plochy pomocí CPU.
	 * @param gl
	 * @param order stupeò plochy+1
	 * @param steps poèet krokù pro vykreslení každé plochy (pro každý parametr)
	 * @param points list bodù, kde jsou body uloženy za sebou po øádcích
	 */
	private synchronized void swBezierSurface(GL2 gl, int order, int steps, List<IPoint3f> points)
	{
		double step=1./(double)steps;
		double u,v;//parametry
		double Bu,Bv;//hodnota bernsteinova polynomu
		int i,j;//index v cyklu sum
		double sumaX, sumaY, sumaZ;//souèet sum
		IPoint3f point;
		ArrayList<IPoint3f> surfacePoints=new ArrayList<IPoint3f>((steps+1)*(steps+1));//pole vypoèetných bodù plochy
		int rows=0,cols=0;//poèítadlo sloupcù a øádkù
		//procházení parametru u
		for (u = -step,rows=0; u < 1;)
		{
			rows++;
			u += step;
			if (u > 1)
			{
				u = 1;
			}
			//procházení parametru v
			for (v = -step, cols=0, sumaX = 0, sumaY = 0, sumaZ = 0; v < 1; sumaX = 0, sumaY = 0, sumaZ = 0)
			{
				cols++;
				v += step;
				if (v > 1)
				{
					v = 1;
				}
				//první suma
				for (i = 0; i < order;i++)
				{
					//druhá suma
					for (j = 0; j < order;j++)
					{
						point = points.get(i*order+j);
						Bv = MathUtils.bernstein(j, getDegree(), v);
						Bu = MathUtils.bernstein(i, getDegree(), u);
						sumaX += Bu * Bv * point.getX();
						sumaY += Bu * Bv * point.getY();
						sumaZ += Bu * Bv * point.getZ();
					}
				}
				//uložení bodu
				surfacePoints.add(new Point3f((float)sumaX, (float)sumaY, (float)sumaZ));
			}
		}
		//vykreslení plochy
		GLUtils.drawSurface(gl, surfacePoints, cols, rows, getMode());
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(ObjectState.notCounted==getState())
		{
			if (columns() > getDegree() && rows() > getDegree())//existuje pouze když stupeò køivky je menší než poèet bodù
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bodù
			{
				setStateMessage("Nedostatek bodù pro vykreslení.");
				setState(ObjectState.inputError);
			}
		}

		if(ObjectState.OK==getState())
		{
			//urèení poètu krokù
			int loopSteps = getSteps();
			if (loopSteps == 0)
			{
				loopSteps = (int) Math.ceil(20 * getDegree());
				//steps = (int) Math.ceil(scale * 20 * getDegree());
			}

			GLUtils.glSetColor(gl, color);
			
			int order=getDegree()+1;//poèet bodù pro jeden úsek køivky
			//body uložím do obyèejného pole
			int cols=columns();
			int rows=rows();
			IPoint3f p;
			
			//naètu body pro jednu plochu do pole
			if (useHWAcceleration)//pomocí HW
			{
				try
				{
					// povolení 2D evaluátoru
					gl.glEnable(gl.GL_MAP2_VERTEX_3);
					float[] pointArray = new float[order*order*3];
					for(int col=0;col<cols-getDegree();col+=getDegree())//projdu plochy v sloupcích
					{
						for(int row=0;row<rows-getDegree();row+=getDegree())//projdu plochy v øádcích
						{

							for(int y=row,i=0;y<row+order;y++)
							{
								for(int x=col;x<col+order;x++)
								{
									p=getPointFromGrid(y, x);
									pointArray[i] = p.getX();
									pointArray[i+1] = p.getY();
									pointArray[i+2] = p.getZ();
									i+=3;
								}
							}
							hwBezierSurface(gl, order, loopSteps, pointArray, 0);
						}
					}
				}
				catch(GLException ex)
				{
					setUseHWAcceleration(false);
				}
				finally
				{
					//ukonceni evulatoru
					gl.glDisable(gl.GL_MAP2_VERTEX_3);
				}
			}
			if (!useHWAcceleration)//pomocí CPU
			{
				ArrayList<IPoint3f> pointArray = new ArrayList<IPoint3f>(order*order);
				for(int col=0;col<cols-getDegree();col+=getDegree())//projdu plochy v sloupcích
				{
					for(int row=0;row<rows-getDegree();row+=getDegree())//projdu plochy v øádcích
					{

						for(int y=row,i=0;y<row+order;y++)
						{
							for(int x=col;x<col+order;x++)
							{
								pointArray.add(getPointFromGrid(y, x));
							}
						}
						swBezierSurface(gl, order, loopSteps, pointArray);
						pointArray.clear();
					}
				}
			}
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleGridNodes(gl);
	}

	/**
	 * Získání stupnì plochy.
	 */
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastavení stupnì plochy..
	 * Stupeò mùže být v rozsahu 1 až MAX_DEGREE, pokud zadáte neplatnou hodnotu, použije se nejbližší platná.
	 */
	public synchronized void setDegree(int degree)
	{
		if (degree < 1)
		{
			degree = 1;
		}
		if (degree > MAX_DEGREE)
		{
			degree = MAX_DEGREE;
		}
		this.degree = degree;
		guiDegree.setValue(degree);
		setState(ObjectState.notCounted);//køivka se zmìnila
		if(checkJoining())//kontrola napojování
		{
			firePointsChange();
		}
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	@Override
	protected final void setSize(int width, int height)
	{
		super.setSize(width, height);
		if(checkJoining())
		{
			firePointsChange();
		}
	}


	
	/**
	 * Vrátí true pokud je povolená HW akcelerace.
	 */
	public synchronized boolean isUseHWAcceleration()
	{
		return useHWAcceleration;
	}

	/**
	 * Povolení HW akcelerace.
	 */
	public synchronized void setUseHWAcceleration(boolean useHWAcceleration)
	{
		guiUseHWAcceleration.setSelected(useHWAcceleration);
		this.useHWAcceleration = useHWAcceleration;
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	/**
	 * Nastavení napojování ploch za sebou.
	 */
	public synchronized void setJoining(Joining joining)
	{
		if(joining==Joining.G0C0)
		{
			guiJoining.setSelectedIndex(0);
		}
		else if(joining==Joining.G1)
		{
			guiJoining.setSelectedIndex(1);
		}
		this.joining=joining;
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	/**
	 * Vrátí aktuálnì nastavené napojování.
	 */
	public synchronized Joining getJoining()
	{
		return joining;
	}

	/**
	 * Získání poètu krokù.
	 * 0=automatické urèení.
	 */
	public synchronized int getSteps()
	{
		return steps;
	}

	/**
	 * Nasatavení poètu krokù pro vykreslení. 0=automatické urèení.
	 */
	public final synchronized void setSteps(int steps)
	{
		if (steps < 0 || steps > MAX_STEPS)
		{
			steps = 0;
		}
		if (steps == 0)
		{
			guiSteps.setEnabled(false);
			guiChangingValue = true;//zabráním opìtovnému vyvolání akce zmìny pøi zmìnì gui
			guiStepsAuto.setSelected(true);
			//zabráním aby byla hodnota "0" v gui prvku pro poèet kroù
			if (((Integer) guiSteps.getValue()).intValue() == 0)
			{
				guiSteps.setValue(100);
			}
			guiChangingValue = false;
		} else
		{
			guiSteps.setEnabled(true);
			guiChangingValue = true;//zabráním opìtovnému vyvolání akce zmìny pøi zmìnì gui
			guiStepsAuto.setSelected(false);
			guiSteps.setValue(steps);
			guiChangingValue = false;
		}
		this.steps = steps;
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	/**
	 * Pøekontroluje celou plochu, a upraví napojování tak aby odpovídalo nastavenému napojování.
	 * @return Vrátí true pøi zmìnì køivky.
	 */
	private synchronized boolean checkJoining()
	{
		if(getJoining()==Joining.G0C0 || getDegree()==1)//volné napojování nebo stupeò plochy je 1
		{
			return false;
		}
		//pro napojování je tøeba vždy naèíst trojici bodù
		boolean change=false;

		/*
		 * kontrola øádkù
		 */
		for(int row=0;row<rows();row++)
		{
			//v i je vždy index tøetího bodu z trojice
			for(int i=getDegree()+1;i<columns();i+=getDegree())
			{
				Point3fChangeable first=getPointFromGrid(row,i-2),//první bod trojice
					middle=getPointFromGrid(row,i-1),//prostøední bod trojice
					last=getPointFromGrid(row,i);//poslední bod trojice
				IPoint3f pivot=PointOperations.pivot(first, last);//spoètu støed úseèky
				//jeli spoètený støed a vypoèítaný shodný, je to ok
				if(PointOperations.compareCoords(pivot, middle))
				{
					continue;
				}
				/*
				 * u G1 napojování musí být pomìr vzdáleností prostøedního bodu od pøedchozího bodu a k následujícímu
				 * bodu shodný pro všechny tojice v øadì, proto pro zjednodušení ovládání se tento pomìr bude udržovat
				 * 1:1 (protøední bod je pøesnì uprostøed pøímky mezi pøedchozím a následujícím bodem) a díky tomu
				 * pøi posunu bodu není tøeba mìnit celou øadu, protože pomìr je v celé øadì 1:1.
				 */
				else if(getJoining()==Joining.G1)
				{
					middle.setX(pivot.getX());
					middle.setY(pivot.getY());
					middle.setZ(pivot.getZ());
					change=true;
				}
			}
		}

		/*
		 * kontrola sloupcù
		 */
		for(int col=0;col<columns();col++)
		{
			//v i je vždy index tøetího bodu z trojice
			for(int i=getDegree()+1;i<rows();i+=getDegree())
			{
				Point3fChangeable first=getPointFromGrid(i-2,col),//první bod trojice
					middle=getPointFromGrid(i-1,col),//prostøední bod trojice
					last=getPointFromGrid(i,col);//poslední bod trojice
				IPoint3f pivot=PointOperations.pivot(first, last);//spoètu støed úseèky

				//jeli spoètený støed a vypoèítaný shodný, je to ok
				if(PointOperations.compareCoords(pivot, middle))
				{
					continue;
				}
				//u C1 napojování musí být prostøední bod pøesnì ve støedu
				else if(getJoining()==Joining.C1)
				{
					middle.setX(pivot.getX());
					middle.setY(pivot.getY());
					middle.setZ(pivot.getZ());
					change=true;
				}
			}
		}


		return change;
	}
}
