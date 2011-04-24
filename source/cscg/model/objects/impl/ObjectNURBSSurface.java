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
 * @author Tom� Re�nar
 */
public class ObjectNURBSSurface
  extends AbstractObjectSurface<Point4fChangeable>
  implements INonUniformSurface<Point4fChangeable>
{

	/**
	 * Maxim�ln� mo�n� stupe� k�ivky.
	 */
	public final int MAX_DEGREE = 50;
	/**
	 * Maxim�ln� mo�n� nastaviteln� po�et krok� pro vykreslen�.
	 */
	public final int MAX_STEPS = 100;
	/**
	 * Po��tadlo instanc�.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * GUI panel s nastaven�m objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastaven� stupn� plochy.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI prvek pro nastaven� po�tu krok� vykreslen�.
	 */
	private transient JSpinner guiSteps;
	/**
	 * Stupe� plochy.
	 * Mus� b�t < po�et bod� ka�d�m sm�rem.
	 */
	private volatile int degree = 3;
	/**
	 * Po�et krok� pro vykreslen� k�ivky. 0=automatick� ur�en�.
	 */
	private volatile int steps = 0;
	/**
	 * GUI prvek pro automatickou volbu po�tu krok�.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * Pomocn� promn�n�, je� m� hodnotu true b�hem zm�ny hodnoty po�tu krok�, kdy nechci aby do�lo k op�tovn�mu zavol�n�
	 * metody pro zm�nu hodnoty.
	 */
	private boolean guiChangingValue = false;
	/**
	 * Poslucha�i zm�n stupn� k�ivky.
	 */
	private transient List<DegreeListener> degreeListeners;
	/**
	 * Uzlov� vektor ��dk�.
	 */
	private IKnotVector<ObjectNURBSSurface> rowKnot;
	/**
	 * Uzlov� vektor sloupc�.
	 */
	private IKnotVector<ObjectNURBSSurface> colKnot;

	public ObjectNURBSSurface()
	{
		super();
		setName("NURBS plocha " + selfCounter++);
		pointPrototype=new Point4f(0,0,0,1);//��d�c� body maj� v�hy
		setState(ObjectState.notCounted);
		//z�kladn� tvar plochy
		makeSquare();
		setSize(4, 4);

		initListeners();
		rowKnot=new SurfaceKnotVector<ObjectNURBSSurface>(this,true);
		colKnot=new SurfaceKnotVector<ObjectNURBSSurface>(this,false);
		initTransients();

	}

	/**
	 * Inicializace transientn�ch vlastnost�.
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
	 * Vytvo�en� seznamu poslucha��.
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
	 * Vytvo�en� GUI.
	 */
	private void initGUI()
	{
		//inicializace gui prvk� nastaven� objektu
		guiSetings = new JPanel(new GridBagLayout());
		guiDegree = new JSpinner(new SpinnerNumberModel(degree, 1, MAX_DEGREE, 1));
		guiSteps = new JSpinner(new SpinnerNumberModel(10, 0, MAX_STEPS, 1));
		guiStepsAuto = new JCheckBox("", true);
		GridBagConstraints gbc = new GridBagConstraints();

		//stupe�
		gbc.weightx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupe� plochy"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiDegree, gbc);

		//p�esnost
		gbc.gridy ++;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto p�esnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy ++;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("P�esnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiSteps, gbc);

		//vlo��m do nad�azen�ho gui
		super.setSpecificGUI(guiSetings);

		//poslucha�i zm�n nastaven�
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
				if (guiChangingValue == false)//obrana pred zacyklen�m
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
				if (guiChangingValue == true)//obrana pred zacyklen�m
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
			if (columns() > getDegree() && rows() > getDegree())//existuje pouze kdy� stupe� k�ivky je men�� ne� po�et bod�
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bod�
			{
				setStateMessage("Nedostatek bod� pro vykreslen�.");
				setState(ObjectState.inputError);
			}
		}

		if(ObjectState.OK==getState())
		{
			//ur�en� po�tu krok�
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

			List<IPoint3f> surface=new ArrayList<IPoint3f>((loopStepsRow+1)^(loopStepsCol+1));//m��ka bod� plochy

			//ulo��m knot do pole+transformace do intervalu <0,1>
			float[] rowKnotArray = getRowKnot().getTransformedValues();
			float[] colKnotArray = getColumnKnot().getTransformedValues();

			//vn�j�� sumy
			double sumx1,sumy1,sumz1;//�itatel
			double sumx2,sumy2,sumz2;//jmenovatel
			//vnit�n� sumy
			double sumx1in,sumy1in,sumz1in;//�itatel
			double sumx2in,sumy2in,sumz2in;//jmenovatel
			int k=getDegree()+1;
			int n=rows()-1;
			int m=columns()-1;
			int i,j;
			double[] Nu=new double[rows()];
			Map<Double,double[]> Nv=new HashMap<Double, double[]>(loopStepsCol+1);
			double[] Nv_actual;
			IPoint4f p;
			int surfaceWidth=0,surfaceHeight=0;//po��tadlo velikosti m��ky v�sledn� plochy

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

					//v�po�et sum
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
			//vykreslen� plochy
			GLUtils.drawSurface(gl, surface, surfaceWidth, surfaceHeight,getMode());
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleGridNodes(gl);
	}

	/**
	 * Z�sk�n� po�tu krok�.
	 * 0=automatick� ur�en�.
	 */
	public synchronized int getSteps()
	{
		return steps;
	}

	/**
	 * Nasataven� po�tu krok� pro vykreslen�. 0=automatick� ur�en�.
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
			guiChangingValue = true;//zabr�n�m op�tovn�mu vyvol�n� akce zm�ny p�i zm�n� gui
			guiStepsAuto.setSelected(true);
			//zabr�n�m aby byla hodnota "0" v gui prvku pro po�et kro�
			if (((Integer) guiSteps.getValue()).intValue() == 0)
			{
				guiSteps.setValue(100);
			}
			guiChangingValue = false;
		} else
		{
			guiSteps.setEnabled(true);
			guiChangingValue = true;//zabr�n�m op�tovn�mu vyvol�n� akce zm�ny p�i zm�n� gui
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
	 * Z�sk�n� stupn� plochy.
	 */
	@Override
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastaven� stupn� plochy.
	 * Stupe� m��e b�t v rozsahu 1 a� MAX_DEGREE, pokud zad�te neplatnou hodnotu, pou�ije se nejbli��� platn�.
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
		setState(ObjectState.notCounted);//k�ivka se zm�nila
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
