package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectSurface;
import cscg.model.objects.EnumNumberSpinnerModel;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import cscg.ui.GLUtils;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
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
 * Implementace Coonsovy B-Spline plochy.
 * @author Tomáš Režnar
 */
public class ObjectCoonsBSplineSurface extends AbstractObjectSurface<Point3f>
{

	/**
	 * Maximální možný nastavitelný poèet krokù pro vykreslení.
	 */
	public final int MAX_STEPS = 100;
	/**
	 * Poèítadlo instancí.
	 */
	private volatile static int selfCounter = 1;
	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastavení poètu krokù vykreslení.
	 */
	private transient JSpinner guiSteps;
	/**
	 * Poèet krokù pro vykreslení køivky. 0=automatické urèení.
	 */
	private volatile int steps = 0;
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
	 * Bázová matice.
	 */
	private static final DoubleMatrix2D coonsBase=new DenseDoubleMatrix2D(new double[][]{
			{-1.,3.,-3.,1.},
			{3.,-6.,3.,0},
			{-3.,0,3.,0},
			{1.,4.,1.,0}
	});
	/**
	 * Bázová matice transponovaná.
	 */
	private static final DoubleMatrix2D coonsBaseT=new DenseDoubleMatrix2D(new double[][]{
			{-1.,3.,-3.,1.},
			{3.,-6.,0.,4.},
			{-3.,3.,3.,1.},
			{1.,0.,0.,0.}
	});
	/**
	 * Pomocná matice mìnící se dle aktuálního parametru u.
	 */
	DoubleMatrix1D parametricMatrixU=new DenseDoubleMatrix1D(new double[]{0,0,0,1});
	/**
	 * Pomocná matice mìnící se dle aktuálního parametru u.
	 */
	DoubleMatrix1D parametricMatrixUnext=new DenseDoubleMatrix1D(new double[]{0,0,0,1});
	/**
	 * Pomocná matice mìnící se dle aktuálního parametru v.
	 */
	DoubleMatrix1D parametricMatrixV=new DenseDoubleMatrix1D(new double[]{0,0,0,1});
	/**
	 * Pomocná matice mìnící se dle aktuálního parametru v.
	 */
	DoubleMatrix1D parametricMatrixVnext=new DenseDoubleMatrix1D(new double[]{0,0,0,1});
	/**
	 * Pomocná matice pro výpoèet souøadnice x.
	 */
	private DoubleMatrix2D mx=new DenseDoubleMatrix2D(4,4);
	/**
	 * Pomocná matice pro výpoèet souøadnice y.
	 */
	private DoubleMatrix2D my=new DenseDoubleMatrix2D(4,4);
	/**
	 * Pomocná matice pro výpoèet souøadnice z.
	 */
	private DoubleMatrix2D mz=new DenseDoubleMatrix2D(4,4);
	/**
	 * Objekt pro øešení matic.
	 */
	private Algebra alg=Algebra.DEFAULT;

