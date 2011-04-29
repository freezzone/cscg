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
 * Třída uchovávající informace o nastavení projekce a obsahující metody pro nastavení této projekce v OpenGL.
 * @author Tomáš Režnar
 */
public class Projection implements Serializable
{

	/**
	 * Základní vzdálenost perspektivní projekce od počátku.
	 */
	private final float PERSPECTIVE_PROJECTION_DISTANCE = 450f;
	/**
	 * Vzdálenost vykreslování.
	 */
	private final float Z_FAR = 50000;
	/**
	 * Jestli je projekce perspektivní.
	 * True-perspektivní projekce, false-ortho projekce.
	 */
	private boolean perspective;
	/**
	 * Zorný úhel perspektivní projekce.
	 */
	private float viewAngle;
	/**
	 * Pozice kamery - bod.
	 */
	private float position[];
	/**
	 * Natočení projekce - vektor bodu position do bodu center.
	 */
	private float direction[];
	/**
	 * Směr kam se kamera dívá - bod.
	 */
	private float center[];
	/**
	 * Vektor směřující k vrchu projekce.
	 */
	private float up[];
	/**
	 * Vektor směřující vpravo projekce.
	 */
	private float right[];
	/**
	 * Zoom projekce.
	 */
	private volatile float zoom;
	/**
	 * Posluchači změn projekce.
	 */
	private transient List<ChangeListener> listeners = new LinkedList<ChangeListener>();

	public Projection()
	{
		reset();
		initTransients();
	}

	/**
	 * Inicializace transientních vlastností.
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
	 * Směrový vektor kamery.
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
	 * Bod kam se kamera dívá.
	 */
	public synchronized Point3f getCenter()
	{
		return new Point3f(center[0], center[1], center[2]);
	}

	/**
	 * Vektor směrem k vrchu projekce.
	 */
	public synchronized Point3f getUp()
	{
		return new Point3f(up[0], up[1], up[2]);
	}

	/**
	 * Vektor směrem vpravo projekce.
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
	 * @param zoom Zoom prjekce, musí být kladné číslo. Pokud je < 0 pak nedojde ke změně.
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
	 * Perspektivní projekce.
	 */
	public boolean isPerspective()
	{
		return perspective;
	}

	/**
	 * Perspektivní projekce.
	 */
	public void setPerspective(boolean perspective)
	{
		this.perspective = perspective;
		fireChange();
	}

	/**
	 * Úhel pohledu perspektivní projekce.
	 * @return Úhel ve stupních.
	 */
	public float getViewAngle()
	{
		return viewAngle;
	}

	/**
	 * Úhel pohledu perspektivní projekce.
	 * @param viewAngle Úhel ve stupních.
	 */
	public void setViewAngle(float viewAngle)
	{
		this.viewAngle = viewAngle;
		fireChange();
	}

