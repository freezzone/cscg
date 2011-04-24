package cscg.model;

import cscg.model.objects.IObject;

/**
 * Prázdná implementare posluchaèe projektu.
 * @author Tomáš Režnar
 */
public class ProjectAdapter implements ProjectListener
{

	@Override
	public void eventFileChanged(String name, String file)
	{
	}

	@Override
	public void eventDisplayAllViewportsChanged(boolean displayAllViewports)
	{
	}

	@Override
	public void eventObjectAdded(IObject newObject, int index)
	{
	}

	@Override
	public void eventObjectRemoved(IObject oldObject, int index)
	{
	}

	@Override
	public void eventSelectedObjectChanged(IObject selectedObject, int index)
	{
	}

	@Override
	public void eventVieport1Changed(Projection p)
	{
	}

	@Override
	public void eventVieport2Changed(Projection p)
	{
	}

	@Override
	public void eventVieport3Changed(Projection p)
	{
	}

	@Override
	public void eventVieport4Changed(Projection p)
	{
	}

	@Override
	public void eventObjectsOrderChanged()
	{
	}

	@Override
	public void eventExlusiveVisibilityChanged()
	{
	}

	@Override
	public void eventEditorInEditingMode(boolean editorInEditingMode)
	{
	}

	@Override
	public void eventShowAxesChanged(boolean showAxes)
	{
	}

	@Override
	public void eventShowInformationText(boolean showInformationText)
	{
	}

	@Override
	public void eventShowOrientationIcon(boolean showOrientationIcon)
	{
	}
}
