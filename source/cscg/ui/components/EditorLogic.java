package cscg.ui.components;

import com.jogamp.opengl.util.gl2.GLUT;
import cscg.model.*;
import cscg.model.objects.*;
import cscg.ui.GLUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputListener;

/**
 * Logika panelu editoru. T��da se star� o vykreslen� a obsluhu vstupn�ch ud�lost� (my�, kl�vesnice).
 * @author Tom� Re�nar
 */
public class EditorLogic implements GLEventListener, FocusListener
{

	/**
	 * Maxim�ln� tlou��ka ��ry podporov�na grafickou kartou.
	 * Hodnota bude inicializov�n� na spr�vnou hodnotu v dob� vytvo�en� prvn� instance objektu a po prvn�m cyklu
	 * vykreslen�.
	 */
	private static int lineMaxWidth = 20;
	/**
	 * Maxim�ln� ��d podporovan� v evul�torech.
	 * Hodnota bude inicializov�n� na spr�vnou hodnotu v dob� vytvo�en� prvn� instance objektu a po prvn�m cyklu
	 * vykreslen�.
	 */
	private static int maxEvalOrder = 8;

	/**
	 * Maxim�ln� tlou��ka ��ry podporov�na grafickou kartou.
	 */
	public synchronized static int getLineMaxWidth()
	{
		return lineMaxWidth;
	}

	/**
	 * Nastaven� maxim�ln� tlou��ky ��ry podporovan� graf. kartou.
	 */
	private synchronized static void setLineMaxWidth(int lineMaxWidth)
	{
		EditorLogic.lineMaxWidth = lineMaxWidth;
	}

	/**
	 * Maxim�ln� ��d podporovan� v evul�torech.
	 */
	public synchronized static int getMaxEvalOrder()
	{
		return maxEvalOrder;
	}

	/**
	 * Nastaven� maxim�ln�ho ��du podporovan�ho v evul�torech.
	 * @param maxEvalOrder
	 */
	private synchronized static void setMaxEvalOrder(int maxEvalOrder)
	{
		EditorLogic.maxEvalOrder = maxEvalOrder;
	}
	/**
	 * Projekt kter� je v komponent� zobrazov�n.
	 */
	private final Project project;
	/**
	 * Cel� model.
	 */
	private final Model model;
	/**
	 * Instance OpenGL.
	 */
	private GL2 gl;
	/**
	 * OpenGL Utility Library.
	 */
	private GLU glu;
	private GLUT glut;
	/**
	 * ���ka sc�ny.
	 */
	private int widthGL;
	/**
	 * V��ka sc�ny.
	 */
	private int heightGL;
	/**
	 * P��znak �e do�lo ke zm�n� nastaven� zobrazen� a m� b�t znovu provedena inicializace OpenGL.
	 */
	private volatile boolean invalidateDisplayOptions = false;
	/**
	 * Hranice pohled� na obrazovce.
	 */
	private volatile ViewportBounds viewport1ScreenBounds;
	/**
	 * Hranice pohled� na obrazovce.
	 */
	private volatile ViewportBounds viewport2ScreenBounds;
	/**
	 * Hranice pohled� na obrazovce.
	 */
	private volatile ViewportBounds viewport3ScreenBounds;
	/**
	 * Hranice pohled� na obrazovce.
	 */
	private volatile ViewportBounds viewport4ScreenBounds;
	/**
	 * Hranice pohled� na obrazovce.
	 */
	private volatile ViewportBounds viewportSingleScreenBounds;
	/**
	 * Vieport kter� m� aktu�ln� focus => ud�losti na vstupu (my�, kl�vesnice) budou prov�d�ny v dan�m viewportu.
	 */
	private volatile ViewportBounds viewportFocus;
	/**
	 * Sledovan� editor.
	 */
	private final Editor editor;
	/**
	 * Po prvn�m zavol�n� reshape(...) bude nastaven na true.
	 */
	private boolean viewportsInited = false;
	/**
	 * Poslucha� zm�n v projektu.
	 */
	private final ProjectListener projectListener;
	/**
	 * Poslucha� zm�n v objektech.
	 */
	private final ObjectListener objectListener;
	/**
	 * Skladov�n� vytvo�en�ch display list� k objekt�m.
	 */
	private final HashMap<IObject, Integer> objectsDisplayLists = new HashMap<IObject, Integer>(16);
	/**
	 * Display list edita�n�ch uzl� aktu�ln� editovan�ho objektu pro viewport 1.
	 */
	private volatile Integer editNodesViewport1DisplayList;
	/**
	 * Display list edita�n�ch uzl� aktu�ln� editovan�ho objektu pro viewport 2.
	 */
	private volatile Integer editNodesViewport2DisplayList;
	/**
	 * Display list edita�n�ch uzl� aktu�ln� editovan�ho objektu pro viewport 3.
	 */
	private volatile Integer editNodesViewport3DisplayList;
	/**
	 * Display list edita�n�ch uzl� aktu�ln� editovan�ho objektu pro viewport 4.
	 */
	private volatile Integer editNodesViewport4DisplayList;
	/**
	 * Slou�� pro zneplatn�n� v�ech display list� objekt�.
	 */
	private volatile boolean objectInvalidDisplayListAll = false;
	/**
	 * Poloha kurzoru my�i v openGl canvasu, [0;0] je vlevo dole.
	 */
	private int[] cursorPositionOnCanvas = new int[2];
	/**
	 * List obsahuj�c� seznam objekt� pro kter� ji� neplat� display list ulo�en� v objectsDisplayLists.
	 * Objekt je t�eba p�i pr�ci zamknout, proto�e je vol�n z vl�kna vykreslen� OpenGL a z�rove� z vl�kna
	 * obsluhuj�c�ho ud�lost� zm�n modelu.
	 */
	private final ArrayList<IObject> objectInvalidDisplayLists = new ArrayList<IObject>(8);
	/**
	 * Slou�� pro zneplatn�n� display list� ukl�daj�c�ch edita�n� uzly vybran�ho objektu.
	 */
	private volatile boolean editNodesInvalidDisplayList = true;
	/**
	 * P�epo�ten� sou�adnice pozice kurzoru na pozici v dokumentu pro viewport 1.
	 */
	private IPoint3f cursorPositionViewport1AndSingle = new Point3f();
	/**
	 * P�epo�ten� sou�adnice pozice kurzoru na pozici v dokumentu pro viewport 2.
	 */
	private IPoint3f cursorPositionViewport2 = new Point3f();
	/**
	 * P�epo�ten� sou�adnice pozice kurzoru na pozici v dokumentu pro viewport 3.
	 */
	private IPoint3f cursorPositionViewport3 = new Point3f();
	/**
	 * P�epo�ten� sou�adnice pozice kurzoru na pozici v dokumentu pro viewport 4
	 */
	private IPoint3f cursorPositionViewport4 = new Point3f();
	/**
	 * Fronta ud�lost� vyvolan�ch my�� ve viewportu 1. Fronta m� vnit�n� synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport1MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Fronta ud�lost� vyvolan�ch my�� ve viewportu 2. Fronta m� vnit�n� synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport2MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Fronta ud�lost� vyvolan�ch my�� ve viewportu 3. Fronta m� vnit�n� synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport3MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Fronta ud�lost� vyvolan�ch my�� ve viewportu 4. Fronta m� vnit�n� synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport4MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Posledn� sou�adnice z my�i p�i posunu pohybu my�i.
	 */
	private volatile Float vieportLastZForMove = null;
	/**
	 * Vl�kno �ekaj�c� na vygenerov�n� obr�zku sc�ny.
	 */
	private volatile Thread threadWaitingForImage = null;
	/**
	 * Vygenerovan� obr�zek sc�ny.
	 */
	private volatile BufferedImage sceneImage = null;
	/**
	 * Ikona kamery.
	 */
	private IntBuffer cameraIcon;
	/**
	 * Rozm�ry ikony kamery.
	 */
	private int[] cameraIconSize = new int[2];
	/**
	 * Popup menu pro nastaven� kamery.
	 */
	private final CameraMenu cameraMenu;

