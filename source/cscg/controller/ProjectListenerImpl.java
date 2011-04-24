package cscg.controller;

import cscg.model.Model;
import cscg.model.Project;
import cscg.model.ProjectAdapter;
import cscg.model.objects.IObject;
import cscg.ui.NodesFrame;
import cscg.ui.View;

/**
 * Implmentace poslucha�e ud�lost� v projektu {@link Project}.
 * Poslucha� sleduje zm�nu vybran�ho objektu {@link IObject} a akti�ln� vybran� objekt po�le oknu {@link NodesFrame}.
 * Tento poslucha� mus� b�t nastaven v�dy jako poslucha� pracovn�ho projektu.
 * @author Tom� Re�nar
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
