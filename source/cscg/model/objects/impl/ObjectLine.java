package cscg.model.objects.impl;

import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import javax.media.opengl.GL2;

/**
 * Základní testovací objekt vykreslují lomenou èáru.
 * @author Tomáš Režnar
 */
public class ObjectLine extends AbstractObjectCurve<Point3f>
{

	/**
	 * Poèítadlo instancí.
	 */
	private volatile static int selfCounter = 0;

	public ObjectLine()
	{
		super();
		setName("Úseèky " + selfCounter++);
		setState(ObjectState.OK);
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		setState(ObjectState.OK);//oznamim zmìnu objektu
		if (points.size() > 1)
		{
			gl.glLineWidth(lineWidth);
			gl.glBegin(gl.GL_LINE_STRIP);
			gl.glColor3fv(color.getColorComponents(null), 0);
			for (IPoint3f p : points)
			{
				gl.glVertex3f(p.getX(), p.getY(), p.getZ());
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
