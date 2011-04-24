package cscg.model;

import cscg.model.objects.IPoint3f;
import cscg.model.objects.Point3f;
import cscg.model.objects.PointOperations;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * T��da uchov�vaj�c� informace o nastaven� projekce, a obsahuj�c� metody pro nastaven� t�to projekce v OpenGL.
 * @author Tom� Re�nar
 */
public class Projection implements Serializable
{

	/**
	 * Z�kladn� vzd�lenost perspektivn� projekce od po��tku.
	 */
	private final float PERSPECTIVE_PROJECTION_DISTANCE = 450f;
	/**
	 * Vzd�lenost vykreslov�n�.
	 */
	private final float Z_FAR = 50000;
	/**
	 * Jestli je projekce perspektivn�.
	 * True-perspektivn� projekce, false-ortho projekce.
	 */
	private boolean perspective;
	/**
	 * Zorn� �hel perspektivn� projekce.
	 */
	private float viewAngle;
	/**
	 * Pozice kamery - bod.
	 */
	private float position[];
	/**
	 * Nato�en� projekce - vektor bodu position do bodu center.
	 */
	private float direction[];
	/**
	 * Sm�r kam se kamera d�v� - bodu.
	 */
	private float center[];
	/**
	 * Vektor sm��uj�c� k vrchu projekce.
	 */
	private float up[];
	/**
	 * Vektor sm��uj�c� vpravo projekce.
	 */
	private float right[];
	/**
	 * Zoom projekce.
	 */
	private volatile float zoom;
	/**
	 * Poslucha�i zm�n projekce.
	 */
	private transient List<ChangeListener> listeners = new LinkedList<ChangeListener>();

	public Projection()
	{
		reset();
		initTransients();
	}

	/**
	 * Inicializace transientn�ch vlastnost�.
	 */
	private void initTransients()
	{
		listeners = new LinkedList<ChangeListener>();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransients();
	}

	/**
	 * Sm�rov� vektor kamery.
	 */
	public synchronized Point3f getDirection()
	{
		return new Point3f(direction[0], direction[1], direction[2]);
	}

	/**
	 * Pozice kamery.
	 */
	public synchronized Point3f getPosition()
	{
		return new Point3f(position[0], position[1], position[2]);
	}

	/**
	 * Bod kam se kamera d�v�.
	 */
	public synchronized Point3f getCenter()
	{
		return new Point3f(center[0], center[1], center[2]);
	}

	/**
	 * Vektor sm�rem k vrchu projekce.
	 */
	public synchronized Point3f getUp()
	{
		return new Point3f(up[0], up[1], up[2]);
	}

	/**
	 * Vektor sm�rem vpravo projekce.
	 */
	public synchronized Point3f getRight()
	{
		return new Point3f(right[0], right[1], right[2]);
	}

	/**
	 * Zoom projekce.
	 */
	public synchronized float getZoom()
	{
		return zoom;
	}

	/**
	 * Zoom projekce.
	 * @param zoom Zoom prjekce, mus� b�t kladn� ��slo. Pokud je < 0 pak nedojde ke zm�n�.
	 */
	public synchronized void setZoom(float zoom)
	{
		if (zoom < 0)
		{
			return;
		}
		this.zoom = zoom;
		fireChange();
	}

	/**
	 * Perspektivn� projekce.
	 */
	public boolean isPerspective()
	{
		return perspective;
	}

	/**
	 * Perspektivn� projekce.
	 */
	public void setPerspective(boolean perspective)
	{
		this.perspective = perspective;
		fireChange();
	}

	/**
	 * �hel pohledu perspektivn� projekce.
	 * @return �hel ve stupn�ch.
	 */
	public float getViewAngle()
	{
		return viewAngle;
	}

	/**
	 * �hel pohledu perspektivn� projekce.
	 * @param viewAngle �hel ve stupn�ch.
	 */
	public void setViewAngle(float viewAngle)
	{
		this.viewAngle = viewAngle;
		fireChange();
	}

