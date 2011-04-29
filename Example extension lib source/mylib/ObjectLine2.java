package mylib;

import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.ui.GLUtils;
import javax.media.opengl.GL2;

/**
 * Ukázková implemetace křivky s body typu MyPoint4f a omezením pozice bodů.
 * @author Tomáš Režnar
 */
public class ObjectLine2 extends AbstractObjectCurve<MyPoint4f> {
	/**
	 * Počítadlo instancí
	 */
	private static int selfCounter=1;

	public ObjectLine2()
	{
		super();
		//Vzor nově vytvářených bodů
		MyPoint4f p=new MyPoint4f();
		p.setW(0.5f);
		pointPrototype=p;
		setName("Omezené úsečky "+selfCounter++);
		setState(ObjectState.OK);
	}

	/**
	 * Omezení pozice bodu na rozmezí x od 0 do 200
	 * @param point bod jehož pozice kontroluji
	 * @return bod který leží v daném rozmezí
	 */
	private IPoint3f checkPosition(IPoint3f point)
	{
		MyPoint4f newPoint=createNewPoint();//nový bod podle vzoru
		newPoint.setBy(point);
		if(newPoint.getX()<0)
		{
			newPoint.setX(0);
		}
		if(newPoint.getX()>200)
		{
			newPoint.setX(200);
		}
		return newPoint;
	}

	@Override
	public synchronized IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint)
	{
		newPoint=checkPosition(newPoint);//kontrola polohy
		return super.addPointAfter(newPoint, afterPoint);
	}

	@Override
	public synchronized IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint)
	{
		newPoint=checkPosition(newPoint);//kontrola polohy
		return super.addPointBefore(newPoint, beforePoint);
	}

	@Override
	public synchronized void editPoint(IPoint3f editedPoint, IPoint3f setBy)
	{
		setBy=checkPosition(setBy);//kontrola polohy
		super.editPoint(editedPoint, setBy);
	}

	@Override
	public synchronized void movePointRelative(IPoint3f point, float xOffset, float yOffset, float zOffset)
	{
		super.movePointRelative(point, xOffset, yOffset, zOffset);
		editPoint(point, point);//kontrola polohy
	}

	@Override
	public synchronized void movePointTo(IPoint3f point, float x, float y, float z)
	{
		super.movePointTo(point, x, y, z);
		editPoint(point, point);//kontrola polohy
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
