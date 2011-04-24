package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import cscg.ui.GLUtils;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implementace Fergusonovy k�ivky.
 * @author Tom� Re�nar
 */
public final class ObjectFerguson extends AbstractObjectCurve<Point3f>
{
	/**
	 * Maxim�ln� mo�n� nastaviteln� po�et krok� pro vykreslen�.
	 */
	public final int MAX_STEPS=200;
	/**
	 * Po��tadlo instanc�.
	 */
	private volatile static int selfCounter=1;

	/**
	 * B�zov� matice.
	 */
	private static final DoubleMatrix2D fergusonBase=new DenseDoubleMatrix2D(new double[][]{
			{2,-2,1,1},
			{-3,3,-2,-1},
			{0,0,1,0},
			{1,0,0,0}
	});

	/**
	 * Po�et krok� pro vykreslen� k�ivky. 0=automatick� ur�en�.
	 */
	private volatile int steps=0;

	/**
	 * Objekt pro �e�en� matic.
	 */
	private Algebra alg=Algebra.DEFAULT;
	/**
	 * Pomocn� matice pro v�po�et sou�adnice x.
	 */
	private DoubleMatrix1D mx=new DenseDoubleMatrix1D(4);
	/**
	 * Pomocn� matice pro v�po�et sou�adnice y.
	 */
	private DoubleMatrix1D my=new DenseDoubleMatrix1D(4);
	/**
	 * Pomocn� matice pro v�po�et sou�adnice z.
	 */
	private DoubleMatrix1D mz=new DenseDoubleMatrix1D(4);
	/**
	 * Pomocn� matice m�n�c� se dle aktu�ln�ho parametru.
	 */
	DoubleMatrix1D parametricMatrix=new DenseDoubleMatrix1D(new double[]{0,0,0,1});
	/**
	 * GUI prvek pro nastaven� po�tu krok� vykreslen�.
	 */
	private transient JSpinner guiSteps;
	/**
	 * GUI panel pro nastaven� specifick�ch vlastnost� k�ivky.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro automatickou volbu po�tu krok�.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * Pomocn� promn�n�, je� m� hodnotu true b�hem zm�ny hodnoty po�tu krok�, kdy nechci aby do�lo k op�tovn�mu zavol�n�
	 * metody pro zm�nu hodnoty.
	 */
	private boolean guiChangingValue=false;

