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
import cscg.model.objects.PointOperations;
import cscg.ui.GLUtils;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implementace Coonsovy B-spline k�ivky.
 * @author Tom� Re�nar
 */
public final class ObjectCoonsBSpline extends AbstractObjectCurve<Point3f>
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
	private static final DoubleMatrix2D coonsBase=new DenseDoubleMatrix2D(new double[][]{
			{-1./6.,3./6.,-3./6.,1./6.},
			{3./6.,-6./6.,3./6.,0},
			{-3./6.,0,3./6.,0},
			{1./6.,4./6.,1./6.,0}
	});

	/**
	 * Po�et krok� pro vykreslen� k�ivky. 0=automatick� ur�en�.
	 */
	private volatile int steps=0;

	/**
	 * Zda zobrazovat pomocn� ��ry.
	 */
	private volatile boolean showPolygon=false;

	/**
	 * Objekt pro �e�en� matic.
	 */
	private Algebra alg=new Algebra();
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
	 * GUI prvek pro p�epnut� na vykreslv�n� n�hradn�ho polygonu.
	 */
	private transient JCheckBox guiShowPolygon;
	/**
	 * Pomocn� promn�n�, je� m� hodnotu true b�hem zm�ny hodnoty po�tu krok�, kdy nechci aby do�lo k op�tovn�mu zavol�n�
	 * metody pro zm�nu hodnoty.
	 */
	private boolean guiChangingValue=false;

	public ObjectCoonsBSpline()
	{
		super();
		setName("Coons�v B-spline "+selfCounter++);

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
		//vytvo��m gui pro nastaven� specifick�ch vlastnost� objekt�
		guiSteps=new JSpinner(new SpinnerNumberModel(steps, 0, MAX_STEPS, 1));
		guiStepsAuto=new JCheckBox("", true);
		guiSetings=new JPanel(new GridBagLayout());
		guiShowPolygon=new JCheckBox("",showPolygon);
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

		gbc.gridy=3;
		gbc.gridx=0;
		gbc.insets=new Insets(0, 0, 0, 0);
		gbc.gridwidth=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Zobraz t�nice"),gbc);

		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		guiSetings.add(guiShowPolygon,gbc);

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

		guiShowPolygon.addChangeListener(new ChangeListener() {//zm�na volby vykreslov�n� n�hradn�ho polygonu

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setShowPolygon(guiShowPolygon.isSelected());
			}
		});


		setSteps(steps);//nastavim presnost
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
			IPoint3f point1,point2,point3,point4;
			double step;
			int loopSteps=getSteps();
			if(loopSteps==0)
			{
				//step=(1/scale)/100;
				step=(1./20.);
			}
			else
			{
				step=1./(double)loopSteps;
			}
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null),0);
			gl.glBegin(gl.GL_LINE_STRIP);
			for(int i=0;i<points.size()-3;i++)
			{
				point1=points.get(i);
				point2=points.get(i+1);
				point3=points.get(i+2);
				point4=points.get(i+3);
				//matice pro x
				mx.set(0, point1.getX());
				mx.set(1, point2.getX());
				mx.set(2, point3.getX());
				mx.set(3, point4.getX());
				//matice pro y
				my.set(0, point1.getY());
				my.set(1, point2.getY());
				my.set(2, point3.getY());
				my.set(3, point4.getY());
				//matice pro z
				mz.set(0, point1.getZ());
				mz.set(1, point2.getZ());
				mz.set(2, point3.getZ());
				mz.set(3, point4.getZ());

				//zm�n�m b�zi
				DoubleMatrix1D coonsX=alg.mult(coonsBase, mx);
				DoubleMatrix1D coonsY=alg.mult(coonsBase, my);
				DoubleMatrix1D coonsZ=alg.mult(coonsBase, mz);

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
					gl.glVertex3d(alg.mult(parametricMatrix, coonsX), alg.mult(parametricMatrix, coonsY),alg.mult(parametricMatrix, coonsZ));
				}
			}
			gl.glEnd();
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		if(!isShowPolygon())//vykreslen� spojovac�ch �ar
		{
			GLUtils.glSetColor(gl, supportColor);
			gl.glLineWidth(1);
			GLUtils.drawSimpleLine(gl, points);
		}
		else//vykreslen� v�etn� t�nic
		{
			if(points.size()>=4)//pokud je dostatek bod�
			{
				//spojnice v�ech bod�
				GLUtils.glSetColor(gl, supportColor);
				gl.glLineWidth(1);
				GLUtils.drawSimpleLine(gl, points);

				//t�nice+troj�heln�k
				gl.glEnable(gl.GL_LINE_STIPPLE);
				IPoint3f stroke[]=new IPoint3f[2];
				float[] vector=new float[3];
				for(int i=0;i<points.size()-2;i++)
				{

					gl.glLineStipple(1, (short)0x0F0F);

					//troj�heln�k - 3. strana
					stroke[0]=points.get(i);
					stroke[1]=points.get(i+2);
					GLUtils.drawSimpleLine(gl, stroke);

					//t�nice
					stroke[0]=PointOperations.pivot(stroke[0], stroke[1]);
					stroke[1]=points.get(i+1);
					GLUtils.drawSimpleLine(gl, stroke);

					//tangenta k�ivky v bod� pr�niku s t�nic�
					vector[0]=stroke[1].getX()-stroke[0].getX();
					vector[1]=stroke[1].getY()-stroke[0].getY();
					vector[2]=stroke[1].getZ()-stroke[0].getZ();
					//zmen��m vekrot na 2/3
					vector[0]*=2f/3f;
					vector[1]*=2f/3f;
					vector[2]*=2f/3f;
					stroke[0]=points.get(i);
					stroke[1]=points.get(i+2);
					gl.glLineStipple(1, (short)0xAAAA);
					gl.glBegin(gl.GL_LINES);
					gl.glVertex3f(
					  stroke[0].getX()+vector[0], 
					  stroke[0].getY()+vector[1], 
					  stroke[0].getZ()+vector[2]);
					gl.glVertex3f(
					  stroke[1].getX()+vector[0], 
					  stroke[1].getY()+vector[1], 
					  stroke[1].getZ()+vector[2]);
					gl.glEnd();
				}
				gl.glDisable(gl.GL_LINE_STIPPLE);
			}
		}
		drawSimpleNodes(gl);
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

	/**
	 * Vr�t� true kdy� se maj� vykreslovat pomocn� t�nice.
	 */
	public synchronized boolean isShowPolygon()
	{
		return showPolygon;
	}

	/**
	 * Nsatav� vykreslov�n� pomocn�ch t�nic.
	 */
	public synchronized void setShowPolygon(boolean show)
	{
		if(show!=showPolygon)
		{
			showPolygon=show;
			guiShowPolygon.setSelected(show);
			for(ObjectListener l:listeners)
			{
				l.eventSpecificPropertiesChanged(this);
			}
		}
	}

	
}
