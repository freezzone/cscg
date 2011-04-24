package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cscg.model.Projection;
import cscg.model.objects.*;
import cscg.ui.GLUtils;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Impplementace B�zierovy k�ivky.
 * @author Tom� Re�nar
 */
public class ObjectBezier extends AbstractObjectCurve<Point3fChangeable>
{

	/**
	 * Maxim�ln� mo�n� stupe� k�ivky.
	 */
	public final int MAX_DEGREE = 90;
	/**
	 * Maxim�ln� mo�n� nastaviteln� po�et krok� pro vykreslen�.
	 */
	public final int MAX_STEPS = 200;
	/**
	 * Po��tadlo instanc�.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * Stupe� k�ivky.
	 * Mus� b�t < po�et bod�.
	 */
	private volatile int degree = 3;
	/**
	 * Po�et krok� pro vykreslen� k�ivky. 0=automatick� ur�en�.
	 */
	private volatile int steps = 0;
	/**
	 * Povolen� pou�it� grafick� karty pro urychlen� vykreslen�.
	 */
	private volatile boolean useHWAcceleration = true;
	/**
	 * Napojov�n� k�ivek.
	 */
	private volatile Joining joining = Joining.G0C0;
	/**
	 * GUI prvek pro nastaven� stupn� k�ivky.
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
	 * GUI prvek pro automatickou volbu po�tu krok�.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * Pomocn� promn�n�, je� m� hodnotu true b�hem zm�ny hodnoty po�tu krok�, kdy nechci aby do�lo k op�tovn�mu zavol�n�
	 * metody pro zm�nu hodnoty.
	 */
	private boolean guiChangingValue = false;
	/**
	 * GUI prvek pro povolen� HW akcelerace.
	 */
	private transient JCheckBox guiUseHWAcceleration;
	/**
	 * GUI prvek pro v�b�r napojov�n� jednotliv�ch k�ivek.
	 */
	private transient JComboBox guiJoining;

