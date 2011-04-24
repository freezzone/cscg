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
 * Vlastní editor/zobrazovaè køivek a ploch v OpenGL2.
 * Tøída zpracovává eventy zmìn v projektu a reaguje na nì.
 * @author Tomáš Režnar
 */
public class Editor extends GLJPanel
{

	static
	{
		GLProfile.initSingleton(true);
	}

	/**
	 * Vytvoøení základních OpenGL vlastností panelu.
	 */
	private static GLCapabilities createGLCapatibilities()
	{
		GLCapabilities cap = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		cap.setHardwareAccelerated(true);
		//nefunkèní
		/*cap.setNumSamples(2);
		cap.setSampleBuffers(true);*/
		return cap;
	}
	/**
	 * Posluchaè editoru vykonávající všechnu práci pro vykreslení.
	 */
	private final EditorLogic listener;
	/**
	 * Posluchaèi editoru.
	 */
	private final LinkedList<EditorListener> listeners = new LinkedList<EditorListener>();
	/**
	 * Editovaný projekt.
	 */
	private final Project project;

	/**
	 * @throws GLException Nepodaøilo se inicializovat.
	 * @throws UnsatisfiedLinkError Pokud nejsou dostupné binární knihovny OpenGL.
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
	 * Zrušení objektu.
	 */
	public void dispose()
	{
		listener.dispose();
	}

	/**
	 * Získání projektu ke ketrému se váže editor.
	 */
	public Project getProject()
	{
		return project;
	}

	/**
	 * Vygenerování obrázku aktuální scény. Vygenerování mùže chvíly trvat, protože vlákno bude pozastaveno do doby
	 * dokud vlákno starající se o vykreslení obrázek nevygeneruje.
	 */
	public BufferedImage getImage()
	{
		return listener.getSceneImage();
	}

	/**
	 * Pøidání posluchaèe editoru.
	 */
	public void addListener(EditorListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchaèe editoru.
	 */
	public void removeListener(EditorListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Vyvolání události pøidání bodu.
	 */
	protected void fireAddPoint(IObject object, IPoint3f newPoint)
	{
		for (EditorListener l : listeners)
		{
			l.evenAddPoint(this, object, newPoint);
		}
	}

	/**
	 * Vyvolání události relativního posunu bodu.
	 * @param object Mìnìný objekt.
	 * @param p Mìnìný bod.
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
	 * Vyvolání události relativního posunu bodù.
	 * @param object Mìnìný objekt.
	 * @param points Mìnìné body.
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
	 * Vyvolání události smazání aktuálního výbìru bodù.
	 */
	void fireDeleteSelectedPoints(IPointsAddable object)
	{
		for (EditorListener l : listeners)
		{
			l.eventDeleteSelectedPoints(this, object);
		}
	}

	/**
	 * Vyvolání události zrušení výbìru bodù.
	 * @param object Objekt jehož výbìr se ruší.
	 */
	void fireCancelSelectedPoints(IObject object)
	{
		for (EditorListener l : listeners)
		{
			l.eventCancelSelectedPoints(this, object);
		}
	}

	/**
	 * Vyvolání události pøidání bodu do výbìru.
	 * @param object Objekt do jehož výbìru bod pøibude.
	 * @param point Novì vybraný bod.
	 */
	void fireAddSelectedPoint(IObject object, IPoint3f point)
	{
		for (EditorListener l : listeners)
		{
			l.eventAddSelectedPoint(this, object, point);
		}
	}

	/**
	 * Vyvolání události nastavení bodu do výbìru.
	 * @param object Objekt do jehož výbìru bod nastaven.
	 * @param point Novì vybraný bod.
	 */
	void fireSetSelectedPoint(IObject object, IPoint3f point)
	{
		for (EditorListener l : listeners)
		{
			l.eventSetSelectedPoint(this, object, point);
		}
	}

	/**
	 * Posluchaè stiskù talèítek na myši za úèelem získání focus aby bylo možné pøijímat eventy z klávesnice.
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
