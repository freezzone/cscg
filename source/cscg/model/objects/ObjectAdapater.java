package cscg.model.objects;

/**
 * Prázdná implemetace ObjectListeneru.
 * @author Tomáš Režnar
 */
public class ObjectAdapater implements ObjectListener
{

	@Override
	public void eventNameChanged(IObject o)
	{
	}

	@Override
	public void eventPointAdded(IObject o, IPoint3f p, int index)
	{
	}

	@Override
	public void eventPointRemoved(IObject o, IPoint3f p, int index)
	{
	}

	@Override
	public void eventPointChanged(IObject o, IPoint3f p, int index)
	{
	}

	@Override
	public void eventColorChanged(IObject o)
	{
	}

	@Override
	public void eventLineWidthChanged(IObject o)
	{
	}

	@Override
	public void eventPointSelectionChanged(IObject o)
	{
	}

	@Override
	public void eventStateMessageChanged(IObject o)
	{
	}

	@Override
	public void eventSpecificPropertiesChanged(IObject o)
	{
	}

	@Override
	public void eventPointsChanged(IObject o)
	{
	}

	@Override
	public void eventSizeChanged(IObject o)
	{
	}

}
