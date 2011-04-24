package cscg.model;

/**
 * Rozhran� poslucha�� modelu.
 * @author Tom� Re�nar
 */
public interface ModelListener
{

	/**
	 * Ud�lost p�id�n� nov�ho projektu.
	 * @param project Nov� projekt
	 */
	void eventProjectAdd(Project project);

	/**
	 * Ud�lost odebr�n� projektu.
	 * @param project Odeb�ran� projekt.
	 * @param index Index projektu v listu projekt�.
	 */
	void eventProjectRemove(Project project, int index);

	/**
	 * Ud�lost nastaven� pracovn�ho projektu.
	 * @param previous P�edchoz� pracovn� projekt.
	 * @param current Nov� pracovn� projekt.
	 */
	void eventSetWorkingProject(Project previous, Project current);

	/**
	 * Ud�lost posl�n� debug zpr�vy.
	 */
	void eventDebugPrintLn(String text);
}
