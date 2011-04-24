package cscg.ui;

import cscg.ui.components.ViewType;
import com.jogamp.opengl.util.gl2.GLUT;
import cscg.model.Projection;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.Point3f;
import cscg.model.objects.PointOperations;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 * Pomocná knihovna pro usnadnìní vykreslení nìkterých grafických prvkù.
 * @author Tomáš Režnar
 */
public class GLUtils
{

	/**
	 * Barva osy x.
	 */
	public static final Color AXIS_X_COLOR = Color.red;
	/**
	 * Barva osy y.
	 */
	public static final Color AXIS_Y_COLOR = Color.green;
	/**
	 * Barva osy z.
	 */
	public static final Color AXIS_Z_COLOR = Color.blue;
	/**
	 * Znaky formátování èísel.
	 */
	public static final DecimalFormatSymbols numberFormatSymbols = new DecimalFormatSymbols();
	/**
	 * Formátování souøadnic.
	 */
	public static final DecimalFormat coordsFormat = new DecimalFormat("#.0", numberFormatSymbols);

	static
	{
		numberFormatSymbols.setGroupingSeparator(' ');
		numberFormatSymbols.setDecimalSeparator(',');
	}

	/**
	 * Nastavení barvy vertexu.
	 */
	public static void glSetColor(GL2 gl, Color color)
	{
		gl.glColor4fv(color.getRGBComponents(null), 0);
	}

	/**
	 * Vykreslení bodù dané barvy a velikosti.
	 */
	public static void drawPoints(GL2 gl, List<IPoint3f> points, Color pointsColor, float pointSize)
	{
		if (points.size() > 0)
		{
			gl.glPointSize(pointSize);
			gl.glColor3fv(pointsColor.getRGBComponents(null), 0);
			gl.glBegin(gl.GL_POINTS);
			for (IPoint3f p : points)
			{
				gl.glVertex3f(p.getX(), p.getY(), p.getZ());
			}
			gl.glEnd();
		}
	}

	/**
	 * Vykreslení šipky-funkèní pouze pro ortho zobrazení.
	 * Pokud je potøeba nastavit tlouštku èáry a barvu, musí se zavolat OpenGL funkce pøed voláním této funkce.
	 * @param startPoint odkud šipka ukazuje
	 * @param endPoint kam ukazuje
	 * @param radius polomìr šipky (polovina šíøky podstavy trojúhelníku)
	 * @param height výška trojúhelníku šipky
	 */
	public static void drawArrow(GL2 gl, float scale, ViewType view, IPoint3f startPoint, IPoint3f endPoint, float radius, float height)
	{
		radius /= scale;
		height /= scale;
		height = -height;
		float dY = endPoint.getY() - startPoint.getY();
		float dX = endPoint.getX() - startPoint.getX();
		float dZ = endPoint.getZ() - startPoint.getZ();
		//nožka šipky
		gl.glBegin(gl.GL_LINES);
		gl.glVertex3f(endPoint.getX(), endPoint.getY(), endPoint.getZ());
		gl.glVertex3f(startPoint.getX(), startPoint.getY(), startPoint.getZ());
		gl.glEnd();
		//trojúhelník šipky
		gl.glPushMatrix();
		gl.glTranslatef(endPoint.getX(), endPoint.getY(), endPoint.getZ());
		if (view == ViewType.FRONT)
		{
			double rotByZ = -Math.toDegrees(Math.atan(dX / dY));
			if (dY < 0)
			{
				rotByZ += 180;
			}
			gl.glRotated(rotByZ, 0, 0, 1);
			gl.glBegin(gl.GL_TRIANGLES);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(-radius, height, 0);
			gl.glVertex3f(+radius, height, 0);
			gl.glEnd();
		} else
		{
			if (view == ViewType.PROFILE)
			{
				double rotByX = Math.toDegrees(Math.atan(dZ / dY));
				if (dY < 0)
				{
					rotByX += 180;
				}
				gl.glRotated(rotByX, 1, 0, 0);
				gl.glBegin(gl.GL_TRIANGLES);
				gl.glVertex3f(0, 0, 0);
				gl.glVertex3f(0, height, -radius);
				gl.glVertex3f(0, height, +radius);
				gl.glEnd();
			} else
			{
				if (view == ViewType.TOP)
				{
					double rotByY = Math.toDegrees(Math.atan(dX / dZ));
					if (dZ < 0)
					{
						rotByY += 180;
					}
					gl.glRotated(rotByY, 0, 1, 0);
					gl.glBegin(gl.GL_TRIANGLES);
					gl.glVertex3f(0, 0, 0);
					gl.glVertex3f(-radius, 0, height);
					gl.glVertex3f(+radius, 0, height);
					gl.glEnd();
				}
			}
		}
		gl.glPopMatrix();
	}

