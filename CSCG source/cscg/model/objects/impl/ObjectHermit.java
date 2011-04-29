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
import cscg.model.objects.PointOperations;
import cscg.ui.GLUtils;
import java.awt.Component;
import java.awt.FlowLayout;
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
 * Implementace Hermitovy interpolace.
 * @author Tomáš Režnar
 */
public class ObjectHermit extends AbstractObjectCurve<Point3fChangeable>
{
	/**
	 * Počítadlo instancí.
	 */
	private static int selfCounter=1;
	/**
	 * Objekt pro řešení matic.
	 */
	private Algebra alg=Algebra.DEFAULT;
	/**
	 * Polynom pro výpočet y souřadnice.
	 */
	private Polynomial polynominalY;
	/**
	 * Polynom pro výpočet z souřadnice.
	 */
	private Polynomial polynominalZ;
	/**
	 * Bod nejvíce vlevo v polynomu.
	 */
	private IPoint3f leftPoint;
	/**
	 * Bod nejvíce vpravo v polynomu.
	 */
	private IPoint3f rightPoint;
	/**
	 * GUI panel s nastavením objektu
	 */
	private transient JPanel guiSetings;
	/**
	 * GUI prvek pro nastavení automatického sařazení vkládáných bodů.
	 */
	private transient JCheckBox guiAutoSort;
	/**
	 * Automatického sařazení vkládáných bodů.
	 */
	private volatile boolean autoSort=true;
	/**
	 * GUI panel pro vlastnosti bodu.
	 */
	private transient JPanel pointPanel;
	/**
	 * GUI label zobrazující tangentu křivky v daném vybraném bodě.
	 */
	private transient JLabel pointTangentLabel;

	public ObjectHermit()
	{
		super();
		pointPrototype=new Point3fChangeable();
		setName("Hermitova interpolace "+selfCounter++);

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
		guiSetings.add(new JLabel("Seřazovat body"),gbc);

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

		//inicializace gui pro vlastnosti bodu
		pointPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pointTangentLabel=new JLabel();
		pointPanel.add(pointTangentLabel);
	}

	/**
	 * Vrátí true když se body třídí automaticky.
	 */
	public synchronized boolean isAutoSort()
	{
		return autoSort;
	}

	/**
	 * Nastaví automatické třídění bodů.
	 */
	public synchronized void setAutoSort(boolean autoSort)
	{
		if(this.autoSort != autoSort)
		{
			this.autoSort = autoSort;
			guiAutoSort.setSelected(autoSort);
			if(!autoSort)
			{
				//zajistím aby nebylo více bodů na stejné souřadnici x
				if(checkOrder())
				{
					setState(ObjectState.notCounted);
					firePointsChange();
				}
			}
			for(ObjectListener l:listeners)
			{
				l.eventSpecificPropertiesChanged(ObjectHermit.this);
			}
		}
	}

	/**
	 * Projde body a v případě neplatného pořadí body posune.
	 * Funkce nevyvolává žádné události.
	 * @return True pokud došlo k úpravě bodů.
	 */
	private boolean checkOrder()
	{
		Point3fChangeable last=null;
		boolean change=false;
		Point3fChangeable p;
		//projdu všechny sudé indexy, kromě posledního sudého indexu pokud je celkový počet bodů lichý
		for(int index=0;index<points.size()-points.size()%2;index+=2)
		{
			p=points.get(index);
			if(last!=null)
			{
				if(p.getX()<=last.getX())
				{
					p.setX(last.getX()+1f);//posunu bod
					change=true;
				}
			}
			last=p;
		}
		return change;
	}

	/**
	 * Zajistí aby daný bod byl v rozmezí mezi souřadnicemi x předchozího a následujícího bodu, ale pouze pro bod
	 * udávající polohu, u bodu udávající tangentu je poloha libovolná.
	 * Funkce nevyvolá události při změně.
	 * @return vrátí true pokud byli nějak ovlivněny body (vlastnosti, pořadí).
	 */
	private boolean checkPoint(IPoint3f pointToCheck)
	{
		int index = points.indexOf(pointToCheck);
		//pokud je to bod udávající polohu
		if(index>=0 && index%2==0)
		{
			Point3fChangeable point=(Point3fChangeable)pointToCheck;
			int prevIndex = index - 2;
			int nextIndex = index + 2;
			Point3f prev = prevIndex >= 0 ? points.get(prevIndex) : null;
			Point3f next = nextIndex < points.size() ? points.get(nextIndex) : null;
			boolean change = false;
			if(!guiAutoSort.isSelected())//pokud se body neseřazují automaticky
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
			else//když se body seřazují automaticky
			{
				//body nejsou spravně seřazeny
				if ((next != null && point.getX() > next.getX()) || (prev != null && point.getX() < prev.getX()))
				{
					change = true;
					insertPointOnProperIndex(point);
				}
			}
			return change;
		}
		//pokud je to bod udávající tangentu
		else
		{
			return false;
		}
	}