	/**
	 * Nastaven� po��te�n�ho nastaven�.
	 */
	public final synchronized void reset()
	{
		perspective = true;
		viewAngle = 45f;
		position = new float[]
		  {
			  0f, 0f, PERSPECTIVE_PROJECTION_DISTANCE
		  };
		direction = new float[]
		  {
			  0f, 0f, -1f
		  };
		center = new float[]
		  {
			  0f, 0f, PERSPECTIVE_PROJECTION_DISTANCE - 1f
		  };
		up = new float[]
		  {
			  0f, 1f, 0f
		  };
		right = new float[]
		  {
			  1f, 0f, 0f
		  };
		zoom = 1;
		fireChange();
	}

	/**
	 * P�id�n� poslucha�e zm�n nastaven�.
	 */
	public synchronized void addListener(ChangeListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e zm�n nastaven�.
	 */
	public synchronized void removeListener(ChangeListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Rotov�n� kamery vertik�ln�m sm�rem - kolem osy x.
	 * @param angle �hel zm�ny ve stupn�ch.
	 */
	public synchronized void rotateVertical(double angle)
	{
		rotate(getRight(), angle);
	}

	/**
	 * Rotov�n� kamery vertik�ln�m sm�rem - kolem osy x.
	 * @param angle �hel zm�ny ve stupn�ch.
	 * @param fixedPoint Bod, kter� bude zafixov�n, tak aby se na obrazovce pohnul minim�ln�.
	 */
	public synchronized void rotateVertical(double angle, IPoint3f fixedPoint)
	{
		rotate(getRight(), angle, fixedPoint);
	}

	/**
	 * Rotov�n� kamery horizont�ln�m sm�rem - kolem osy y.
	 * @param angle �hel zm�ny ve stupn�ch.
	 */
	public synchronized void rotateHorizontal(double angle)
	{
		rotate(getUp(), angle);
	}

	/**
	 * Rotov�n� kamery horizont�ln�m sm�rem - kolem osy y.
	 * @param angle �hel zm�ny ve stupn�ch.
	 * @param fixedPoint Bod, kter� bude zafixov�n, tak aby se na obrazovce pohnul minim�ln�.
	 */
	public synchronized void rotateHorizontal(double angle, IPoint3f fixedPoint)
	{
		rotate(getUp(), angle, fixedPoint);
	}

	/**
	 * Rotov�n� kamery kolem st�edov� osy (nastaven� sm�ru kde je strop) - kolem osy z.
	 * @param angle �hel zm�ny ve stupn�ch.
	 */
	public synchronized void rotatePivot(double angle)
	{
		rotate(getDirection(), angle);
	}

	/**
	 * Rotov�n� kamery kolem st�edov� osy (nastaven� sm�ru kde je strop) - kolem osy z.
	 * @param angle �hel zm�ny ve stupn�ch.
	 * @param fixedPoint Bod, kter� bude zafixov�n, tak aby se na obrazovce pohnul minim�ln�.
	 */
	public synchronized void rotatePivot(double angle, IPoint3f fixedPoint)
	{
		rotate(getDirection(), angle, fixedPoint);
	}

	/**
	 * Rotov�n� kamery kolem os.
	 * @param verticalAngle Vertik�ln� zm�na �hlu. �hel zm�ny ve stupn�ch.
	 * @param horizontalAngle Horizont�ln� zm�na �hlu. �hel zm�ny ve stupn�ch.
	 * @param pivotAngle Zm�na sm�ru vrchu. �hel zm�ny ve stupn�ch.
	 */
	public synchronized void rotate(double verticalAngle, double horizontalAngle, double pivotAngle)
	{
		rotate(verticalAngle, horizontalAngle, pivotAngle, null);
	}

	/**
	 * Rotov�n� kamery kolem os.
	 * @param verticalAngle Vertik�ln� zm�na �hlu. �hel zm�ny ve stupn�ch.
	 * @param horizontalAngle Horizont�ln� zm�na �hlu. �hel zm�ny ve stupn�ch.
	 * @param pivotAngle Zm�na sm�ru vrchu. �hel zm�ny ve stupn�ch.
	 * @param fixedPoint Bod, kter� bude zafixov�n, tak aby se na obrazovce pohnul minim�ln�.
	 */
	public synchronized void rotate(double verticalAngle, double horizontalAngle, double pivotAngle, IPoint3f fixedPoint)
	{
		Point3f d = getDirection(),
		  u = getUp(),
		  r = getRight();
		if (verticalAngle != 0)
		{
			rotate(r, verticalAngle, fixedPoint);
		}
		if (horizontalAngle != 0)
		{
			rotate(u, horizontalAngle, fixedPoint);
		}
		if (pivotAngle != 0)
		{
			rotate(d, pivotAngle, fixedPoint);
		}
	}

	/**
	 * Rotace perspektivy dle osy.
	 * @param axisVector Vektor osy rotace, mus� b�t jednotkov�.
	 * @param angle �hel rotace ve stupn�ch.
	 * @param fixedPoint Bod, kter� bude zafixov�n, tak aby se na obrazovce pohnul minim�ln�. �hel zm�ny ve stupn�ch.
	 */
	public synchronized void rotate(IPoint3f axisVector, double angle, IPoint3f fixedPoint)
	{
		if (fixedPoint != null)
		{
			//vektor k pozici kamery
			float toPosition[] = PointOperations.directionVector(fixedPoint, getPosition());
			//oto��m vektor kamery kolem fixn�ho bodu
			toPosition = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(toPosition));
			//nov� poloha kamery
			IPoint3f pos = PointOperations.move(fixedPoint, toPosition);
			position[0] = pos.getX();
			position[1] = pos.getY();
			position[2] = pos.getZ();

			//oto�en� aktu�ln�ch vektor� ur�uj�c�ch kam se kamera d�v�
			up = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(up));
			right = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(right));
			direction = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(direction));

