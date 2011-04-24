package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectSurface;
import cscg.model.objects.DegreeListener;
import cscg.model.objects.EnumNumberSpinnerModel;
import cscg.model.objects.IKnotVector;
import cscg.model.objects.INonUniformSurface;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.KnotListener;
import cscg.model.objects.MathUtils;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import cscg.model.objects.Point4f;
import cscg.model.objects.SurfaceKnotVector;
import cscg.ui.GLUtils;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.media.opengl.GL2;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implementace NURBS plochy.
 * @author Tomáš Režnar
 */
public class ObjectNURBSSurface
  extends AbstractObjectSurface<Point4fChangeable>
  implements INonUniformSurface<Point4fChangeable>
{

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
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastavení stupnì plochy.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI prvek pro nastavení poètu krokù vykreslení.
	 */
	private transient JSpinner guiSteps;
	/**
	 * Stupeò plochy.
	 * Musí být < poèet bodù každým smìrem.
	 */
	private volatile int degree = 3;
	/**
	 * Poèet krokù pro vykreslení køivky. 0=automatické urèení.
	 */
	private volatile int steps = 0;
	/**
	 * GUI prvek pro automatickou volbu poètu krokù.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * Pomocná promnìná, jež má hodnotu true bìhem zmìny hodnoty poètu krokù, kdy nechci aby došlo k opìtovnému zavolání
	 * metody pro zmìnu hodnoty.
	 */
	private boolean guiChangingValue = false;
	/**
	 * Posluchaèi zmìn stupnì køivky.
	 */
	private transient List<DegreeListener> degreeListeners;
	/**
	 * Uzlový vektor øádkù.
	 */
	private IKnotVector<ObjectNURBSSurface> rowKnot;
	/**
	 * Uzlový vektor sloupcù.
	 */
	private IKnotVector<ObjectNURBSSurface> colKnot;

	public ObjectNURBSSurface()
	{
		super();
		setName("NURBS plocha " + selfCounter++);
		pointPrototype=new Point4f(0,0,0,1);//øídící body mají váhy
		setState(ObjectState.notCounted);
		//základní tvar plochy
		makeSquare();
		setSize(4, 4);

		initListeners();
		rowKnot=new SurfaceKnotVector<ObjectNURBSSurface>(this,true);
		colKnot=new SurfaceKnotVector<ObjectNURBSSurface>(this,false);
		initTransients();

	}

	/**
	 * Inicializace transientních vlastností.
	 */
	private void initTransients()
	{
		rowKnot.addKnotListener(new KnotListener() {
			@Override
			public void eventKnotChanged(IKnotVector source)
			{
				for(ObjectListener l:listeners)
				{
					l.eventSpecificPropertiesChanged(ObjectNURBSSurface.this);
				}
			}
		});
		colKnot.addKnotListener(new KnotListener() {
			@Override
			public void eventKnotChanged(IKnotVector source)
			{
				for(ObjectListener l:listeners)
				{
					l.eventSpecificPropertiesChanged(ObjectNURBSSurface.this);
				}
			}
		});
		initGUI();
	}

	/**
	 * Vytvoøení seznamu posluchaèù.
	 */
	private void initListeners()
	{
		degreeListeners=new LinkedList<DegreeListener>();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		initListeners();
		in.defaultReadObject();
		initTransients();
	}
	/**
	 * Vytvoøení GUI.
	 */
	private void initGUI()
	{
		//inicializace gui prvkù nastavení objektu
		guiSetings = new JPanel(new GridBagLayout());
		guiDegree = new JSpinner(new SpinnerNumberModel(degree, 1, MAX_DEGREE, 1));
		guiSteps = new JSpinner(new SpinnerNumberModel(10, 0, MAX_STEPS, 1));
		guiStepsAuto = new JCheckBox("", true);
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

		//pøesnost
		gbc.gridy ++;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy ++;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiSteps, gbc);

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

		//nastavim defaultni presnost
		setSteps(steps);
	}

	@Override
	protected synchronized SpinnerModel getWidthSpinnerModel()
	{
		return new EnumNumberSpinnerModel(2, 4, 1, 100);
	}

	@Override
	protected synchronized SpinnerModel getHeightSpinnerModel()
	{
		return new EnumNumberSpinnerModel(2, 4, 1, 100);
	}

	@Override
	protected synchronized boolean eventHandlerWidthChange(int newWidth)
	{
		setSize(newWidth,rows());
		return true;
	}

	@Override
	protected synchronized boolean eventHandlerHeightChange(int newHeight)
	{
		setSize(columns(),newHeight);
		return true;
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
			int loopStepsRow=loopSteps,loopStepsCol=loopSteps;
			if (loopSteps == 0)
			{
				loopStepsRow = 5*rows();
				loopStepsCol = 5*columns();
				loopStepsRow = loopStepsRow>MAX_STEPS?MAX_STEPS:loopStepsRow;
				loopStepsCol = loopStepsCol>MAX_STEPS?MAX_STEPS:loopStepsCol;
			}
			GLUtils.glSetColor(gl, color);
			float rowStep=1f/(float)loopStepsRow;
			float colStep=1f/(float)loopStepsCol;

			List<IPoint3f> surface=new ArrayList<IPoint3f>((loopStepsRow+1)^(loopStepsCol+1));//møížka bodù plochy

			//uložím knot do pole+transformace do intervalu <0,1>
			float[] rowKnotArray = getRowKnot().getTransformedValues();
			float[] colKnotArray = getColumnKnot().getTransformedValues();

			//vnìjší sumy
			double sumx1,sumy1,sumz1;//èitatel
			double sumx2,sumy2,sumz2;//jmenovatel
			//vnitøní sumy
			double sumx1in,sumy1in,sumz1in;//èitatel
			double sumx2in,sumy2in,sumz2in;//jmenovatel
			int k=getDegree()+1;
			int n=rows()-1;
			int m=columns()-1;
			int i,j;
			double[] Nu=new double[rows()];
			Map<Double,double[]> Nv=new HashMap<Double, double[]>(loopStepsCol+1);
			double[] Nv_actual;
			IPoint4f p;
			int surfaceWidth=0,surfaceHeight=0;//poèítadlo velikosti møížky výsledné plochy

			for(double u=rowKnotArray[getDegree()]-rowStep,uMax=rowKnotArray[rowKnotArray.length-k],v,vMax;u<uMax;)
			{
				surfaceHeight++;
				surfaceWidth=0;
				u+=rowStep;
				if(u>uMax)
				{
					u=uMax;
				}

				for(i=0;i<=n;i++)
				{
					Nu[i]=MathUtils.bsplineBaseFunction(i, k, u, rowKnotArray);
				}

				for(v=colKnotArray[getDegree()]-colStep,vMax=colKnotArray[colKnotArray.length-k];v<vMax;)
				{
					surfaceWidth++;
					v+=colStep;
					if(v>vMax)
					{
						v=vMax;
					}

					if(Nv.containsKey(v)==false)
					{
						Nv_actual=new double[columns()];
						for(j=0;j<=m;j++)
						{
							Nv_actual[j]=MathUtils.bsplineBaseFunction(j, k, v, colKnotArray);
						}
						Nv.put(v, Nv_actual);
					}
					else
					{
						Nv_actual=Nv.get(v);
					}

					//výpoèet sum
					sumx1=sumy1=sumz1=sumx2=sumy2=sumz2=0;
					for(i=0;i<=n;i++)
					{
						sumx1in=sumy1in=sumz1in=sumx2in=sumy2in=sumz2in=0;
						for(j=0;j<=m;j++)
						{
							p = (IPoint4f)getPointFromGrid(i, j);
							sumx1in+=p.getW()*p.getX()*Nu[i]*Nv_actual[j];
							sumy1in+=p.getW()*p.getY()*Nu[i]*Nv_actual[j];
							sumz1in+=p.getW()*p.getZ()*Nu[i]*Nv_actual[j];
							sumx2in+=p.getW()*Nu[i]*Nv_actual[j];
							sumy2in+=p.getW()*Nu[i]*Nv_actual[j];
							sumz2in+=p.getW()*Nu[i]*Nv_actual[j];
						}
						sumx1+=sumx1in;
						sumy1+=sumy1in;
						sumz1+=sumz1in;
						sumx2+=sumx2in;
						sumy2+=sumy2in;
						sumz2+=sumz2in;
					}
					surface.add(new Point3f((float)(sumx1/sumx2), (float)(sumy1/sumy2), (float)(sumz1/sumz2)));
				}
			}
			//vykreslení plochy
			GLUtils.drawSurface(gl, surface, surfaceWidth, surfaceHeight,getMode());
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleGridNodes(gl);
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
	 * Získání stupnì plochy.
	 */
	@Override
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastavení stupnì plochy.
	 * Stupeò mùže být v rozsahu 1 až MAX_DEGREE, pokud zadáte neplatnou hodnotu, použije se nejbližší platná.
	 */
	@Override
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
		for (DegreeListener l : degreeListeners)
		{
			l.degreeChanged(this);
		}
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	@Override
	public IKnotVector getRowKnot()
	{
		return rowKnot;
	}

	@Override
	public IKnotVector getColumnKnot()
	{
		return colKnot;
	}

	@Override
	public void addDegreeListener(DegreeListener degreeListener)
	{
		degreeListeners.add(degreeListener);
	}

	@Override
	public void removeDegreeListener(DegreeListener degreeListener)
	{
		degreeListeners.remove(degreeListener);
	}
}