	/**
	 * Zajistí aby dvojice bodů (bod+tangenta zadaná bodem)
	 * byla zařazená do pole bodů dle velikosti x souřadnice.
	 * Nevyvolá událost při změně.
	 * @param point Libovolný bod ze dvojice bodů
	 * @return Vrátí true pokud bylo nějak manipulováno s body.
	 */
	private synchronized boolean insertPointOnProperIndex(Point3f point)
	{
		int indexPoint=points.indexOf(point);
		boolean pointIsTangent=indexPoint%2==1;//zda je bod 2. z dvojice - udává tangentu
		//pokud daný bod neexistuje nebo chybí druhý bod z páru
		if(indexPoint<0 || (pointIsTangent==false && indexPoint==points.size()-1))
		{
			return false;
		}
		//zjistím celý pár bodů
		Point3fChangeable positionPoint=pointIsTangent==false?(Point3fChangeable)point:points.get(indexPoint-1);
		Point3fChangeable tangentPoint=pointIsTangent==true?(Point3fChangeable)point:points.get(indexPoint+1);

		//odeberu oba body
		points.remove(positionPoint);
		points.remove(tangentPoint);

		//vyhledám správné umístění a vložím
		boolean inserted = false; //zda jsem bod vložil
		int position = -1;
		boolean isTangentPosition=true;//zda se v cyklu nacházím na pozci s bodem udávájícím tangentu
		for (IPoint3f p : points)
		{
			isTangentPosition=!isTangentPosition;
			position++;
			if(!isTangentPosition)//u bodů udávajících polohu zkontroluji pořadí
			{
				if (p.getX() >= positionPoint.getX())
				{
					//vložím body na správné místo
					points.add(position, tangentPoint);
					points.add(position, positionPoint);
					inserted = true;
					break;
				}
			}
		}
		if (inserted == false)//dvojice bodů má největší souřadnice x=>vložit na konec
		{
			if(isTangentPosition)//je-li na posledním místě tangentový bod (všechny dvojice jsou kompletní)
			{
				points.add(positionPoint);
				points.add(tangentPoint);
			}
			else//na posledním místě je bod udávající pozici (nekompletní dvojice na konci)
			{
				//vložím dvojici před neuzavřenou dvojici
				points.add(position, tangentPoint);
				points.add(position, positionPoint);
			}
		}
		return true;
	}

