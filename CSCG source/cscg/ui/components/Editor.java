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
 * Vlastní editor/zobrazovač křivek a ploch v OpenGL2.
 * Třída zpracovává eventy změn v projektu a reaguje na ně.
 * @author Tomáš Režnar
 */
public class Editor extends GLJPanel
{

	static
	{
		GLProfile.initSingleton(true);
	}

	/**
	 * Vytvoření základních OpenGL vlastností panelu.
	 */
	private static GLCapabilities createGLCapatibilities()
	{
		GLCapabilities cap = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		cap.setHardwareAccelerated(true);
		//nefunkční
		/*cap.setNumSamples(2);
		cap.setSampleBuffers(true);*/
		return cap;
	}
	/**
	 * Posluchač editoru vykonávající všechnu práci pro vykreslení.
	 */
	private final EditorLogic listener;
	/**
	 * Posluchači editoru.
	 */
	private final LinkedList<EditorListener> listeners = new LinkedList<EditorListener>();
	/**
	 * Editovaný projekt.
	 */
	private final Project project;

	/**
	 * @throws GLException Nepodařilo se inicializovat.
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
	 * Vygenerování obrázku aktuální scény. Vygenerování může chvíly trvat, protože vlákno bude pozastaveno do doby
	 * dokud vlákno starající se o vykreslení obrázek nevygeneruje.
	 */
	public BufferedImage getImage()
	{
		return listener.getSceneImage();
	}

	/**
	 * Přidání posluchače editoru.
	 */
	public void addListener(EditorListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchače editoru.
	 */
	public void removeListener(EditorListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Vyvolání události přidání bodu.
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
	 * @param object Měněný objekt.
	 * @param p Měněný bod.
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
	 * Vyvolání události relativního posunu bodů.
	 * @param object Měněný objekt.
	 * @param points Měněné body.
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
	 * Vyvolání události smazání aktuálního výběru bodů.
	 */
	void fireDeleteSelectedPoints(IPointsAddable object)
	{
		for (EditorListener l : listeners)
		{
			l.eventDeleteSelectedPoints(this, object);
		}
	}

	/**
	 * Vyvolání události zrušení výběru bodů.
	 * @param object Objekt jehož výběr se ruší.
	 */
	void fireCancelSelectedPoints(IObject object)
	{
		for (EditorListener l : listeners)
		{
			l.eventCancelSelectedPoints(this, object);
		}
	}

	/**
	 * Vyvolání události přidání bodu do výběru.
	 * @param object Objekt do jehož výběru bod přibude.
	 * @param point Nově vybraný bod.
	 */
	void fireAddSelectedPoint(IObject object, IPoint3f point)
	{
		for (EditorListener l : listeners)
		{
			l.eventAddSelectedPoint(this, object, point);
		}
	}

	/**
	 * Vyvolání události nastavení bodu do výběru.
	 * @param object Objekt do jehož výběru bod nastaven.
	 * @param point Nově vybraný bod.
	 */
	void fireSetSelectedPoint(IObject object, IPoint3f point)
	{
		for (EditorListener l : listeners)
		{
			l.eventSetSelectedPoint(this, object, point);
		}
	}

	/**
	 * Posluchač stisků talčítek na myši za účelem získání focus aby bylo možné přijímat eventy z klávesnice.
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
