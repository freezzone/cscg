package cscg.model.objects.impl;

import cscg.model.Projection;
import cscg.model.objects.AbstractObjectSurface;
import cscg.model.objects.EnumNumberSpinnerModel;
import cscg.model.objects.ObjectState;
import cscg.ui.GLUtils;
import javax.media.opengl.GL2;
import javax.swing.SpinnerModel;

/**
 * Z�kladn� implementace plochy.
 * @author Tom� Re�nar
 */
public class ObjectSimpleSurface extends AbstractObjectSurface {

	/**
	 * Po��tadlo instanc�.
	 */
	private volatile static int selfCounter=0;

	public ObjectSimpleSurface()
	{
		super();
		setName("Plocha "+selfCounter++);
		setState(ObjectState.notCounted);
		//z�kladn� tvar plochy
		makeSquare();
	}

	@Override
	protected synchronized SpinnerModel getWidthSpinnerModel()
	{
		return new EnumNumberSpinnerModel(2, 2, 1, 100);
	}

	@Override
	protected synchronized SpinnerModel getHeightSpinnerModel()
	{
		return new EnumNumberSpinnerModel(2, 2, 1, 100);
	}

	@Override
	protected synchronized boolean eventHandlerWidthChange(int newWidth)
	{
		setSize(newWidth,rows());
		return true;
	}

	@Override
	protected synchronized boolean eventHandlerHeightChange(int newHeight)
	{
		setSize(columns(),newHeight);
		return true;
	}

	@Override
	public synchronized void drawObject(GL2 gl)
	{
		GLUtils.glSetColor(gl, color);
		GLUtils.drawSurface(gl, points, columns(), rows(), getMode());
	}

	@Override
	public synchronized void drawNodes(GL2 gl, Projection projection)
	{
		drawSimpleGridNodes(gl);
	}

}