	public ObjectCoonsBSplineSurface()
	{
		super();
		setName("Coonsova plocha " + selfCounter++);
		setState(ObjectState.notCounted);
		//základní tvar plochy
		makeSquare();
		setSize(4, 4);

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
		guiSetings = new JPanel(new GridBagLayout());
		guiSteps = new JSpinner(new SpinnerNumberModel(10, 0, MAX_STEPS, 1));
		guiStepsAuto = new JCheckBox("", true);
		GridBagConstraints gbc = new GridBagConstraints();

		//pøesnost
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Auto pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiStepsAuto, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		guiSetings.add(new JLabel("Pøesnost"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiSteps, gbc);

		//vložím do nadøazeného gui
		super.setSpecificGUI(guiSetings);

		//posluchaèi zmìn nastavení

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

		//nastavim defaultni presnost
		setSteps(steps);
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


	/**
	 * Vykreslení B-Spline plochy pomocí grafické karty.
	 * @param order stupeò plochy+1
	 * @param steps poèet krokù pro vykreslení každé plochy (pro každý parametr)
	 * @param points pole bodù, kde jsou souøadnice x,y,z uloženy v øadì za sebou
	 * @deprecated JOGL aktuálnì nepodporuje správnì NURBS objektu, proto je funkce nepoužitelná
	 */
	private synchronized void hwBSplineSurface(GL2 gl, int order, int steps, float[] points, int cols, int rows)
	{
		//vygenerování uniformního knot vektoru pro sloupce
		/*int i,k;
		float[] knotCols=new float[cols+order];

		for(i=0;i<knotCols.length;i++)
		{
				knotCols[i]=i;
		}

		//vygenerování uniformního knot vektoru pro øádky
		float[] knotRows=new float[rows+order];

		for(i=0;i<knotRows.length;i++)
		{
				knotRows[i]=i;
		}*/

		//vykreslení
		/*GLU glu=new GLU();
		gl.glPushAttrib(gl.GL_EVAL_BIT);
		GLUnurbs nurbs = glu.gluNewNurbsRenderer();
		//glu.gluNurbsProperty(nurbs,glu.GLU_U_STEP,steps);//nepodporavane v JOGLu
		glu.gluBeginSurface(nurbs);*/
		/*glu.gluNurbsProperty(nurbs,
                     glu.GLU_DISPLAY_MODE, glu.GLU_FILL);*/
		/*glu.gluNurbsSurface(nurbs,
			knotCols.length, knotCols,
			knotRows.length, knotRows,
			3,cols*3,
			points,
			order,order,
			gl.GL_MAP2_VERTEX_3);
		glu.gluEndSurface(nurbs);
		gl.glPopAttrib();*/
	}

	/**
	 * Vykreslení plochy vypoètené pomocí CPU.
	 */
	private void swCoonsBSplineSurface(GL2 gl,int steps)
	{
		IPoint3f point;
		GLUtils.glSetColor(gl,color);
		int cols=columns(),
		  rows=rows();
		double step=1./steps;
		int stepCounter;
		List<IPoint3f> surface=new ArrayList<IPoint3f>((steps+1)^2);
		for(int row=0;row<rows-3;row++)
		{
			for(int col=0;col<cols-3;col++)
			{
				surface.clear();
				//naètu body pro jednu plochu do matic
				for(int y=0;y<4;y++)
				{
					for(int x=0;x<4;x++)
					{
						point=getPointFromGrid(y+row, x+col);
						mx.set(y, x, point.getX());
						my.set(y, x, point.getY());
						mz.set(y, x, point.getZ());
					}
				}

				//zmìním bázi
				DoubleMatrix2D coonsX=alg.mult(alg.mult(coonsBase, mx),coonsBaseT);
				DoubleMatrix2D coonsY=alg.mult(alg.mult(coonsBase, my),coonsBaseT);
				DoubleMatrix2D coonsZ=alg.mult(alg.mult(coonsBase, mz),coonsBaseT);

				//vykreslení
				stepCounter=0;
				for(double u=-step;u<1.;)
				{
					u+=step;
					if(u>1)
					{
						u=1;
					}
					stepCounter++;

					//naplnìní matice pro parametr u
					parametricMatrixU.set(0, Math.pow(u, 3));
					parametricMatrixU.set(1, Math.pow(u, 2));
					parametricMatrixU.set(2, u);
					for(double v=-step;v<1.;)
					{
						v+=step;
						if(v>1)
						{
							v=1;
						}


						parametricMatrixV.set(0, Math.pow(v, 3));
						parametricMatrixV.set(1, Math.pow(v, 2));
						parametricMatrixV.set(2, v);

						surface.add(new Point3f(
						  (float)alg.mult(alg.mult(coonsX,parametricMatrixV),parametricMatrixU)/36f,
						  (float)alg.mult(alg.mult(coonsY,parametricMatrixV),parametricMatrixU)/36f,
						  (float)alg.mult(alg.mult(coonsZ,parametricMatrixV),parametricMatrixU)/36f
						 ));
					}
				}
				GLUtils.drawSurface(gl, surface, stepCounter, stepCounter, getMode());
			}
		}
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(ObjectState.notCounted==getState())
		{
			if (columns() > 3 && rows() > 3)//existuje pouze když stupeò køivky je menší než poèet bodù
			{
				setState(ObjectState.OK);
				setStateMessage(null);
			} else//nedostatek bodù
			{
				setStateMessage("Nedostatek bodù pro vykreslení.");
				setState(ObjectState.inputError);
			}
		}

		if(ObjectState.OK==getState())
		{
			//urèení poètu krokù
			int loopSteps = getSteps();
			if (loopSteps == 0)
			{
				//steps = (int) Math.ceil(scale * 20);
				loopSteps = (int) Math.ceil(20);
				loopSteps = loopSteps>MAX_STEPS?MAX_STEPS:loopSteps;
			}

			GLUtils.glSetColor(gl, color);

			//vykreslení pomocí CPU
			swCoonsBSplineSurface(gl,loopSteps);

			//vykreslení pomocí hw-nefunguje v aktuální verzi knihovny JOGL
			/*int order=getDegree()+1;//poèet bodù pro jeden úsek køivky
			//body uložím do obyèejného pole
			int cols=columns();
			int rows=rows();
			float[] pointArray = new float[cols*rows*3];
			IPoint3f p;
			int index=0;
			for(int row=0;row<rows;row++)//projdu øádky
			{
				for(int col=0;col<cols;col++)//projdu sloupce
				{
					p=getPointFromGrid(row, col);
					pointArray[index] = p.getX();
					pointArray[index+1] = p.getY();
					pointArray[index+2] = p.getZ();
					index+=3;
				}
			}
			//vlastní vykreslení
			hwBSplineSurface(gl, 4, steps, pointArray,cols,rows);*/
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleGridNodes(gl);
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
}