	/**
	 * Vykreslení šipky-funkèní pro libovolnou projekci.
	 * Pokud je potøeba nastavit tlouštku èáry a barvu, musí se zavolat OpenGL funkce pøed voláním této funkce.
	 * @param startPoint odkud šipka ukazuje
	 * @param endPoint kam ukazuje
	 * @param radius polomìr šipky (polovina šíøky podstavy trojúhelníku)
	 * @param height výška trojúhelníku šipky
	 */
	public static void drawArrow(GL2 gl, GLU glu, Projection projection, IPoint3f startPoint, IPoint3f endPoint, float radius, float height)
	{
		height = -height;
		//nožka šipky
		gl.glBegin(gl.GL_LINES);
		gl.glVertex3f(endPoint.getX(), endPoint.getY(), endPoint.getZ());
		gl.glVertex3f(startPoint.getX(), startPoint.getY(), startPoint.getZ());
		gl.glEnd();
		//trojúhelník šipky
		IPoint3f startPointScreen = gluProject(gl, glu, startPoint);
		IPoint3f endPointScreen = gluProject(gl, glu, endPoint);

		if (PointOperations.compareCoords(startPointScreen, endPointScreen) == false)//pokud je šipka na obrazovce vidìt
		{
			gl.glPushMatrix();
			float dY = endPointScreen.getY() - startPointScreen.getY();
			float dX = endPointScreen.getX() - startPointScreen.getX();
			double rotByZ = -Math.toDegrees(Math.atan(dX / dY));//výpoèet úhlu natoèení šipky
			if (dY > 0)
			{
				rotByZ += 180;
			}
			IPoint3f p1, p2;//vrcholy trojuhelniku
			p1 = gluUnProject(gl, glu,
			  endPointScreen.getX() - radius,
			  endPointScreen.getY() + height,
			  endPointScreen.getZ());
			p2 = gluUnProject(gl, glu,
			  endPointScreen.getX() + radius,
			  endPointScreen.getY() + height,
			  endPointScreen.getZ());
			gl.glTranslatef(endPoint.getX(), endPoint.getY(), endPoint.getZ());
			IPoint3f dir = projection.getDirection();
			gl.glRotatef(-(float) rotByZ, dir.getX(), dir.getY(), dir.getZ());
			gl.glBegin(gl.GL_TRIANGLES);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(
			  endPoint.getX() - p1.getX(),
			  endPoint.getY() - p1.getY(),
			  endPoint.getZ() - p1.getZ());
			gl.glVertex3f(
			  endPoint.getX() - p2.getX(),
			  endPoint.getY() - p2.getY(),
			  endPoint.getZ() - p2.getZ());
			gl.glEnd();
			gl.glPopMatrix();
		}
	}

	/**
	 * Vykreslení jednoduché èáry.
	 * @param points body
	 */
	public static void drawSimpleLine(GL2 gl, Collection<? extends IPoint3f> points)
	{
		if (points.size() > 1)
		{
			gl.glBegin(gl.GL_LINE_STRIP);
			for (IPoint3f p : points)
			{
				gl.glVertex3f(p.getX(), p.getY(), p.getZ());
			}
			gl.glEnd();
		}
	}

	/**
	 * Vykreslení jednoduché èáry.
	 * @param points body
	 */
	public static void drawSimpleLine(GL2 gl, IPoint3f[] points)
	{
		if (points.length > 1)
		{
			gl.glBegin(gl.GL_LINE_STRIP);
			for (IPoint3f p : points)
			{
				gl.glVertex3f(p.getX(), p.getY(), p.getZ());
			}
			gl.glEnd();
		}
	}

	/**
	 * Vykreslení os v prostoru.
	 * @param bounds polomìr velikosti prostoru, osy budou vykresleny v rozmezí od -bounds do bounds
	 */
	public static void drawAxes(GL2 gl, int bounds)
	{
		gl.glLineWidth(1f);
		gl.glBegin(gl.GL_LINES);
		glSetColor(gl, AXIS_X_COLOR);
		gl.glVertex3i(-bounds, 0, 0);
		gl.glVertex3i(bounds, 0, 0);
		glSetColor(gl, AXIS_Y_COLOR);
		gl.glVertex3i(0, -bounds, 0);
		gl.glVertex3i(0, bounds, 0);
		glSetColor(gl, AXIS_Z_COLOR);
		gl.glVertex3i(0, 0, -bounds);
		gl.glVertex3i(0, 0, bounds);
		gl.glEnd();
	}

