package cscg.model.objects;

import java.io.Serializable;

/**
 * Interface bodu v prostoru. Tento interface je z�kladn� pro v�echny body vyu��van� v objektech projektu.
 * @author Tom� Re�nar
 */
public interface IPoint3f extends Cloneable, Serializable
{

	Object clone() throws CloneNotSupportedException;

	/**
	 * Z�sk�n� sou�adnice x.
	 */
	float getX();

	/**
	 * Z�sk�n� sou�adnice y.
	 */
	float getY();

	/**
	 * Z�sk�n� sou�adnice z.
	 */
	float getZ();
}