	@Override
	public synchronized IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint)
	{
		newPoint=super.addPointBefore(newPoint, null);//vložím nový bod
		if(insertPointOnProperIndex((Point3f)newPoint))
		{
			firePointsChange();
		}
		return newPoint;
	}

	@Override
	public synchronized IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint)
	{
		newPoint=super.addPointBefore(newPoint, null);//vložím nový bod
		if(insertPointOnProperIndex((Point3f)newPoint))
		{
			firePointsChange();
		}
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

	/**
	 * Při odstranění bodu dojde k odstranění celé dvojice bodů.
	 */
	@Override
	public synchronized void removePoint(IPoint3f point)
	{
		int index=points.indexOf(point);
		if(index>=0)
		{
			try
			{
				if(index%2==0)
				{
					super.removePoint(points.get(index+1));//smažu bod udávající tangentu
				}
				else
				{
					super.removePoint(points.get(index-1));//smažu bod udávající polohu
				}
			}
			catch(IndexOutOfBoundsException ex){}//když chybí druhý bod z dvojice
			super.removePoint(point);
		}
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSetings;
	}

	@Override
	public Component getPointGUI()
	{
		return pointPanel;
	}

	@Override
	protected synchronized void reportAnyPointChange()
	{
		updatePointPanel();
	}

	/**
	 * Nastavení panelu vlastností bodu.
	 */
	private synchronized void updatePointPanel()
	{
		int selectedSize=selectedPoints.size();
		IPoint3f point,//bod
		  pointTangent;//bod určující tangentu
		//mám vybráno mnoho bodů nebo žádný
		if(selectedSize>2 || selectedSize==0)
		{
			pointTangentLabel.setText("");
			return;
		}
		//mam vtbrany 2 body
		if(selectedSize==2)
		{
			//indexy vybraných bodů
			int index1=points.indexOf(selectedPoints.get(0));
			int index2=points.indexOf(selectedPoints.get(1));
			//nejedná se o sousední body
			if(Math.abs(index1-index2)!=1)
			{
				pointTangentLabel.setText("");
				return;
			}
			if(index1>index2)//v index1 musí být menší index
			{
				int tmp=index2;
				index2=index1;
				index1=tmp;
			}
			//jsou vybrane 2 body jež netvoří pár
			if(index1%2==1)
			{
				pointTangentLabel.setText("");
				return;
			}
			//uložím dvojici body
			point=points.get(index1);
			pointTangent=points.get(index2);
		}
		//mam vybraný pouze jeden bod
		else
		{
			//indexy vybraného bodu
			int index=points.indexOf(selectedPoints.get(0));
			//pokud se jedná o poslední bod jež nemá druhý bod do dvojice
			if(index==points.size()-1 && index%2==0)
			{
				pointTangentLabel.setText("");
				return;
			}
			//uložení dvojice bodu
			if(index%2==0)
			{
				point=points.get(index);
				pointTangent=points.get(index+1);
			}
			else
			{
				point=points.get(index-1);
				pointTangent=points.get(index);
			}
		}

		/*
		 * zde mám již naplněn bod a bod určující tangentu
		 */
		float direction[]=PointOperations.normalizeVector3(PointOperations.directionVector(point, pointTangent));
		pointTangentLabel.setText("Směrnice tečny "+String.format("[%.2f; %.2f; %.2f]",
		  direction[0],direction[1],direction[2]));
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(state==ObjectState.notCounted)
		{
			if(points.size()>=4)
			{
				try
				{
					int pointsCount=points.size()-(points.size()%2);//pocet bodu musi byt sudy pro vykresleni
					DenseDoubleMatrix2D base=new DenseDoubleMatrix2D(pointsCount, pointsCount);
					DenseDoubleMatrix2D Y=new DenseDoubleMatrix2D(pointsCount,1);
					DenseDoubleMatrix2D Z=new DenseDoubleMatrix2D(pointsCount,1);
					for(int row=0,col,mocnina;row<pointsCount-1;row+=2)
					{
							for(col=0,mocnina=pointsCount-1;col<pointsCount;col++,mocnina--)
							{
									//zakladni rovnice krivky v bode
									base.set(row,col,Math.pow(points.get(row).getX(), mocnina));
									Y.set(row,0,points.get(row).getY());
									Z.set(row,0,points.get(row).getZ());
									//rovnice derivace
									if(mocnina!=0)//po derivaci neni posledni a koeficient-cisla v derivaci vypadavaji
									{
											base.set(row+1,col,mocnina*Math.pow(points.get(row).getX(), mocnina-1));
									}
							}
							Y.set(row+1,0,(points.get(row+1).getY()-points.get(row).getY())/(points.get(row+1).getX()-points.get(row).getX()));
							Z.set(row+1,0,(points.get(row+1).getZ()-points.get(row).getZ())/(points.get(row+1).getX()-points.get(row).getX()));
							if(Y.get(row+1,0)==Double.NEGATIVE_INFINITY || Y.get(row+1,0)==Double.POSITIVE_INFINITY)
							{
								throw new ArithmeticException("Výraz se blíží nekonečnu.");
							}
					}
					DoubleMatrix2D coefsY = alg.solve(base, Y);
					DoubleMatrix2D coefsZ = alg.solve(base, Z);
					double coefsYReverse[]=new double[coefsY.rows()];
					double coefsZReverse[]=new double[coefsZ.rows()];
					for(int i=0;i<coefsYReverse.length;i++)
					{
							coefsYReverse[i]=coefsY.get(coefsYReverse.length-1-i, 0);
							coefsZReverse[i]=coefsZ.get(coefsZReverse.length-1-i, 0);
					}
					polynominalY=new Polynomial(coefsYReverse);
					polynominalZ=new Polynomial(coefsZReverse);
					//zjištění nejvíc levého bodu a nejvíc pravého bodu
					leftPoint=points.get(0);
					rightPoint=points.get(0);
					IPoint3f p;//porovnavany bod
					for(int i=2;i<pointsCount;i+=2)//body leží na sudých pozicích(liché pozice jsou tečny)
					{
						p=points.get(i);
						if(p.getX()<leftPoint.getX())
						{
							leftPoint=p;
						}
						if(p.getX()>rightPoint.getX())
						{
							rightPoint=p;
						}
					}

					setState(ObjectState.OK);
					setStateMessage(null);
				}
				//při bodech umístěných nad sebou křivka neexistuje
				catch(IllegalArgumentException ex)
				{
					setStateMessage("Explicitní tvar rovnice neexistuje, protože existují dva body pro jedno x.");
					setState(ObjectState.inputError);
				}
				//pokud je tečna kolmá k ose x
				catch(ArithmeticException ex)
				{
					setStateMessage("Křivka neexistuje, protože tečna je kolná k ose x.");
					setState(ObjectState.inputError);
				}
			}
			else//nedostatek bodů
			{
				setStateMessage("Zadejte minimálně čtyři body pro vykreslení křivky.");
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
				startInt=drawBounds.getX1()>leftPoint.getX()?drawBounds.getX1():leftPoint.getX(),//začátek interpolace
				endInt=drawBounds.getX2()<rightPoint.getX()?drawBounds.getX2():rightPoint.getX();//konec interpolace
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
		//vykreslení tečen
		int tangentsCount=points.size()-(points.size()%2);//2 body pro tečnu
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		IPoint3f startPoint,endPoint;//prostřední bod a koncový bod tangenty/tečny
		gl.glBegin(gl.GL_LINES);
		for(int i=0;i<tangentsCount;i+=2)
		{
			endPoint=points.get(i+1);
			startPoint=PointOperations.mirror(endPoint,points.get(i));
			gl.glVertex3f(endPoint.getX(), endPoint.getY(),endPoint.getZ());
			gl.glVertex3f(startPoint.getX(), startPoint.getY(),startPoint.getZ());
		}
		gl.glEnd();
	}

}