	/**
	 * Vykreslení ikony camery zobrazující natoèení kamery.
	 */
	public static void drawOrientationCameraIcon(GL2 gl, GLU glu, Projection projection)
	{
		int radius = 25;//polomìr ikony
		gl.glLineWidth(1f);
		gl.glPushMatrix();
		//rotace
		gl.glTranslatef(radius, radius, -radius);//posunu na støed
		IPoint3f center = projection.getDirection(),
		  up = projection.getUp();
		glu.gluLookAt(0, 0, 0,
		  //1,1,0,
		  center.getX(), center.getY(), center.getZ(),
		  up.getX(), up.getY(), up.getZ());
		gl.glRotatef(180f, 0, 1, 0);

		int lineRadius = 3;//polomìr tlouštky nohy šipky
		int headRadius = 8;//polomìr hlavy šipky


		//noha šipky
		gl.glBegin(gl.GL_QUAD_STRIP);
		//vrchní stìna šipky je odlišné barvy pro odlišení
		glSetColor(gl, AXIS_X_COLOR);
		gl.glNormal3f(0, 1, 0);
		gl.glVertex3f(-lineRadius, lineRadius, radius);
		gl.glVertex3f(-lineRadius, lineRadius, 0);
		gl.glVertex3f(lineRadius, lineRadius, radius);
		gl.glVertex3f(lineRadius, lineRadius, 0);
		//pravá stìna
		glSetColor(gl, AXIS_Y_COLOR);
		gl.glNormal3f(1, 0, 0);
		gl.glVertex3f(lineRadius, lineRadius, radius);
		gl.glVertex3f(lineRadius, lineRadius, 0);
		gl.glVertex3f(lineRadius, -lineRadius, radius);
		gl.glVertex3f(lineRadius, -lineRadius, 0);
		//spodní stìna
		gl.glNormal3f(0, -1, 0);
		gl.glVertex3f(-lineRadius, -lineRadius, radius);
		gl.glVertex3f(-lineRadius, -lineRadius, 0);
		//levá stìna
		gl.glNormal3f(-1, 0, 0);
		gl.glVertex3f(-lineRadius, lineRadius, radius);
		gl.glVertex3f(-lineRadius, lineRadius, 0);
		gl.glEnd();

		gl.glBegin(gl.GL_QUADS);
		//podstava
		glSetColor(gl, AXIS_X_COLOR);
		gl.glNormal3f(0, 0, 1);
		gl.glVertex3f(-lineRadius, lineRadius, radius);
		gl.glVertex3f(lineRadius, lineRadius, radius);
		gl.glVertex3f(lineRadius, -lineRadius, radius);
		gl.glVertex3f(-lineRadius, -lineRadius, radius);
		glSetColor(gl, AXIS_Z_COLOR);
		gl.glEnd();

		//hlava šipky kužel
		GLUquadric q = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(q, GLU.GLU_FILL);
		glu.gluCylinder(q, headRadius, 0, -radius, 8, 4);
		glu.gluDeleteQuadric(q);

		gl.glPopMatrix();
	}

