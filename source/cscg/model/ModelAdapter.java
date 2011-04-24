package cscg.model;

/**
 * Prázdná implementace posluchaèù modelu.
 * @author Tomáš Režnar
 */
public class ModelAdapter implements ModelListener
{

	@Override
	public void eventProjectAdd(Project project)
	{
	}

	@Override
	public void eventProjectRemove(Project project, int index)
	{
	}

	@Override
	public void eventSetWorkingProject(Project previous, Project current)
	{
	}

	@Override
	public void eventDebugPrintLn(String text)
	{
	}
}
