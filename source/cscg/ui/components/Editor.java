package cscg.ui.components;

import cscg.model.Model;
import cscg.model.Project;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPointsAddable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.event.MouseInputListener;

/**
 * Vlastn� editor/zobrazova� k�ivek a ploch v OpenGL2.
 * T��da zpracov�v� eventy zm�n v projektu a reaguje na n�.
 * @author Tom� Re�nar
 */
public class Editor extends GLJPanel
{

	static
	{
		GLProfile.initSingleton(true);
	}

	/**
	 * Vytvo�en� z�kladn�ch OpenGL vlastnost� panelu.
	 */
	private static GLCapabilities createGLCapatibilities()
	{
		GLCapabilities cap = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		cap.setHardwareAccelerated(true);
		//nefunk�n�
		/*cap.setNumSamples(2);
		cap.setSampleBuffers(true);*/
		return cap;
	}
	/**
	 * Poslucha� editoru vykon�vaj�c� v�echnu pr�ci pro vykreslen�.
	 */
	private final EditorLogic listener;
	/**
	 * Poslucha�i editoru.
	 */
	private final LinkedList<EditorListener> listeners = new LinkedList<EditorListener>();
	/**
	 * Editovan� projekt.
	 */
	private final Project project;

	/**
	 * @throws GLException Nepoda�ilo se inicializovat.
	 * @throws UnsatisfiedLinkError Pokud nejsou dostupn� bin�rn� knihovny OpenGL.
	 */
	public Editor(Project project, Model model) throws GLException, UnsatisfiedLinkError
	{
		super(createGLCapatibilities());
		this.project = project;
		FocusSetter fl = new FocusSetter();
		addMouseListener(fl);
		addMouseMotionListener(fl);
		addMouseWheelListener(fl);
		listener = new EditorLogic(project, model, this);
		addGLEventListener(listener);
		addFocusListener(listener);
	}

	/**
	 * Zru�en� objektu.
	 */
	public void dispose()
	{
		listener.dispose();
	}

	/**
	 * Z�sk�n� projektu ke ketr�mu se v�e editor.
	 */
	public Project getProject()
	{
		return project;
	}

	/**
	 * Vygenerov�n� obr�zku aktu�ln� sc�ny. Vygenerov�n� m��e chv�ly trvat, proto�e vl�kno bude pozastaveno do doby
	 * dokud vl�kno staraj�c� se o vykreslen� obr�zek nevygeneruje.
	 */
	public BufferedImage getImage()
	{
		return listener.getSceneImage();
	}

	/**
	 * P�id�n� poslucha�e editoru.
	 */
	public void addListener(EditorListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e editoru.
	 */
	public void removeListener(EditorListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Vyvol�n� ud�losti p�id�n� bodu.
	 */
	protected void fireAddPoint(IObject object, IPoint3f newPoint)
	{
		for (EditorListener l : listeners)
		{
			l.evenAddPoint(this, object, newPoint);
		}
	}

	/**
	 * Vyvol�n� ud�losti relativn�ho posunu bodu.
	 * @param object M�n�n� objekt.
	 * @param p M�n�n� bod.
	 * @param x Posun x.
	 * @param y Posun y.
	 * @param z Posun z.
	 */
	protected void fireMovePointRelative(IObject object, IPoint3f p, float x, float y, float z)
	{
		for (EditorListener l : listeners)
		{
			l.eventMovePointRelative(this, object, p, x, y, z);
		}
	}

	/**
	 * Vyvol�n� ud�losti relativn�ho posunu bod�.
	 * @param object M�n�n� objekt.
	 * @param points M�n�n� body.
	 * @param x Posun x.
	 * @param y Posun y.
	 * @param z Posun z.
	 */
	protected void fireMovePointsRelative(IObject object, List<IPoint3f> points, float x, float y, float z)
	{
		for (EditorListener l : listeners)
		{
			l.eventMovePointsRelative(this, object, points, x, y, z);
		}
	}

	/**
	 * Vyvol�n� ud�losti smaz�n� aktu�ln�ho v�b�ru bod�.
	 */
	void fireDeleteSelectedPoints(IPointsAddable object)
	{
		for (EditorListener l : listeners)
		{
			l.eventDeleteSelectedPoints(this, object);
		}
	}

	/**
	 * Vyvol�n� ud�losti zru�en� v�b�ru bod�.
	 * @param object Objekt jeho� v�b�r se ru��.
	 */
	void fireCancelSelectedPoints(IObject object)
	{
		for (EditorListener l : listeners)
		{
			l.eventCancelSelectedPoints(this, object);
		}
	}

	/**
	 * Vyvol�n� ud�losti p�id�n� bodu do v�b�ru.
	 * @param object Objekt do jeho� v�b�ru bod p�ibude.
	 * @param point Nov� vybran� bod.
	 */
	void fireAddSelectedPoint(IObject object, IPoint3f point)
	{
		for (EditorListener l : listeners)
		{
			l.eventAddSelectedPoint(this, object, point);
		}
	}

	/**
	 * Vyvol�n� ud�losti nastaven� bodu do v�b�ru.
	 * @param object Objekt do jeho� v�b�ru bod nastaven.
	 * @param point Nov� vybran� bod.
	 */
	void fireSetSelectedPoint(IObject object, IPoint3f point)
	{
		for (EditorListener l : listeners)
		{
			l.eventSetSelectedPoint(this, object, point);
		}
	}

	/**
	 * Poslucha� stisk� tal��tek na my�i za ��elem z�sk�n� focus aby bylo mo�n� p�ij�mat eventy z kl�vesnice.
	 */
	private class FocusSetter implements MouseInputListener, MouseWheelListener
	{

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			Editor.this.requestFocusInWindow();
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			Editor.this.requestFocusInWindow();
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			Editor.this.requestFocusInWindow();
		}
	}
}
