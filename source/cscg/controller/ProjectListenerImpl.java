package cscg.controller;

import cscg.model.Model;
import cscg.model.Project;
import cscg.model.ProjectAdapter;
import cscg.model.objects.IObject;
import cscg.ui.NodesFrame;
import cscg.ui.View;

/**
 * Implmentace posluchaèe událostí v projektu {@link Project}.
 * Posluchaè sleduje zmìnu vybraného objektu {@link IObject} a aktiálnì vybraný objekt pošle oknu {@link NodesFrame}.
 * Tento posluchaè musí být nastaven vždy jako posluchaè pracovního projektu.
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
