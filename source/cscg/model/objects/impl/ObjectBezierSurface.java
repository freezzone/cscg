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
 * Implementace B�zierovy plochy.
 * @author Tom� Re�nar
 */
public class ObjectBezierSurface extends AbstractObjectSurface<Point3fChangeable>{

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
	 * GUI prvek pro nastaven� stupn� plochy.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI panel s nastaven�m objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastaven� po�tu krok� vykreslen�.
	 */
	private transient JSpinner guiSteps;
	/**
	 * Po�et krok� pro vykreslen� k�ivky. 0=automatick� ur�en�.
	 */
	private volatile int steps = 0;
	/**
	 * Stupe� plochy.
	 * Mus� b�t < po�et bod� ka�d�m sm�rem.
	 */
	private volatile int degree = 3;
	/**
	 * Povolen� pou�it� grafick� karty pro urychlen� vykreslen�.
	 */
	private volatile boolean useHWAcceleration = true;
	/**
	 * Napojov�n� ploch.
	 */
	private volatile Joining joining=Joining.G0C0;
	/**
	 * GUI prvek pro automatickou volbu po�tu krok�.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * GUI prvek pro povolen� HW akcelerace.
	 */
	private transient JCheckBox guiUseHWAcceleration;
	/**
	 * GUI prvek pro v�b�r napojov�n� jednotliv�ch k�ivek.
	 */
	private transient JComboBox guiJoining;
	/**
	 * Pomocn� promn�n�, je� m� hodnotu true b�hem zm�ny hodnoty po�tu krok�, kdy nechci aby do�lo k op�tovn�mu zavol�n�
	 * metody pro zm�nu hodnoty.
	 */
	private boolean guiChangingValue = false;

