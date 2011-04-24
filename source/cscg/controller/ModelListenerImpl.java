package cscg.controller;

import cscg.model.Model;
import cscg.model.ModelAdapter;
import cscg.model.Project;
import cscg.ui.View;

/**
 * Implementace poslucha�e modelu {@link Model}.
 * Poslucha� sleduje zm�ny pracovn�ho projektu, a p�i zm�n� registruje aktu�ln�mu pracovn�mu projektu
 * poslucha�e {@link ProjectListenerImpl}.
 * @author Tom� Re�nar
 */
public class ModelListenerImpl extends ModelAdapter {

	private Model model;
	private View view;
	/**
	 * Poslucha� aktu�ln� pracovn�ho projektu
	 */
	private final ProjectListenerImpl workingProjectListener;

	public ModelListenerImpl(Model model, View view)
	{
		this.model = model;
		this.view = view;
		workingProjectListener=new ProjectListenerImpl(model, view);
		eventSetWorkingProject(null, model.getWorkingProject());
	}

	@Override
	public final void eventSetWorkingProject(Project previous,Project current)
	{
		if(previous!=null)
		{
			previous.removeProjectListener(workingProjectListener);
		}
		if(current!=null)
		{
			current.addProjectListener(workingProjectListener);
			workingProjectListener.eventSelectedObjectChanged(current.getSelectedObject(),
			  current.getSelectedObjectIndex());
		}
		else
		{
			workingProjectListener.eventSelectedObjectChanged(null, -1);
		}
	}


}
