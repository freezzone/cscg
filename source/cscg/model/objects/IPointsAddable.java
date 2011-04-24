package cscg.model.objects;

/**
 * Interface objektù, které umožòují pøidávat a mazat body.
 * @author Tomáš Režnar
 */
public interface IPointsAddable extends IObject
{

	/**
	 * Pøidání nového bodu za jiný.
	 * @param newPoint Nový bodu pro vložení.
	 * @param afterPoint Bod za který se má nový bod vložit. Pokud je zadán null, pak se vloží na zaèátek.
	 * @return Instance pøidaného bodu. Instance se mùže lišit od parametru newPoint.
	 */
	IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint);

	/**
	 * Pøidání nového bodu pøed jiný bod.
	 * @param newPoint Nový bodu pro vložení.
	 * @param beforePoint Bod pøed který se má nový bod vložit. Pokud je zadán null, pak se vloží na konec.
	 * @return Instance pøidaného bodu. Instance se mùže lišit od parametru newPoint.
	 */
	IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint);

	/**
	 * Odebrání bodu.
	 */
	void removePoint(IPoint3f point);
}
