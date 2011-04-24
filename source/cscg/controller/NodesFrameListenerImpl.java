package cscg.controller;

import cscg.model.Model;
import cscg.model.objects.IKnotVector;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import cscg.ui.NodesFrame;
import cscg.ui.NodesFrameListener;
import cscg.ui.View;
import javax.swing.JOptionPane;

/**
 * Implementace posluchaèe událostí v oknì pro editaci bodù a uzlových vektorù {@link NodesFrame}.
 * @author Tomáš Režnar
 */
public class NodesFrameListenerImpl implements NodesFrameListener {

	private Model model;
	private View view;

	public NodesFrameListenerImpl(Model model, View view)
	{
		this.model = model;
		this.view = view;
	}

	@Override
	public void eventPointChanged(IObject object,IPoint3f oldPoint, IPoint3f newPoint)
	{
		object.editPoint(oldPoint, newPoint);
	}

	@Override
	public void eventPointSelectionChanged(IObject object,int[] selectedPoints)
	{
		object.setSelectedPoints(selectedPoints);
	}

	@Override
	public void eventChangeKnotNode(IObject object, IKnotVector knot, int index, float value)
	{
		try
		{
			knot.setValue(index, value);
		}
		catch(IllegalArgumentException ex)
		{
			JOptionPane.showMessageDialog(null, ex.getMessage());
		}
		catch(IndexOutOfBoundsException ex){}
	}
}