	public ObjectFerguson()
	{
		super();
		setName("Fergusonova k�ivka "+selfCounter++);

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
	 * Vytvo�en� z�kladn�ho GUI.
	 */
	private void initGUI()
	{
		//vytvo��m gui pro nastaven� specifick�ch vlastnost� objekt�
		guiSteps=new JSpinner(new SpinnerNumberModel(0, 0, MAX_STEPS, 1));
		guiStepsAuto=new JCheckBox("", true);
		guiSetings=new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridwidth=2;
		gbc.weightx=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		guiSetings.add(super.getObjectGUI(),gbc);

		gbc.gridy=1;
		gbc.gridx=0;
		gbc.gridwidth=1;
		gbc.insets=new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Auto p�esnost"),gbc);

		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto,gbc);

		gbc.gridy=2;
		gbc.gridx=0;
		gbc.insets=new Insets(0, 0, 0, 0);
		gbc.gridwidth=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.WEST;
		guiSetings.add(new JLabel("P�esnost"),gbc);

		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		guiSetings.add(guiSteps,gbc);

		//nastav�m poslucha�e
		guiSteps.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if(guiChangingValue==false)//obrana pred zacyklen�m
				{
					setSteps((Integer)guiSteps.getValue());
				}
			}
		});

		guiStepsAuto.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if(guiChangingValue==true)//obrana pred zacyklen�m
				{
					return;
				}
				if(guiStepsAuto.isSelected())
				{
					setSteps(0);
				}
				else
				{
					setSteps((Integer)guiSteps.getValue());
				}
			}
		});

		setSteps(steps);//nastavim p�esnost
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(state==ObjectState.notCounted)
		{
			if(points.size()>=4)
			{
				//v�echny vypo�ty se prov�d� a� p�i vykreslen�
				setState(ObjectState.OK);
				setStateMessage(null);
			}
			else//nedostatek bod�
			{
				setStateMessage("Zadejte minim�ln� �ty�i body pro vykreslen� k�ivky.");
				setState(ObjectState.inputError);
			}
		}

		//vykresleni
		if(getState()==ObjectState.OK)
		{
			double P0X,P0Y,P1X,P1Y,p0X,p0Y,p1X,p1Y;
			IPoint3f point1,point2,pointVector1,pointVector2;
			double step;
			int loopSteps=getSteps();
			if(loopSteps==0)
			{
				step=1./40.;
				//step=(1/scale)/100;
			}
			else
			{
				step=1/(double)loopSteps;
			}
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null),0);
			gl.glBegin(gl.GL_LINE_STRIP);
			for(int i=0;i<points.size()-3;i=i+2)
			{
				point1=points.get(i);
				pointVector1=points.get(i+1);
				point2=points.get(i+2);
				pointVector2=points.get(i+3);
				//matice pro x
				mx.set(0, point1.getX());
				mx.set(1, point2.getX());
				mx.set(2, pointVector1.getX()-point1.getX());
				mx.set(3, pointVector2.getX()-point2.getX());
				//matice pro y
				my.set(0, point1.getY());
				my.set(1, point2.getY());
				my.set(2, pointVector1.getY()-point1.getY());
				my.set(3, pointVector2.getY()-point2.getY());
				//matice pro z
				mz.set(0, point1.getZ());
				mz.set(1, point2.getZ());
				mz.set(2, pointVector1.getZ()-point1.getZ());
				mz.set(3, pointVector2.getZ()-point2.getZ());

				//zm�n�m b�zi
				DoubleMatrix1D fergusonX=alg.mult(fergusonBase, mx);
				DoubleMatrix1D fergusonY=alg.mult(fergusonBase, my);
				DoubleMatrix1D fergusonZ=alg.mult(fergusonBase, mz);

				//vykreslim
				for(double param=-step;param<1;)
				{
					param+=step;
					if(param>1)
					{
						param=1;
					}

					parametricMatrix.set(0, Math.pow(param, 3));
					parametricMatrix.set(1, Math.pow(param, 2));
					parametricMatrix.set(2, param);
					gl.glVertex3d(alg.mult(parametricMatrix, fergusonX), alg.mult(parametricMatrix, fergusonY),alg.mult(parametricMatrix, fergusonZ));
				}
			}
			gl.glEnd();
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleNodes(gl);
		//vykreslen� �ipek
		int vectorsCount=points.size()-(points.size()%2);//2 body pro te�nu
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		
		IPoint3f from,to;
		GLU glu=new GLU();
		for(int i=0;i<vectorsCount;i+=2)
		{
			from=points.get(i);
			to=points.get(i+1);
			GLUtils.drawArrow(gl, glu, projection, from, to, 4, 12);
		}
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSetings;
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
	public synchronized void setSteps(int steps)
	{
		if(steps<0 || steps>MAX_STEPS)
		{
			steps=0;
		}
		if(steps==0)
		{
			guiSteps.setEnabled(false);
			guiChangingValue=true;//zabr�n�m op�tovn�mu vyvol�n� akce zm�ny p�i zm�n� gui
			guiStepsAuto.setSelected(true);
			//zabr�n�m aby byla hodnota "0" v gui prvku pro po�et kro�
			if(((Integer)guiSteps.getValue()).intValue()==0)
			guiSteps.setValue(1);
			guiChangingValue=false;
		}
		else
		{
			guiSteps.setEnabled(true);
			guiChangingValue=true;//zabr�n�m op�tovn�mu vyvol�n� akce zm�ny p�i zm�n� gui
			guiStepsAuto.setSelected(false);
			guiSteps.setValue(steps);
			guiChangingValue=false;
		}
		this.steps = steps;
		for(ObjectListener l:listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	
}
