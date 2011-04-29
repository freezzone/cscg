package cscg.model.objects.impl;

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
 * Implementace Bézierovy křivky.
 * @author Tomáš Režnar
 */
public class ObjectBezier extends AbstractObjectCurve<Point3fChangeable>
{

	/**
	 * Maximální možný stupeň křivky.
	 */
	public final int MAX_DEGREE = 90;
	/**
	 * Maximální možný nastavitelný počet kroků pro vykreslení.
	 */
	public final int MAX_STEPS = 200;
	/**
	 * Počítadlo instancí.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * Stupeň křivky.
	 * Musí být < počet bodů.
	 */
	private volatile int degree = 3;
	/**
	 * Počet kroků pro vykreslení křivky. 0=automatické určení.
	 */
	private volatile int steps = 0;
	/**
	 * Povolení použití grafické karty pro urychlení vykreslení.
	 */
	private volatile boolean useHWAcceleration = true;
	/**
	 * Napojování křivek.
	 */
	private volatile Joining joining = Joining.G0C0;
	/**
	 * GUI prvek pro nastavení stupně křivky.
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
	 * GUI prvek pro automatickou volbu počtu kroků.
	 */
	private transient JCheckBox guiStepsAuto;
	/**
	 * Pomocná promněná, jež má hodnotu true během změny hodnoty počtu kroků, kdy nechci aby došlo k opětovnému zavolání
	 * metody pro změnu hodnoty.
	 */
	private boolean guiChangingValue = false;
	/**
	 * GUI prvek pro povolení HW akcelerace.
	 */
	private transient JCheckBox guiUseHWAcceleration;
	/**
	 * GUI prvek pro výběr napojování jednotlivých křivek.
	 */
	private transient JComboBox guiJoining;

	/**
	 * Konstruktor.
	 */
	public ObjectBezier()
	{
		super();
		pointPrototype = new Point3fChangeable();
		setName("Bézierova křivka " + selfCounter++);

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

		//zděděné gui
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		guiSetings.add(super.getObjectGUI(), gbc);

		//stupeň
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupeň křivky"), gbc);

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

		//přesnost
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto přesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 4;
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

		//posluchači změn nastavení
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
	 * Překontroluje celou křivku, a upraví napojování tak aby odpovídalo nastavenému napojování.
	 * @return Vrátí true při změně křivky.
	 */
	private synchronized boolean checkJoining()
	{
		if (getJoining() == Joining.G0C0 || getDegree() == 1)//volné napojování nebo stupeň křivky je 1 (přímky)
		{
			return false;
		}
		//pro napojování je třeba vždy načíst trojici bodů
		boolean change = false;
		//v i je vždy index třetího bodu z trojice
		for (int i = getDegree() + 1; i < points.size(); i += getDegree())
		{
			Point3fChangeable first = (Point3fChangeable) points.get(i - 2),//první bod trojice
			  middle = (Point3fChangeable) points.get(i - 1),//prostřední bod trojice
			  last = (Point3fChangeable) points.get(i);//poslední bod trojice
			IPoint3f pivot = PointOperations.pivot(first, last);//spočtu střed úsečky
			//jeli spočtený střed a vypočítaný shodný, je to ok
			if (PointOperations.compareCoords(pivot, middle))
			{
				continue;
			}
			//u napojování G1 stačí aby byli body na jedné přímce
			if (getJoining() == Joining.G1 && PointOperations.isLine(first, middle, last) == false)
			{
				//přesunu prostřední bod tak aby tvořili body přímku
				IPoint3f intersection = PointOperations.intersection(first, last, middle);
				middle.setX(intersection.getX());
				middle.setY(intersection.getY());
				middle.setZ(intersection.getZ());
				change = true;
			} //u C1 napojování musí být prostřední bod přesně ve středu
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
			if (pointsCount > getDegree())//existuje pouze když stupeň křivky je menší než počet bodů
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bodů
			{
				setStateMessage("Nedostatek bodů pro vykreslení. Přidejte bodů: " + (degree - pointsCount + 1));
				setState(ObjectState.inputError);
			}
		}

		//vykresleni
		if (getState() == ObjectState.OK)
		{
			//nastavení vlastností čáry
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null), 0);

			/*
			 * Výpočet pomocí grafické karty
			 */
			if (useHWAcceleration)
			{
				//body uložím do obyčejného pole
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

				//určení počtu kroků
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
			 * Výpočet pomocí CPU
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
				for (krivka = 0; krivka < krivek; krivka++)//při každém cyklu se vykreslí jedna bezierova křivka
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
		//vykreslení pomocných čar
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		GLUtils.drawSimpleLine(gl, points);
		drawSimpleNodes(gl);
	}

	/**
	 * vykresleni béziera s výpočtem na GPU, funkce vykreslí tolik bezierových křivek kolik je možné z daných bodů.
	 * @param gl
	 * @param degree stupeň křivky
	 * @param steps počet kroků pro vykreslení každé křivky
	 * @param points pole bodů, kde jsou souřadnice x,y,z uloženy v řadě za sebou
	 * @param pointOffset offset v poli bodů
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
	 * Vykreslení jedné bézierovy křivky.
	 * @param degree stupeň křivky
	 * @param steps počet kroků pro vykreslení každé křivky
	 * @param points pole bodů, kde jsou souřadnice x,y,z uloženy v řadě za sebou
	 * @param pointOffset offset v poli bodů
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
	 * Získání stupně křivky
	 */
	public synchronized int getDegree()
	{
		return degree;
	}

	/**
	 * Nastavení stupně křivky.
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
				guiSteps.setValue(1);
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
	 * Nastavení napojování křivek za sebou.
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
	 * Vrátí aktuálně nastavené napojování.
	 */
	public synchronized Joining getJoining()
	{
		return joining;
	}
}
