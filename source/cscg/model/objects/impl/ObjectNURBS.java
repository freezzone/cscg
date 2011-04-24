package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.CurveKnotVector;
import cscg.model.objects.DegreeListener;
import cscg.model.objects.IKnotVector;
import cscg.model.objects.INonUniformCurve;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.KnotListener;
import cscg.model.objects.MathUtils;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point4f;
import cscg.ui.GLUtils;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * NURBS køivka.
 * @author Tomáš Režnar
 */
public class ObjectNURBS extends AbstractObjectCurve<Point4fChangeable> implements INonUniformCurve
{

	/**
	 * Maximální možný stupeò køivky.
	 */
	public final int MAX_DEGREE = 90;
	/**
	 * Maximální možný nastavitelný poèet krokù pro vykreslení.
	 */
	public final int MAX_STEPS = 200;
	/**
	 * Poèítadlo instancí.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * Stupeò køivky.
	 * Musí být < poèet bodù.
	 */
	private volatile int degree = 3;
	/**
	 * Poèet krokù pro vykreslení køivky. 0=automatické urèení.
	 */
	private volatile int steps = 0;
	/**
	 * Povolení použití grafické karty pro urychlení vykreslení.
	 */
	private volatile boolean useHWAcceleration = false;
	/**
	 * GUI prvek pro nastavení stupnì køivky.
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
	 * GUI prvek pro automatickou volbu poètu krokù.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * Pomocná promnìná, jež má hodnotu true bìhem zmìny hodnoty poètu krokù, kdy nechci aby došlo k opìtovnému zavolání
	 * metody pro zmìnu hodnoty.
	 */
	private boolean guiChangingValue = false;
	/**
	 * GUI prvek pro povolení HW akcelerace.
	 */
	private transient JCheckBox guiUseHWAcceleration;
	/**
	 * Posluchaèi zmìn stupnì køivky.
	 */
	private transient List<DegreeListener> degreeListeners;
	/**
	 * Uzlový vektor.
	 */
	private IKnotVector<ObjectNURBS> knot;

	public ObjectNURBS()
	{
		super();
		Point4fChangeable prototype = new Point4fChangeable();
		prototype.setW(1);
		pointPrototype = prototype;
		setName("NURBS køivka " + selfCounter++);
		pointPrototype = new Point4f(0, 0, 0, 1);//øídící body mají váhy
		initListeners();
		knot = new CurveKnotVector<ObjectNURBS>(this);
		initTransients();
	}