			//nov� center
			center[0] = position[0] + direction[0];
			center[1] = position[1] + direction[1];
			center[2] = position[2] + direction[2];

			fireChange();
		} else
		{
			rotate(axisVector, angle);
		}
	}

	/**
	 * Rotace perspektivy dle osy.
	 * @param axisVector Vektor osy rotace, mus� b�t jednotkov�.
	 * @param angle �hel rotace ve stupn�ch.
	 */
	public synchronized void rotate(IPoint3f axisVector, double angle)
	{
		direction = PointOperations.normalizeVector3(PointOperations.axisRotationVector3(axisVector, angle, getDirection()));
		up = PointOperations.normalizeVector3(PointOperations.axisRotationVector3(axisVector, angle, getUp()));
		right = PointOperations.normalizeVector3(PointOperations.axisRotationVector3(axisVector, angle, getRight()));

		//vypo��t�n� nov�ch sou�adnic
		center[0] = position[0] + direction[0];
		center[1] = position[1] + direction[1];
		center[2] = position[2] + direction[2];

		fireChange();
	}

	/**
	 * Vypo�te bod kter� se nach�z� p�esn� uprost�ed p�ed kamerou ve vzd�lenosti od pozice kamery rovn�
	 * vzd�lenosti bodu dan�ho parametrem point.
	 * @param point Bod ud�vaj�c� vzd�lenost od pozice kamery.
	 */
	public synchronized Point3f getCenterInDistance(IPoint3f point)
	{
		//vektor z pozice kamery k bodu
		float toFixedPoint[] = PointOperations.directionVector(getPosition(), point);
		//vzdalenost bodu od pozice kamery
		float distanceFixedPoint = (float) PointOperations.sizeVector3(toFixedPoint);
		//vypo�tu bod ve stejne vzdalenosti jako je zadan� bod, akorat p�esn� uprost�ed p�ed kamerou
		Point3f centerPoint = new Point3f(
		  position[0] + direction[0] * distanceFixedPoint,
		  position[1] + direction[1] * distanceFixedPoint,
		  position[2] + direction[2] * distanceFixedPoint);
		return centerPoint;
	}

	/**
	 * Vypo�te bod kter� se nach�z� p�esn� uprost�ed p�ed kamerou ve vzd�lenosti od pozice kamery.
	 * @param distance Vzd�lenost od pozice kamery.
	 */
	public synchronized Point3f getCenterInDistance(float distance)
	{
		//vypo�tu bod ve stejne vzdalenosti jako je zadan� bod, akorat p�esn� uprost�ed p�ed kamerou
		Point3f centerPoint = new Point3f(
		  position[0] + direction[0] * distance,
		  position[1] + direction[1] * distance,
		  position[2] + direction[2] * distance);
		return centerPoint;
	}

	/**
	 * Vypo�te bod kter� se nach�z� p�esn� uprost�ed p�ed kamerou v optim�ln� vzd�lenosti dle zvolen�ho nastaven�.
	 */
	public synchronized Point3f getCenterInOptimalDistance()
	{
		float distance;
		if (isPerspective())
		{
			distance = PERSPECTIVE_PROJECTION_DISTANCE / zoom;
		} else
		{
			distance = 0;
		}
		//vypo�tu bod ve stejne vzdalenosti jako je zadan� bod, akorat p�esn� uprost�ed p�ed kamerou
		Point3f centerPoint = new Point3f(
		  position[0] + direction[0] * distance,
		  position[1] + direction[1] * distance,
		  position[2] + direction[2] * distance);
		return centerPoint;
	}

	/**
	 * Nastaven� �hlu kamery tak aby se d�vala dop�edu.
	 * Krom� nastaven� �hlu, funkce taky p�esune kameru na pozici:
	 * <ul>
	 *	<li>
	 *		u perspektivn� projekce tak aby kamera zab�rala z�eteln� objekty u po��tku.
	 *	</li>
	 *	<li>
	 *		u ortho projekce se kamera p�esuna na po��tek.
	 *	</li>
	 * </ul>
	 */
	public synchronized void lookFront()
	{
		direction[0] = 0f;
		direction[1] = 0f;
		direction[2] = -1f;
		up[0] = 0f;
		up[1] = 1f;
		up[2] = 0f;
		right[0] = 1f;
		right[1] = 0f;
		right[2] = 0f;
		if (isPerspective())
		{
			moveTo(new Point3f(0, 0, PERSPECTIVE_PROJECTION_DISTANCE));
		} else
		{
			moveTo(new Point3f(0, 0, 0));
		}
	}

	/**
	 * Nastaven� �hlu kamery tak aby se d�val z boku (zprava doleva).
	 * Krom� nastaven� �hlu, funkce taky p�esune kameru na pozici:
	 * <ul>
	 *	<li>
	 *		u perspektivn� projekce tak aby kamera zab�rala z�eteln� objekty u po��tku.
	 *	</li>
	 *	<li>
	 *		u ortho projekce se kamera p�esuna na po��tek.
	 *	</li>
	 * </ul>
	 */
	public synchronized void lookProfile()
	{
		direction[0] = -1f;
		direction[1] = 0f;
		direction[2] = 0f;
		up[0] = 0f;
		up[1] = 1f;
		up[2] = 0f;
		right[0] = 0f;
		right[1] = 0f;
		right[2] = -1f;
		if (isPerspective())
		{
			moveTo(new Point3f(PERSPECTIVE_PROJECTION_DISTANCE, 0, 0));
		} else
		{
			moveTo(new Point3f(0, 0, 0));
		}
	}

	/**
	 * Nastaven� �hlu kamery tak aby se d�vala zvrchu dol�.
	 * Krom� nastaven� �hlu, funkce taky p�esune kameru na pozici:
	 * <ul>
	 *	<li>
	 *		u perspektivn� projekce tak aby kamera zab�rala z�eteln� objekty u po��tku.
	 *	</li>
	 *	<li>
	 *		u ortho projekce se kamera p�esuna na po��tek.
	 *	</li>
	 * </ul>
	 */
	public synchronized void lookTop()
	{
		direction[0] = 0f;
		direction[1] = -1f;
		direction[2] = 0f;
		up[0] = 0f;
		up[1] = 0f;
		up[2] = -1f;
		right[0] = 1f;
		right[1] = 0f;
		right[2] = 0f;
		if (isPerspective())
		{
			moveTo(new Point3f(0, PERSPECTIVE_PROJECTION_DISTANCE, 0));
		} else
		{
			moveTo(new Point3f(0, 0, 0));
		}
	}

	/**
	 * Pohyb kamery, vypo�ten� dle zadan�ch zm�n polohy my�.
	 * @param coordsFrom P�vodn� poloha my�i v prostoru (ne na obrazovce!).
	 * @param coordsTo Nov� poloha my�i v prostoru (ne na obrazovce!).
	 */
	public synchronized void move(IPoint3f coordsFrom, IPoint3f coordsTo)
	{
		try
		{
			//spo�tu pohyb
			float[] dif = PointOperations.directionVector(coordsFrom, coordsTo);
			position[0] += dif[0];
			position[1] += dif[1];
			position[2] += dif[2];
			center[0] += dif[0];
			center[1] += dif[1];
			center[2] += dif[2];
		} //byly zad�ny neplatn� argumenty
		catch (Exception ex)
		{
		}
		fireChange();
	}

	/**
	 * Pohyb kamery.
	 * @param forward Zad�n� posunu vp�ed v pixelech. Vzd�lenost bude vyd�lena zoomem.
	 * @param right  Zad�n� posunu vpravo v pixelech. Vzd�lenost bude vyd�lena zoomem.
	 * @param up Zad�n� posunu nahoru v pixelech. Vzd�lenost bude vyd�lena zoomem.
	 */
	public synchronized void move(float forward, float right, float up)
	{
		//vp�ed
		position[0] += direction[0] * (forward / zoom);
		position[1] += direction[1] * (forward / zoom);
		position[2] += direction[2] * (forward / zoom);
		center[0] += direction[0] * (forward / zoom);
		center[1] += direction[1] * (forward / zoom);
		center[2] += direction[2] * (forward / zoom);
		//vpravo
		position[0] += this.right[0] * (right / zoom);
		position[1] += this.right[1] * (right / zoom);
		position[2] += this.right[2] * (right / zoom);
		center[0] += this.right[0] * (right / zoom);
		center[1] += this.right[1] * (right / zoom);
		center[2] += this.right[2] * (right / zoom);
		//nahoru
		position[0] += this.up[0] * (up / zoom);
		position[1] += this.up[1] * (up / zoom);
		position[2] += this.up[2] * (up / zoom);
		center[0] += this.up[0] * (up / zoom);
		center[1] += this.up[1] * (up / zoom);
		center[2] += this.up[2] * (up / zoom);

		fireChange();
	}

	/**
	 * Posunut� polohy kamery o dan� vektor.
	 * @param vector Sm�r posunu, mus� b�t pole o 3 slo�k�ch.
	 */
	public synchronized void move(float[] vector)
	{
		//vp�ed
		position[0] += vector[0];
		position[1] += vector[1];
		position[2] += vector[2];
		center[0] += vector[0];
		center[1] += vector[1];
		center[2] += vector[2];

		fireChange();
	}

	/**
	 * Pohyb projekce na dan� sou�adnice.
	 * @param moveTo Bod kam projekci p�esunout.
	 */
	public synchronized void moveTo(IPoint3f moveTo)
	{
		//zm�na polohy
		position[0] = moveTo.getX();
		position[1] = moveTo.getY();
		position[2] = moveTo.getZ();

		//p�epo��t�n� st�edu
		center[0] = direction[0] + position[0];
		center[1] = direction[1] + position[1];
		center[2] = direction[2] + position[2];

		fireChange();
	}

	/**
	 * Ozn�men� zm�ny projekce.
	 */
	private synchronized void fireChange()
	{
		EventObject e = new EventObject(this);
		for (ChangeListener l : listeners)
		{
			l.changeEvent(e);
		}
	}

	/**
	 * Zavol� funkci GLU.gluLookAt() a p�ed� ji parametry z aktu�ln�ho nastaven� projekce.
	 */
	public synchronized void gluLookAt(GLU glu)
	{
		glu.gluLookAt(
		  position[0], position[1], position[2],
		  center[0], center[1], center[2],
		  up[0], up[1], up[2]);
	}

	/**
	 * Zavol� funkci GL2.glScale() a nastav� aktu�ln� zoom projekce.
	 */
	public synchronized void glScale(GL2 gl)
	{
		gl.glScalef(zoom, zoom, zoom);
	}

	/**
	 * Nastav� projekci.
	 * Aplikuje nastaven� akmery a jej� zam��en�. Po skon�en� funkce bude nastavena modelov� matice jako aktu�ln�.
	 * @param width ���ka zobrazen�ho prostoru (u perspektivn� projekce slou�� pro v�po�et pom�ru stran).
	 * @param height V��ka zobrazen�ho prostoru (u perspektivn� projekce slou�� pro v�po�et pom�ru stran).
	 */
	public synchronized void applyProjection(GL2 gl, GLU glu, double width, double height)
	{
		applyCamera(gl, glu, width, height);
		applyLookAt(gl, glu);
	}

	/**
	 * Provede nastaven� kamery.
	 * Provede kroky:<br />
	 * 1)vynuluje aktu�ln� matici<br />
	 * 2)nastav� kameru pomoc� gluPerspective nebo glOtho (dle nastaven� {@link #setPerspective(boolean)})<br />
	 * 3)nastav� zoom pomoc� {@link #glScale(javax.media.opengl.GL2)}<br />
	 * Po skon�en� funkce bude nastavena projek�n� matice jako aktu�ln�.
	 * @param width ���ka zobrazen�ho prostoru (u perspektivn� projekce slou�� pro v�po�et pom�ru stran).
	 * @param height V��ka zobrazen�ho prostoru (u perspektivn� projekce slou�� pro v�po�et pom�ru stran).
	 */
	public synchronized void applyCamera(GL2 gl, GLU glu, double width, double height)
	{
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glLoadIdentity();
		if (isPerspective())
		{
			glu.gluPerspective(getViewAngle(), width / (height == 0 ? 1 : height), 1f, Z_FAR);
			gl.glScalef(1f, 1f, 1f / zoom);
		} else
		{
			double wh = width / 2;
			double hh = height / 2;
			gl.glOrtho(-wh, wh,
			  -hh, hh,
			  -Z_FAR, Z_FAR);
			glScale(gl);
		}
	}

	/**
	 * Provede nastaven� projekce. 
	 * Provede n�sleduj�c� kroky:<br />
	 * 1)vynuluje aktu�ln� matici<br />
	 * 2)nastav� pozici sv�tla na pozici kamery<br />
	 * 3)nastav� pohled pomoc� {@link #gluLookAt(javax.media.opengl.glu.GLU)}<br />
	 * Po skon�en� funkce bude nastavena modelov� matice jako aktu�ln�.
	 */
	public synchronized void applyLookAt(GL2 gl, GLU glu)
	{
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, new float[]
		  {
			  //position[0],position[1],position[2],1}, 0);//bodov� sv�tlo
			  -direction[0], -direction[1], -direction[2], 0
		  }, 0);//vektororv� sv�tlo
		gluLookAt(glu);
	}
}
