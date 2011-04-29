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
	 * Maximální možný stupeň křivky.
	 */
	public final int MAX_DEGREE = 50;
	/**
	 * Maximální možný nastavitelný počet kroků pro vykreslení.
	 */
	public final int MAX_STEPS = 100;
	/**
	 * Počítadlo instancí.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * GUI prvek pro nastavení stupně plochy.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastavení počtu kroků vykreslení.
	 */
	private transient JSpinner guiSteps;
	/**
	 * Počet kroků pro vykreslení křivky. 0=automatické určení.
	 */
	private volatile int steps = 0;
	/**
	 * Stupeň plochy.
	 * Musí být < počet bodů každým směrem.
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
	 * GUI prvek pro automatickou volbu počtu kroků.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * GUI prvek pro povolení HW akcelerace.
	 */
	private transient JCheckBox guiUseHWAcceleration;
	/**
	 * GUI prvek pro výběr napojování jednotlivých křivek.
	 */
	private transient JComboBox guiJoining;
	/**
	 * Pomocná promněná, jež má hodnotu true během změny hodnoty počtu kroků, kdy nechci aby došlo k opětovnému zavolání
	 * metody pro změnu hodnoty.
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
	 * Vytvoření GUI.
	 */
	private void initGUI()
	{
		//inicializace gui prvků nastavení objektu
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

		//stupeň
		gbc.weightx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupeň plochy"), gbc);

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

		//přesnost
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto přesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Přesnost"), gbc);

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

		//vložím do nadřazeného gui
		super.setSpecificGUI(guiSetings);

		//posluchači změn nastavení
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
	 * @param order stupeň plochy+1
	 * @param steps počet kroků pro vykreslení každé plochy (pro každý parametr)
	 * @param points pole bodů, kde jsou souřadnice x,y,z uloženy v řadě za sebou
	 * @param pointOffset offset v poli bodů vydělený 3
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
	 * @param order stupeň plochy+1
	 * @param steps počet kroků pro vykreslení každé plochy (pro každý parametr)
	 * @param points list bodů, kde jsou body uloženy za sebou po řádcích
	 */
	private synchronized void swBezierSurface(GL2 gl, int order, int steps, List<IPoint3f> points)
	{
		double step=1./(double)steps;
		double u,v;//parametry
		double Bu,Bv;//hodnota bernsteinova polynomu
		int i,j;//index v cyklu sum
		double sumaX, sumaY, sumaZ;//součet sum
		IPoint3f point;
		ArrayList<IPoint3f> surfacePoints=new ArrayList<IPoint3f>((steps+1)*(steps+1));//pole vypočetných bodů plochy
		int rows=0,cols=0;//počítadlo sloupců a řádků
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
			if (columns() > getDegree() && rows() > getDegree())//existuje pouze když stupeň křivky je menší než počet bodů
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bodů
			{
				setStateMessage("Nedostatek bodů pro vykreslení.");
				setState(ObjectState.inputError);
			}
		}

		if(ObjectState.OK==getState())
		{
			//určení počtu kroků
			int loopSteps = getSteps();
			if (loopSteps == 0)
			{
				loopSteps = (int) Math.ceil(20 * getDegree());
				//steps = (int) Math.ceil(scale * 20 * getDegree());
			}

			GLUtils.glSetColor(gl, color);
			
			int order=getDegree()+1;//počet bodů pro jeden úsek křivky
			//body uložím do obyčejného pole
			int cols=columns();
			int rows=rows();
			IPoint3f p;
			
			//načtu body pro jednu plochu do pole
			if (useHWAcceleration)//pomocí HW
			{
				try
				{
					// povolení 2D evaluátoru
					gl.glEnable(gl.GL_MAP2_VERTEX_3);
					float[] pointArray = new float[order*order*3];
					for(int col=0;col<cols-getDegree();col+=getDegree())//projdu plochy v sloupcích
					{
						for(int row=0;row<rows-getDegree();row+=getDegree())//projdu plochy v řádcích
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
					for(int row=0;row<rows-getDegree();row+=getDegree())//projdu plochy v řádcích
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
	 * Získání stupně plochy.
	 */
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastavení stupně plochy..
	 * Stupeň může být v rozsahu 1 až MAX_DEGREE, pokud zadáte neplatnou hodnotu, použije se nejbližší platná.
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
		setState(ObjectState.notCounted);//křivka se změnila
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
	 * Vrátí aktuálně nastavené napojování.
	 */
	public synchronized Joining getJoining()
	{
		return joining;
	}

	/**
	 * Získání počtu kroků.
	 * 0=automatické určení.
	 */
	public synchronized int getSteps()
	{
		return steps;
	}

	/**
	 * Nasatavení počtu kroků pro vykreslení. 0=automatické určení.
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
			guiChangingValue = true;//zabráním opětovnému vyvolání akce změny při změně gui
			guiStepsAuto.setSelected(true);
			//zabráním aby byla hodnota "0" v gui prvku pro počet kroů
			if (((Integer) guiSteps.getValue()).intValue() == 0)
			{
				guiSteps.setValue(100);
			}
			guiChangingValue = false;
		} else
		{
			guiSteps.setEnabled(true);
			guiChangingValue = true;//zabráním opětovnému vyvolání akce změny při změně gui
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
	 * Překontroluje celou plochu, a upraví napojování tak aby odpovídalo nastavenému napojování.
	 * @return Vrátí true při změně křivky.
	 */
	private synchronized boolean checkJoining()
	{
		if(getJoining()==Joining.G0C0 || getDegree()==1)//volné napojování nebo stupeň plochy je 1
		{
			return false;
		}
		//pro napojování je třeba vždy načíst trojici bodů
		boolean change=false;

		/*
		 * kontrola řádků
		 */
		for(int row=0;row<rows();row++)
		{
			//v i je vždy index třetího bodu z trojice
			for(int i=getDegree()+1;i<columns();i+=getDegree())
			{
				Point3fChangeable first=getPointFromGrid(row,i-2),//první bod trojice
					middle=getPointFromGrid(row,i-1),//prostřední bod trojice
					last=getPointFromGrid(row,i);//poslední bod trojice
				IPoint3f pivot=PointOperations.pivot(first, last);//spočtu střed úsečky
				//jeli spočtený střed a vypočítaný shodný, je to ok
				if(PointOperations.compareCoords(pivot, middle))
				{
					continue;
				}
				/*
				 * u G1 napojování musí být poměr vzdáleností prostředního bodu od předchozího bodu a k následujícímu
				 * bodu shodný pro všechny tojice v řadě, proto pro zjednodušení ovládání se tento poměr bude udržovat
				 * 1:1 (protřední bod je přesně uprostřed přímky mezi předchozím a následujícím bodem) a díky tomu
				 * při posunu bodu není třeba měnit celou řadu, protože poměr je v celé řadě 1:1.
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
		 * kontrola sloupců
		 */
		for(int col=0;col<columns();col++)
		{
			//v i je vždy index třetího bodu z trojice
			for(int i=getDegree()+1;i<rows();i+=getDegree())
			{
				Point3fChangeable first=getPointFromGrid(i-2,col),//první bod trojice
					middle=getPointFromGrid(i-1,col),//prostřední bod trojice
					last=getPointFromGrid(i,col);//poslední bod trojice
				IPoint3f pivot=PointOperations.pivot(first, last);//spočtu střed úsečky

				//jeli spočtený střed a vypočítaný shodný, je to ok
				if(PointOperations.compareCoords(pivot, middle))
				{
					continue;
				}
				//u C1 napojování musí být prostřední bod přesně ve středu
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