	public EditorLogic(Project project, Model model, Editor canvas)
	{
		this.project = project;
		this.editor = canvas;
		this.model = model;
		cameraMenu = new CameraMenu();
		projectListener = new ProjectListener();
		project.addProjectListener(projectListener);
		//poslucha�i vstupu
		InputListener listener = new InputListener();
		canvas.addMouseListener(listener);
		canvas.addMouseMotionListener(listener);
		canvas.addMouseWheelListener(listener);
		canvas.addKeyListener(listener);

		//p�id�m poslucha�e objekt� ke v�em vytvo�en�m objekt�m
		objectListener = new ObjectListener();
		for (IObject o : project.getObjects())
		{
			o.addObjectListener(objectListener);
		}

		//p�id�n� poslucha�e zm�n nastaven� zobrazen�
		model.getDisplayOptions().addListener(new ChangeListener()
		{

			@Override
			public void changeEvent(EventObject e)
			{
				synchronized (EditorLogic.this)
				{
					invalidateDisplayOptions = true;
				}
			}
		});
	}

	/**
	 * Inicializace OpenGL.
	 */
	@Override
	public synchronized void init(GLAutoDrawable drawable)
	{
		//nastaven� debugingu OpenGL
		//povoleno v�dy-je kv�li pot�eb� zachyt�vat vyj�mky v n�jter�ch objektech
		if (true || Model.isDebug())
		{
			drawable.setGL(new DebugGL2((GL2) drawable.getGL()));
		}
		drawable.setAutoSwapBufferMode(true);

		gl = (GL2) drawable.getGL();
		glu = new GLU();
		glut = new GLUT();

		//nastaven� maxim�ln� tlou��ky ��ry
		float[] range = new float[2];
		gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, range, 0);
		setLineMaxWidth((int) range[1]);
		gl.glGetFloatv(GL2.GL_MAX_EVAL_ORDER, range, 0);
		setMaxEvalOrder((int) range[1]);

		float[] backColor = project.getBackgroundColor().getColorComponents(null);
		gl.glClearColor(backColor[0], backColor[1], backColor[2], 0.0f);//prostor je b�l�

		gl.glShadeModel(GL2.GL_SMOOTH);//Gouraudovo st�nov�n� - p�echodem

		initDisplay(gl);

		//na�ten� ikony kamery
		BufferedImage image;
		try
		{
			image = ImageIO.read(getClass().getResource("camera.png"));
		} catch (IOException ex)
		{
			Logger.getLogger(EditorLogic.class.getName()).log(Level.WARNING, null, ex);
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
		int[] bufferRGBA = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		cameraIcon = IntBuffer.allocate(bufferRGBA.length);
		cameraIconSize[0] = image.getWidth();
		cameraIconSize[1] = image.getHeight();
		//p�ekop�rov�n� dat z obr�zku do bufferu
		for (int row = cameraIconSize[1] - 1; row >= 0; row--)//��dky projdu pozp�tku
		{
			for (int col = 0; col < cameraIconSize[0]; col++)//projdu sloupce
			{
				cameraIcon.put(bufferRGBA[(row * cameraIconSize[0]) + col]);
			}
		}
		cameraIcon.rewind();
	}

	/**
	 * Inicializace z�kladn�ch nastaven� zobrazen�.
	 */
	private void initDisplay(GL2 gl)
	{
		DisplayOptions options = model.getDisplayOptions();

		gl.setSwapInterval(options.isVsync() ? 1 : 0);//povolen� vertik�ln� synchronizace

		/*
		 * Antialising
		 */
		if (options.getPointSmooth() != 0)//bod�
		{
			gl.glEnable(GL2.GL_POINT_SMOOTH);//zapnut�
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, options.getPointSmooth());//nastaven� kvality
		} else
		{
			gl.glDisable(GL2.GL_POINT_SMOOTH);//vypnut�
		}

		if (options.getLineSmooth() != 0)//linek
		{
			gl.glEnable(GL2.GL_LINE_SMOOTH);//zapnut�
			gl.glHint(GL2.GL_LINE_SMOOTH_HINT, options.getLineSmooth());//nastaven� kvality
		} else
		{
			gl.glDisable(GL2.GL_LINE_SMOOTH);//vypnut�
		}

