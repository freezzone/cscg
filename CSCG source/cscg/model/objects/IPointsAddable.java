package cscg.model.objects;

/**
 * Interface objektů, které umožňují přidávat a mazat body.
 * @author Tomáš Režnar
 */
public interface IPointsAddable extends IObject
{

	/**
	 * Přidání nového bodu za jiný.
	 * @param newPoint Nový bodu pro vložení.
	 * @param afterPoint Bod za který se má nový bod vložit. Pokud je zadán null, pak se vloží na začátek.
	 * @return Instance přidaného bodu. Instance se může lišit od parametru newPoint.
	 */
	IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint);

	/**
	 * Přidání nového bodu před jiný bod.
	 * @param newPoint Nový bodu pro vložení.
	 * @param beforePoint Bod před který se má nový bod vložit. Pokud je zadán null, pak se vloží na konec.
	 * @return Instance přidaného bodu. Instance se může lišit od parametru newPoint.
	 */
	IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint);

	/**
	 * Odebrání bodu.
	 */
	void removePoint(IPoint3f point);
}
