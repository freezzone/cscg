package cscg.controller;

import cscg.model.Model;
import cscg.model.ModelAdapter;
import cscg.model.Project;
import cscg.ui.View;

/**
 * Implementace posluchaèe modelu {@link Model}.
 * Posluchaè sleduje zmìny pracovního projektu, a pøi zmìnì registruje aktuálnímu pracovnímu projektu
 * posluchaèe {@link ProjectListenerImpl}.
 * @author Tomáš Režnar
 */
public class ModelListenerImpl extends ModelAdapter {

	private Model model;
	private View view;
	/**
	 * Posluchaè aktuální pracovního projektu
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
