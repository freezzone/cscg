package cscg.model.objects;

import java.io.Serializable;

/**
 * Interface bodu v prostoru. Tento interface je základní pro všechny body využívané v objektech projektu.
 * @author Tomáš Režnar
 */
public interface IPoint3f extends Cloneable, Serializable
{

	Object clone() throws CloneNotSupportedException;

	/**
	 * Získání souřadnice x.
	 */
	float getX();

	/**
	 * Získání souřadnice y.
	 */
	float getY();

	/**
	 * Získání souřadnice z.
	 */
	float getZ();
}
