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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
 * Logika panelu editoru. Třída se stará o vykreslení a obsluhu vstupních událostí (myš, klávesnice).
 * @author Tomáš Režnar
 */
public class EditorLogic implements GLEventListener, FocusListener
{

	/**
	 * Maximální tloušťka čáry podporována grafickou kartou.
	 * Hodnota bude inicializováná na správnou hodnotu v době vytvoření první instance objektu a po prvním cyklu
	 * vykreslení.
	 */
	private static int lineMaxWidth = 20;
	/**
	 * Maximální řád podporovaný v evulátorech.
	 * Hodnota bude inicializováná na správnou hodnotu v době vytvoření první instance objektu a po prvním cyklu
	 * vykreslení.
	 */
	private static int maxEvalOrder = 8;

	/**
	 * Maximální tloušťka čáry podporována grafickou kartou.
	 */
	public synchronized static int getLineMaxWidth()
	{
		return lineMaxWidth;
	}

	/**
	 * Nastavení maximální tloušťky čáry podporované graf. kartou.
	 */
	private synchronized static void setLineMaxWidth(int lineMaxWidth)
	{
		EditorLogic.lineMaxWidth = lineMaxWidth;
	}

	/**
	 * Maximální řád podporovaný v evulátorech.
	 */
	public synchronized static int getMaxEvalOrder()
	{
		return maxEvalOrder;
	}