	/**
	 * Konstruktor.
	 */
	public ObjectBezierSurface()
	{
		super();
		pointPrototype=new Point3fChangeable();
		setName("B�zierova plocha " + selfCounter++);
		setState(ObjectState.notCounted);
		//z�kladn� tvar plochy
		makeSquare();
		setSize(4, 4);

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

		//napojov�n�
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
		guiSetings.add(new JLabel("Napojov�n�"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiJoining, gbc);

		//p�esnost
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto p�esnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("P�esnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiSteps, gbc);

		//povolen� hw akcelerace
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
	 * Vykreslen� jedn� b�zierovy plochy pomoc� grafick� karty.
	 * @param gl
	 * @param order stupe� plochy+1
	 * @param steps po�et krok� pro vykreslen� ka�d� plochy (pro ka�d� parametr)
	 * @param points pole bod�, kde jsou sou�adnice x,y,z ulo�eny v �ad� za sebou
	 * @param pointOffset offset v poli bod� vyd�len� 3
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
	 * Vykreslen� jedn� b�zierovy plochy pomoc� CPU.
	 * @param gl
	 * @param order stupe� plochy+1
	 * @param steps po�et krok� pro vykreslen� ka�d� plochy (pro ka�d� parametr)
	 * @param points list bod�, kde jsou body ulo�eny za sebou po ��dc�ch
	 */
	private synchronized void swBezierSurface(GL2 gl, int order, int steps, List<IPoint3f> points)
	{
		double step=1./(double)steps;
		double u,v;//parametry
		double Bu,Bv;//hodnota bernsteinova polynomu
		int i,j;//index v cyklu sum
		double sumaX, sumaY, sumaZ;//sou�et sum
		IPoint3f point;
		ArrayList<IPoint3f> surfacePoints=new ArrayList<IPoint3f>((steps+1)*(steps+1));//pole vypo�etn�ch bod� plochy
		int rows=0,cols=0;//po��tadlo sloupc� a ��dk�
		//proch�zen� parametru u
		for (u = -step,rows=0; u < 1;)
		{
			rows++;
			u += step;
			if (u > 1)
			{
				u = 1;
			}
			//proch�zen� parametru v
			for (v = -step, cols=0, sumaX = 0, sumaY = 0, sumaZ = 0; v < 1; sumaX = 0, sumaY = 0, sumaZ = 0)
			{
				cols++;
				v += step;
				if (v > 1)
				{
					v = 1;
				}
				//prvn� suma
				for (i = 0; i < order;i++)
				{
					//druh� suma
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
				//ulo�en� bodu
				surfacePoints.add(new Point3f((float)sumaX, (float)sumaY, (float)sumaZ));
			}
		}
		//vykreslen� plochy
		GLUtils.drawSurface(gl, surfacePoints, cols, rows, getMode());
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
			if (loopSteps == 0)
			{
				loopSteps = (int) Math.ceil(20 * getDegree());
				//steps = (int) Math.ceil(scale * 20 * getDegree());
			}

			GLUtils.glSetColor(gl, color);
			
			int order=getDegree()+1;//po�et bod� pro jeden �sek k�ivky
			//body ulo��m do oby�ejn�ho pole
			int cols=columns();
			int rows=rows();
			IPoint3f p;
			
			//na�tu body pro jednu plochu do pole
			if (useHWAcceleration)//pomoc� HW
			{
				try
				{
					// povolen� 2D evalu�toru
					gl.glEnable(gl.GL_MAP2_VERTEX_3);
					float[] pointArray = new float[order*order*3];
					for(int col=0;col<cols-getDegree();col+=getDegree())//projdu plochy v sloupc�ch
					{
						for(int row=0;row<rows-getDegree();row+=getDegree())//projdu plochy v ��dc�ch
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
			if (!useHWAcceleration)//pomoc� CPU
			{
				ArrayList<IPoint3f> pointArray = new ArrayList<IPoint3f>(order*order);
				for(int col=0;col<cols-getDegree();col+=getDegree())//projdu plochy v sloupc�ch
				{
					for(int row=0;row<rows-getDegree();row+=getDegree())//projdu plochy v ��dc�ch
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
	 * Z�sk�n� stupn� plochy.
	 */
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastaven� stupn� plochy..
	 * Stupe� m��e b�t v rozsahu 1 a� MAX_DEGREE, pokud zad�te neplatnou hodnotu, pou�ije se nejbli��� platn�.
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
		setState(ObjectState.notCounted);//k�ivka se zm�nila
		if(checkJoining())//kontrola napojov�n�
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
	 * Vr�t� true pokud je povolen� HW akcelerace.
	 */
	public synchronized boolean isUseHWAcceleration()
	{
		return useHWAcceleration;
	}

	/**
	 * Povolen� HW akcelerace.
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
	 * Nastaven� napojov�n� ploch za sebou.
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
	 * Vr�t� aktu�ln� nastaven� napojov�n�.
	 */
	public synchronized Joining getJoining()
	{
		return joining;
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
	 * P�ekontroluje celou plochu, a uprav� napojov�n� tak aby odpov�dalo nastaven�mu napojov�n�.
	 * @return Vr�t� true p�i zm�n� k�ivky.
	 */
	private synchronized boolean checkJoining()
	{
		if(getJoining()==Joining.G0C0 || getDegree()==1)//voln� napojov�n� nebo stupe� plochy je 1
		{
			return false;
		}
		//pro napojov�n� je t�eba v�dy na��st trojici bod�
		boolean change=false;

		/*
		 * kontrola ��dk�
		 */
		for(int row=0;row<rows();row++)
		{
			//v i je v�dy index t�et�ho bodu z trojice
			for(int i=getDegree()+1;i<columns();i+=getDegree())
			{
				Point3fChangeable first=getPointFromGrid(row,i-2),//prvn� bod trojice
					middle=getPointFromGrid(row,i-1),//prost�edn� bod trojice
					last=getPointFromGrid(row,i);//posledn� bod trojice
				IPoint3f pivot=PointOperations.pivot(first, last);//spo�tu st�ed �se�ky
				//jeli spo�ten� st�ed a vypo��tan� shodn�, je to ok
				if(PointOperations.compareCoords(pivot, middle))
				{
					continue;
				}
				/*
				 * u G1 napojov�n� mus� b�t pom�r vzd�lenost� prost�edn�ho bodu od p�edchoz�ho bodu a k n�sleduj�c�mu
				 * bodu shodn� pro v�echny tojice v �ad�, proto pro zjednodu�en� ovl�d�n� se tento pom�r bude udr�ovat
				 * 1:1 (prot�edn� bod je p�esn� uprost�ed p��mky mezi p�edchoz�m a n�sleduj�c�m bodem) a d�ky tomu
				 * p�i posunu bodu nen� t�eba m�nit celou �adu, proto�e pom�r je v cel� �ad� 1:1.
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
		 * kontrola sloupc�
		 */
		for(int col=0;col<columns();col++)
		{
			//v i je v�dy index t�et�ho bodu z trojice
			for(int i=getDegree()+1;i<rows();i+=getDegree())
			{
				Point3fChangeable first=getPointFromGrid(i-2,col),//prvn� bod trojice
					middle=getPointFromGrid(i-1,col),//prost�edn� bod trojice
					last=getPointFromGrid(i,col);//posledn� bod trojice
				IPoint3f pivot=PointOperations.pivot(first, last);//spo�tu st�ed �se�ky

				//jeli spo�ten� st�ed a vypo��tan� shodn�, je to ok
				if(PointOperations.compareCoords(pivot, middle))
				{
					continue;
				}
				//u C1 napojov�n� mus� b�t prost�edn� bod p�esn� ve st�edu
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
