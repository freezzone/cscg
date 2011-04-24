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
 * @author Tom� Re�nar
 */
public class ObjectHermit extends AbstractObjectCurve<Point3fChangeable>
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
	 * Bod nejv�ce vlevo v polynomu.
	 */
	private IPoint3f leftPoint;
	/**
	 * Bod nejv�ce vpravo v polynomu.
	 */
	private IPoint3f rightPoint;
	/**
	 * GUI panel s nastaven�m objektu
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
	/**
	 * GUI panel pro vlastnosti bodu.
	 */
	private transient JPanel pointPanel;
	/**
	 * GUI label zobrazuj�c� tangentu k�ivky v dan�m vybran�m bod�.
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

		//inicializace gui pro vlastnosti bodu
		pointPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pointTangentLabel=new JLabel();
		pointPanel.add(pointTangentLabel);
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
				l.eventSpecificPropertiesChanged(ObjectHermit.this);
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
		Point3fChangeable last=null;
		boolean change=false;
		Point3fChangeable p;
		//projdu v�echny sud� indexy, krom� posledn�ho sud�ho indexu pokud je celkov� po�et bod� lich�
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
	 * Zajist� aby dan� bod byl v rozmez� mezi sou�adnicemi x p�edchoz�ho a n�sleduj�c�ho bodu, ale pouze pro bod
	 * ud�vaj�c� polohu, u bodu ud�vaj�c� tangentu je poloha libovoln�.
	 * Funkce nevyvol� ud�losti p�i zm�n�.
	 * @return vr�t� true pokud byli n�jak ovlivn�ny body (vlastnosti, po�ad�).
	 */
	private boolean checkPoint(IPoint3f pointToCheck)
	{
		int index = points.indexOf(pointToCheck);
		//pokud je to bod ud�vaj�c� polohu
		if(index>=0 && index%2==0)
		{
			Point3fChangeable point=(Point3fChangeable)pointToCheck;
			int prevIndex = index - 2;
			int nextIndex = index + 2;
			Point3f prev = prevIndex >= 0 ? points.get(prevIndex) : null;
			Point3f next = nextIndex < points.size() ? points.get(nextIndex) : null;
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
					change = true;
					insertPointOnProperIndex(point);
				}
			}
			return change;
		}
		//pokud je to bod ud�vaj�c� tangentu
		else
		{
			return false;
		}
	}

	/**
	 * Zajist� aby dvojice bod� (bod+tangenta zadan� bodem)
	 * byla za�azen� do pole bod� dle velikosti x sou�adnice.
	 * Nevyvol� ud�lost p�i zm�n�.
	 * @param point Libovoln� bod ze dvojice bod�
	 * @return Vr�t� true pokud bylo n�jak manipulov�no s body.
	 */
	private synchronized boolean insertPointOnProperIndex(Point3f point)
	{
		int indexPoint=points.indexOf(point);
		boolean pointIsTangent=indexPoint%2==1;//zda je bod 2. z dvojice - ud�v� tangentu
		//pokud dan� bod neexistuje nebo chyb� druh� bod z p�ru
		if(indexPoint<0 || (pointIsTangent==false && indexPoint==points.size()-1))
		{
			return false;
		}
		//zjist�m cel� p�r bod�
		Point3fChangeable positionPoint=pointIsTangent==false?(Point3fChangeable)point:points.get(indexPoint-1);
		Point3fChangeable tangentPoint=pointIsTangent==true?(Point3fChangeable)point:points.get(indexPoint+1);

		//odeberu oba body
		points.remove(positionPoint);
		points.remove(tangentPoint);

		//vyhled�m spr�vn� um�st�n� a vlo��m
		boolean inserted = false; //zda jsem bod vlo�il
		int position = -1;
		boolean isTangentPosition=true;//zda se v cyklu nach�z�m na pozci s bodem ud�v�j�c�m tangentu
		for (IPoint3f p : points)
		{
			isTangentPosition=!isTangentPosition;
			position++;
			if(!isTangentPosition)//u bod� ud�vaj�c�ch polohu zkontroluji po�ad�
			{
				if (p.getX() >= positionPoint.getX())
				{
					//vlo��m body na spr�vn� m�sto
					points.add(position, tangentPoint);
					points.add(position, positionPoint);
					inserted = true;
					break;
				}
			}
		}
		if (inserted == false)//dvojice bod� m� nejv�t�� sou�adnice x=>vlo�it na konec
		{
			if(isTangentPosition)//je-li na posledn�m m�st� tangentov� bod (v�echny dvojice jsou kompletn�)
			{
				points.add(positionPoint);
				points.add(tangentPoint);
			}
			else//na posledn�m m�st� je bod ud�vaj�c� pozici (nekompletn� dvojice na konci)
			{
				//vlo��m dvojici p�ed neuzav�enou dvojici
				points.add(position, tangentPoint);
				points.add(position, positionPoint);
			}
		}
		return true;
	}

	@Override
	public synchronized IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint)
	{
		newPoint=super.addPointBefore(newPoint, null);//vlo��m nov� bod
		if(insertPointOnProperIndex((Point3f)newPoint))
		{
			firePointsChange();
		}
		return newPoint;
	}

	@Override
	public synchronized IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint)
	{
		newPoint=super.addPointBefore(newPoint, null);//vlo��m nov� bod
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
	 * P�i odstran�n� bodu dojde k odstran�n� cel� dvojice bod�.
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
					super.removePoint(points.get(index+1));//sma�u bod ud�vaj�c� tangentu
				}
				else
				{
					super.removePoint(points.get(index-1));//sma�u bod ud�vaj�c� polohu
				}
			}
			catch(IndexOutOfBoundsException ex){}//kdy� chyb� druh� bod z dvojice
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
	 * Nastaven� panelu vlastnost� bodu.
	 */
	private synchronized void updatePointPanel()
	{
		int selectedSize=selectedPoints.size();
		IPoint3f point,//bod
		  pointTangent;//bod ur�uj�c� tangentu
		//m�m vybr�no mnoho bod� nebo ��dn�
		if(selectedSize>2 || selectedSize==0)
		{
			pointTangentLabel.setText("");
			return;
		}
		//mam vtbrany 2 body
		if(selectedSize==2)
		{
			//indexy vybran�ch bod�
			int index1=points.indexOf(selectedPoints.get(0));
			int index2=points.indexOf(selectedPoints.get(1));
			//nejedn� se o sousedn� body
			if(Math.abs(index1-index2)!=1)
			{
				pointTangentLabel.setText("");
				return;
			}
			if(index1>index2)//v index1 mus� b�t men�� index
			{
				int tmp=index2;
				index2=index1;
				index1=tmp;
			}
			//jsou vybrane 2 body je� netvo�� p�r
			if(index1%2==1)
			{
				pointTangentLabel.setText("");
				return;
			}
			//ulo��m dvojici body
			point=points.get(index1);
			pointTangent=points.get(index2);
		}
		//mam vybran� pouze jeden bod
		else
		{
			//indexy vybran�ho bodu
			int index=points.indexOf(selectedPoints.get(0));
			//pokud se jedn� o posledn� bod je� nem� druh� bod do dvojice
			if(index==points.size()-1 && index%2==0)
			{
				pointTangentLabel.setText("");
				return;
			}
			//ulo�en� dvojice bodu
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
		 * zde m�m ji� napln�n bod a bod ur�uj�c� tangentu
		 */
		float direction[]=PointOperations.normalizeVector3(PointOperations.directionVector(point, pointTangent));
		pointTangentLabel.setText("Sm�rnice te�ny "+String.format("[%.2f; %.2f; %.2f]",
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
								throw new ArithmeticException("V�raz se bl�� nekone�nu.");
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
					//zji�t�n� nejv�c lev�ho bodu a nejv�c prav�ho bodu
					leftPoint=points.get(0);
					rightPoint=points.get(0);
					IPoint3f p;//porovnavany bod
					for(int i=2;i<pointsCount;i+=2)//body le�� na sud�ch pozic�ch(lich� pozice jsou te�ny)
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
				//p�i bodech um�st�n�ch nad sebou k�ivka neexistuje
				catch(IllegalArgumentException ex)
				{
					setStateMessage("Explicitn� tvar rovnice neexistuje, proto�e existuj� dva body pro jedno x.");
					setState(ObjectState.inputError);
				}
				//pokud je te�na kolm� k ose x
				catch(ArithmeticException ex)
				{
					setStateMessage("K�ivka neexistuje, proto�e te�na je koln� k ose x.");
					setState(ObjectState.inputError);
				}
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
			Bounds drawBounds=getBounds();
			gl.glLineWidth(lineWidth);
			gl.glColor3fv(color.getColorComponents(null),0);
			double step=1.,//step=1/scale,
				startInt=drawBounds.getX1()>leftPoint.getX()?drawBounds.getX1():leftPoint.getX(),//za��tek interpolace
				endInt=drawBounds.getX2()<rightPoint.getX()?drawBounds.getX2():rightPoint.getX();//konec interpolace
			//vlastn� interpolace
			gl.glBegin(gl.GL_LINE_STRIP);
			for(double i=startInt;i<endInt;i+=step)
			{
				gl.glVertex3d(i, polynominalY.evaluate(i),polynominalZ.evaluate(i));
			}
			gl.glVertex3d(endInt, polynominalY.evaluate(endInt),polynominalZ.evaluate(endInt));//dot�hnut� do bodu
			gl.glEnd();

		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleNodes(gl);
		//vykreslen� te�en
		int tangentsCount=points.size()-(points.size()%2);//2 body pro te�nu
		GLUtils.glSetColor(gl, supportColor);
		gl.glLineWidth(1);
		IPoint3f startPoint,endPoint;//prost�edn� bod a koncov� bod tangenty/te�ny
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
