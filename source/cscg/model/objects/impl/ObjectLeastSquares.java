package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cscg.model.objects.Bounds;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import umontreal.iro.lecuyer.functionfit.LeastSquares;
import umontreal.iro.lecuyer.functions.Polynomial;

/**
 * Interpola�n� k�ivky.
 * @author Tom� Re�nar
 */
public class ObjectLeastSquares extends AbstractObjectCurve<Point3f>
{
	/**
	 * maxim�ln� mo�n� stupe� k�ivky.
	 */
	public final int MAX_DEGREE=99;
	/**
	 * Po��tadlo instanc�.
	 */
	private volatile static int selfCounter=1;
	/**
	 * Polynom pro v�po�et y sou�adnice.
	 */
	private Polynomial polynominalY;
	/**
	 * Polynom pro v�po�et z sou�adnice.
	 */
	private Polynomial polynominalZ;
	/**
	 * Stupe� k�ivky.
	 * Mus� b�t < po�et bod�.
	 */
	private volatile int degree=1;
	/**
	 * GUI prvek pro nastaven� stupn� k�ivky.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI panel s nastaven�m objektu.
	 */
	private transient JPanel guiSetings;

	public ObjectLeastSquares()
	{
		super();
		setName("Aprox. metodou nejm. �tverc� "+selfCounter++);

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
	 * Vytvo�en� GUI.
	 */
	private void initGUI()
	{
		//inicializace gui prvk� nastaven� objektu
		guiDegree=new JSpinner(new SpinnerNumberModel(degree, 1, MAX_DEGREE, 1));
		guiSetings=new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridwidth=2;
		gbc.weightx=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		guiSetings.add(super.getObjectGUI(),gbc);

		gbc.gridy=1;
		gbc.gridwidth=1;
		gbc.insets=new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupe� k�ivky"),gbc);

		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		guiSetings.add(guiDegree,gbc);

		//poslucha�i zm�n nastaven�
		guiDegree.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setDegree((Integer)guiDegree.getValue());
			}
		});
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(state==ObjectState.notCounted)
		{
			int pointsCount=points.size();//pocet bodu
			if(pointsCount>getDegree())//existuje pouze kdy� stupe� k�ivky je men�� ne� po�et bod�
			{
				try
				{
					double[] x=new double[pointsCount],
						y=new double[pointsCount],
						z=new double[pointsCount];
					//napln�m pole bod�
					for(int i=0;i<pointsCount;i++)
					{
						x[i]=points.get(i).getX();
						y[i]=points.get(i).getY();
						z[i]=points.get(i).getZ();
					}
					polynominalY=new LeastSquares(x, y, getDegree());
					polynominalZ=new LeastSquares(x, z, getDegree());

					setState(ObjectState.OK);
					setStateMessage(null);
				}
				//p�i bodech um�st�n�ch nad sebou k�ivka neexistuje
				catch(IllegalArgumentException ex)
				{
					setStateMessage("Pro vykreslen� k�ivky p�idejte dal�� body.");
					setState(ObjectState.inputError);
				}
			}
			else//nedostatek bod�
			{
				setStateMessage("Nedostatek bod� pro vykreslen�. P�idejte bod�: "+(degree-pointsCount+1));
				setState(ObjectState.inputError);
			}
		}

		//vykresleni
		if(getState()==ObjectState.OK)
		{
			Bounds drawBounds=getBounds();
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null),0);
			double step=1.,//step=1/scale,
				startInt=drawBounds.getX1()>getBounds().getX1()?drawBounds.getX1():getBounds().getX1(),//za��tek interpolace
				endInt=drawBounds.getX2()<getBounds().getX2()?drawBounds.getX2():getBounds().getX2();//konec interpolace
			//vlastn� interpolace
			gl.glBegin(gl.GL_LINE_STRIP);
			for(double i=startInt;i<endInt;i+=step)
			{
				gl.glVertex3d(i, polynominalY.evaluate(i),polynominalZ.evaluate(i));
			}
			gl.glVertex3d(endInt, polynominalY.evaluate(endInt),polynominalZ.evaluate(endInt));//dot�hnut� do bodu
			gl.glEnd();

		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleNodes(gl);
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSetings;
	}

	/**
	 * Z�sk�n� stupn� k�ivky.
	 */
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastaven� stupn� k�ivky.
	 * Stupe� m��e b�t v rozsahu 1 a� MAX_DEGREE, pokud zad�te neplatnou hodnotu, pou�ije se nejbli��� platn�.
	 */
	public synchronized void setDegree(int degree)
	{
		if(degree<1)
		{
			degree=1;
		}
		if(degree>MAX_DEGREE)
		{
			degree=MAX_DEGREE;
		}
		this.degree = degree;
		guiDegree.setValue(degree);
		setState(ObjectState.notCounted);//k�ivka se zm�nila
		for(ObjectListener l:listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	
}