		if (options.getPolygonSmooth() != 0)//polygon�
		{
			gl.glEnable(GL2.GL_POLYGON_SMOOTH);//zapnut�
			gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, options.getPolygonSmooth());//nastaven� kvality
		} else
		{
			gl.glDisable(GL2.GL_POLYGON_SMOOTH);//vypnut�
		}

		if (options.getPerspectiveCorection() != 0)//perspektivy
		{
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, options.getPerspectiveCorection());//nastaven� kvality
		} else
		{
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_FASTEST);
		}

		//povolen� testov�n� hloubky
		if (options.isDepthTest())
		{
			gl.glEnable(gl.GL_DEPTH_TEST);
		} else
		{
			gl.glDisable(gl.GL_DEPTH_TEST);
		}

		//osv�tlen�
		gl.glMaterialfv(gl.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[]
		  {
			  1f, 1f, 1f, 1.0f
		  }, 0);// nastaveni odlesku material
		gl.glMaterialfv(gl.GL_FRONT_AND_BACK, GL2.GL_SHININESS, new float[]
		  {
			  options.getSpecularLightShininess()
		  }, 0);// nastaveni s�ly odlesk�
		gl.glColorMaterial(gl.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[]
		  {
			  options.getAmbientLight(), options.getAmbientLight(), options.getAmbientLight(), 1.0f
		  }, 0);
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, new float[]
		  {
			  options.getDifuseLight(), options.getDifuseLight(), options.getDifuseLight(), 1.0f
		  }, 0);
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, new float[]
		  {
			  options.getSpecularLight(), options.getSpecularLight(), options.getSpecularLight(), 1f
		  }, 0);
		gl.glEnable(gl.GL_COLOR_MATERIAL);//zapnut� v�po�et bavrvy materi�lu dle nastaven�ch barev vrchol�
		gl.glEnable(gl.GL_LIGHT0);//zapnut� sv�tla 0


		gl.glEnable(GL2.GL_BLEND);//povolen� blendingu - alfakan�lu
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);//typ blendingu
		gl.glDisable(gl.GL_CULL_FACE);//zak�zan� odstran�ov�n� odvr�cen�ch hran
		gl.glPolygonMode(gl.GL_FRONT, gl.GL_FILL);//vykreslovani prednich stran
		gl.glPolygonMode(gl.GL_BACK, gl.GL_FILL);//vykreslovani zadnich stran
		gl.glEnable(gl.GL_NORMALIZE);//automatick� normalizace norm�lov�ch vektor�
		gl.glEnable(gl.GL_AUTO_NORMAL);//automatick� vytv��en� norm�l

		//nefunk�n�
		/*if (gl.isExtensionAvailable("GL_ARB_multisample")) {
		System.out.println("MS");
		gl.glEnable(gl.GL_MULTISAMPLE);
		}*/
	}

	/**
	 * Event zm�ny zobrazen�.
	 */
	@Override
	public synchronized void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		gl.glViewport(0, 0, width, height);
		this.widthGL = width;
		this.heightGL = height;
		//vypo��t�n� hranic jednotliv�ch pohled� v r�mci obrazovky
		viewport1ScreenBounds = new ViewportBounds(1, width / 2, height / 2, height - 1);
		viewport2ScreenBounds = new ViewportBounds(width / 2 + 1, width - 1, height / 2, height - 1);
		viewport3ScreenBounds = new ViewportBounds(1, width / 2, 1, height / 2 - 1);
		viewport4ScreenBounds = new ViewportBounds(width / 2 + 1, width - 1, 1, height / 2 - 1);
		viewportSingleScreenBounds = new ViewportBounds(1, width - 1, 1, height - 1);
		viewportFocus = null;
		viewportsInited = true;
	}

	/**
	 * Vlatn� implementace vykreslen�
	 */
	@Override
	public synchronized void display(GLAutoDrawable drawable)
	{
		if (invalidateDisplayOptions)
		{
			invalidateDisplayOptions = false;
			initDisplay(gl);
		}

		if (!viewportsInited)
		{
			return;
		}
		// vypr�zdn�n� buffer�
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

		//odstran�n� display list�, pokud do�lo ke zm�n� zobrazn� v n�m
		if (objectInvalidDisplayListAll)
		{
			editNodesInvalidDisplayList = true;//nastavim p��znak, aby do�lo i kd odstran�n� display list� edita�n�ch uzl�
			objectInvalidDisplayListAll = false;
			for (Integer list : objectsDisplayLists.values())
			{
				if (list != null)
				{
					gl.glDeleteLists(list, 1);
				}
			}
			objectsDisplayLists.clear();
		} else
		{
			if (objectInvalidDisplayLists.isEmpty() == false)
			{
				//odstran�n� neplatn�ch display list� uveden�ch v seznamu neplatn�ch list� dle objektu
				synchronized (objectInvalidDisplayLists)
				{
					Integer old;
					for (IObject o : objectInvalidDisplayLists)
					{
						//odstran�n� ds objektu
						old = objectsDisplayLists.remove(o);
						if (old != null)
						{
							gl.glDeleteLists(old, 1);
						}
					}
					objectInvalidDisplayLists.clear();//vypr�zdn�n� seznamu neplatn�ch display list�
				}
			}
		}

		//odstran�n� display list� edita�n�ch uzl�
		if (editNodesInvalidDisplayList)
		{
			editNodesInvalidDisplayList = false;
			if (editNodesViewport1DisplayList != null)
			{
				gl.glDeleteLists(editNodesViewport1DisplayList, 1);
			}
			editNodesViewport1DisplayList = null;
			if (editNodesViewport2DisplayList != null)
			{
				gl.glDeleteLists(editNodesViewport2DisplayList, 1);
			}
			editNodesViewport2DisplayList = null;
			if (editNodesViewport3DisplayList != null)
			{
				gl.glDeleteLists(editNodesViewport3DisplayList, 1);
			}
			editNodesViewport3DisplayList = null;
			if (editNodesViewport4DisplayList != null)
			{
				gl.glDeleteLists(editNodesViewport4DisplayList, 1);
			}
			editNodesViewport4DisplayList = null;
		}

		//postupn� vykreslen� viewport�

		if (project.isDisplayAllViewports())
		{
			setViewport1();
			setViewport2();
			setViewport3();
			setViewport4();
		} else
		{
			setViewportSingle();
		}

		//r�me�ek viewport�
		drawViewportsBorder();

		//vygenerov�n� obr�zku sc�ny
		if (threadWaitingForImage != null)
		{
			IntBuffer buffer = IntBuffer.allocate(widthGL * heightGL);
			System.out.println(buffer.limit() + " " + (widthGL * heightGL));
			gl.glReadPixels(0, 0, widthGL, heightGL, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, buffer);
			int[] bufferRGBA;
			bufferRGBA = buffer.array();//na windows funguje, na linuxu je obr�zek p�evr�cen�

			//pro linux - v bufferu je obr�zek pozp�tku, mus� se oto�it
			try
			{
				if (System.getProperty("os.name").toLowerCase().indexOf("nux") >= 0)//jsem na Linuxu
				{
					//ulo�en� bufferu bufferRGBA opa�n�
					for (int i = (bufferRGBA.length / 2) - 1,
					  j = (bufferRGBA.length / 2) + (bufferRGBA.length % 2), tmp; i >= 0; i--, j++)//projdu od st�edu ke kraji
					{
						tmp = bufferRGBA[i];
						bufferRGBA[i] = bufferRGBA[j];
						bufferRGBA[j] = tmp;
					}
				}
			} catch (SecurityException ex)//nemus� b�t povolen p��stup k syst�mov� promn�n�
			{
			}
			//vytvo�en� v�sledn�ho obr�zku
			BufferedImage outputImage = new BufferedImage(widthGL, heightGL, BufferedImage.TYPE_INT_RGB);
			outputImage.setRGB(0, 0, widthGL, heightGL, bufferRGBA, 0, widthGL);
			sceneImage = outputImage;
			this.notifyAll();//probuzen� vl�kna �ekaj�c�ho na vygenerovan� obr�zek
			threadWaitingForImage = null;
		}

		//Posl�n� v�ech operac� grafick� kart�
		gl.glFlush();
	}

	/**
	 * Metoda nutn� kv�li rozhran� GLEventListener; Neiplementov�na
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	/**
	 * Z�sk�n� vygenerovan�ho obr�zku sc�ny.
	 */
	protected synchronized BufferedImage getSceneImage()
	{
		while (this.threadWaitingForImage != null)//po�k�m na uvoln�n� prost�edk�
		{
			try
			{
				this.wait(20);
			} catch (InterruptedException ex)
			{
			}
		}
		sceneImage = null;
		this.threadWaitingForImage = Thread.currentThread();
		while (sceneImage == null)
		{
			try
			{
				this.wait(20);
			} catch (InterruptedException ex)
			{
			}
		}
		return sceneImage;
	}

	/**
	 * Vygenerov�n� nov�ho ��sla display listu. Pokud se nepoda�� vygenerovat,
	 * zobraz� se u�ivateli varov�n� a aplikace ukon��.
	 */
	private Integer getEmptyDisplayList()
	{
		Integer displayList = gl.glGenLists(1); //vygenerov�n� id nov�ho listu
		if (displayList.intValue() == 0)
		{
			try
			{
				JOptionPane.showMessageDialog(null, "Do�la pam� na grafick� kart�. Program bude ukon�en.", "Chyba", JOptionPane.ERROR_MESSAGE);
			} catch (HeadlessException ex)
			{
			}
			System.exit(0);
		}
		return displayList;
	}

	/**
	 * Vytvo�en� samostatn�ho viewportu.
	 */
	private void setViewportSingle()
	{
		glViewport(viewportSingleScreenBounds);//cel� obrazovka
		//cel� obrazovka
		Projection projection = project.getViewport1Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslen� obsahu
		drawViewport(viewportSingleScreenBounds);

		//v�po�et pozice my�i
		cursorPositionViewport1AndSingle = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracov�n� ud�lost� my�i - z�m�rn� a� po vykreseln�, kv�li pr�ci s hloubkov�m bufferem
		mouseEventService(projection, viewportSingleScreenBounds, viewport1MouseEvents);
	}

	/**
	 * Vytvo�en� viewportu 1.
	 */
	private void setViewport1()
	{
		glViewport(viewport1ScreenBounds);
		Projection projection = project.getViewport1Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslen� obsahu
		drawViewport(viewport1ScreenBounds);

		//v�po�et pozice my�i
		cursorPositionViewport1AndSingle = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracov�n� ud�lost� my�i - z�m�rn� a� po vykreseln�, kv�li pr�ci s hloubkov�m bufferem
		mouseEventService(projection, viewport1ScreenBounds, viewport1MouseEvents);
	}

	/**
	 * Vytvo�en� viewportu 2.
	 */
	private void setViewport2()
	{
		glViewport(viewport2ScreenBounds);
		Projection projection = project.getViewport2Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslen� obsahu
		drawViewport(viewport2ScreenBounds);

		//v�po�et pozice my�i
		cursorPositionViewport2 = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracov�n� ud�lost� my�i - z�m�rn� a� po vykreseln�, kv�li pr�ci s hloubkov�m bufferem
		mouseEventService(projection, viewport2ScreenBounds, viewport2MouseEvents);
	}

	/**
	 * Vytvo�en� viewportu 3.
	 */
	private void setViewport3()
	{
		glViewport(viewport3ScreenBounds);
		Projection projection = project.getViewport3Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslen� obsahu
		drawViewport(viewport3ScreenBounds);

		//v�po�et pozice my�i
		cursorPositionViewport3 = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracov�n� ud�lost� my�i - z�m�rn� a� po vykreseln�, kv�li pr�ci s hloubkov�m bufferem
		mouseEventService(projection, viewport3ScreenBounds, viewport3MouseEvents);
	}

	/**
	 * Vytvo�en� viewportu 4.
	 */
	private void setViewport4()
	{
		glViewport(viewport4ScreenBounds);
		Projection projection = project.getViewport4Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslen� obsahu
		drawViewport(viewport4ScreenBounds);

		//v�po�et pozice my�i
		cursorPositionViewport4 = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracov�n� ud�lost� my�i - z�m�rn� a� po vykreseln�, kv�li pr�ci s hloubkov�m bufferem
		mouseEventService(projection, viewport4ScreenBounds, viewport4MouseEvents);
	}

	/**
	 * Obsluha ud�lost� my�� v dan�m viewportu.
	 * @param projection projekce viewportu.
	 * @param events Fronta ud�lost�.
	 */
	private void mouseEventService(Projection projection, ViewportBounds screenBouns, Queue<ViewportMouseEvent> events)
	{
		if (events.isEmpty())
		{
			return;
		}
		ViewportMouseEvent e;
		MouseScreenPosition from, to;
		IPoint3f coordsFrom, coordsTo;
		while ((e = events.poll()) != null)
		{
			to = e.getCursorPosition();
			//ud�lost to�en� kole�ka
			if (e instanceof ViewportMouseWheelEvent)
			{
				//kole�ko je v ortho zobrazen� zoom a v perspektivn�m pohyb
				MouseWheelEvent we = ((ViewportMouseWheelEvent) e).getWheelEvt();
				if (projection.isPerspective())
				{
					projection.move(we.getWheelRotation() * -20, 0, 0);
				} else
				{
					//zoom se zachov�n� polohy bodu pod kurzorem
					float oldZoom = projection.getZoom();
					float newZoom = countNewZoom(-we.getWheelRotation(), oldZoom);
					float zoomRatio = oldZoom / newZoom;
					coordsTo = GLUtils.gluUnProject(gl, glu, //bod na pozici kurzoru, kter� chci fixovat
					  to.getX(), to.getY(), 0f);
					IPoint3f center = GLUtils.gluUnProject(gl, glu,
					  screenBouns.getCenterX(), screenBouns.getCenterY(), 0f);
					float direction[] = PointOperations.directionVector(center, coordsTo);
					projection.move(direction);
					projection.setZoom(newZoom);
					direction = PointOperations.scaleVector3(direction, -zoomRatio);
					projection.move(direction);
				}
				continue;
			}
			//ud�lost stisku tla��tka my�i
			if (e instanceof ViewportMousePressEvent)
			{
				MouseScreenPosition mouse = e.getCursorPosition();
				IObject selObject = project.getSelectedObject();
				//zm�na v�b�ru bod� objektu
				if (e.isButton1() && selObject != null)
				{
					int radius = 8;//vzdalenost hledani bodu
					IPoint3f pointClicked = null;//hledan� body na kter� bylo kliknuto
					IPoint3f tmp;
					for (IPoint3f p : selObject.getPoints())
					{
						//p�evedu bod na sou�adnici na obrazovce
						tmp = GLUtils.gluProject(gl, glu, p);
						if (Math.abs(tmp.getX() - mouse.getX()) <= radius
						  && Math.abs(tmp.getY() - mouse.getY()) <= radius)
						{
							pointClicked = p;
							break;
						}
					}
					//provedu akce s nalezen�m bodem
					if ((pointClicked == null || e.getEvt().isShiftDown() == true)
					  && e.getEvt().isControlDown() == false)//p�id�n� bodu
					{
						if (selObject instanceof IPointsAddable)
						{
							IPoint3f lastPoint = selObject.getLastSelectedPoint();
							IPoint3f newPoint;
							if (lastPoint != null)//nov� bod bude ve stejn� hladin� jako posledn� vybran�
							{
								float z = GLUtils.gluProject(gl, glu, lastPoint).getZ();
								newPoint = GLUtils.gluUnProject(gl, glu, mouse.getX(), mouse.getY(), z);
							} else//mus�m ur�it nov� bod
							{
								IPoint3f optimal = projection.getCenterInOptimalDistance();
								float z = GLUtils.gluProject(gl, glu, optimal).getZ();
								newPoint = GLUtils.gluUnProject(gl, glu, mouse.getX(), mouse.getY(), z);
							}
							editor.fireAddPoint(selObject, newPoint);
						} else
						{
							editor.fireCancelSelectedPoints(selObject);
						}
					} else
					{
						if (pointClicked != null && e.getEvt().isShiftDown() == false)//v�b�r bodu
						{
							if (e.getEvt().isControlDown())
							{
								editor.fireAddSelectedPoint(selObject, pointClicked);
							} else
							{
								editor.fireSetSelectedPoint(selObject, pointClicked);
							}
						}
					}
				}
				continue;
			}
			//ud�lost pu�t�n� tla��tka my�i
			if (e instanceof ViewportMouseRelasedEvent)
			{
				if (e.isButton3())
				{
					vieportLastZForMove = null;
				}
				continue;
			}
			//ud�losti posunu my�i
			if (e instanceof ViewportMouseDragEvent)
			{
				ViewportMouseDragEvent ev = (ViewportMouseDragEvent) e;
				from = ev.getFromCursorPosition();//poloha kurzoru odkud byl ta�en
				IObject selObject = project.getSelectedObject();

				//posun bod� objektu
				if (ev.isButton1() && selObject != null && project.isEditorInEditingMode())
				{
					IPoint3f last = selObject.getLastSelectedPoint();//naposledy vybran� bod
					if (last != null)
					{
						float z = GLUtils.gluProject(gl, glu, last).getZ();
						coordsFrom = GLUtils.gluUnProject(gl, glu, from.getX(), from.getY(), z);
						coordsTo = GLUtils.gluUnProject(gl, glu, to.getX(), to.getY(), z);
						float direction[] = PointOperations.directionVector(coordsFrom, coordsTo);
						editor.fireMovePointsRelative(selObject, selObject.getSelectedPoints(),
						  direction[0], direction[1], direction[2]);
					}
				} else
				{
					//rotace kamery
					if (selObject != null
					  && ((ev.getEvt().isControlDown() && ev.isButton3())
					  || ev.isButton2()))
					{
						float deltaY = from.getY() - to.getY();
						float deltaX = to.getX() - from.getX();
						float verticalAngle = (45f / screenBouns.getHeight()) * -deltaY;
						float horizontalAngle = (45f / screenBouns.getHeight()) * -deltaX;
						projection.rotate(verticalAngle, horizontalAngle, 0, selObject.getAverageCenter());
					} else
					{
						//posun kamery
						if (ev.isButton3())
						{
							//zapamatov�n� si m�sta kliku my�i
							if (vieportLastZForMove == null)
							{
								coordsFrom = GLUtils.gluUnProjectWithRadius(gl, glu, from.getX(), from.getY(), 4);
								vieportLastZForMove = GLUtils.gluProject(gl, glu, coordsFrom).getZ();
								if (vieportLastZForMove >= 0.99999f)//kdy� se na pozici nenach�z� objekt
								{
									if (selObject != null)
									{
										vieportLastZForMove = GLUtils.gluProject(gl, glu,
										  selObject.getAverageCenter()).getZ();
									} else
									{
										vieportLastZForMove = 0.95f;
									}
									coordsFrom = GLUtils.gluUnProject(gl, glu,
									  from.getX(), from.getY(), vieportLastZForMove);
								}
							} else
							{
								coordsFrom = GLUtils.gluUnProject(gl, glu,
								  from.getX(), from.getY(), vieportLastZForMove);
							}
							coordsTo = GLUtils.gluUnProject(gl, glu,
							  to.getX(), to.getY(), vieportLastZForMove);
							projection.move(coordsTo, coordsFrom);
						}
					}
				}
				continue;
			}
		}
	}

	/**
	 * Vykreslen� v�ech objekt�.
	 */
	private void drawObjects()
	{
		//objekty
		Integer displayList;//identifikace ��sla display listu v OpenGL

		//ur��m kter� objekty vykresl�m
		IObject objects[];
		synchronized (project)
		{
			if (project.isExlusiveVisibility())
			{
				objects = new IObject[]
				  {
					  project.getSelectedObject()
				  };
			} else
			{
				objects = project.getObjectsReverse(true);
			}
		}

		//projdu v�echny objekty
		for (IObject o : objects)
		{
			try
			{
				gl.glDepthFunc(gl.GL_LEQUAL);//bli��� t�leso p�ep�e vzd�len�j��
				gl.glEnable(gl.GL_LIGHTING);//povolen� osv�tlen�
				gl.glLineWidth(1);
				gl.glPointSize(1);
				displayList = objectsDisplayLists.get(o);
				long time=System.currentTimeMillis();
				if (displayList == null || !gl.glIsList(displayList))//nem�li objekt vytvo�en display list
				{
					displayList = getEmptyDisplayList();
					gl.glNewList(displayList, gl.GL_COMPILE_AND_EXECUTE);//zah�jen� nahr�v�n� a z�rove� vykreslen� listu
					//zachycen� jak�koli chyby v implementaci objektu
					try
					{
						o.draw(gl);//vykreslen� objektu
					} catch (Exception ex)
					{
						model.debugPrintException(ex);
					} finally
					{
						gl.glEndList();//ukon�en� nahr�v�n� listu
						System.out.println(o.getName()+": GEN: "+(System.currentTimeMillis()-time)+" ms");
						objectsDisplayLists.put(o, displayList);
					}
				} else//objekt m� vytvo�en disply list
				{
					gl.glCallList(displayList);
				}
			} catch (GLException ex)
			{
				model.debugPrintException(ex);
			} finally
			{
				gl.glDepthFunc(gl.GL_LEQUAL);//bli��� t�leso p�ep�e vzd�len�j��
			}
		}
	}

	/**
	 * Vykreslen� edita�n�ch uzl� vybran�ho objektu do viewportu.
	 */
	private void drawEditingNodes(ViewportBounds viewport)
	{
		if (project.isEditorInEditingMode())
		{
			IObject o = project.getSelectedObject();
			if (o != null)//jeli vybr�n objekt
			{
				Integer displayList = getEditNodesDisplayListForViewport(viewport);
				Projection projection = getProjectionForViewport(viewport);
				gl.glDepthFunc(gl.GL_ALWAYS);//bli��� t�leso p�ep�e vzd�len�j��
				if (displayList == null || !gl.glIsList(displayList))//nem�li objekt vytvo�en display list edita�n�ch uzl�
				{
					displayList = getEmptyDisplayList();
					gl.glNewList(displayList, gl.GL_COMPILE_AND_EXECUTE);//zah�jen� nahr�v�n� a z�rove� vykreslen� listu
					o.drawNodes(gl, projection);//vykreslen� uzl�
					gl.glEndList();//ukon�en� nahr�v�n� listu
					setEditNodesDisplayListForViewport(viewport, displayList);
				} else//jeli vytvo�en display list edita�n�ch uzl�
				{
					gl.glCallList(displayList);
				}
			}
		}
	}

	/**
	 * Vykreslen� viewportu.
	 */
	private void drawViewport(ViewportBounds viewport)
	{
		Projection projection = getProjectionForViewport(viewport);
		if (projection.isPerspective())//v perspektivn�m je osov� k�� v prostoru
		{
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);// vypr�zdn�n� buffer�
			gl.glDepthFunc(gl.GL_LEQUAL);//bli��� t�leso p�ep�e vzd�len�j��
			//osy
			if (project.isShowAxes())
			{
				gl.glDisable(gl.GL_LIGHTING);
				gl.glPushAttrib(gl.GL_ALL_ATTRIB_BITS);
				gl.glDisable(GL2.GL_LINE_SMOOTH);//vypnu antiliasing �ar
				GLUtils.drawAxes(gl, 5000);
				gl.glPopAttrib();
			}
			//objekty
			drawObjects();//vykreslim objekty
		} else//v otho je osov� k�� na pozad�
		{
			gl.glDepthFunc(gl.GL_ALWAYS);//bli��� t�leso p�ep�e vzd�len�j��
			//osy
			if (project.isShowAxes())
			{
				gl.glDisable(gl.GL_LIGHTING);
				GLUtils.drawAxes(gl, 5000);
			}
			//objekty
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);// vypr�zdn�n� buffer�
			drawObjects();//vykreslim objekty
		}
		gl.glDisable(gl.GL_LIGHTING);
		drawEditingNodes(viewport);//vykreslen� edita�n�ch uzl� vybran�ho objektu
	}

	/**
	 * Nastaven� r�me�k� mezi vieporty, informa�n� popisky, tla��tka.
	 */
	private void drawViewportsBorder()
	{
		//vypnu sv�tlo
		gl.glDisable(gl.GL_LIGHTING);
		// vypr�zdn�n� buffer�
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		gl.glDepthFunc(gl.GL_ALWAYS);//r�me�ek je vid�t v�dy
		gl.glViewport(0, 0, widthGL, heightGL);//cel� okno
		gl.glMatrixMode(gl.GL_PROJECTION);//Projek�n� matice
		gl.glLoadIdentity();//Reset projek�n� matice
		gl.glOrtho(0, widthGL, 0, heightGL, 5000, -5000);//Pravo�hl� projekce

		//r�me�ky
		gl.glLineWidth(1f);//Tlou��ka �ar 1px
		ViewportBounds borders[];
		if (project.isDisplayAllViewports())
		{
			borders = new ViewportBounds[]
			  {
				  viewport1ScreenBounds,
				  viewport2ScreenBounds,
				  viewport3ScreenBounds,
				  viewport4ScreenBounds
			  };
		} else
		{
			borders = new ViewportBounds[]
			  {
				  viewportSingleScreenBounds
			  };
		}

		//vykresl�m r�me�ky
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLUtils.glSetColor(gl, Color.black);
		ViewportBounds vpFocus;
		for (ViewportBounds vp : borders)
		{
			gl.glBegin(gl.GL_LINE_LOOP);
			gl.glVertex2f(vp.getX1() - 0.5f, vp.getY1() - 0.5f);
			gl.glVertex2f(vp.getX2() + 0.5f, vp.getY1() - 0.5f);
			gl.glVertex2f(vp.getX2() + 0.5f, vp.getY2() + 0.5f);
			gl.glVertex2f(vp.getX1() - 0.5f, vp.getY2() + 0.5f);
			gl.glEnd();
		}
		//vieport s focusem
		if ((vpFocus = viewportFocus) != null)
		{
			GLUtils.glSetColor(gl, Color.ORANGE);
			gl.glBegin(gl.GL_LINE_LOOP);
			gl.glVertex2f(vpFocus.getX1() - 0.5f, vpFocus.getY1() - 0.5f);
			gl.glVertex2f(vpFocus.getX2() + 0.5f, vpFocus.getY1() - 0.5f);
			gl.glVertex2f(vpFocus.getX2() + 0.5f, vpFocus.getY2() + 0.5f);
			gl.glVertex2f(vpFocus.getX1() - 0.5f, vpFocus.getY2() + 0.5f);
			gl.glEnd();
		}

		//ikony os + zoom
		Projection projection;
		for (ViewportBounds vp : borders)
		{
			if (threadWaitingForImage == null)//pokud se negeneruje obraz pro export obr�zku
			{
				//tla��tko pro nastaven� kamery
				gl.glRasterPos2i(vp.getX2() - cameraIconSize[0], vp.getY2() - cameraIconSize[1]);
				gl.glDrawPixels(
				  cameraIconSize[0], cameraIconSize[1],
				  gl.GL_RGBA,
				  gl.GL_UNSIGNED_BYTE,
				  cameraIcon);
			}


			gl.glDisable(gl.GL_LIGHTING);
			projection = getProjectionForViewport(vp);
			gl.glPushMatrix();
			gl.glTranslatef(vp.getX1() + 10f, vp.getY1() + 6f, 0);
			if (project.isShowInformationText())
			{
				GLUtils.drawZoomInfo(gl, glut, projection.getZoom());
			}
			gl.glTranslatef(vp.getWidth() - 20f, 0f, 0f);
			if (project.isShowInformationText())
			{
				GLUtils.drawCursorInfo(gl, glut, ViewType.PERSPECTIVE, getCursorPositionForViewport(vp), false, false);
			}
			gl.glTranslatef(-vp.getWidth() + 20.5f, 5.5f, 0.5f);
			gl.glDepthFunc(gl.GL_LEQUAL);
			gl.glEnable(gl.GL_LIGHTING);
			gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, new float[]
			  {
				  -100, 100, -100, 1
			  }, 0);
			if (project.isShowOrientationIcon())
			{
				GLUtils.drawOrientationCameraIcon(gl, glu, projection);
			}
			gl.glPopMatrix();
		}
	}

	/**
	 * Zji�t�n� ve kter�m vieportu se nach�z� sou�adnice.
	 * @param x x sou�adnice, kde lev� doln� roh==0
	 * @param y y sou�adnice, kde lev� doln� roh==0
	 * @return Vr�t� vieport nebo null kdy� je bod mimo v�echny vieporty.
	 */
	protected ViewportBounds detectVieport(int x, int y)
	{
		if (project.isDisplayAllViewports())
		{
			if (viewport1ScreenBounds.contains(x, y))
			{
				return viewport1ScreenBounds;
			}
			if (viewport2ScreenBounds.contains(x, y))
			{
				return viewport2ScreenBounds;
			}
			if (viewport3ScreenBounds.contains(x, y))
			{
				return viewport3ScreenBounds;
			}
			if (viewport4ScreenBounds.contains(x, y))
			{
				return viewport4ScreenBounds;
			}
		} else
		{
			if (viewportSingleScreenBounds.contains(x, y))
			{
				return viewportSingleScreenBounds;
			}
		}
		return null;
	}

	/**
	 * Z�sk�n� aktu�ln� projekce viewportu.
	 */
	protected Projection getProjectionForViewport(ViewportBounds vp)
	{
		if (vp == viewport1ScreenBounds)
		{
			return project.getViewport1Projection();
		}
		if (vp == viewport2ScreenBounds)
		{
			return project.getViewport2Projection();
		}
		if (vp == viewport3ScreenBounds)
		{
			return project.getViewport3Projection();
		}
		if (vp == viewport4ScreenBounds)
		{
			return project.getViewport4Projection();
		}
		if (vp == viewportSingleScreenBounds)
		{
			return project.getViewport1Projection();
		}
		return null;
	}

	/**
	 * Vr�t� display list edita�n�ch uzl� vybran�ho viewportu.
	 * @return Vr�t� null pokud je zad�n neplatn� viewport.
	 */
	protected Integer getEditNodesDisplayListForViewport(ViewportBounds vp)
	{
		if (vp == viewport1ScreenBounds)
		{
			return editNodesViewport1DisplayList;
		}
		if (vp == viewport2ScreenBounds)
		{
			return editNodesViewport2DisplayList;
		}
		if (vp == viewport3ScreenBounds)
		{
			return editNodesViewport3DisplayList;
		}
		if (vp == viewport4ScreenBounds)
		{
			return editNodesViewport4DisplayList;
		}
		if (vp == viewportSingleScreenBounds)
		{
			return editNodesViewport1DisplayList;
		}
		return null;
	}

	/**
	 * Nastav� display list edita�n�ch uzl� vybran�ho viewportu.
	 * @throws IllegalArgumentException Pokud je neplatn� viewport.
	 */
	protected void setEditNodesDisplayListForViewport(ViewportBounds vp, Integer displayList)
	{
		if (vp == viewport1ScreenBounds)
		{
			editNodesViewport1DisplayList = displayList;
		} else
		{
			if (vp == viewport2ScreenBounds)
			{
				editNodesViewport2DisplayList = displayList;
			} else
			{
				if (vp == viewport3ScreenBounds)
				{
					editNodesViewport3DisplayList = displayList;
				} else
				{
					if (vp == viewport4ScreenBounds)
					{
						editNodesViewport4DisplayList = displayList;
					} else
					{
						if (vp == viewportSingleScreenBounds)
						{
							editNodesViewport1DisplayList = displayList;
						} else
						{
							throw new IllegalArgumentException("Neexistuj�c� viewport.");
						}
					}
				}
			}
		}
	}

	/**
	 * Z�sk�n� pozice kurzoru p�epo��tanou na sou�adnice v prostoru v dan�m viewportu.
	 */
	protected IPoint3f getCursorPositionForViewport(ViewportBounds vp)
	{
		if (vp == viewport1ScreenBounds)
		{
			return cursorPositionViewport1AndSingle;
		}
		if (vp == viewport2ScreenBounds)
		{
			return cursorPositionViewport2;
		}
		if (vp == viewport3ScreenBounds)
		{
			return cursorPositionViewport3;
		}
		if (vp == viewport4ScreenBounds)
		{
			return cursorPositionViewport4;
		}
		if (vp == viewportSingleScreenBounds)
		{
			return cursorPositionViewport1AndSingle;
		}
		return null;
	}

	/**
	 * Spo��t�n� nov� hodnoty zoomu na z�klad� star� a po�tu krok�.
	 * @param steps Kladn� ��slo znamen� zmen�en� zoomu.
	 * @param oldZoom P�edchoz� zoom.
	 */
	protected float countNewZoom(int steps, float oldZoom)
	{
		float newZoom = oldZoom * (1f - 0.2f * -steps);
		if (newZoom < oldZoom * 0.5)//omezen� maxim�ln� zm�ny kroku zoomu
		{
			newZoom = oldZoom * 0.5f;
		}
		if (newZoom > oldZoom * 1.5)//omezen� maxim�ln� zm�ny kroku zoomu
		{
			newZoom = oldZoom * 1.5f;
		}
		newZoom = newZoom > 50f ? 50f : newZoom;
		newZoom = newZoom < 0.05f ? 0.05f : newZoom;
		return newZoom;
	}

	/**
	 * Nastaven� pohledu OpenGL.
	 */
	protected void glViewport(ViewportBounds bounds)
	{
		gl.glViewport(bounds.getX1(), bounds.getY1(), bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * Souhrn� metoda volan� p�i jak�koli zm�n� objektu p�i kter� je nutn� zm�nit display listy.
	 */
	private void objectChange(IObject o)
	{
		synchronized (objectInvalidDisplayLists)
		{
			objectInvalidDisplayLists.add(o);
		}
		//pokud je objekt aktu�ln� i editovan�, zneplatn�m jeho display list s edita�n�my uzly
		if (o == project.getSelectedObject())
		{
			editNodesInvalidDisplayList = true;
		}
	}

	@Override
	public void focusGained(FocusEvent e)
	{
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		viewportFocus = null;
	}

	@Override
	public void dispose(GLAutoDrawable glad)
	{
	}

	public void dispose()
	{
		project.removeProjectListener(this.projectListener);
	}

	/**
	 * T��da pro zpracov�n� ud�lost� v projektu.
	 */
	public class ProjectListener extends ProjectAdapter
	{

		@Override
		public void eventVieport1Changed(Projection p)
		{
			//zneplatn�m disply listy s edita�n�my uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventVieport2Changed(Projection p)
		{
			//zneplatn�m disply listy s edita�n�my uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventVieport3Changed(Projection p)
		{
			//zneplatn�m disply listy s edita�n�my uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventVieport4Changed(Projection p)
		{
			//zneplatn�m disply listy s edita�n�my uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventSelectedObjectChanged(IObject selectedObject, int index)
		{
			//zneplatn�m disply listy s edita�n�my uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventObjectAdded(IObject newObject, int index)
		{
			newObject.addObjectListener(objectListener);
		}

		@Override
		public void eventObjectRemoved(IObject oldObject, int index)
		{
			oldObject.removeObjectListener(objectListener);
		}

		@Override
		public void eventDisplayAllViewportsChanged(boolean displayAllViewports)
		{
			synchronized (EditorLogic.this)
			{
				viewportFocus = null;
			}
		}
	}

	/**
	 * T��da zpracov�vaj�c� ud�losti ze vstupu od u�ivatelE.
	 */
	public class InputListener implements MouseInputListener, MouseWheelListener, KeyListener
	{

		/**
		 * pozice posledn�ho kliku v r�mci vieportu
		 */
		private int[] lastClickCords = new int[3];

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			//sou�adnice kliku my�i
			int mouseX = e.getX();
			int mouseY = e.getComponent().getHeight() - e.getY();
			ViewportBounds vp = getMouseViewport(e);//viewport ve kter�m do�lo k ud�losti
			if (vp == null)
			{
				return;
			}
			lastClickCords = new int[]
			  {
				  mouseX, mouseY
			  };
			viewportFocus = vp;

			//rozezn�n� jestli se my� nach�z� nad ikonou pro nastaven� kamery - menu kamery
			if (viewportFocus != null)
			{
				//nach�z� se nad ikonou
				if (viewportFocus.getX2() >= cursorPositionOnCanvas[0]
				  && (viewportFocus.getX2() - cameraIconSize[0]) <= cursorPositionOnCanvas[0]
				  && viewportFocus.getY2() >= cursorPositionOnCanvas[1]
				  && (viewportFocus.getY2() - cameraIconSize[1]) <= cursorPositionOnCanvas[1])
				{
					cameraMenu.setProjection(getProjectionForViewport(viewportFocus));
					cameraMenu.show(e.getComponent(), e.getX(), e.getY());
					return;//��dn� dal�� akce se neprovedou
				}
			}

			try
			{
				if (vp == viewport1ScreenBounds)
				{
					viewport1MouseEvents.offer(new ViewportMousePressEvent(e));
				} else
				{
					if (vp == viewport2ScreenBounds)
					{
						viewport2MouseEvents.offer(new ViewportMousePressEvent(e));
					} else
					{
						if (vp == viewport3ScreenBounds)
						{
							viewport3MouseEvents.offer(new ViewportMousePressEvent(e));
						} else
						{
							if (vp == viewport4ScreenBounds)
							{
								viewport4MouseEvents.offer(new ViewportMousePressEvent(e));
							} else
							{
								if (vp == viewportSingleScreenBounds)
								{
									viewport1MouseEvents.offer(new ViewportMousePressEvent(e));
								}
							}
						}
					}
				}
			} catch (ClassCastException ex)
			{
			} catch (NullPointerException ex)
			{
			} catch (IllegalArgumentException ex)
			{
			}

		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			viewport1MouseEvents.offer(new ViewportMouseRelasedEvent(e));
			viewport2MouseEvents.offer(new ViewportMouseRelasedEvent(e));
			viewport3MouseEvents.offer(new ViewportMouseRelasedEvent(e));
			viewport4MouseEvents.offer(new ViewportMouseRelasedEvent(e));
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
			//sou�adnice my�i
			int mouseX = e.getX();
			int mouseY = e.getComponent().getHeight() - e.getY();

			ViewportBounds vp = viewportFocus;
			if (vp != null)//ud�lost zpracov�na pouze pokud do�lo ke kliku v n�kter�m vieportu
			{
				//p�id�n� ud�losti do fronty
				try
				{
					if (vp == viewport1ScreenBounds)
					{
						viewport1MouseEvents.offer(
						  new ViewportMouseDragEvent(e, new MouseScreenPosition(lastClickCords[0], lastClickCords[1])));
					} else
					{
						if (vp == viewport2ScreenBounds)
						{
							viewport2MouseEvents.offer(
							  new ViewportMouseDragEvent(e, new MouseScreenPosition(lastClickCords[0], lastClickCords[1])));
						} else
						{
							if (vp == viewport3ScreenBounds)
							{
								viewport3MouseEvents.offer(
								  new ViewportMouseDragEvent(e, new MouseScreenPosition(lastClickCords[0], lastClickCords[1])));
							} else
							{
								if (vp == viewport4ScreenBounds)
								{
									viewport4MouseEvents.offer(
									  new ViewportMouseDragEvent(e, new MouseScreenPosition(lastClickCords[0], lastClickCords[1])));
								} else
								{
									if (vp == viewportSingleScreenBounds)
									{
										viewport1MouseEvents.offer(
										  new ViewportMouseDragEvent(e, new MouseScreenPosition(lastClickCords[0], lastClickCords[1])));
									}
								}
							}
						}
					}
				} catch (ClassCastException ex)
				{
				} catch (NullPointerException ex)
				{
				} catch (IllegalArgumentException ex)
				{
				}
			}


			lastClickCords = new int[]
			  {
				  mouseX, mouseY
			  };
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			cursorPositionOnCanvas[0] = e.getX();
			cursorPositionOnCanvas[1] = e.getComponent().getHeight() - e.getY();

			//rozezn�n� jestli se my� nach�z� nad ikonou pro nastaven� kamery
			if (viewportFocus != null)
			{
				//nach�z� se nad ikonou
				if (viewportFocus.getX2() >= cursorPositionOnCanvas[0]
				  && (viewportFocus.getX2() - cameraIconSize[0]) <= cursorPositionOnCanvas[0]
				  && viewportFocus.getY2() >= cursorPositionOnCanvas[1]
				  && (viewportFocus.getY2() - cameraIconSize[1]) <= cursorPositionOnCanvas[1])
				{
					editor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} //nenach�z� se nad ikonou
				else
				{
					editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			ViewportBounds vp = getMouseViewport(e);//viewport ve kter�m do�lo k ud�losti

			viewportFocus = vp;
			try
			{
				if (vp == viewport1ScreenBounds)
				{
					viewport1MouseEvents.offer(new ViewportMouseWheelEvent(e));
				} else
				{
					if (vp == viewport2ScreenBounds)
					{
						viewport2MouseEvents.offer(new ViewportMouseWheelEvent(e));
					} else
					{
						if (vp == viewport3ScreenBounds)
						{
							viewport3MouseEvents.offer(new ViewportMouseWheelEvent(e));
						} else
						{
							if (vp == viewport4ScreenBounds)
							{
								viewport4MouseEvents.offer(new ViewportMouseWheelEvent(e));
							} else
							{
								if (vp == viewportSingleScreenBounds)
								{
									viewport1MouseEvents.offer(new ViewportMouseWheelEvent(e));
								}
							}
						}
					}
				}
			} catch (ClassCastException ex)
			{
			} catch (NullPointerException ex)
			{
			} catch (IllegalArgumentException ex)
			{
			}
		}

		/**
		 * Z�sk�n� vieportu ve kter�m se nach�z� my�. Null pokud v ��dn�m.
		 * @param e Ud�lost my�i je� je kontrolov�na.
		 */
		protected ViewportBounds getMouseViewport(MouseEvent e)
		{
			return detectVieport(e.getX(), e.getComponent().getHeight() - e.getY());
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			IObject selObject = project.getSelectedObject();//vybran� objekt
			Projection projection = getProjectionForViewport(viewportFocus);
			if (projection == null)
			{
				return;
			}
			/*
			 * Ovl�d�n� kamery
			 */
			if (selObject != null)
			{
				switch (e.getKeyChar())
				{
					//rotace vrchu
					case '*':
						projection.rotatePivot(5, selObject.getAverageCenter());
						break;
					case '/':
						projection.rotatePivot(-5, selObject.getAverageCenter());
						break;
					//rotace kamery nahoru/dol�
					case '9':
						projection.rotateVertical(5, selObject.getAverageCenter());
						break;
					case '3':
						projection.rotateVertical(-5, selObject.getAverageCenter());
						break;
					//rotace kamery vpravo/vlevo
					case ',':
					case '.':
						projection.rotateHorizontal(-5, selObject.getAverageCenter());
						break;
					case '0':
						projection.rotateHorizontal(5, selObject.getAverageCenter());
						break;
				}
			}
			switch (e.getKeyChar())
			{
				//resetov�n� projekce
				case '5':
					projection.reset();
					break;
				//posun vp�ed/vzad
				case '7':
					projection.move(20f, 0, 0);
					break;
				case '1':
					projection.move(-20f, 0, 0);
					break;
				//posun nahoru/dol�
				case '8':
					projection.move(0, 0, 20f);
					break;
				case '2':
					projection.move(0, 0, -20f);
					break;
				//posun vpravo/vlevo
				case '6':
					projection.move(0, 20f, 0);
					break;
				case '4':
					projection.move(0, -20f, 0);
					break;
				//zoom
				case '+':
					projection.setZoom(countNewZoom(1, projection.getZoom()));
					break;
				case '-':
					projection.setZoom(countNewZoom(-1, projection.getZoom()));
					break;
			}
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			Projection projection = getProjectionForViewport(viewportFocus);
			IObject selObject = project.getSelectedObject();//vybran� objekt
			if (projection == null)
			{
				return;
			}
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_F3:
					projection.lookProfile();
					break;
				case KeyEvent.VK_F4:
					projection.lookTop();
					break;
				case KeyEvent.VK_F2:
					projection.lookFront();
					break;
				case KeyEvent.VK_F1:
					projection.setPerspective(!projection.isPerspective());
					break;
			}

			if (selObject != null)
			{
				IPoint3f dir;
				float dis;
				switch (e.getKeyCode())
				{
					//smaz�n� vybran�ch bod�
					case KeyEvent.VK_DELETE:
						if (project.isEditorInEditingMode())
						{
							if (selObject instanceof IPointsAddable)
							{
								editor.fireDeleteSelectedPoints((IPointsAddable) selObject);
							}
						}
						break;
					//posun vybran�ch bod�
					case KeyEvent.VK_LEFT:
						dir = projection.getRight();
						dis = -20f / projection.getZoom();
						editor.fireMovePointsRelative(selObject, selObject.getSelectedPoints(),
						  dir.getX() * dis, dir.getY() * dis, dir.getZ() * dis);
						break;
					case KeyEvent.VK_RIGHT:
						dir = projection.getRight();
						dis = 20f / projection.getZoom();
						editor.fireMovePointsRelative(selObject, selObject.getSelectedPoints(),
						  dir.getX() * dis, dir.getY() * dis, dir.getZ() * dis);
						break;
					case KeyEvent.VK_UP:
						dir = projection.getUp();
						dis = 20f / projection.getZoom();
						editor.fireMovePointsRelative(selObject, selObject.getSelectedPoints(),
						  dir.getX() * dis, dir.getY() * dis, dir.getZ() * dis);
						break;
					case KeyEvent.VK_DOWN:
						dir = projection.getUp();
						dis = -20f / projection.getZoom();
						editor.fireMovePointsRelative(selObject, selObject.getSelectedPoints(),
						  dir.getX() * dis, dir.getY() * dis, dir.getZ() * dis);
						break;
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}
	}

	/**
	 * Poslucha� zm�n objekt�, jedna instance poslucha�e pro v�echny objekty v projektu.
	 */
	public class ObjectListener implements cscg.model.objects.ObjectListener
	{

		@Override
		public void eventColorChanged(IObject o)
		{
			objectChange(o);
		}

		@Override
		public void eventPointAdded(IObject o, IPoint3f p, int index)
		{
			objectChange(o);
		}

		@Override
		public void eventLineWidthChanged(IObject o)
		{
			objectChange(o);
		}

		@Override
		public void eventPointChanged(IObject o, IPoint3f p, int index)
		{
			objectChange(o);
		}

		@Override
		public void eventPointRemoved(IObject o, IPoint3f p, int index)
		{
			objectChange(o);
		}

		@Override
		public void eventPointSelectionChanged(IObject o)
		{
			objectChange(o);
		}

		@Override
		public void eventSpecificPropertiesChanged(IObject o)
		{
			objectChange(o);
		}

		@Override
		public void eventNameChanged(IObject o)
		{
		}

		@Override
		public void eventStateMessageChanged(IObject o)
		{
		}

		@Override
		public void eventPointsChanged(IObject o)
		{
			objectChange(o);
		}

		@Override
		public void eventSizeChanged(IObject o)
		{
			objectChange(o);
		}
	}
}