	/**
	 * Nastavení počátečního nastavení.
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
	 * Přidání posluchače změn nastavení.
	 */
	public synchronized void addListener(ChangeListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebrání posluchače změn nastavení.
	 */
	public synchronized void removeListener(ChangeListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Rotování kamery vertikálním směrem - kolem osy x.
	 * @param angle Úhel změny ve stupních.
	 */
	public synchronized void rotateVertical(double angle)
	{
		rotate(getRight(), angle);
	}

	/**
	 * Rotování kamery vertikálním směrem - kolem osy x.
	 * @param angle Úhel změny ve stupních.
	 * @param fixedPoint Bod, který bude zafixován, tak aby se na obrazovce pohnul minimálně.
	 */
	public synchronized void rotateVertical(double angle, IPoint3f fixedPoint)
	{
		rotate(getRight(), angle, fixedPoint);
	}

	/**
	 * Rotování kamery horizontálním směrem - kolem osy y.
	 * @param angle Úhel změny ve stupních.
	 */
	public synchronized void rotateHorizontal(double angle)
	{
		rotate(getUp(), angle);
	}

	/**
	 * Rotování kamery horizontálním směrem - kolem osy y.
	 * @param angle Úhel změny ve stupních.
	 * @param fixedPoint Bod, který bude zafixován, tak aby se na obrazovce pohnul minimálně.
	 */
	public synchronized void rotateHorizontal(double angle, IPoint3f fixedPoint)
	{
		rotate(getUp(), angle, fixedPoint);
	}

	/**
	 * Rotování kamery kolem středové osy (nastavení směru kde je strop) - kolem osy z.
	 * @param angle Úhel změny ve stupních.
	 */
	public synchronized void rotatePivot(double angle)
	{
		rotate(getDirection(), angle);
	}

	/**
	 * Rotování kamery kolem středové osy (nastavení směru kde je strop) - kolem osy z.
	 * @param angle Úhel změny ve stupních.
	 * @param fixedPoint Bod, který bude zafixován, tak aby se na obrazovce pohnul minimálně.
	 */
	public synchronized void rotatePivot(double angle, IPoint3f fixedPoint)
	{
		rotate(getDirection(), angle, fixedPoint);
	}

	/**
	 * Rotování kamery kolem os.
	 * @param verticalAngle Vertikální změna úhlu. Úhel změny ve stupních.
	 * @param horizontalAngle Horizontální změna úhlu. Úhel změny ve stupních.
	 * @param pivotAngle Změna směru vrchu. Úhel změny ve stupních.
	 */
	public synchronized void rotate(double verticalAngle, double horizontalAngle, double pivotAngle)
	{
		rotate(verticalAngle, horizontalAngle, pivotAngle, null);
	}

	/**
	 * Rotování kamery kolem os.
	 * @param verticalAngle Vertikální změna úhlu. Úhel změny ve stupních.
	 * @param horizontalAngle Horizontální změna úhlu. Úhel změny ve stupních.
	 * @param pivotAngle Změna směru vrchu. Úhel změny ve stupních.
	 * @param fixedPoint Bod, který bude zafixován, tak aby se na obrazovce pohnul minimálně.
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
	 * @param axisVector Vektor osy rotace, musí být jednotkový.
	 * @param angle Úhel rotace ve stupních.
	 * @param fixedPoint Bod, který bude zafixován, tak aby se na obrazovce pohnul minimálně. Úhel změny ve stupních.
	 */
	public synchronized void rotate(IPoint3f axisVector, double angle, IPoint3f fixedPoint)
	{
		if (fixedPoint != null)
		{
			//vektor k pozici kamery
			float toPosition[] = PointOperations.directionVector(fixedPoint, getPosition());
			//otočím vektor kamery kolem fixního bodu
			toPosition = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(toPosition));
			//nová poloha kamery
			IPoint3f pos = PointOperations.move(fixedPoint, toPosition);
			position[0] = pos.getX();
			position[1] = pos.getY();
			position[2] = pos.getZ();

			//otočení aktuálních vektorú určujících kam se kamera dívá
			up = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(up));
			right = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(right));
			direction = PointOperations.axisRotationVector3(axisVector, angle, new Point3f(direction));

			//nový center
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
	 * @param axisVector Vektor osy rotace, musí být jednotkový.
	 * @param angle Úhel rotace ve stupních.
	 */
	public synchronized void rotate(IPoint3f axisVector, double angle)
	{
		direction = PointOperations.normalizeVector3(PointOperations.axisRotationVector3(axisVector, angle, getDirection()));
		up = PointOperations.normalizeVector3(PointOperations.axisRotationVector3(axisVector, angle, getUp()));
		right = PointOperations.normalizeVector3(PointOperations.axisRotationVector3(axisVector, angle, getRight()));

		//vypočítání nových souřadnic
		center[0] = position[0] + direction[0];
		center[1] = position[1] + direction[1];
		center[2] = position[2] + direction[2];

		fireChange();
	}

	/**
	 * Vypočte bod který se nachází přesně uprostřed před kamerou ve vzdálenosti od pozice kamery rovné
	 * vzdálenosti bodu daného parametrem point.
	 * @param point Bod udávající vzdálenost od pozice kamery.
	 */
	public synchronized Point3f getCenterInDistance(IPoint3f point)
	{
		//vektor z pozice kamery k bodu
		float toFixedPoint[] = PointOperations.directionVector(getPosition(), point);
		//vzdalenost bodu od pozice kamery
		float distanceFixedPoint = (float) PointOperations.sizeVector3(toFixedPoint);
		//vypočtu bod ve stejne vzdalenosti jako je zadaný bod, akorat přesně uprostřed před kamerou
		Point3f centerPoint = new Point3f(
		  position[0] + direction[0] * distanceFixedPoint,
		  position[1] + direction[1] * distanceFixedPoint,
		  position[2] + direction[2] * distanceFixedPoint);
		return centerPoint;
	}

	/**
	 * Vypočte bod který se nachází přesně uprostřed před kamerou ve vzdálenosti od pozice kamery.
	 * @param distance Vzdálenost od pozice kamery.
	 */
	public synchronized Point3f getCenterInDistance(float distance)
	{
		//vypočtu bod ve stejne vzdalenosti jako je zadaný bod, akorat přesně uprostřed před kamerou
		Point3f centerPoint = new Point3f(
		  position[0] + direction[0] * distance,
		  position[1] + direction[1] * distance,
		  position[2] + direction[2] * distance);
		return centerPoint;
	}

	/**
	 * Vypočte bod který se nachází přesně uprostřed před kamerou v optimální vzdálenosti dle zvoleného nastavení.
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
		//vypočtu bod ve stejne vzdalenosti jako je zadaný bod, akorat přesně uprostřed před kamerou
		Point3f centerPoint = new Point3f(
		  position[0] + direction[0] * distance,
		  position[1] + direction[1] * distance,
		  position[2] + direction[2] * distance);
		return centerPoint;
	}

	/**
	 * Nastavení úhlu kamery tak aby se dívala dopředu.
	 * Kromě nastavení úhlu, funkce taky přesune kameru na pozici:
	 * <ul>
	 *	<li>
	 *		u perspektivní projekce tak aby kamera zabírala zřetelně objekty u počátku.
	 *	</li>
	 *	<li>
	 *		u ortho projekce se kamera přesune na počátek.
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
	 * Nastavení úhlu kamery tak aby se dívala z boku (zprava doleva).
	 * Kromě nastavení úhlu, funkce taky přesune kameru na pozici:
	 * <ul>
	 *	<li>
	 *		u perspektivní projekce tak aby kamera zabírala zřetelně objekty u počátku.
	 *	</li>
	 *	<li>
	 *		u ortho projekce se kamera přesune na počátek.
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
	 * Nastavení úhlu kamery tak aby se dívala zvrchu dolů.
	 * Kromě nastavení úhlu, funkce taky přesune kameru na pozici:
	 * <ul>
	 *	<li>
	 *		u perspektivní projekce tak aby kamera zabírala zřetelně objekty u počátku.
	 *	</li>
	 *	<li>
	 *		u ortho projekce se kamera přesune na počátek.
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
	 * Pohyb kamery, vypočtený dle zadaných změn polohy myš.
	 * @param coordsFrom Původní poloha myši v prostoru (ne na obrazovce!).
	 * @param coordsTo Nová poloha myši v prostoru (ne na obrazovce!).
	 */
	public synchronized void move(IPoint3f coordsFrom, IPoint3f coordsTo)
	{
		try
		{
			//spočtu pohyb
			float[] dif = PointOperations.directionVector(coordsFrom, coordsTo);
			position[0] += dif[0];
			position[1] += dif[1];
			position[2] += dif[2];
			center[0] += dif[0];
			center[1] += dif[1];
			center[2] += dif[2];
		} //byly zadány neplatné argumenty
		catch (Exception ex)
		{
		}
		fireChange();
	}

	/**
	 * Pohyb kamery.
	 * @param forward Zadání posunu vpřed v pixelech. Vzdálenost bude vydělena zoomem.
	 * @param right  Zadání posunu vpravo v pixelech. Vzdálenost bude vydělena zoomem.
	 * @param up Zadání posunu nahoru v pixelech. Vzdálenost bude vydělena zoomem.
	 */
	public synchronized void move(float forward, float right, float up)
	{
		//vpřed
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
	 * Posunutí polohy kamery o daný vektor.
	 * @param vector Směr posunu, musí být pole o 3 složkách.
	 */
	public synchronized void move(float[] vector)
	{
		//vpřed
		position[0] += vector[0];
		position[1] += vector[1];
		position[2] += vector[2];
		center[0] += vector[0];
		center[1] += vector[1];
		center[2] += vector[2];

		fireChange();
	}

	/**
	 * Pohyb projekce na dané souřadnice.
	 * @param moveTo Bod kam projekci přesunout.
	 */
	public synchronized void moveTo(IPoint3f moveTo)
	{
		//změna polohy
		position[0] = moveTo.getX();
		position[1] = moveTo.getY();
		position[2] = moveTo.getZ();

		//přepočítání středu
		center[0] = direction[0] + position[0];
		center[1] = direction[1] + position[1];
		center[2] = direction[2] + position[2];

		fireChange();
	}

	/**
	 * Oznámení změny projekce.
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
	 * Zavolá funkci GLU.gluLookAt() a předá ji parametry z aktuálního nastavení projekce.
	 */
	public synchronized void gluLookAt(GLU glu)
	{
		glu.gluLookAt(
		  position[0], position[1], position[2],
		  center[0], center[1], center[2],
		  up[0], up[1], up[2]);
	}

	/**
	 * Zavolá funkci GL2.glScale() a nastaví aktuální zoom projekce.
	 */
	public synchronized void glScale(GL2 gl)
	{
		gl.glScalef(zoom, zoom, zoom);
	}

	/**
	 * Nastaví projekci.
	 * Aplikuje nastavení akmery a její zamíření. Po skončení funkce bude nastavena modelová matice jako aktuální.
	 * @param width Šířka zobrazeného prostoru (u perspektivní projekce slouží pro výpočet poměru stran).
	 * @param height Výška zobrazeného prostoru (u perspektivní projekce slouží pro výpočet poměru stran).
	 */
	public synchronized void applyProjection(GL2 gl, GLU glu, double width, double height)
	{
		applyCamera(gl, glu, width, height);
		applyLookAt(gl, glu);
	}

	/**
	 * Provede nastavení kamery.
	 * Provede kroky:<br />
	 * 1)vynuluje aktuální matici<br />
	 * 2)nastaví kameru pomocí gluPerspective nebo glOrtho (dle nastavení {@link #setPerspective(boolean)})<br />
	 * 3)nastaví zoom pomocí {@link #glScale(javax.media.opengl.GL2)}<br />
	 * Po skončení funkce bude nastavena projekční matice jako aktuální.
	 * @param width Šířka zobrazeného prostoru (u perspektivní projekce slouží pro výpočet poměru stran).
	 * @param height Výška zobrazeného prostoru (u perspektivní projekce slouží pro výpočet poměru stran).
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
	 * Provede nastavení projekce. 
	 * Provede následující kroky:<br />
	 * 1)vynuluje aktuální matici<br />
	 * 2)nastaví pozici světla na pozici kamery<br />
	 * 3)nastaví pohled pomocí {@link #gluLookAt(javax.media.opengl.glu.GLU)}<br />
	 * Po skončení funkce bude nastavena modelová matice jako aktuální.
	 */
	public synchronized void applyLookAt(GL2 gl, GLU glu)
	{
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, new float[]
		  {
			  //position[0],position[1],position[2],1}, 0);//bodové světlo
			  -direction[0], -direction[1], -direction[2], 0
		  }, 0);//vektororvé světlo
		gluLookAt(glu);
	}
}
