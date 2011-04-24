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
 * Interpolaèní køivky.
 * @author Tomáš Režnar
 */
public class ObjectLeastSquares extends AbstractObjectCurve<Point3f>
{
	/**
	 * maximální možný stupeò køivky.
	 */
	public final int MAX_DEGREE=99;
	/**
	 * Poèítadlo instancí.
	 */
	private volatile static int selfCounter=1;
	/**
	 * Polynom pro výpoèet y souøadnice.
	 */
	private Polynomial polynominalY;
	/**
	 * Polynom pro výpoèet z souøadnice.
	 */
	private Polynomial polynominalZ;
	/**
	 * Stupeò køivky.
	 * Musí být < poèet bodù.
	 */
	private volatile int degree=1;
	/**
	 * GUI prvek pro nastavení stupnì køivky.
	 */
	private transient JSpinner guiDegree;
	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiSetings;

	public ObjectLeastSquares()
	{
		super();
		setName("Aprox. metodou nejm. ètvercù "+selfCounter++);

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
		guiSetings.add(new JLabel("Stupeò køivky"),gbc);

		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		guiSetings.add(guiDegree,gbc);

		//posluchaèi zmìn nastavení
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
			if(pointsCount>getDegree())//existuje pouze když stupeò køivky je menší než poèet bodù
			{
				try
				{
					double[] x=new double[pointsCount],
						y=new double[pointsCount],
						z=new double[pointsCount];
					//naplním pole bodù
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
				//pøi bodech umístìných nad sebou køivka neexistuje
				catch(IllegalArgumentException ex)
				{
					setStateMessage("Pro vykreslení køivky pøidejte další body.");
					setState(ObjectState.inputError);
				}
			}
			else//nedostatek bodù
			{
				setStateMessage("Nedostatek bodù pro vykreslení. Pøidejte bodù: "+(degree-pointsCount+1));
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
				startInt=drawBounds.getX1()>getBounds().getX1()?drawBounds.getX1():getBounds().getX1(),//zaèátek interpolace
				endInt=drawBounds.getX2()<getBounds().getX2()?drawBounds.getX2():getBounds().getX2();//konec interpolace
			//vlastní interpolace
			gl.glBegin(gl.GL_LINE_STRIP);
			for(double i=startInt;i<endInt;i+=step)
			{
				gl.glVertex3d(i, polynominalY.evaluate(i),polynominalZ.evaluate(i));
			}
			gl.glVertex3d(endInt, polynominalY.evaluate(endInt),polynominalZ.evaluate(endInt));//dotáhnutí do bodu
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
	 * Získání stupnì køivky.
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
		setState(ObjectState.notCounted);//køivka se zmìnila
		for(ObjectListener l:listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	
}
