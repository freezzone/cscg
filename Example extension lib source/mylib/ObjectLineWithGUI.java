package mylib;

import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import cscg.model.objects.ObjectListener;
import cscg.ui.GLUtils;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.media.opengl.GL2;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Ukázková implemetace křivky s valastním GUI.
 * V GUI pro nstavení objektu se bude volit stupeň křivky
 * a v GUI pro nastavení bodu se bude zobrazovat pořadí bodu.
 * @author Tomáš Režnar
 */
public class ObjectLineWithGUI extends AbstractObjectCurve<Point3f> {
	/**
	 * Počítadlo instancí
	 */
	private static int selfCounter=1;
	/**
	 * GUI prvek pro nastavení stupně křivky.
	 * GUI prvky se nebudou serializovat, proto se označí transient.
	 */
	private transient JSpinner guiDegree;
	/**
	 * Stupeň křivky.
	 */
	private volatile int degree = 3;
	/**
	 * GUI panel pro vlastnosti bodu
	 */
	private transient JPanel pointPanel;
	/**
	 * GUI label zobrazující pořadí bodu
	 */
	private transient JLabel pointLabel;

	public ObjectLineWithGUI()
	{
		super();
		setName("Úsečky s GUI "+selfCounter++);
		initTransients();
	}

	/**
	 * Inicializace transientních vlastností
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
	 * Vytvoření GUI
	 */
	private void initGUI()
	{
		//panel s nastavením vlastností objektu
		JPanel guiSetings=new JPanel(new GridBagLayout());
		//inicializace gui prvků nastavení objektu
		guiDegree = new JSpinner(new SpinnerNumberModel(degree, 1, 10, 1));
		GridBagConstraints gbc = new GridBagConstraints();

		//stupeň
		gbc.weightx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		guiSetings.add(new JLabel("Stupeň křivky"), gbc);

		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		guiSetings.add(guiDegree, gbc);

		//vložím do nadřazeného gui
		super.setSpecificGUI(guiSetings);

		//posluchač změn GUI pro nastavení stupně
		guiDegree.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e)
			{
				setDegree((Integer) guiDegree.getValue());
			}
		});

		//inicializace gui pro vlastnosti bodu
		pointPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pointLabel=new JLabel();
		pointPanel.add(pointLabel);

	}

	/**
	 * Vrácení panelu s GUI pro nastavení bodu
	 */
	@Override
	public Component getPointGUI()
	{
		return pointPanel;
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
	 */
	public synchronized void setDegree(int degree)
	{
		this.degree = degree;
		guiDegree.setValue(degree);
		setState(ObjectState.notCounted);//křivka se změnila
		//oznámení změny
		for (ObjectListener l : listeners)
		{
			l.eventSpecificPropertiesChanged(this);
		}
	}

	/**
	 * Sledování změn bodu
	 */
	@Override
	protected synchronized void reportAnyPointChange()
	{
		int selectedSize=selectedPoints.size();
		//mám vybráno mnoho bodů nebo žádný
		if(selectedSize!=1)
		{
			pointLabel.setText("Vyberte jeden bod");
			return;
		}
		//mam vtbran 1 bod
		//index bodu
		int index=points.indexOf(selectedPoints.get(0));
		pointLabel.setText("Pořadí bodu je: "+index);
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		if(points.size()>1)//musí být zadány alespoň 2 body
		{
			gl.glLineWidth(lineWidth);
			gl.glBegin(gl.GL_LINE_STRIP);
			GLUtils.glSetColor(gl, color);
			for(IPoint3f p:points)
			{
				gl.glVertex3f(p.getX(), p.getY(),p.getZ());
			}
			gl.glEnd();
		}
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleNodes(gl);
	}
}
