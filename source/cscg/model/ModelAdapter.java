package cscg.model;

/**
 * Pr�zdn� implementace poslucha�� modelu.
 * @author Tom� Re�nar
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
