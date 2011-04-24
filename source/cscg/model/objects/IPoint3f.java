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
	 * Získání souøadnice x.
	 */
	float getX();

	/**
	 * Získání souøadnice y.
	 */
	float getY();

	/**
	 * Získání souøadnice z.
	 */
	float getZ();
}
