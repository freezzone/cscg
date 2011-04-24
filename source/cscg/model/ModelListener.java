package cscg.model;

/**
 * Rozhraní posluchaèù modelu.
 * @author Tomáš Režnar
 */
public interface ModelListener
{

	/**
	 * Událost pøidání nového projektu.
	 * @param project Nový projekt
	 */
	void eventProjectAdd(Project project);

	/**
	 * Událost odebrání projektu.
	 * @param project Odebíraný projekt.
	 * @param index Index projektu v listu projektù.
	 */
	void eventProjectRemove(Project project, int index);

	/**
	 * Událost nastavení pracovního projektu.
	 * @param previous Pøedchozí pracovní projekt.
	 * @param current Nový pracovní projekt.
	 */
	void eventSetWorkingProject(Project previous, Project current);

	/**
	 * Událost poslání debug zprávy.
	 */
	void eventDebugPrintLn(String text);
}
