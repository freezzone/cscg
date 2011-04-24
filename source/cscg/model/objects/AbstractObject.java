package cscg.model.objects;

import cscg.ui.components.ColorSelector;
import cscg.ui.GLUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Základní abstraktní implementace objektu, implementující všechny spoleèné vlastnosti objektù. Tøída je abstraktní,
 * parametr PointClass udává s jakımi typy bodù tøída pracuje. Tøídy které dìdí z této tøídy musí ve svém konstruktoru
 * uloit do instanèní promnìné pointPrototype instanci bodu kterı je typu PointClass. Defaultnì je v promnìné
 * pointPrototype instance bodu tøídy Point3f.
 * @author Tomáš Renar
 */
public abstract class AbstractObject<PointClass extends Point3f> implements IObject {
	/**
	 * Jméno objektu.
	 */
	protected volatile String name;
	/**
	 * Prototyp øídících bodù objektu. Tuto promnìnou musí zmìnit potomci tøídy, kteøí pouívají jinı typ bodù
	 * (napøíklad objekt NURBS køivky uloí do této promnìné instanci Point4f). Novì vytváøené øídící body
	 * budou vytvoøeny jako klony tohoto bodu.
	 */
	protected Point3f pointPrototype=new Point3f();
	/**
	 * Mnoina vybranıch bodù.
	 */
	protected transient ArrayList<PointClass> selectedPoints;
	/**
	 * Poèítadlo vytvoøenıch instancí.
	 */
	static int counter = 0;
	/**
	 * Hranice objektu - pøedstavuje prostor potøebnı pro vykreslenı objektu.
	 */
	protected volatile Bounds bounds;
	/**
	 * Barva objektu.
	 */
	protected volatile Color color;
	/**
	 * Barva pro vykreslení pomocnıch editaèních èar.
	 */
	protected final Color supportColor;
	/**
	 * GUI prvek pro vıbìr barvy objektu.
	 */
	protected transient ColorSelector guiColorButton;
	/**
	 * GUI panel s nastavením objektu.
	 */
	private transient JPanel guiSimplePanel;
	/**
	 * GUI prvek pro zapnutí osvìtlení.
	 */
	private transient JCheckBox guiLightOn;
	/**
	 * Posluchaèi zmìn objektu.
	 */
	protected transient ArrayList<ObjectListener> listeners;
	/**
	 * Pole bodù objektu, nebo jinıch uzlù definujích objekt.
	 */
	protected final ArrayList<PointClass> points = new ArrayList<PointClass>(16);
	/**
	 * Stav objektu (jestli se zmìnil, jestli se vykresluje...).
	 */
	protected ObjectState state = ObjectState.notCounted;
	/**
	 * Stavová zpráva, ve které je uivatelsky zamìøenı text, napøíklad o dùvodu proè není mono objekt vykreslit.
	 */
	protected String stateMessage = "";
	/**
	 * Zobrazování objektu.
	 */
	protected volatile boolean visible = true;
	/**
	 * Zapnutí svìtel.
	 */
	protected volatile boolean lightOn = true;