	/**
	 * Vykreslení ikony os pro použití v ortho zobrazení. Køíž je vykreslen na pozici [0,0,0].
	 * Køíž je vykreslen vždy z pohledu zepøedu, dle parametru viewType se mìní pouze barvy a popisky
	 */
	public static void drawAxesIcon(GL2 gl, GLUT glut, ViewType viewType)
	{
		float pading = 0.5f;
		IPoint3f zeroPoint = new Point3f(pading, pading, pading);
		gl.glLineWidth(1f);
		if (viewType == ViewType.FRONT) // pohled ze pøedu
		{
			glSetColor(gl, AXIS_X_COLOR);
			drawArrow(gl, 1f, ViewType.FRONT, zeroPoint, new Point3f(50f, pading, pading), 4, 15);
			gl.glRasterPos3f(53f, -3f, pading);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "x");
			glSetColor(gl, AXIS_Y_COLOR);
			drawArrow(gl, 1f, ViewType.FRONT, zeroPoint, new Point3f(pading, 50f, pading), 4, 15);
			gl.glRasterPos3f(-3f, 56f, pading);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "y");
		} else
		{
			if (viewType == ViewType.PROFILE)// pohled z boku
			{
				glSetColor(gl, AXIS_Z_COLOR);
				drawArrow(gl, 1f, ViewType.FRONT, zeroPoint, new Point3f(50f, pading, pading), 4, 15);
				gl.glRasterPos3f(53f, -3f, pading);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "-z");
				glSetColor(gl, AXIS_Y_COLOR);
				drawArrow(gl, 1f, ViewType.FRONT, zeroPoint, new Point3f(pading, 50f, pading), 4, 15);
				gl.glRasterPos3f(-3f, 56f, pading);
				glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "y");
			} else
			{
				if (viewType == ViewType.TOP)// pohled z boku
				{
					glSetColor(gl, AXIS_X_COLOR);
					drawArrow(gl, 1f, ViewType.FRONT, zeroPoint, new Point3f(50f, pading, pading), 4, 15);
					gl.glRasterPos3f(53f, -3f, pading);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "x");
					glSetColor(gl, AXIS_Z_COLOR);
					drawArrow(gl, 1f, ViewType.FRONT, zeroPoint, new Point3f(pading, 50f, pading), 4, 15);
					gl.glRasterPos3f(-3f, 56f, pading);
					glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "-z");
				}
			}
		}
	}

	/**
	 * Vykreslení nápisu s informací o zoomu.
	 */
	public static void drawZoomInfo(GL2 gl, GLUT glut, float scale)
	{
		glSetColor(gl, Color.black);
		gl.glRasterPos3i(0, 0, 0);
		String zoom = Integer.toString(Math.round(scale * 100f));
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Zoom " + zoom + "%");
	}

	/**
	 * Vykreslení nápisu s informací o pozici kurzoru.
	 * @param viewType vypíše pouze ty souøadnice jež se mìní s pozicí kurzoru v daném pohledu
	 * @param alignLeft true-zarovnat vlevo, false-vpravo
	 * @param alignTop  true-text pujde smìrem dolù, false-opaènì
	 */
	public static void drawCursorInfo(GL2 gl, GLUT glut, ViewType viewType, IPoint3f point, boolean alignLeft, boolean alignTop)
	{
		glSetColor(gl, Color.black);
		String text, x, y, z;
		LinkedList<String> lines = new LinkedList<String>();//øádky co se vypíší
		x = "x=" + coordsFormat.format(point.getX());
		y = "y=" + coordsFormat.format(point.getY());
		z = "z=" + coordsFormat.format(point.getZ());
		switch (viewType)
		{
			case FRONT:
				lines.add(x);
				lines.add(y);
				break;
			case PROFILE:
				lines.add(y);
				lines.add(z);
				break;
			case TOP:
				lines.add(x);
				lines.add(z);
				break;
			case PERSPECTIVE:
				lines.add(x);
				lines.add(y);
				lines.add(z);
				break;
		}
		drawTextBox(gl, glut, lines, GLUT.BITMAP_HELVETICA_10, 10, 3, alignLeft, alignTop);
	}

	/**
	 * Vepíše text do boxu.
	 * @param lines øádky text
	 * @param glutBitmapFont konstanta GLUT tøídy s fontem, napø GLUT.BITMAP_HELVETICA_10
	 * @param lineHeight výška øádku
	 * @param lineSpacing mezera mezi øádky
	 * @param alignLeft true-zarovnat vlevo, false-vpravo
	 * @param alignTop  true-text pujde smìrem dolù, false-opaènì
	 */
	public static void drawTextBox(GL2 gl, GLUT glut, Deque lines, int glutBitmapFont,
	  int lineHeight, int lineSpacing, boolean alignLeft, boolean alignTop)
	{
		int positionLeft,
		  positionTop = alignTop ? -lineHeight : 0;
		Iterator<String> iter = alignTop ? lines.iterator() : lines.descendingIterator();
		String line;
		while (iter.hasNext())
		{
			line = iter.next();
			positionLeft = alignLeft ? 0 : -glut.glutBitmapLength(glutBitmapFont, line);
			gl.glRasterPos2i(positionLeft, positionTop);
			glut.glutBitmapString(glutBitmapFont, line);
			positionTop += alignTop ? -(lineHeight + lineSpacing) : +lineHeight + lineSpacing;
		}
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù.
	 * @param points kolekce bodù v seøazená po øádcích
	 * @param width poèet bodù v øádku
	 * @param height poèet øádkù
	 */
	public static void drawSurface(GL2 gl, List<? extends IPoint3f> points, int width, int height)
	{
		drawSurface(gl, points, width, height, GL2.GL_FILL);
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù.
	 * @param points kolekce bodù v seøazená po øádcích
	 * @param width poèet bodù v øádku
	 * @param height poèet øádkù
	 * @param mode režim vykreslení, jedna z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL
	 */
	public static void drawSurface(GL2 gl, List<? extends IPoint3f> points, int width, int height, int mode)
	{
		drawSurfaceViaTriangles(gl, points, width, mode);
		/*float[] pintsArray = listOfPoints3fToArray(points);
		gl.glEnable(gl.GL_MAP2_VERTEX_3);
		for (int row = 1; row < height; row++)
		{
		for (int col = 1; col < width; col++)
		{
		drawSurfaceViaEvulatorsVertex3(gl, pintsArray, ((row - 1) * width + col - 1) * 3, width, mode);
		}
		}
		gl.glDisable(gl.GL_MAP2_VERTEX_3);*/
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù pomocí primitiva trojúhelníkù.
	 * @param points list bodù v seøazená po øádcích
	 * @param rowStride poèet bodù v øádku
	 */
	public static void drawSurfaceViaTriangles(GL2 gl, List<? extends IPoint3f> points, int rowStride)
	{
		drawSurfaceViaTriangles(gl, points, rowStride, GL2.GL_FILL);
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù pomocí primitiva trojúhelníkù.
	 * @param points list bodù v seøazená po øádcích
	 * @param rowStride poèet bodù v øádku
	 * @param mode režim vykreslení, jedna z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL
	 */
	public static void drawSurfaceViaTriangles(GL2 gl, List<? extends IPoint3f> points, int rowStride, int mode)
	{
		gl.glPushAttrib(gl.GL_POLYGON_BIT);
		gl.glPolygonMode(gl.GL_FRONT_AND_BACK, mode);

		float[][] normals = new float[points.size()][3];//pole normál

		//spoèítání normál - ve více vláknech
		int procesors = Runtime.getRuntime().availableProcessors();
		NormalCounter[] normalCounters = new NormalCounter[procesors < points.size() ? procesors : points.size()];
		double range = (double) normals.length / (double) normalCounters.length;
		for (int i = 0; i < normalCounters.length - 1; i++)//vytvoøím objekty vláken bez posledního
		{
			normalCounters[i] = new NormalCounter(normals, (int) (i * range), (int) ((i + 1) * range) - 1, points, rowStride);
		}
		//poslední objekt vlákna má nastaven koncový index normály napevno, kvùli oøezání indexu v pøedchozím cyklu
		normalCounters[normalCounters.length - 1] = new NormalCounter(
		  normals, (int) ((normalCounters.length - 1) * range), normals.length - 1, points, rowStride);
		//spuštìní vláken
		Thread[] workers = new Thread[normalCounters.length];
		for (int i = 0; i < normalCounters.length; i++)//spustím vlákna
		{
			workers[i] = new Thread(normalCounters[i]);
			workers[i].start();
		}

		for (int i = 0; i < workers.length; i++)//zkontroluji
		{
			try
			{
				workers[i].join();
			} catch (InterruptedException ex)
			{
				Logger.getLogger(GLUtils.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		//zde již jsou normály kompletní

		IPoint3f p0, p1;//body trojúhelníku
		float[] n0, n1;//normály bodù
		//projdu øádky
		for (int row = 1, rowEnd = points.size() / rowStride; row < rowEnd; row++)
		{
			gl.glBegin(gl.GL_TRIANGLE_STRIP);
			//projdu sloupce
			for (int col = 0; col < rowStride; col++)
			{
				p0 = points.get((row - 1) * rowStride + col);
				p1 = points.get(row * rowStride + col);
				n0 = normals[(row - 1) * rowStride + col];
				n1 = normals[row * rowStride + col];

				gl.glNormal3f(n0[0], n0[1], n0[2]);
				gl.glVertex3f(p0.getX(), p0.getY(), p0.getZ());
				gl.glNormal3f(n1[0], n1[1], n1[2]);
				gl.glVertex3f(p1.getX(), p1.getY(), p1.getZ());
			}
			gl.glEnd();
		}

		gl.glPopAttrib();
	}

	/**
	 * Spoèítání vektorového souèinu v0 a v1 a pøiètená jeho výsledku do result.
	 * @param v0 pole velikosti 3
	 * @param v1 pole velikosti 3
	 * @param result pole velikosti 3
	 */
	private static void crossAdd(float[] v0, float[] v1, float[] result)
	{
		result[0] += (v0[1] * v1[2]) - (v0[2] * v1[1]);
		result[1] += (v0[2] * v1[0]) - (v0[0] * v1[2]);
		result[2] += (v0[0] * v1[1]) - (v0[1] * v1[0]);
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù pomocí evulátorù.
	 * Je nutné pøed použitím zavolat gl.glEnable(gl.GL_MAP2_VERTEX_3).
	 * @param points pole bodù v seøazená po øádcích a v poøadí x,y,z, musí obsahovat 4 body
	 * @param offset offset v poli points
	 * @param rowStride poèet bodù v øádku
	 */
	public static void drawSurfaceViaEvulatorsVertex3(GL2 gl, float[] points, int offset, int rowStride)
	{
		drawSurfaceViaEvulatorsVertex3(gl, points, offset, rowStride, GL2.GL_FILL);
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù pomocí evulátorù.
	 * Je nutné pøed použitím zavolat gl.glEnable(gl.GL_MAP2_VERTEX_3).
	 * @param points pole bodù v seøazená po øádcích a v poøadí x,y,z, musí obsahovat 4 body
	 * @param offset offset v poli points
	 * @param rowStride poèet bodù v øádku
	 * @param mode režim vykreslení, jedna z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL
	 */
	public static void drawSurfaceViaEvulatorsVertex3(GL2 gl, float[] points, int offset, int rowStride, int mode)
	{
		gl.glMap2f(gl.GL_MAP2_VERTEX_3,
		  0, 1, 3, 2,
		  0, 1, 3 * rowStride, 2,
		  points, offset);
		gl.glMapGrid2f(
		  1, 0, 1,
		  1, 0, 1);
		gl.glEvalMesh2(mode,
		  0, 1,
		  0, 1);
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù pomocí evulátorù.
	 * Je nutné pøed použitím zavolat gl.glEnable(gl.GL_MAP2_VERTEX_4).
	 * @param points pole bodù v seøazená po øádcích a v poøadí x,y,z,w, musí obsahovat 4 body
	 * @param offset offset v poli points zadaný
	 * @param rowStride poèet bodù v øádku
	 */
	public static void drawSurfaceViaEvulatorsVertex4(GL2 gl, float[] points, int offset, int rowStride)
	{
		drawSurfaceViaEvulatorsVertex4(gl, points, offset, rowStride, GL2.GL_FILL);
	}

	/**
	 * Vykreslí plochu zadanou møížkou bodù pomocí evulátorù.
	 * Je nutné pøed použitím zavolat gl.glEnable(gl.GL_MAP2_VERTEX_4).
	 * @param points pole bodù v seøazená po øádcích a v poøadí x,y,z,w, musí obsahovat 4 body
	 * @param offset offset v poli points zadaný
	 * @param rowStride poèet bodù v øádku
	 * @param mode režim vykreslení, jedna z konstant: GL2.GL_POINT, GL2.GL_LINE, GL2.GL_FILL
	 */
	public static void drawSurfaceViaEvulatorsVertex4(GL2 gl, float[] points, int offset, int rowStride, int mode)
	{
		gl.glMap2f(gl.GL_MAP2_VERTEX_4,
		  0, 1, 4, 2,
		  0, 1, 4 * rowStride, 2,
		  points, offset);
		gl.glMapGrid2f(
		  1, 0, 1,
		  1, 0, 1);
		gl.glEvalMesh2(mode,
		  0, 1,
		  0, 1);
	}

	/**
	 * Vykreslí møížku zadanou body.
	 * @param points kolekce bodù v seøazená po øádcích
	 * @param width poèet bodù v øádku
	 * @param height poèet øádkù
	 */
	public static void drawGrid(GL2 gl, List<? extends IPoint3f> points, int width, int height)
	{
		IPoint3f p;
		/*
		 * èára ve sloupcích
		 */
		for (int col = 0; col < width; col++)
		{
			gl.glBegin(gl.GL_LINE_STRIP);
			for (int row = 0; row < height; row++)
			{
				p = points.get(row * width + col);
				gl.glVertex3f(p.getX(), p.getY(), p.getZ());
			}
			gl.glEnd();
		}
		/*
		 * èára v øádcích
		 */
		for (int row = 0; row < height; row++)
		{
			gl.glBegin(gl.GL_LINE_STRIP);
			for (int col = 0; col < width; col++)
			{
				p = points.get(row * width + col);
				gl.glVertex3f(p.getX(), p.getY(), p.getZ());
			}
			gl.glEnd();
		}
	}

	/**
	 * Uložení bodù v listu do jednorozmìrného pole floatù, s ukládáním souøadnic x,y,z za sebe.
	 * @return pole o velikosti list.size()*3
	 */
	public static float[] listOfPoints3fToArray(List<? extends IPoint3f> list)
	{
		float[] ret = new float[list.size() * 3];
		int i = 0;
		for (IPoint3f p : list)
		{
			ret[i] = p.getX();
			ret[i + 1] = p.getY();
			ret[i + 2] = p.getZ();
			i += 3;
		}
		return ret;
	}

	/**
	 * Uložení bodù v listu do jednorozmìrného pole floatù, s ukládáním souøadnic x,y,z,w za sebe.
	 * @return pole o velikosti list.size()*4
	 */
	public static float[] listOfPoints4fToArray(List<? extends IPoint4f> list)
	{
		float[] ret = new float[list.size() * 4];
		int i = 0;
		for (IPoint4f p : list)
		{
			ret[i] = p.getX();
			ret[i + 1] = p.getY();
			ret[i + 2] = p.getZ();
			ret[i + 3] = p.getW();
			i += 4;
		}
		return ret;
	}

	/**
	 * Pøevod bodu ze souøadnic v obrazovce na souøadnice v dokumentu, vèetnì nastavení vyhledávací oblasti od pozice.
	 * Funkce musí být volána v bloku pro vykreslení vieportu, pro který se má pøevod provést.
	 * @param radius vzdálenost od souøadnic [x,y] pro kterou se nalezne nejbližší souøadnice z
	 */
	public static IPoint3f gluUnProjectWithRadius(GL2 gl, GLU glu, double x, double y, int radius)
	{
		IPoint3f point = gluUnProject(gl, glu, x, y);//první spoètu pøesnì pro støed radiusu
		if (gluProject(gl, glu, point).getZ() != 1f)//pokud je nìco na dané pozici souøadnice z
		{
			return point;
		}
		//budu hledat v okolí takovou vzdálenost, pro kterou není souøadnice z ve view rovna 1(zadní stìna)
		for (float xIndex = (float) x - radius; xIndex < (float) x + radius; xIndex += 1)
		{
			for (float yIndex = (float) y - radius; yIndex < (float) y + radius; yIndex += 1)
			{
				point = gluUnProject(gl, glu, xIndex, yIndex);
				if (gluProject(gl, glu, point).getZ() != 1f)//pokud je nìco na dané pozici souøadnice z
				{
					return point;
				}
			}
		}
		return point;
	}

	/**
	 * Pøevod bodu ze souøadnic v obrazovce na souøadnice v dokumentu.
	 * Funkce musí být volána v bloku pro vykreslení vieportu, pro který se má pøevod provést.
	 */
	public static IPoint3f gluUnProject(GL2 gl, GLU glu, double x, double y)
	{
		FloatBuffer z = FloatBuffer.allocate(1);
		gl.glReadPixels((int) x, (int) y, 1, 1, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, z);
		return gluUnProject(gl, glu, x, y, z.get(0));
	}

	/**
	 * Pøevod bodu ze souøadnic v obrazovce na souøadnice v dokumentu.
	 * Funkce musí být volána v bloku pro vykreslení vieportu, pro který se má pøevod provést.
	 */
	public static IPoint3f gluUnProject(GL2 gl, GLU glu, double x, double y, double z)
	{
		//aktuální matice viewportu
		int[] viewPortMatrix = new int[4];
		gl.glGetIntegerv(gl.GL_VIEWPORT, viewPortMatrix, 0);

		//aktuální matice modelu
		double[] viewModelMatrix = new double[16];
		gl.glGetDoublev(gl.GL_MODELVIEW_MATRIX, viewModelMatrix, 0);

		//aktuální matice projekce
		double[] viewProjectionMatrix = new double[16];
		gl.glGetDoublev(gl.GL_PROJECTION_MATRIX, viewProjectionMatrix, 0);

		//pøevedené souøadnice
		double[] point = new double[3];

		glu.gluUnProject(x, y, z, viewModelMatrix, 0, viewProjectionMatrix, 0, viewPortMatrix, 0, point, 0);

		return new Point3f((float) point[0], (float) point[1], (float) point[2]);
	}

	/**
	 * Pøevod bodu ze souøadnic dokumentu na bod souøadnic na obrazovce.
	 * Funkce musí být volána v bloku pro vykreslení vieportu, pro který se má pøevod provést.
	 * @param p bod v dokumentu
	 */
	public static IPoint3f gluProject(GL2 gl, GLU glu, IPoint3f p)
	{
		//aktuální matice viewportu
		int[] viewPortMatrix = new int[4];
		gl.glGetIntegerv(gl.GL_VIEWPORT, viewPortMatrix, 0);

		//aktuální matice modelu
		double[] viewModelMatrix = new double[16];
		gl.glGetDoublev(gl.GL_MODELVIEW_MATRIX, viewModelMatrix, 0);

		//aktuální matice projekce
		double[] viewProjectionMatrix = new double[16];
		gl.glGetDoublev(gl.GL_PROJECTION_MATRIX, viewProjectionMatrix, 0);

		//pøevedené souøadnice
		double[] point = new double[3];

		glu.gluProject(p.getX(), p.getY(), p.getZ(),
		  viewModelMatrix, 0, viewProjectionMatrix, 0, viewPortMatrix, 0, point, 0);

		return new Point3f((float) point[0], (float) point[1], (float) point[2]);
	}

	/**
	 * Tøída vlákna pro výpoèet normál plochy zadané sítí bodù.
	 */
	private static class NormalCounter implements Runnable
	{

		private final float[][] normals;
		private final int startIndex;
		private final int endIndex;
		private final List<? extends IPoint3f> points;
		private final int rowStride;

		/**
		 * @param normals pole normál jež se bude vypoèítávat, pole musí být již inicializavané
		 * @param startIndex index první vypoèítané normály
		 * @param endIndex index poslední vypoèítané normály
		 * @param points pole bodù seøazené po øádcích
		 * @param rowStride poèet bodù v øádku
		 */
		public NormalCounter(float[][] normals, int startIndex, int endIndex, List<? extends IPoint3f> points, int rowStride)
		{
			this.normals = normals;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.points = points;
			this.rowStride = rowStride;
		}

		@Override
		public void run()
		{
			float n[];//výsledná normála
			float[] v0, v1, v2, v3, v4, v5;//smìrové vektory z aktuálno bodu do okolních
			IPoint3f p;//bod pro ktery se normala urèuje
			//projdu indexy
			int row, col,
			  rowEnd = points.size() / rowStride;
			for (int index = startIndex; index <= endIndex; index++)
			{
				row = index / rowStride;
				col = index % rowStride;

				p = points.get(row * rowStride + col);
				n = normals[row * rowStride + col];
				//získání smìrových vektorù z bodu do okolních 6 bodù
				//0,1,2
				if (col > 0)
				{
					try
					{
						v0 = PointOperations.normalizeVector3(PointOperations.directionVector(p, points.get((row) * rowStride + (col - 1))));
					} catch (IllegalArgumentException ex)
					{
						v0 = new float[3];
					}
					if (row + 1 < rowEnd)
					{
						try
						{
							v1 = PointOperations.normalizeVector3(PointOperations.directionVector(p, points.get((row + 1) * rowStride + (col - 1))));
						} catch (IllegalArgumentException ex)
						{
							v1 = new float[3];
						}
					} else
					{
						v1 = new float[3];
					}
				} else
				{
					v0 = new float[3];
					v1 = new float[3];
				}
				//2
				if (row + 1 < rowEnd)
				{
					try
					{
						v2 = PointOperations.normalizeVector3(PointOperations.directionVector(p, points.get((row + 1) * rowStride + (col))));
					} catch (IllegalArgumentException ex)
					{
						v2 = new float[3];
					}
				} else
				{
					v2 = new float[3];
				}
				//3,4
				if (col + 1 < rowStride)
				{
					try
					{
						v3 = PointOperations.normalizeVector3(PointOperations.directionVector(p, points.get((row) * rowStride + (col + 1))));
					} catch (IllegalArgumentException ex)
					{
						v3 = new float[3];
					}
					if (row > 0)
					{
						try
						{
							v4 = PointOperations.normalizeVector3(PointOperations.directionVector(p, points.get((row - 1) * rowStride + (col + 1))));
						} catch (IllegalArgumentException ex)
						{
							v4 = new float[3];
						}
					} else
					{
						v4 = new float[3];
						v5 = new float[3];
					}
				} else
				{
					v3 = new float[3];
					v4 = new float[3];
				}
				//5
				if (row > 0)
				{
					try
					{
						v5 = PointOperations.normalizeVector3(PointOperations.directionVector(p, points.get((row - 1) * rowStride + (col))));
					} catch (IllegalArgumentException ex)
					{
						v5 = new float[3];
					}
				} else
				{
					v5 = new float[3];
				}

				//mám smìrové vektory => spoètu normály zjištìných ploch
				crossAdd(v5, v4, n);
				crossAdd(v4, v3, n);
				crossAdd(v3, v2, n);
				crossAdd(v2, v1, n);
				crossAdd(v1, v0, n);
				crossAdd(v0, v5, n);
			}
		}
	}
}
