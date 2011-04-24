package cscg.model.objects.impl;

import cscg.model.objects.ObjectListener;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cscg.model.objects.Bounds;
import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import umontreal.iro.lecuyer.functions.Polynomial;

/**
 * Interpola�n� k�ivky.
 * @author Tom� Re�nar
 */
public class ObjectInterpolation extends AbstractObjectCurve<Point3fChangeable>
{
	/**
	 * Po��tadlo instanc�.
	 */
	private static int selfCounter=1;
	/**
	 * Objekt pro �e�en� matic.
	 */
	private Algebra alg=Algebra.DEFAULT;
	/**
	 * Polynom pro v�po�et y sou�adnice.
	 */
	private Polynomial polynominalY;
	/**
	 * Polynom pro v�po�et z sou�adnice.
	 */
	private Polynomial polynominalZ;
	/**
	 * GUI panel s nastaven�m objektu.
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastaven� automatick�ho sa�azen� vkl�d�n�ch bod�.
	 */
	private transient JCheckBox guiAutoSort;
	/**
	 * Automatick�ho sa�azen� vkl�d�n�ch bod�.
	 */
	private volatile boolean autoSort=true;

	public ObjectInterpolation()
	{
		super();
		pointPrototype=new Point3fChangeable();
		setName("Interpolace polynomem "+selfCounter++);

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
		guiAutoSort=new JCheckBox("", autoSort);
		guiSetings=new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridwidth=2;
		gbc.weightx=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		guiSetings.add(super.getObjectGUI(),gbc);

		gbc.gridy=1;
		gbc.gridwidth=1;
		gbc.insets=new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Se�azovat body"),gbc);

		gbc.gridx=1;
		gbc.fill=GridBagConstraints.NONE;
		gbc.anchor=GridBagConstraints.EAST;
		guiSetings.add(guiAutoSort,gbc);

		guiAutoSort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				setAutoSort(guiAutoSort.isSelected());
			}

		});
	}

	/**
	 * Vr�t� true kdy� se body t��d� automaticky.
	 */
	public synchronized boolean isAutoSort()
	{
		return autoSort;
	}

	/**
	 * Nastav� automatick� t��d�n� bod�.
	 */
	public synchronized void setAutoSort(boolean autoSort)
	{
		if(this.autoSort != autoSort)
		{
			this.autoSort = autoSort;
			guiAutoSort.setSelected(autoSort);
			if(!autoSort)
			{
				//zajist�m aby nebylo v�ce bod� na stejn� sou�adnici x
				if(checkOrder())
				{
					setState(ObjectState.notCounted);
					firePointsChange();
				}
			}
			for(ObjectListener l:listeners)
			{
				l.eventSpecificPropertiesChanged(ObjectInterpolation.this);
			}
		}
	}

	/**
	 * Projde body a v p��pad� neplatn�ho po�ad� body posune.
	 * Funkce nevyvol�v� ��dn� ud�losti.
	 * @return True pokud do�lo k �prav� bod�.
	 */
	private boolean checkOrder()
	{
		IPoint3f last=null;
		int index=0;
		boolean change=false;
		for(Point3fChangeable p:points)
		{
			if(last!=null)
			{
				if(p.getX()<=last.getX())
				{
					p.setX(last.getX()+1f);//posunu bod
					change=true;
				}
			}
			last=p;
			index++;
		}
		return change;
	}

	/**
	 * Zajist� aby dan� bod byl v rozmez� mezi sou�adnicemi x p�edchoz�ho a n�sleduj�c�ho bodu.
	 * Funkce nevyvol� ud�losti p�i zm�n�.
	 * @return Vr�t� true pokud byli n�jak ovlivn�ny body (vlastnosti, po�ad�).
	 */
	private boolean checkPoint(IPoint3f pointToCheck)
	{
		int index = points.indexOf(pointToCheck);
		if(index>=0)
		{
			Point3fChangeable point=(Point3fChangeable)pointToCheck;
			int prevIndex = index - 1;
			int nextIndex = index + 1;
			IPoint3f prev = prevIndex >= 0 ? points.get(prevIndex) : null;
			IPoint3f next = nextIndex < points.size() ? points.get(nextIndex) : null;
			boolean change = false;
			if(!guiAutoSort.isSelected())//pokud se body nese�azuj� automaticky
			{
				if (next != null && point.getX() >= next.getX())
				{
					point.setX(next.getX() - 1f);
					change = true;
				}
				if (prev != null && point.getX() <= prev.getX())
				{
					point.setX(prev.getX() + 1f);
					change = true;
				}
				change = change || checkOrder();
			}
			else//kdy� se body se�azuj� automaticky
			{
				//body nejsou spravn� se�azeny
				if ((next != null && point.getX() > next.getX()) || (prev != null && point.getX() < prev.getX()))
				{
					//Collections.sort(points, new Point3fComparatorByX());
					change = true;
					insertPointOnProperIndex(point);
				}
			}
			return change;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Vyjme a znovu vlo�� bod do listu bod�, na spr�vn� m�sto - se�azen� dle x. Nevyvol� ud�lost p�i zm�n�.
	 * @param point Bod je� bude p�esunut na spr�vn� index.
	 */
	private synchronized void insertPointOnProperIndex(Point3fChangeable point)
	{
		points.remove(point); //odeberu �patn� za�azen� bod
		//vyhled�m spr�vn� um�st�n� a vlo��m
		boolean inserted = false; //zda jsem bod vlo�il
		int position = 0;
		for (Point3fChangeable p : points)
		{
			if (p.getX() >= point.getX())
			{
				points.add(position, point);
				inserted = true;
				break;
			}
			position++;
		}
		if (inserted == false)
		{
			points.add(point);
		}
	}

	@Override
	public synchronized IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint)
	{
		newPoint=super.addPointBefore(newPoint, null);//vlo��m nov� bod
		insertPointOnProperIndex((Point3fChangeable)newPoint);
		firePointsChange();
		return newPoint;
	}

	@Override
	public synchronized IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint)
	{
		newPoint=super.addPointBefore(newPoint, null);//vlo��m nov� bod
		insertPointOnProperIndex((Point3fChangeable)newPoint);
		firePointsChange();
		return newPoint;
	}

	@Override
	public synchronized void editPoint(IPoint3f editedPoint, IPoint3f setBy)
	{
		super.editPoint(editedPoint, setBy);
		if(checkPoint(editedPoint))
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset)
	{
		super.movePointRelative(point, xOffset, yOffset, zOffset);
		if(checkPoint(point))
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void movePointTo(IPoint3f point, float x, float y, float z)
	{
		super.movePointTo(point, x, y, z);
		if(checkPoint(point))
		{
			firePointsChange();
		}
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(state==ObjectState.notCounted)
		{
			if(points.size()>=2)
			{
				try
				{
					int pointsCount=points.size();//pocet bodu
					DenseDoubleMatrix2D matrixA=new DenseDoubleMatrix2D(pointsCount, pointsCount);
					DenseDoubleMatrix2D matrixBforY=new DenseDoubleMatrix2D(pointsCount,1);
					DenseDoubleMatrix2D matrixBforZ=new DenseDoubleMatrix2D(pointsCount,1);
					IPoint3f point;
					for(int row=0,col;row<pointsCount;row++)
					{
						point=points.get(row);
						for(col=0;col<pointsCount;col++)
						{
								matrixA.set(row,col,Math.pow(point.getX(), col));
						}
						matrixBforY.set(row,0,point.getY());
						matrixBforZ.set(row,0,point.getZ());
					}
					DoubleMatrix2D matrixCoefsY = alg.solve(matrixA, matrixBforY);
					DoubleMatrix2D matrixCoefsZ = alg.solve(matrixA, matrixBforZ);
					double coefsY[]=new double[matrixCoefsY.rows()];
					double coefsZ[]=new double[matrixCoefsZ.rows()];
					for(int i=0;i<coefsY.length;i++)
					{
						coefsY[i] = matrixCoefsY.get(i, 0);
						coefsZ[i]=matrixCoefsZ.get(i, 0);
					}
					polynominalY=new Polynomial(coefsY);
					polynominalZ=new Polynomial(coefsZ);

					setState(ObjectState.OK);
					setStateMessage(null);
				}
				//p�i bodech um�st�n�ch nad sebou k�ivka neexistuje
				catch(IllegalArgumentException ex)
				{
					setStateMessage("Explicitn� tvar rovnice neexistuje, proto�e existuj� dva body pro jedno x.");
					setState(ObjectState.inputError);
					throw ex;
				}
			}
			else//nedostatek bod�
			{
				setStateMessage("Zadejte minim�ln� dva body pro vykreslen� k�ivky.");
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
			IPoint3f point;
			for(double i=startInt-step;i<endInt;)
			{
				i+=step;
				if(i>endInt)
				{
					i=endInt;
				}
				point=new Point3f((float)i,(float)polynominalY.evaluate(i),(float)polynominalZ.evaluate(i));
				gl.glVertex3d(point.getX(), point.getY(),point.getZ());
			}
			//gl.glVertex3d(endInt, polynominalY.evaluate(endInt),polynominalZ.evaluate(endInt));//dot�hnut� do bodu
			gl.glEnd();

		}
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSetings;
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleNodes(gl);
	}

}