	/**
	 * Nastavení maximálního řádu podporovaného v evulátorech.
	 * @param maxEvalOrder
	 */
	private synchronized static void setMaxEvalOrder(int maxEvalOrder)
	{
		EditorLogic.maxEvalOrder = maxEvalOrder;
	}
	/**
	 * Projekt který je v komponentě zobrazován.
	 */
	private final Project project;
	/**
	 * Celý model.
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
	 * Šířka scény.
	 */
	private int widthGL;
	/**
	 * Výška scény.
	 */
	private int heightGL;
	/**
	 * Příznak že došlo ke změně nastavení zobrazení a má být znovu provedena inicializace OpenGL.
	 */
	private volatile boolean invalidateDisplayOptions = false;
	/**
	 * Hranice pohledů na obrazovce.
	 */
	private volatile ViewportBounds viewport1ScreenBounds;
	/**
	 * Hranice pohledů na obrazovce.
	 */
	private volatile ViewportBounds viewport2ScreenBounds;
	/**
	 * Hranice pohledů na obrazovce.
	 */
	private volatile ViewportBounds viewport3ScreenBounds;
	/**
	 * Hranice pohledů na obrazovce.
	 */
	private volatile ViewportBounds viewport4ScreenBounds;
	/**
	 * Hranice pohledů na obrazovce.
	 */
	private volatile ViewportBounds viewportSingleScreenBounds;
	/**
	 * Vieport který má aktuálně focus => události na vstupu (myš, klávesnice) budou prováděny v daném viewportu.
	 */
	private volatile ViewportBounds viewportFocus;
	/**
	 * Sledovaný editor.
	 */
	private final Editor editor;
	/**
	 * Po prvním zavolání reshape(...) bude nastaven na true.
	 */
	private boolean viewportsInited = false;
	/**
	 * Posluchač změn v projektu.
	 */
	private final ProjectListener projectListener;
	/**
	 * Posluchač změn v objektech.
	 */
	private final ObjectListener objectListener;
	/**
	 * Skladování vytvořených display listů k objektům.
	 */
	private final HashMap<IObject, Integer> objectsDisplayLists = new HashMap<IObject, Integer>(16);
	/**
	 * Display list editačních uzlů aktuálně editovaného objektu pro viewport 1.
	 */
	private volatile Integer editNodesViewport1DisplayList;
	/**
	 * Display list editačních uzlů aktuálně editovaného objektu pro viewport 2.
	 */
	private volatile Integer editNodesViewport2DisplayList;
	/**
	 * Display list editačních uzlů aktuálně editovaného objektu pro viewport 3.
	 */
	private volatile Integer editNodesViewport3DisplayList;
	/**
	 * Display list editačních uzlů aktuálně editovaného objektu pro viewport 4.
	 */
	private volatile Integer editNodesViewport4DisplayList;
	/**
	 * Slouží pro zneplatnění všech display listů objektů.
	 */
	private volatile boolean objectInvalidDisplayListAll = false;
	/**
	 * Poloha kurzoru myši v openGl canvasu, [0;0] je vlevo dole.
	 */
	private int[] cursorPositionOnCanvas = new int[2];
	/**
	 * List obsahující seznam objektů pro které již neplatí display list uložený v objectsDisplayLists.
	 * Objekt je třeba při práci zamknout, protože je volán z vlákna vykreslení OpenGL a zároveň z vlákna
	 * obsluhujícího událostí změn modelu.
	 */
	private final ArrayList<IObject> objectInvalidDisplayLists = new ArrayList<IObject>(8);
	/**
	 * Slouží pro zneplatnění display listů ukládajícéch editační uzly vybraného objektu.
	 */
	private volatile boolean editNodesInvalidDisplayList = true;
	/**
	 * Přepočtené souřadnice pozice kurzoru na pozici v dokumentu pro viewport 1.
	 */
	private IPoint3f cursorPositionViewport1AndSingle = new Point3f();
	/**
	 * Přepočtené souřadnice pozice kurzoru na pozici v dokumentu pro viewport 2.
	 */
	private IPoint3f cursorPositionViewport2 = new Point3f();
	/**
	 * Přepočtené souřadnice pozice kurzoru na pozici v dokumentu pro viewport 3.
	 */
	private IPoint3f cursorPositionViewport3 = new Point3f();
	/**
	 * Přepočtené souřadnice pozice kurzoru na pozici v dokumentu pro viewport 4
	 */
	private IPoint3f cursorPositionViewport4 = new Point3f();
	/**
	 * Fronta událostí vyvolaných myší ve viewportu 1. Fronta má vnitřní synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport1MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Fronta událostí vyvolaných myší ve viewportu 2. Fronta má vnitřní synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport2MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Fronta událostí vyvolaných myší ve viewportu 3. Fronta má vnitřní synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport3MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Fronta událostí vyvolaných myší ve viewportu 4. Fronta má vnitřní synchronizaci.
	 */
	private final Queue<ViewportMouseEvent> viewport4MouseEvents = new LinkedBlockingQueue<ViewportMouseEvent>();
	/**
	 * Poslední souřadnice z myši při posunu pohybu myši.
	 */
	private volatile Float vieportLastZForMove = null;
	/**
	 * Vlákno čekající na vygenerování obrázku scény.
	 */
	private volatile Thread threadWaitingForImage = null;
	/**
	 * Vygenerovaný obrázek scény.
	 */
	private volatile BufferedImage sceneImage = null;
	/**
	 * Ikona kamery.
	 */
	private IntBuffer cameraIcon;
	/**
	 * Rozměry ikony kamery.
	 */
	private int[] cameraIconSize = new int[2];
	/**
	 * Popup menu pro nastavení kamery.
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
		//posluchači vstupu
		InputListener listener = new InputListener();
		canvas.addMouseListener(listener);
		canvas.addMouseMotionListener(listener);
		canvas.addMouseWheelListener(listener);
		canvas.addKeyListener(listener);

		//přidám posluchače objektů ke všem vytvořeným objektům
		objectListener = new ObjectListener();
		for (IObject o : project.getObjects())
		{
			o.addObjectListener(objectListener);
		}

		//přidání posluchače změn nastavení zobrazení
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
		//nastavení debugingu OpenGL
		//povoleno vždy-je kvůli potřebě zachytávat vyjímky v nějterých objektech
		if (true || Model.isDebug())
		{
			drawable.setGL(new DebugGL2((GL2) drawable.getGL()));
		}
		drawable.setAutoSwapBufferMode(true);

		gl = (GL2) drawable.getGL();
		glu = new GLU();
		glut = new GLUT();

		//nastavení maximální tloušťky čáry
		float[] range = new float[2];
		gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, range, 0);
		setLineMaxWidth((int) range[1]);
		gl.glGetFloatv(GL2.GL_MAX_EVAL_ORDER, range, 0);
		setMaxEvalOrder((int) range[1]);

		float[] backColor = project.getBackgroundColor().getColorComponents(null);
		gl.glClearColor(backColor[0], backColor[1], backColor[2], 0.0f);//prostor je bílý

		gl.glShadeModel(GL2.GL_SMOOTH);//Gouraudovo stínování - přechodem

		initDisplay(gl);

		//načtení ikony kamery
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
		//překopírování dat z obrázku do bufferu
		for (int row = cameraIconSize[1] - 1; row >= 0; row--)//řádky projdu pozpátku
		{
			for (int col = 0; col < cameraIconSize[0]; col++)//projdu sloupce
			{
				cameraIcon.put(bufferRGBA[(row * cameraIconSize[0]) + col]);
			}
		}
		cameraIcon.rewind();
	}

	/**
	 * Inicializace základních nastavení zobrazení.
	 */
	private void initDisplay(GL2 gl)
	{
		DisplayOptions options = model.getDisplayOptions();

		gl.setSwapInterval(options.isVsync() ? 1 : 0);//povolení vertikální synchronizace

		/*
		 * Antialising
		 */
		if (options.getPointSmooth() != 0)//bodů
		{
			gl.glEnable(GL2.GL_POINT_SMOOTH);//zapnutí
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, options.getPointSmooth());//nastavení kvality
		} else
		{
			gl.glDisable(GL2.GL_POINT_SMOOTH);//vypnutí
		}