	public AbstractObject()
	{
		name = "Object " + counter++;
		color=Color.BLACK;
		supportColor=new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB),
		  Color.darkGray.getRGBColorComponents(null), 0.8f);
		initListeners();
		initTransients();
	}

	/**
	 * Inicializace transientních vlastností.
	 */
	private void initTransients()
	{
		selectedPoints=new ArrayList<PointClass>();
		initGUI();
	}

	/**
	 * Inicializace seznamu posluchaèù.
	 */
	private void initListeners()
	{
		listeners=new ArrayList<ObjectListener>();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		initListeners();
		in.defaultReadObject();
		initTransients();
	}

	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.defaultWriteObject();
	}

	/**
	 * Vytvoøení základního GUI.
	 */
	private void initGUI()
	{
		guiSimplePanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx=0;
		gbc.gridy=0;
		gbc.insets=new Insets(3, 0, 0, 0);
		gbc.anchor=GridBagConstraints.WEST;
		guiSimplePanel.add(new JLabel("Svìtlo"),gbc);

		gbc.gridx=1;
		gbc.gridy=0;
		gbc.weightx=1;
		gbc.anchor=GridBagConstraints.EAST;
		guiLightOn=new JCheckBox("", lightOn);
		guiSimplePanel.add(guiLightOn,gbc);

		gbc.gridx=0;
		gbc.gridy=1;
		gbc.insets=new Insets(3, 0, 0, 0);
		gbc.anchor=GridBagConstraints.WEST;
		guiSimplePanel.add(new JLabel("Barva"),gbc);

		gbc.gridx=1;
		gbc.gridy=1;
		gbc.weightx=1;
		gbc.anchor=GridBagConstraints.EAST;
		guiColorButton=new ColorSelector(color);
		guiSimplePanel.add(guiColorButton,gbc);

		//poslouchání událostí z gui prvkù
		guiLightOn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				setLightOn(guiLightOn.isSelected());
			}
		});

		guiColorButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				setColor(guiColorButton.getColor());
			}
		});
	}

	/**
	 * Vytvoøí novı øídící bod klonováním z prototypu.
	 * Pokud potøebujete zmìnit prototyp bodù, zmìe promnìnou pointPrototype.
	 */
	protected final PointClass createNewPoint()
	{
		return (PointClass)pointPrototype.clone();
	}

	/**
	 * Nastavení GUI s vlastnostmi objektu.
	 * Napøíklad tøídy je dìdí z této tøídy mùou jednoduše definovat další GUI prvky.
	 * Funkce by nemìla bıt volána opakovanì, protoe vkládá prvek na stejné místo v panelu.
	 */
	protected synchronized void setSpecificGUI(JComponent gui)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=2;
		gbc.gridwidth=2;
		gbc.weightx=1;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		guiSimplePanel.add(gui,gbc);
	}

	@Override
	public synchronized void addObjectListener(ObjectListener l)
	{
		listeners.add(l);
	}

	@Override
	public synchronized void addSelectedPoint(IPoint3f selectedPoint)
	{
		if (points.contains(selectedPoint))
		{
			if (!selectedPoints.contains((PointClass)selectedPoint))
			{
				selectedPoints.add((PointClass)selectedPoint);
			} else //kdy bod v seznamu ji je, pøesunu jej na posledni misto
			{
				selectedPoints.remove((PointClass)selectedPoint);
				selectedPoints.add((PointClass)selectedPoint);
			}
			firePointsSelectionChange();
		}
	}

	@Override
	public synchronized void cancelSelectedPoint(IPoint3f selectedPoint)
	{
		if (this.selectedPoints.remove(selectedPoint))
		{
			firePointsSelectionChange();
		}
	}

	@Override
	public synchronized void cancelSelectedPoints()
	{
		selectedPoints.clear();
		firePointsSelectionChange();
	}

	/**
	 * Základní implementace vykreslení editaèní bodù objektu. Funkce provede i pøíkaz gl.glDepthFunc(gl.GL_ALWAYS);
	 */
	protected synchronized void drawSimpleNodes(GL2 gl)
	{
		gl.glDepthFunc(gl.GL_ALWAYS); //body jsou zobrazeny pøes vešketı obsah
		List<IPoint3f> nodes = getPoints();
		GLUtils.drawPoints(gl, nodes, Color.BLACK, 8); //vnìjší kruh
		GLUtils.drawPoints(gl, nodes, Color.GRAY, 5); //vnitøní kruh
		GLUtils.drawPoints(gl, getSelectedPoints(), Color.RED, 5); //vnitøní kruh vybranıch bodù
	}

	@Override
	public synchronized void editPoint(IPoint3f editedPoint, IPoint3f setBy)
	{
		int index = points.indexOf(editedPoint);
		if (index < 0)
		{
			return;
		}
		setState(ObjectState.notCounted); //oznamim zmìnu objektu
		Point3f point=(Point3f)editedPoint;
		point.setX(setBy.getX());
		point.setY(setBy.getY());
		point.setZ(setBy.getZ());
		if (editedPoint instanceof Point4f && setBy instanceof IPoint4f)
		{
			((Point4f) editedPoint).setW(((IPoint4f) setBy).getW());
		}
		reportAnyPointChange();
		for (ObjectListener l : listeners)
		{
			l.eventPointChanged(this, editedPoint, index);
		}
	}

	/**
	 * Základní implementace nalezení hranic objektu, metodou nalezení okrajovıch bodù.
	 */
	protected synchronized Bounds findBounds()
	{
		return Bounds.findBounds(points);
	}

	/**
	 * Vyvolání události zmìny bodu/ù a jejich vlastností.
	 */
	protected synchronized void firePointsChange()
	{
		reportAnyPointChange();
		for (ObjectListener l : listeners)
		{
			l.eventPointsChanged(this);
		}
	}

	/**
	 * Metoda je je zavolána pøi jakékoli zmìnì bodù nebo vybranıch bodù.
	 * Poznámka: slouí ke sledování zmìn bodù pro aktualizaci panelu s nastevením bodù, napøíklad u Hermita
	 * pro aktualizaci informace o tangentì vybranıch bodù.
	 */
	protected synchronized void reportAnyPointChange()
	{}

	/**
	 * Vyvolání události zmìny vybranıch bodù.
	 */
	protected synchronized void firePointsSelectionChange()
	{
		reportAnyPointChange();
		for (ObjectListener l : listeners)
		{
			l.eventPointSelectionChanged(this);
		}
	}

	@Override
	public synchronized Bounds getBounds()
	{
		if (state == ObjectState.notCounted || bounds == null)
		{
			bounds = findBounds();
		}
		return bounds;
	}

	/**
	 * Získání barvy objektu.
	 */
	public synchronized Color getColor()
	{
		return color;
	}

	@Override
	public synchronized Component getObjectGUI()
	{
		return guiSimplePanel;
	}

	@Override
	public Component getPointGUI()
	{
		return null;
	}



	@Override
	public synchronized PointClass getLastSelectedPoint()
	{
		try
		{
			return selectedPoints.get(selectedPoints.size() - 1);
		} catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	@Override
	public synchronized String getName()
	{
		return name;
	}

	@Override
	public synchronized PointClass getPoint(int index)
	{
		return points.get(index);
	}

	@Override
	public synchronized ArrayList<IPoint3f> getPoints()
	{
		return new ArrayList<IPoint3f>(points);
	}

	@Override
	public synchronized int getPointsCount()
	{
		return points.size();
	}

	@Override
	public synchronized ArrayList<IPoint3f> getSelectedPoints()
	{
		return new ArrayList<IPoint3f>(selectedPoints);
	}

	@Override
	public int[] getSelectedPointsIndexes()
	{
		int[] indexes=new int[selectedPoints.size()];
		int i=0;
		for(IPoint3f p:selectedPoints)
		{
			indexes[i]=points.indexOf(p);
			i++;
		}
		return indexes;
	}



	@Override
	public synchronized int getSelectedPointsCount()
	{
		return selectedPoints.size();
	}

	@Override
	public synchronized ObjectState getState()
	{
		return state;
	}

	@Override
	public synchronized String getStateMessage()
	{
		return stateMessage;
	}

	@Override
	public synchronized boolean isVisible()
	{
		return visible;
	}

	@Override
	public synchronized void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset)
	{
		int index = points.indexOf(point);
		if (index < 0)
		{
			return;
		}
		IPoint3f newPoint = PointOperations.move(point, xOffset, yOffset, zOffset);
		Point3f point3f=(Point3f)point;
		point3f.setX(newPoint.getX());
		point3f.setY(newPoint.getY());
		point3f.setZ(newPoint.getZ());
		reportAnyPointChange();
		setState(ObjectState.notCounted); //oznamim zmìnu objektu
		for (ObjectListener l : listeners)
		{
			l.eventPointChanged(this, point, index);
		}
	}

	@Override
	public synchronized void movePointTo(IPoint3f point, float x, float y, float z)
	{
		int index = points.indexOf(point);
		if (index < 0)
		{
			return;
		}
		Point3f point3f=(Point3f)point;
		point3f.setX(x);
		point3f.setY(y);
		point3f.setZ(z);
		reportAnyPointChange();
		setState(ObjectState.notCounted); //oznamim zmìnu objektu
		for (ObjectListener l : listeners)
		{
			l.eventPointChanged(this, point, index);
		}
	}

	@Override
	public synchronized void removeObjectListener(ObjectListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Nastavení barvy objektu.
	 */
	public synchronized void setColor(Color color)
	{
		if (color == null)
		{
			throw new NullPointerException();
		}
		if (this.color.equals(color))
		{
			return;
		}
		this.color = color;
		guiColorButton.setColor(color);
		for (ObjectListener l : listeners)
		{
			l.eventColorChanged(this);
		}
	}

	@Override
	public synchronized void setName(String name)
	{
		if (!this.name.equals(name))
		{
			this.name = name;
			for (ObjectListener l : listeners)
			{
				l.eventNameChanged(this);
			}
		}
	}

	@Override
	public synchronized void setSelectedPoint(IPoint3f selectedPoint)
	{
		this.selectedPoints.clear();
		if (points.contains(selectedPoint))
		{
			this.selectedPoints.add((PointClass)selectedPoint);
			firePointsSelectionChange();
		}
	}

	@Override
	public synchronized void setSelectedPoints(List<? extends IPoint3f> selectedPoints)
	{
		this.selectedPoints.clear();
		for (IPoint3f p : selectedPoints)
		{
			if (points.contains(p))
			{
				this.selectedPoints.add((PointClass)p);
			}
		}
		firePointsSelectionChange();
	}

	@Override
	public void setSelectedPoints(int[] selectedPoints)
	{
		this.selectedPoints.clear();
		PointClass p;
		for(int i:selectedPoints)
		{
			try
			{
				p=points.get(i);
				this.selectedPoints.add(p);
			}
			catch(IndexOutOfBoundsException ex){}//pokud je ve vıbìru neexistující bod
		}
		firePointsSelectionChange();
	}



	/**
	 * Nastavení stavu objektu.
	 */
	protected synchronized void setState(ObjectState state)
	{
		this.state = state;
		if (state == ObjectState.notCounted)
		{
			bounds = null;
		}
	}

	/**
	 * Nastavení stavové zprávy
	 * @param inputErrorText
	 */
	protected synchronized void setStateMessage(String inputErrorText)
	{
		this.stateMessage = inputErrorText;
		for (ObjectListener l : listeners)
		{
			l.eventStateMessageChanged(this);
		}
	}

	@Override
	public synchronized void setVisible(boolean visible)
	{
		this.visible = visible;
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	@Override
	public synchronized String toString()
	{
		return getName();
	}

	@Override
	public synchronized void rotate(IPoint3f pivot, IPoint3f axesVector, double angle)
	{
		if(points.isEmpty())
		{
			return;
		}
		//projdu všechny body
		float vector[];
		axesVector=PointOperations.normalize(axesVector);
		for(Point3f p:points)
		{
			vector=PointOperations.axisRotationVector3(axesVector,
				angle,
				new Point3f(PointOperations.directionVector(pivot, p)) {});
			p.setX(vector[0]+pivot.getX());
			p.setY(vector[1]+pivot.getY());
			p.setZ(vector[2]+pivot.getZ());
		}
		setState(ObjectState.notCounted);
		firePointsChange();
	}

	@Override
	public synchronized IPoint3f getCenter()
	{
		return getBounds().getCenterPoint();
	}

	@Override
	public synchronized IPoint3f getAverageCenter()
	{
		float x=0,y=0,z=0;
		for(IPoint3f p:points)
		{
			x+=p.getX();
			y+=p.getY();
			z+=p.getZ();
		}
		float count=points.size();
		if(count>=1f)
		{
			x/=count;
			y/=count;
			z/=count;
		}
		return new Point3f(x, y, z);
	}

	@Override
	public synchronized void translateTo(IPoint3f to)
	{
		if(points.isEmpty())
		{
			return;
		}
		IPoint3f moveVector=new Point3f(PointOperations.directionVector(getCenter(), to));
		translateBy(moveVector);
	}

	@Override
	public synchronized void translateBy(IPoint3f vector)
	{
		if(points.isEmpty())
		{
			return;
		}
		float[] moveVector=new float[]{vector.getX(),vector.getY(),vector.getZ()};
		for(Point3f p:points)
		{
			p.setX(p.getX()+moveVector[0]);
			p.setY(p.getY()+moveVector[1]);
			p.setZ(p.getZ()+moveVector[2]);
		}
		setState(ObjectState.notCounted);
		firePointsChange();
	}

	/**
	 * Jestli se pouívá osvìtlení pøi vykreslení objektu.
	 */
	public synchronized boolean isLightOn()
	{
		return lightOn;
	}

	/**
	 * Nastaví pouívání osvìtlení pøi vykreslování objektu.
	 */
	public synchronized void setLightOn(boolean lightOn)
	{
		if(this.lightOn != lightOn)
		{
			this.lightOn = lightOn;
			guiLightOn.setSelected(lightOn);
			for(ObjectListener l:listeners)
			{
				l.eventSpecificPropertiesChanged(this);
			}
		}
	}

	@Override
	public final void draw(GL2 gl)
	{
		if(lightOn==false)
		{
			gl.glDisable(gl.GL_LIGHTING);
		}
		drawObject(gl);
	}

	/**
	 * Vykreslení objektu.
	 */
	protected abstract void drawObject(GL2 gl);


}
