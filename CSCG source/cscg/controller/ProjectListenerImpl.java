package cscg.controller;

import cscg.model.Model;
import cscg.model.Project;
import cscg.model.ProjectAdapter;
import cscg.model.objects.IObject;
import cscg.ui.NodesFrame;
import cscg.ui.View;

/**
 * Implmentace posluchače událostí v projektu {@link Project}.
 * Posluchač sleduje změnu vybraného objektu {@link IObject} a aktuálně vybraný objekt pošle oknu {@link NodesFrame}.
 * Tento posluchač musí být nastaven vždy jako posluchač pracovního projektu.
 * @author Tomáš Režnar
 */
public class ProjectListenerImpl extends ProjectAdapter {

	private Model model;
	private View view;

	public ProjectListenerImpl(Model model, View view)
	{
		this.model = model;
		this.view = view;
	}

	@Override
	public void eventSelectedObjectChanged(IObject selectedObject, int index)
	{
		if(view.getNodesFrame().isVisible())
		{
			view.setNodesFrameTo(selectedObject);
		}
	}


}
