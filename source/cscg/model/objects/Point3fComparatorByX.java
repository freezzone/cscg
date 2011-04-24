package cscg.model.objects;

import java.util.Comparator;

/**
 * Porovn�n� dvou bod� dle sou�adnice x.
 * @author Tom� Re�nar
 */
public class Point3fComparatorByX implements Comparator<IPoint3f>
{

	@Override
	public int compare(IPoint3f o1, IPoint3f o2)
	{
		if (o1.getX() == o2.getX())
		{
			return 0;
		}
		if (o1.getX() < o2.getX())
		{
			return -1;
		}
		return 1;
	}
}