		if (options.getLineSmooth() != 0)//linek
		{
			gl.glEnable(GL2.GL_LINE_SMOOTH);//zapnutí
			gl.glHint(GL2.GL_LINE_SMOOTH_HINT, options.getLineSmooth());//nastavení kvality
		} else
		{
			gl.glDisable(GL2.GL_LINE_SMOOTH);//vypnutí
		}

		if (options.getPolygonSmooth() != 0)//polygonů
		{
			gl.glEnable(GL2.GL_POLYGON_SMOOTH);//zapnutí
			gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, options.getPolygonSmooth());//nastavení kvality
		} else
		{
			gl.glDisable(GL2.GL_POLYGON_SMOOTH);//vypnutí
		}

		if (options.getPerspectiveCorection() != 0)//perspektivy
		{
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, options.getPerspectiveCorection());//nastavení kvality
		} else
		{
			gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_FASTEST);
		}

		//povolení testování hloubky
		if (options.isDepthTest())
		{
			gl.glEnable(gl.GL_DEPTH_TEST);
		} else
		{
			gl.glDisable(gl.GL_DEPTH_TEST);
		}

		//osvětlení
		gl.glMaterialfv(gl.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[]
		  {
			  1f, 1f, 1f, 1.0f
		  }, 0);// nastaveni odlesku material
		gl.glMaterialfv(gl.GL_FRONT_AND_BACK, GL2.GL_SHININESS, new float[]
		  {
			  options.getSpecularLightShininess()
		  }, 0);// nastaveni síly odlesků
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
		gl.glEnable(gl.GL_COLOR_MATERIAL);//zapnutí výpočet bavrvy materiálu dle nastavených barev vrcholů
		gl.glEnable(gl.GL_LIGHT0);//zapnutí světla 0


		gl.glEnable(GL2.GL_BLEND);//povolení blendingu - alfakanálu
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);//typ blendingu
		gl.glDisable(gl.GL_CULL_FACE);//zakázaní odstranňování odvrácených hran
		gl.glPolygonMode(gl.GL_FRONT, gl.GL_FILL);//vykreslovani prednich stran
		gl.glPolygonMode(gl.GL_BACK, gl.GL_FILL);//vykreslovani zadnich stran
		gl.glEnable(gl.GL_NORMALIZE);//automatická normalizace normálových vektorů
		gl.glEnable(gl.GL_AUTO_NORMAL);//automatické vytváření normál

		//nefunkční
		/*if (gl.isExtensionAvailable("GL_ARB_multisample")) {
		System.out.println("MS");
		gl.glEnable(gl.GL_MULTISAMPLE);
		}*/
	}

	/**
	 * Event změny zobrazení.
	 */
	@Override
	public synchronized void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		gl.glViewport(0, 0, width, height);
		this.widthGL = width;
		this.heightGL = height;
		//vypočítání hranic jednotlivých pohledů v rámci obrazovky
		viewport1ScreenBounds = new ViewportBounds(1, width / 2, height / 2, height - 1);
		viewport2ScreenBounds = new ViewportBounds(width / 2 + 1, width - 1, height / 2, height - 1);
		viewport3ScreenBounds = new ViewportBounds(1, width / 2, 1, height / 2 - 1);
		viewport4ScreenBounds = new ViewportBounds(width / 2 + 1, width - 1, 1, height / 2 - 1);
		viewportSingleScreenBounds = new ViewportBounds(1, width - 1, 1, height - 1);
		viewportFocus = null;
		viewportsInited = true;
	}

	/**
	 * Vlatní implementace vykreslení
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
		// vyprázdnění bufferů
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

		//odstranění display listů, pokud došlo ke změně zobrazní v něm
		if (objectInvalidDisplayListAll)
		{
			editNodesInvalidDisplayList = true;//nastavim příznak, aby došlo i kd odstranění display listů editačních uzlů
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
				//odstranění neplatných display listů uvedených v seznamu neplatných listů dle objektu
				synchronized (objectInvalidDisplayLists)
				{
					Integer old;
					for (IObject o : objectInvalidDisplayLists)
					{
						//odstranění ds objektu
						old = objectsDisplayLists.remove(o);
						if (old != null)
						{
							gl.glDeleteLists(old, 1);
						}
					}
					objectInvalidDisplayLists.clear();//vyprázdnění seznamu neplatných display listů
				}
			}
		}

		//odstranění display listů editačních uzlů
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

		//postupné vykreslení viewportů

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

		//rámeček viewportů
		drawViewportsBorder();

		//vygenerování obrázku scény
		if (threadWaitingForImage != null)
		{
			//označení [0;0] bodu jinou barvou pixelu
			gl.glRasterPos2i(0, 0);
			IntBuffer markPixel = IntBuffer.allocate(1);
			int pixel = 0xff000001;
			markPixel.put(pixel);
			markPixel.rewind();
			gl.glDrawPixels(
			  1, 1,
			  gl.GL_BGRA,
			  gl.GL_UNSIGNED_BYTE,
			  markPixel);

			//čtení obrazu
			IntBuffer buffer = IntBuffer.allocate(widthGL * heightGL);
			System.out.println(buffer.limit() + " " + (widthGL * heightGL));
			gl.glReadPixels(0, 0, widthGL, heightGL, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, buffer);
			int[] bufferRGBA;
			bufferRGBA = buffer.array();

			//vytvoření výsledného obrázku
			BufferedImage outputImage = new BufferedImage(widthGL, heightGL, BufferedImage.TYPE_INT_RGB);
			outputImage.setRGB(0, 0, widthGL, heightGL, bufferRGBA, 0, widthGL);

			//pokud je obrázek scény vertikálně otočený
			if (outputImage.getRGB(0, 0) == pixel)
			{
				AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
				tx.translate(0, -outputImage.getHeight());
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				outputImage = op.filter(outputImage, null);
			}

			sceneImage = outputImage;
			this.notifyAll();//probuzení vlákna čekajícího na vygenerovaný obrázek
			threadWaitingForImage = null;
		}

		//Poslání všech operací grafické kartě
		gl.glFlush();
	}

	/**
	 * Metoda nutná kvůli rozhraní GLEventListener; Neiplementována
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	/**
	 * Získání vygenerovaného obrázku scény.
	 */
	protected synchronized BufferedImage getSceneImage()
	{
		while (this.threadWaitingForImage != null)//počkám na uvolnění prostředků
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
	 * Vygenerování nového čísla display listu. Pokud se nepodaří vygenerovat,
	 * zobrazí se uživateli varování a aplikace ukončí.
	 */
	private Integer getEmptyDisplayList()
	{
		Integer displayList = gl.glGenLists(1); //vygenerování id nového listu
		if (displayList.intValue() == 0)
		{
			try
			{
				JOptionPane.showMessageDialog(null, "Došla paměť na grafické kartě. Program bude ukončen.", "Chyba", JOptionPane.ERROR_MESSAGE);
			} catch (HeadlessException ex)
			{
			}
			System.exit(0);
		}
		return displayList;
	}

	/**
	 * Vytvoření samostatného viewportu.
	 */
	private void setViewportSingle()
	{
		glViewport(viewportSingleScreenBounds);//celá obrazovka
		//celá obrazovka
		Projection projection = project.getViewport1Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslení obsahu
		drawViewport(viewportSingleScreenBounds);

		//výpočet pozice myši
		cursorPositionViewport1AndSingle = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracování událostí myši - záměrně až po vykreselní, kvůli práci s hloubkovým bufferem
		mouseEventService(projection, viewportSingleScreenBounds, viewport1MouseEvents);
	}

	/**
	 * Vytvoření viewportu 1.
	 */
	private void setViewport1()
	{
		glViewport(viewport1ScreenBounds);
		Projection projection = project.getViewport1Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslení obsahu
		drawViewport(viewport1ScreenBounds);

		//výpočet pozice myši
		cursorPositionViewport1AndSingle = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracování událostí myši - záměrně až po vykreselní, kvůli práci s hloubkovým bufferem
		mouseEventService(projection, viewport1ScreenBounds, viewport1MouseEvents);
	}

	/**
	 * Vytvoření viewportu 2.
	 */
	private void setViewport2()
	{
		glViewport(viewport2ScreenBounds);
		Projection projection = project.getViewport2Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslení obsahu
		drawViewport(viewport2ScreenBounds);

		//výpočet pozice myši
		cursorPositionViewport2 = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracování událostí myši - záměrně až po vykreselní, kvůli práci s hloubkovým bufferem
		mouseEventService(projection, viewport2ScreenBounds, viewport2MouseEvents);
	}

	/**
	 * Vytvoření viewportu 3.
	 */
	private void setViewport3()
	{
		glViewport(viewport3ScreenBounds);
		Projection projection = project.getViewport3Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslení obsahu
		drawViewport(viewport3ScreenBounds);

		//výpočet pozice myši
		cursorPositionViewport3 = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracování událostí myši - záměrně až po vykreselní, kvůli práci s hloubkovým bufferem
		mouseEventService(projection, viewport3ScreenBounds, viewport3MouseEvents);
	}

	/**
	 * Vytvoření viewportu 4.
	 */
	private void setViewport4()
	{
		glViewport(viewport4ScreenBounds);
		Projection projection = project.getViewport4Projection();
		projection.applyProjection(gl, glu, widthGL, heightGL);

		//vykreslení obsahu
		drawViewport(viewport4ScreenBounds);

		//výpočet pozice myši
		cursorPositionViewport4 = GLUtils.gluUnProject(gl, glu, cursorPositionOnCanvas[0], cursorPositionOnCanvas[1]);

		//zpracování událostí myši - záměrně až po vykreselní, kvůli práci s hloubkovým bufferem
		mouseEventService(projection, viewport4ScreenBounds, viewport4MouseEvents);
	}

	/**
	 * Obsluha událostí myší v daném viewportu.
	 * @param projection projekce viewportu.
	 * @param events Fronta událostí.
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
			//událost točení kolečka
			if (e instanceof ViewportMouseWheelEvent)
			{
				//kolečko je v ortho zobrazení zoom a v perspektivním pohyb
				MouseWheelEvent we = ((ViewportMouseWheelEvent) e).getWheelEvt();
				if (projection.isPerspective())
				{
					projection.move(we.getWheelRotation() * -20, 0, 0);
				} else
				{
					//zoom se zachování polohy bodu pod kurzorem
					float oldZoom = projection.getZoom();
					float newZoom = countNewZoom(-we.getWheelRotation(), oldZoom);
					float zoomRatio = oldZoom / newZoom;
					coordsTo = GLUtils.gluUnProject(gl, glu, //bod na pozici kurzoru, který chci fixovat
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
			//událost stisku tlačítka myši
			if (e instanceof ViewportMousePressEvent)
			{
				MouseScreenPosition mouse = e.getCursorPosition();
				IObject selObject = project.getSelectedObject();
				//změna výběru bodů objektu
				if (e.isButton1() && selObject != null)
				{
					int radius = 8;//vzdalenost hledani bodu
					IPoint3f pointClicked = null;//hledaný body na který bylo kliknuto
					IPoint3f tmp;
					for (IPoint3f p : selObject.getPoints())
					{
						//převedu bod na souřadnici na obrazovce
						tmp = GLUtils.gluProject(gl, glu, p);
						if (Math.abs(tmp.getX() - mouse.getX()) <= radius
						  && Math.abs(tmp.getY() - mouse.getY()) <= radius)
						{
							pointClicked = p;
							break;
						}
					}
					//provedu akce s nalezeným bodem
					if ((pointClicked == null || e.getEvt().isShiftDown() == true)
					  && e.getEvt().isControlDown() == false)//přidání bodu
					{
						if (selObject instanceof IPointsAddable)
						{
							IPoint3f lastPoint = selObject.getLastSelectedPoint();
							IPoint3f newPoint;
							if (lastPoint != null)//nový bod bude ve stejné hladině jako posledně vybraný
							{
								float z = GLUtils.gluProject(gl, glu, lastPoint).getZ();
								newPoint = GLUtils.gluUnProject(gl, glu, mouse.getX(), mouse.getY(), z);
							} else//musím určit nový bod
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
						if (pointClicked != null && e.getEvt().isShiftDown() == false)//výběr bodu
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
			//událost puštění tlačítka myši
			if (e instanceof ViewportMouseRelasedEvent)
			{
				if (e.isButton3())
				{
					vieportLastZForMove = null;
				}
				continue;
			}
			//události posunu myši
			if (e instanceof ViewportMouseDragEvent)
			{
				ViewportMouseDragEvent ev = (ViewportMouseDragEvent) e;
				from = ev.getFromCursorPosition();//poloha kurzoru odkud byl tažen
				IObject selObject = project.getSelectedObject();

				//posun bodů objektu
				if (ev.isButton1() && selObject != null && project.isEditorInEditingMode())
				{
					IPoint3f last = selObject.getLastSelectedPoint();//naposledy vybraný bod
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
							//zapamatování si místa kliku myši
							if (vieportLastZForMove == null)
							{
								coordsFrom = GLUtils.gluUnProjectWithRadius(gl, glu, from.getX(), from.getY(), 4);
								vieportLastZForMove = GLUtils.gluProject(gl, glu, coordsFrom).getZ();
								if (vieportLastZForMove >= 0.99999f)//když se na pozici nenachází objekt
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
	 * Vykreslení všech objektů.
	 */
	private void drawObjects()
	{
		//objekty
		Integer displayList;//identifikace čísla display listu v OpenGL

		//určím které objekty vykreslím
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

		//projdu všechny objekty
		for (IObject o : objects)
		{
			try
			{
				gl.glDepthFunc(gl.GL_LEQUAL);//bližší těleso přepíše vzdálenější
				gl.glEnable(gl.GL_LIGHTING);//povolení osvětlení
				gl.glLineWidth(1);
				gl.glPointSize(1);
				displayList = objectsDisplayLists.get(o);
				long time=System.currentTimeMillis();
				if (displayList == null || !gl.glIsList(displayList))//nemáli objekt vytvořen display list
				{
					displayList = getEmptyDisplayList();
					gl.glNewList(displayList, gl.GL_COMPILE_AND_EXECUTE);//zahájení nahrávání a zároveň vykreslení listu
					//zachycení jakékoli chyby v implementaci objektu
					try
					{
						o.draw(gl);//vykreslení objektu
					} catch (Exception ex)
					{
						model.debugPrintException(ex);
					} finally
					{
						gl.glEndList();//ukončení nahrávání listu
						System.out.println(o.getName()+": GEN: "+(System.currentTimeMillis()-time)+" ms");
						objectsDisplayLists.put(o, displayList);
					}
				} else//objekt má vytvořen disply list
				{
					gl.glCallList(displayList);
				}
			} catch (GLException ex)
			{
				model.debugPrintException(ex);
			} finally
			{
				gl.glDepthFunc(gl.GL_LEQUAL);//bližší těleso přepíše vzdálenější
			}
		}
	}

	/**
	 * Vykreslení editačních uzlů vybraného objektu do viewportu.
	 */
	private void drawEditingNodes(ViewportBounds viewport)
	{
		if (project.isEditorInEditingMode())
		{
			IObject o = project.getSelectedObject();
			if (o != null)//jeli vybrán objekt
			{
				Integer displayList = getEditNodesDisplayListForViewport(viewport);
				Projection projection = getProjectionForViewport(viewport);
				gl.glDepthFunc(gl.GL_ALWAYS);//bližší těleso přepíše vzdálenější
				if (displayList == null || !gl.glIsList(displayList))//nemáli objekt vytvořen display list editačních uzlů
				{
					displayList = getEmptyDisplayList();
					gl.glNewList(displayList, gl.GL_COMPILE_AND_EXECUTE);//zahájení nahrávání a zároveň vykreslení listu
					o.drawNodes(gl, projection);//vykreslení uzlů
					gl.glEndList();//ukončení nahrávání listu
					setEditNodesDisplayListForViewport(viewport, displayList);
				} else//jeli vytvořen display list editačních uzlů
				{
					gl.glCallList(displayList);
				}
			}
		}
	}

	/**
	 * Vykreslení viewportu.
	 */
	private void drawViewport(ViewportBounds viewport)
	{
		Projection projection = getProjectionForViewport(viewport);
		if (projection.isPerspective())//v perspektivním je osový kříž v prostoru
		{
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);// vyprázdnění bufferů
			gl.glDepthFunc(gl.GL_LEQUAL);//bližší těleso přepíše vzdálenější
			//osy
			if (project.isShowAxes())
			{
				gl.glDisable(gl.GL_LIGHTING);
				gl.glPushAttrib(gl.GL_ALL_ATTRIB_BITS);
				gl.glDisable(GL2.GL_LINE_SMOOTH);//vypnu antiliasing čar
				GLUtils.drawAxes(gl, 5000);
				gl.glPopAttrib();
			}
			//objekty
			drawObjects();//vykreslim objekty
		} else//v otho je osový kříž na pozadí
		{
			gl.glDepthFunc(gl.GL_ALWAYS);//bližší těleso přepíše vzdálenější
			//osy
			if (project.isShowAxes())
			{
				gl.glDisable(gl.GL_LIGHTING);
				GLUtils.drawAxes(gl, 5000);
			}
			//objekty
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);// vyprázdnění bufferů
			drawObjects();//vykreslim objekty
		}
		gl.glDisable(gl.GL_LIGHTING);
		drawEditingNodes(viewport);//vykreslení editačních uzlů vybraného objektu
	}

	/**
	 * Nastavení rámečků mezi vieporty, informační popisky, tlačítka.
	 */
	private void drawViewportsBorder()
	{
		//vypnu světlo
		gl.glDisable(gl.GL_LIGHTING);
		// vyprázdnění bufferů
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		gl.glDepthFunc(gl.GL_ALWAYS);//rámeček je vidět vždy
		gl.glViewport(0, 0, widthGL, heightGL);//celé okno
		gl.glMatrixMode(gl.GL_PROJECTION);//Projekční matice
		gl.glLoadIdentity();//Reset projekční matice
		gl.glOrtho(0, widthGL, 0, heightGL, 5000, -5000);//Pravoúhlá projekce

		//rámečky
		gl.glLineWidth(1f);//Tloušťka čar 1px
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

		//vykreslím rámečky
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
			if (threadWaitingForImage == null)//pokud se negeneruje obraz pro export obrázku
			{
				//tlačítko pro nastavení kamery
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
	 * Zjištění ve kterém vieportu se nachází souřadnice.
	 * @param x x souřadnice, kde levý dolní roh==0
	 * @param y y souřadnice, kde levý dolní roh==0
	 * @return Vrátí vieport nebo null když je bod mimo všechny vieporty.
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
	 * Získání aktuální projekce viewportu.
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
	 * Vrátí display list editačních uzlů vybraného viewportu.
	 * @return Vrátí null pokud je zadán neplatný viewport.
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
	 * Nastaví display list editačních uzlů vybraného viewportu.
	 * @throws IllegalArgumentException Pokud je neplatný viewport.
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
							throw new IllegalArgumentException("Neexistující viewport.");
						}
					}
				}
			}
		}
	}

	/**
	 * Získání pozice kurzoru přepočítanou na souřadnice v prostoru v daném viewportu.
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
	 * Spočítání nové hodnoty zoomu na základě staré a počtu kroků.
	 * @param steps Kladné číslo znamená zmenšení zoomu.
	 * @param oldZoom Předchozí zoom.
	 */
	protected float countNewZoom(int steps, float oldZoom)
	{
		float newZoom = oldZoom * (1f - 0.2f * -steps);
		if (newZoom < oldZoom * 0.5)//omezení maximální změny kroku zoomu
		{
			newZoom = oldZoom * 0.5f;
		}
		if (newZoom > oldZoom * 1.5)//omezení maximální změny kroku zoomu
		{
			newZoom = oldZoom * 1.5f;
		}
		newZoom = newZoom > 50f ? 50f : newZoom;
		newZoom = newZoom < 0.05f ? 0.05f : newZoom;
		return newZoom;
	}

	/**
	 * Nastavení pohledu OpenGL.
	 */
	protected void glViewport(ViewportBounds bounds)
	{
		gl.glViewport(bounds.getX1(), bounds.getY1(), bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * Souhrná metoda volaná při jakékoli změně objektu při které je nutné změnit display listy.
	 */
	private void objectChange(IObject o)
	{
		synchronized (objectInvalidDisplayLists)
		{
			objectInvalidDisplayLists.add(o);
		}
		//pokud je objekt aktuálně i editovaný, zneplatním jeho display list s editačnímy uzly
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
	 * Třída pro zpracování událostí v projektu.
	 */
	public class ProjectListener extends ProjectAdapter
	{

		@Override
		public void eventVieport1Changed(Projection p)
		{
			//zneplatním disply listy s editačnímy uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventVieport2Changed(Projection p)
		{
			//zneplatním disply listy s editačnímy uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventVieport3Changed(Projection p)
		{
			//zneplatním disply listy s editačnímy uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventVieport4Changed(Projection p)
		{
			//zneplatním disply listy s editačnímy uzly
			editNodesInvalidDisplayList = true;
		}

		@Override
		public void eventSelectedObjectChanged(IObject selectedObject, int index)
		{
			//zneplatním disply listy s editačnímy uzly
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
	 * Třída zpracovávající události ze vstupu od uživatelE.
	 */
	public class InputListener implements MouseInputListener, MouseWheelListener, KeyListener
	{

		/**
		 * pozice posledního kliku v rámci vieportu
		 */
		private int[] lastClickCords = new int[3];

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			//souřadnice kliku myši
			int mouseX = e.getX();
			int mouseY = e.getComponent().getHeight() - e.getY();
			ViewportBounds vp = getMouseViewport(e);//viewport ve kterém došlo k události
			if (vp == null)
			{
				return;
			}
			lastClickCords = new int[]
			  {
				  mouseX, mouseY
			  };
			viewportFocus = vp;

			//rozeznání jestli se myš nachází nad ikonou pro nastavení kamery - menu kamery
			if (viewportFocus != null)
			{
				//nachází se nad ikonou
				if (viewportFocus.getX2() >= cursorPositionOnCanvas[0]
				  && (viewportFocus.getX2() - cameraIconSize[0]) <= cursorPositionOnCanvas[0]
				  && viewportFocus.getY2() >= cursorPositionOnCanvas[1]
				  && (viewportFocus.getY2() - cameraIconSize[1]) <= cursorPositionOnCanvas[1])
				{
					cameraMenu.setProjection(getProjectionForViewport(viewportFocus));
					cameraMenu.show(e.getComponent(), e.getX(), e.getY());
					return;//žádné další akce se neprovedou
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
			//souřadnice myši
			int mouseX = e.getX();
			int mouseY = e.getComponent().getHeight() - e.getY();

			ViewportBounds vp = viewportFocus;
			if (vp != null)//událost zpracována pouze pokud došlo ke kliku v některém vieportu
			{
				//přidání události do fronty
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

			//rozeznání jestli se myš nachází nad ikonou pro nastavení kamery
			if (viewportFocus != null)
			{
				//nachází se nad ikonou
				if (viewportFocus.getX2() >= cursorPositionOnCanvas[0]
				  && (viewportFocus.getX2() - cameraIconSize[0]) <= cursorPositionOnCanvas[0]
				  && viewportFocus.getY2() >= cursorPositionOnCanvas[1]
				  && (viewportFocus.getY2() - cameraIconSize[1]) <= cursorPositionOnCanvas[1])
				{
					editor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} //nenachází se nad ikonou
				else
				{
					editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			ViewportBounds vp = getMouseViewport(e);//viewport ve kterém došlo k události

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
		 * Získání vieportu ve kterém se nachází myš. Null pokud v žádném.
		 * @param e Událost myši jež je kontrolována.
		 */
		protected ViewportBounds getMouseViewport(MouseEvent e)
		{
			return detectVieport(e.getX(), e.getComponent().getHeight() - e.getY());
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			IObject selObject = project.getSelectedObject();//vybraný objekt
			Projection projection = getProjectionForViewport(viewportFocus);
			if (projection == null)
			{
				return;
			}
			/*
			 * Ovládání kamery
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
					//rotace kamery nahoru/dolů
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
				//resetování projekce
				case '5':
					projection.reset();
					break;
				//posun vpřed/vzad
				case '7':
					projection.move(20f, 0, 0);
					break;
				case '1':
					projection.move(-20f, 0, 0);
					break;
				//posun nahoru/dolů
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
			IObject selObject = project.getSelectedObject();//vybraný objekt
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
					//smazání vybraných bodů
					case KeyEvent.VK_DELETE:
						if (project.isEditorInEditingMode())
						{
							if (selObject instanceof IPointsAddable)
							{
								editor.fireDeleteSelectedPoints((IPointsAddable) selObject);
							}
						}
						break;
					//posun vybraných bodů
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
	 * Posluchač změn objektů, jedna instance posluchače pro všechny objekty v projektu.
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
