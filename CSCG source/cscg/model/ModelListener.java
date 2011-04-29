package cscg.model;

/**
 * Rozhraní posluchačů modelu.
 * @author Tomáš Režnar
 */
public interface ModelListener
{

	/**
	 * Událost přidání nového projektu.
	 * @param project Nový projekt
	 */
	void eventProjectAdd(Project project);

	/**
	 * Událost odebrání projektu.
	 * @param project Odebíraný projekt.
	 * @param index Index projektu v listu projektů.
	 */
	void eventProjectRemove(Project project, int index);

	/**
	 * Událost nastavení pracovního projektu.
	 * @param previous Předchozí pracovní projekt.
	 * @param current Nový pracovní projekt.
	 */
	void eventSetWorkingProject(Project previous, Project current);

	/**
	 * Událost poslání debug zprávy.
	 */
	void eventDebugPrintLn(String text);
}