	/**
	 * Konstruktor.
	 */
	public ObjectBezier()
	{
		super();
		pointPrototype = new Point3fChangeable();
		setName("B�zierova k�ivka " + selfCounter++);

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
		guiSteps = new JSpinner(new SpinnerNumberModel(steps, 0, MAX_STEPS, 1));
		guiStepsAuto = new JCheckBox("", true);
		guiUseHWAcceleration = new JCheckBox("", useHWAcceleration);
		guiJoining = new JComboBox(new String[]
		  {
			  "<html>G<sub>0</sub></html>",
			  "<html>G<sub>1</sub></html>",
			  "<html>C<sub>1</sub></html>"
		  });
		guiJoining.setPrototypeDisplayValue("<html>G<sub>0</sub>G</html>");
		setJoining(joining);
		GridBagConstraints gbc = new GridBagConstraints();

		//zd�d�n� gui
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		guiSetings.add(super.getObjectGUI(), gbc);

		//stupe�
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupe� k�ivky"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiDegree, gbc);

		//napojov�n�
		gbc.gridy = 2;
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
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto p�esnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 4;
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
		gbc.gridy = 5;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("HW akcelerace"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiUseHWAcceleration, gbc);

		//poslucha�i zm�n nastaven�
		guiDegree.addChangeListener(new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setDegree((Integer) guiDegree.getValue());
			}
		});

		guiJoining.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Joining newJoining = null;
				switch (guiJoining.getSelectedIndex())
				{
					case 0:
						newJoining = Joining.G0C0;
						break;

					case 1:
						newJoining = Joining.G1;
						break;

					case 2:
						newJoining = Joining.C1;
						break;

				}
				if (joining != newJoining)
				{
					setJoining(newJoining);
					if (checkJoining())
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

	/**
	 * P�ekontroluje celou k�ivku, a uprav� napojov�n� tak aby odpov�dalo nastaven�mu napojov�n�.
	 * @return Vr�t� true p�i zm�n� k�ivky.
	 */
	private synchronized boolean checkJoining()
	{
		if (getJoining() == Joining.G0C0 || getDegree() == 1)//voln� napojov�n� nebo stupe� k�ivky je 1 (p��mky)
		{
			return false;
		}
		//pro napojov�n� je t�eba v�dy na��st trojici bod�
		boolean change = false;
		//v i je v�dy index t�et�ho bodu z trojice
		for (int i = getDegree() + 1; i < points.size(); i += getDegree())
		{
			Point3fChangeable first = (Point3fChangeable) points.get(i - 2),//prvn� bod trojice
			  middle = (Point3fChangeable) points.get(i - 1),//prost�edn� bod trojice
			  last = (Point3fChangeable) points.get(i);//posledn� bod trojice
			IPoint3f pivot = PointOperations.pivot(first, last);//spo�tu st�ed �se�ky
			//jeli spo�ten� st�ed a vypo��tan� shodn�, je to ok
			if (PointOperations.compareCoords(pivot, middle))
			{
				continue;
			}
			//u napojov�n� G1 sta�� aby byli body na jedn� p��mce
			if (getJoining() == Joining.G1 && PointOperations.isLine(first, middle, last) == false)
			{
				//p�esunu prost�edn� bod tak aby tvo�ili body p��mku
				IPoint3f intersection = PointOperations.intersection(first, last, middle);
				middle.setX(intersection.getX());
				middle.setY(intersection.getY());
				middle.setZ(intersection.getZ());
				change = true;
			} //u C1 napojov�n� mus� b�t prost�edn� bod p�esn� ve st�edu
			else
			{
				if (getJoining() == Joining.C1)
				{
					middle.setX(pivot.getX());
					middle.setY(pivot.getY());
					middle.setZ(pivot.getZ());
					change = true;
				}
			}
		}


		return change;
	}

	@Override
	public synchronized IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint)
	{
		IPoint3f ret = super.addPointAfter(newPoint, afterPoint);
		if (checkJoining())
		{
			firePointsChange();
		}
		return ret;
	}

	@Override
	public synchronized IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint)
	{
		IPoint3f ret = super.addPointBefore(newPoint, beforePoint);
		if (checkJoining())
		{
			firePointsChange();
		}
		return ret;
	}

	@Override
	public synchronized void editPoint(IPoint3f editedPoint, IPoint3f setBy)
	{
		super.editPoint(editedPoint, setBy);
		if (checkJoining())
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset)
	{
		super.movePointRelative(point, xOffset, yOffset, zOffset);
		if (checkJoining())
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void movePointTo(IPoint3f point, float x, float y, float z)
	{
		super.movePointTo(point, x, y, z);
		if (checkJoining())
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void removePoint(IPoint3f point)
	{
		super.removePoint(point);
		if (checkJoining())
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if (state == ObjectState.notCounted)
		{
			int pointsCount = points.size();//pocet bodu
			if (pointsCount > getDegree())//existuje pouze kdy� stupe� k�ivky je men�� ne� po�et bod�
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bod�
			{
				setStateMessage("Nedostatek bod� pro vykreslen�. P�idejte bod�: " + (degree - pointsCount + 1));
				setState(ObjectState.inputError);
			}
		}

		//vykresleni
		if (getState() == ObjectState.OK)
		{
			//nastaven� vlastnost� ��ry
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null), 0);

			/*
			 * V�po�et pomoc� grafick� karty
			 */
			if (useHWAcceleration)
			{
				//body ulo��m do oby�ejn�ho pole
				double[] pointArray = new double[points.size() * 3];
				int i = 0;
				for (IPoint3f p : points)
				{
					//pro beziera
					pointArray[i] = p.getX();
					pointArray[i + 1] = p.getY();
					pointArray[i + 2] = p.getZ();
					i += 3;
				}

				//ur�en� po�tu krok�
				int loopSteps = getSteps();
				if (loopSteps == 0)
				{
					loopSteps = (int) Math.ceil(40 * getDegree());
				}

				//vlastn� vykreslen�
				try
				{
					// povolen� 1D evalu�toru
					gl.glEnable(gl.GL_MAP1_VERTEX_3);
					hwBezierMulti(gl, getDegree() + 1, loopSteps, pointArray, 0);
				} catch (GLException ex)
				{
					setUseHWAcceleration(false);//zaka�u HW akceleraci
				} finally
				{
					//ukonceni evulatoru
					gl.glDisable(gl.GL_MAP1_VERTEX_3);
				}
			}
			/*
			 * V�po�et pomoc� CPU
			 */
			if (!useHWAcceleration)
			{
				double step;
				int loopSteps = getSteps();
				if (loopSteps == 0)
				{
					step = 1. / (1. + (10. * ((double) getDegree() - 1)));
					//step = ((1 / scale) / 40) / getDegree();
				} else
				{
					step = 1 / (double) loopSteps;
				}
				gl.glBegin(gl.GL_LINE_STRIP);
				int i, j, krivka,
				  krivek = (points.size() - 1) / getDegree(),
				  startPointIndex, nextStartPointIndex = 0;
				IPoint3f point;
				double pozice, sumaX, sumaY, sumaZ, B;
				for (krivka = 0; krivka < krivek; krivka++)//p�i ka�d�m cyklu se vykresl� jedna bezierova k�ivka
				{
					startPointIndex = nextStartPointIndex;
					nextStartPointIndex += getDegree();
					for (pozice = -step, sumaX = 0, sumaY = 0, sumaZ = 0; pozice < 1; sumaX = 0, sumaY = 0, sumaZ = 0)
					{
						pozice += step;
						if (pozice > 1)
						{
							pozice = 1;
						}
						for (i = startPointIndex, j = 0; i <= nextStartPointIndex; i++, j++)
						{
							point = points.get(i);
							B = MathUtils.bernstein(j, getDegree(), pozice);
							sumaX += B * point.getX();
							sumaY += B * point.getY();
							sumaZ += B * point.getZ();
						}
						gl.glVertex3d(sumaX, sumaY, sumaZ);
					}
				}
				gl.glEnd();
			}
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		//vykreslen� pomocn�ch �ar
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		GLUtils.drawSimpleLine(gl, points);
		drawSimpleNodes(gl);
	}

	/**
	 * vykresleni b�ziera s v�po�tem na GPU, funkce vykresl� tolik bezierov�ch k�ivek kolik je mo�n� z dan�ch bod�.
	 * @param gl
	 * @param degree stupe� k�ivky
	 * @param steps po�et krok� pro vykreslen� ka�d� k�ivky
	 * @param points pole bod�, kde jsou sou�adnice x,y,z ulo�eny v �ad� za sebou
	 * @param pointOffset offset v poli bod�
	 */
	private synchronized void hwBezierMulti(GL2 gl, int degree, int steps, double[] points, int pointOffset)
	{
		int j = pointOffset;
		while (j <= ((points.length / 3 - degree)))
		{
			hwBezierOne(gl, degree, steps, points, j);
			j += degree - 1;
		}
	}

	/**
	 * Vykreslen� jedn� b�zierovy k�ivky.
	 * @param gl
	 * @param degree stupe� k�ivky
	 * @param steps po�et krok� pro vykreslen� ka�d� k�ivky
	 * @param points pole bod�, kde jsou sou�adnice x,y,z ulo�eny v �ad� za sebou
	 * @param pointOffset offset v poli bod�
	 */
	private synchronized void hwBezierOne(GL2 gl, int degree, int steps, double[] points, int pointOffset)
	{
		gl.glMap1d(gl.GL_MAP1_VERTEX_3, 0, 1, 3, degree, points, pointOffset * 3);
		gl.glMapGrid1d(steps, 0, 1);
		gl.glEvalMesh1(GL2.GL_LINE, 0, steps);
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSetings;
	}

	/**
	 * Z�sk�n� stupn� k�ivky
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
		if (checkJoining())//kontrola napojov�n�
		{
			firePointsChange();
		}
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
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
				guiSteps.setValue(1);
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
	 * Vr�t� true pokud je povolen� HW akcelerace
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
	 * Nastaven� napojov�n� k�ivek za sebou.
	 */
	public synchronized void setJoining(Joining joining)
	{
		if (joining == Joining.G0C0)
		{
			guiJoining.setSelectedIndex(0);
		} else
		{
			if (joining == Joining.G1)
			{
				guiJoining.setSelectedIndex(1);
			} else
			{
				if (joining == Joining.C1)
				{
					guiJoining.setSelectedIndex(2);
				}
			}
		}
		this.joining = joining;
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
}
