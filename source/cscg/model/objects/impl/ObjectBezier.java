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
 * Impplementace Bézierovy køivky.
 * @author Tomáš Režnar
 */
public class ObjectBezier extends AbstractObjectCurve<Point3fChangeable>
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
	private volatile boolean useHWAcceleration = true;
	/**
	 * Napojování køivek.
	 */
	private volatile Joining joining = Joining.G0C0;
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
	 * GUI prvek pro výbìr napojování jednotlivých køivek.
	 */
	private transient JComboBox guiJoining;

	/**
	 * Konstruktor.
	 */
	public ObjectBezier()
	{
		super();
		pointPrototype = new Point3fChangeable();
		setName("Bézierova køivka " + selfCounter++);

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

		//napojování
		gbc.gridy = 2;
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
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 4;
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

		//posluchaèi zmìn nastavení
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

	/**
	 * Pøekontroluje celou køivku, a upraví napojování tak aby odpovídalo nastavenému napojování.
	 * @return Vrátí true pøi zmìnì køivky.
	 */
	private synchronized boolean checkJoining()
	{
		if (getJoining() == Joining.G0C0 || getDegree() == 1)//volné napojování nebo stupeò køivky je 1 (pøímky)
		{
			return false;
		}
		//pro napojování je tøeba vždy naèíst trojici bodù
		boolean change = false;
		//v i je vždy index tøetího bodu z trojice
		for (int i = getDegree() + 1; i < points.size(); i += getDegree())
		{
			Point3fChangeable first = (Point3fChangeable) points.get(i - 2),//první bod trojice
			  middle = (Point3fChangeable) points.get(i - 1),//prostøední bod trojice
			  last = (Point3fChangeable) points.get(i);//poslední bod trojice
			IPoint3f pivot = PointOperations.pivot(first, last);//spoètu støed úseèky
			//jeli spoètený støed a vypoèítaný shodný, je to ok
			if (PointOperations.compareCoords(pivot, middle))
			{
				continue;
			}
			//u napojování G1 staèí aby byli body na jedné pøímce
			if (getJoining() == Joining.G1 && PointOperations.isLine(first, middle, last) == false)
			{
				//pøesunu prostøední bod tak aby tvoøili body pøímku
				IPoint3f intersection = PointOperations.intersection(first, last, middle);
				middle.setX(intersection.getX());
				middle.setY(intersection.getY());
				middle.setZ(intersection.getZ());
				change = true;
			} //u C1 napojování musí být prostøední bod pøesnì ve støedu
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

			/*
			 * Výpoèet pomocí grafické karty
			 */
			if (useHWAcceleration)
			{
				//body uložím do obyèejného pole
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

				//urèení poètu krokù
				int loopSteps = getSteps();
				if (loopSteps == 0)
				{
					loopSteps = (int) Math.ceil(40 * getDegree());
				}

				//vlastní vykreslení
				try
				{
					// povolení 1D evaluátoru
					gl.glEnable(gl.GL_MAP1_VERTEX_3);
					hwBezierMulti(gl, getDegree() + 1, loopSteps, pointArray, 0);
				} catch (GLException ex)
				{
					setUseHWAcceleration(false);//zakažu HW akceleraci
				} finally
				{
					//ukonceni evulatoru
					gl.glDisable(gl.GL_MAP1_VERTEX_3);
				}
			}
			/*
			 * Výpoèet pomocí CPU
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
				for (krivka = 0; krivka < krivek; krivka++)//pøi každém cyklu se vykreslí jedna bezierova køivka
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
		//vykreslení pomocných èar
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		GLUtils.drawSimpleLine(gl, points);
		drawSimpleNodes(gl);
	}

	/**
	 * vykresleni béziera s výpoètem na GPU, funkce vykreslí tolik bezierových køivek kolik je možné z daných bodù.
	 * @param gl
	 * @param degree stupeò køivky
	 * @param steps poèet krokù pro vykreslení každé køivky
	 * @param points pole bodù, kde jsou souøadnice x,y,z uloženy v øadì za sebou
	 * @param pointOffset offset v poli bodù
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
	 * Vykreslení jedné bézierovy køivky.
	 * @param gl
	 * @param degree stupeò køivky
	 * @param steps poèet krokù pro vykreslení každé køivky
	 * @param points pole bodù, kde jsou souøadnice x,y,z uloženy v øadì za sebou
	 * @param pointOffset offset v poli bodù
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
	 * Získání stupnì køivky
	 */
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastavení stupnì køivky.
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
		if (checkJoining())//kontrola napojování
		{
			firePointsChange();
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
				guiSteps.setValue(1);
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
	 * Vrátí true pokud je povolená HW akcelerace
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
	 * Nastavení napojování køivek za sebou.
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
	 * Vrátí aktuálnì nastavené napojování.
	 */
	public synchronized Joining getJoining()
	{
		return joining;
	}
}
