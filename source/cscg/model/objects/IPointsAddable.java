package cscg.model.objects;

/**
 * Interface objekt�, kter� umo��uj� p�id�vat a mazat body.
 * @author Tom� Re�nar
 */
public interface IPointsAddable extends IObject
{

	/**
	 * P�id�n� nov�ho bodu za jin�.
	 * @param newPoint Nov� bodu pro vlo�en�.
	 * @param afterPoint Bod za kter� se m� nov� bod vlo�it. Pokud je zad�n null, pak se vlo�� na za��tek.
	 * @return Instance p�idan�ho bodu. Instance se m��e li�it od parametru newPoint.
	 */
	IPoint3f addPointAfter(IPoint3f newPoint, IPoint3f afterPoint);

	/**
	 * P�id�n� nov�ho bodu p�ed jin� bod.
	 * @param newPoint Nov� bodu pro vlo�en�.
	 * @param beforePoint Bod p�ed kter� se m� nov� bod vlo�it. Pokud je zad�n null, pak se vlo�� na konec.
	 * @return Instance p�idan�ho bodu. Instance se m��e li�it od parametru newPoint.
	 */
	IPoint3f addPointBefore(IPoint3f newPoint, IPoint3f beforePoint);

	/**
	 * Odebr�n� bodu.
	 */
	void removePoint(IPoint3f point);
}
