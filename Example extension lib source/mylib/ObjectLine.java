package mylib;

import cscg.model.Projection;
import cscg.model.objects.AbstractObjectCurve;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.ObjectState;
import cscg.model.objects.Point3f;
import cscg.ui.GLUtils;
import javax.media.opengl.GL2;

/**
 * Ukázková implemetace křivky - úsečky.
 * @author Tomáš Režnar
 */
public class ObjectLine extends AbstractObjectCurve<Point3f> {
	/**
	 * Počítadlo instancí
	 */
	private static int selfCounter=1;

	public ObjectLine()
	{
		super();
		setName("Úsečky "+selfCounter++);
		setState(ObjectState.OK);
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