	/**
	 * Inicializace transientních vlastností.
	 */
	private void initTransients()
	{
		knot.addKnotListener(new KnotListener()
		{

			@Override
			public void eventKnotChanged(IKnotVector source)
			{
				for (ObjectListener l : listeners)
				{
					l.eventSpecificPropertiesChanged(ObjectNURBS.this);
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
		degreeListeners = new LinkedList<DegreeListener>();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		initListeners();
		in.defaultReadObject();
		initTransients();
	}

	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.defaultWriteObject();
	}

	/**
	 * Vytvoøení GUI.
	 */
	private void initGUI()
	{
		//inicializace gui prvkù nastavení objektu
		guiDegree = new JSpinner(new SpinnerNumberModel(degree, 1, MAX_DEGREE, 1));
		guiSetings = new JPanel(new GridBagLayout());
		guiSteps = new JSpinner(new SpinnerNumberModel(0, 0, MAX_STEPS, 1));
		guiStepsAuto = new JCheckBox("", true);
		guiUseHWAcceleration = new JCheckBox("", true);
		GridBagConstraints gbc = new GridBagConstraints();

		//zdìdìné gui
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		guiSetings.add(super.getObjectGUI(), gbc);

		//stupeò
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupeò køivky"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiDegree, gbc);

		//pøesnost
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
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
		//guiSetings.add(new JLabel("HW akcelerace"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		//guiSetings.add(guiUseHWAcceleration, gbc);

		//posluchaèi zmìn nastavení
		guiDegree.addChangeListener(new ChangeListener()
		{

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

		guiStepsAuto.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
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

		guiUseHWAcceleration.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setUseHWAcceleration(guiUseHWAcceleration.isSelected());
			}
		});

		setSteps(steps);
		setUseHWAcceleration(useHWAcceleration);
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if (state == ObjectState.notCounted)
		{
			int pointsCount = points.size();//pocet bodu
			if (pointsCount > getDegree())//existuje pouze když stupeò køivky je menší než poèet bodù
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bodù
			{
				setStateMessage("Nedostatek bodù pro vykreslení. Pøidejte bodù: " + (degree - pointsCount + 1));
				setState(ObjectState.inputError);
			}
		}

		//vykresleni
		if (getState() == ObjectState.OK)
		{
			//nastavení vlastností èáry
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null), 0);

			//urèení poètu krokù
			int loopSteps = getSteps();
			if (loopSteps == 0)
			{
				loopSteps = 20 * points.size();
				loopSteps = loopSteps>MAX_STEPS?MAX_STEPS:loopSteps;
				//steps = (int)scale*40*points.size();
			}

			/*
			 * Výpoèet pomocí grafické karty
			 */
			/*if (useHWAcceleration)
			{
			//body uložím do obyèejného pole
			float[] pointArray = new float[points.size() * 4];
			int i = 0;
			for (IPoint3f p : points)
			{
			pointArray[i] = p.getX();
			pointArray[i + 1] = p.getY();
			pointArray[i + 2] = p.getZ();
			pointArray[i + 3] = ((IPoint4f) p).getW();
			i += 4;
			}
			//uložím knot do pole
			float[] knotArray = new float[knot.size()];
			i = 0;
			for (Float p : knot)
			{
			knotArray[i] = p.floatValue();
			i++;
			}

			//vlastní vykreslení
			try
			{
			GLU glu=new GLU();
			GLUnurbs nurbs = glu.gluNewNurbsRenderer();
			//glu.gluNurbsProperty(nurbs,glu.GLU_U_STEP,steps);//nepodporavane v JOGLu
			glu.gluBeginCurve(nurbs);
			glu.gluNurbsCurve(nurbs,
			knotArray.length, knotArray,
			4,
			pointArray,
			getDegree()+1,
			gl.GL_MAP1_VERTEX_4);
			glu.gluEndCurve(nurbs);
			} catch (GLException ex)
			{
			setUseHWAcceleration(false);//zakažu HW akceleraci
			}
			}*/
			/*
			 * Výpoèet pomocí CPU
			 */
			if (!useHWAcceleration)
			{
				double step = 1. / (double) loopSteps;

				//uložím knot do pole+transformace do intervalu <0,1>
				float[] knotArray = knot.getTransformedValues();

				double sumx1, sumy1, sumz1;
				double sumx2, sumy2, sumz2;
				int k = getDegree() + 1;
				int n = points.size() - 1;
				int i;
				double[] N = new double[points.size()];
				IPoint4f p;

				gl.glBegin(gl.GL_LINE_STRIP);
				for (double t = knotArray[getDegree()] - step, tMax = knotArray[knotArray.length - k]; t < tMax;)
				{
					t += step;
					if (t > tMax)
					{
						t = tMax;
					}

					for (i = 0; i <= n; i++)
					{
						N[i] = MathUtils.bsplineBaseFunction(i, k, t, knotArray);
					}

					sumx1 = sumy1 = sumz1 = sumx2 = sumy2 = sumz2 = 0;
					for (i = 0; i <= n; i++)
					{
						p = (IPoint4f) points.get(i);
						sumx1 += p.getW() * p.getX() * N[i];
						sumy1 += p.getW() * p.getY() * N[i];
						sumz1 += p.getW() * p.getZ() * N[i];
						sumx2 += p.getW() * N[i];
						sumy2 += p.getW() * N[i];
						sumz2 += p.getW() * N[i];
					}
					gl.glVertex3d(sumx1 / sumx2, sumy1 / sumy2, sumz1 / sumz2);
				}
				gl.glEnd();
			}
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		//vykreslení pomocných èar
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		GLUtils.drawSimpleLine(gl, points);
		drawSimpleNodes(gl);
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSetings;
	}

	@Override
	public synchronized int getDegree()
	{
		return degree;
	}

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
	 * Vrátí true pokud je povolená HW akcelerace.
	 */
	public synchronized boolean isUseHWAcceleration()
	{
		return useHWAcceleration;
	}

	/**
	 * Povolení HW akcelerace.
	 */
	public final synchronized void setUseHWAcceleration(boolean useHWAcceleration)
	{
		guiUseHWAcceleration.setSelected(useHWAcceleration);
		//pøi HW akceleraci nelze ruènì volit poèet krokù vykreslení
		if (useHWAcceleration)
		{
			setSteps(0);
			guiStepsAuto.setEnabled(false);
		} else
		{

			guiStepsAuto.setEnabled(true);
		}
		this.useHWAcceleration = useHWAcceleration;
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	@Override
	public IKnotVector getKnot()
	{
		return knot;
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
