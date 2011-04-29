package cscg.model.objects;

import java.util.List;

/**
 * Třída obsahující některé matematické funkce potřebné pro výpočet křivek.
 * @author Tomáš Režnar
 */
public class MathUtils
{

	/**
	 * Výpočet Bernsteinova polynomu. B<sub>i,n</sub>(t)
	 */
	public static double bernstein(int i, int n, double t)
	{
		double ret = 1;
		ret *= Math.pow(t, i);
		ret *= Math.pow(1 - t, n - i);
		ret *= combination(n, i);
		return ret;
	}

	/**
	 * Výpočet faktorálu.
	 * @return cislo!
	 */
	public static double factorial(int cislo)
	{
		double vysledek = 1;
		for (int i = cislo; i > 0; i--)
		{
			vysledek *= i;
		}
		return vysledek;
	}

	/**
	 * Výpočet kombinace n nad k.
	 */
	public static double combination(int n, int k)
	{
		double vysledek;
		if (n < 0 || k < 0 || n < k)
		{
			return -1;
		}
		if (k == 0)
		{
			return 1;
		}
		vysledek = factorial(n) / (factorial(k) * factorial(n - k));
		return vysledek;
	}

	/**
	 * Normalizovaná B-spline bázová funkce N<sub>i,k</sub>(t). Kvůli rychlosti výpočtu nedochází ke kontrole
	 * vstupních parametrů.
	 * @param i Parametr v intervalu <0,počet bodů-1>.
	 * @param k Řád.
	 * @param t Parametr náležící <0,1> a zároveň t<sub>i</sub> < t<sub>i+k+1</sub>.
	 * @param knot Uzlový vektor o velikosti = počet bodů+k.
	 */
	public static double bsplineBaseFunction(int i, int k, double t, float[] knot)
	{
		float ti = knot[i];
		float tnext = knot[i + 1];
		if (k == 1)
		{
			if (t >= ti && t <= tnext)
			{
				return 1;
			} else
			{
				return 0;
			}
		}

		//jmenovatel prvního zlomku
		double denominator1 = (knot[i + k - 1] - ti);
		//první zlomek
		double fraction1 = denominator1 == 0 ? 0 : ((t - ti) / denominator1) * bsplineBaseFunction(i, k - 1, t, knot);

		//jmenovatel druhého zlomku
		double denominator2 = (knot[i + k] - tnext);
		//druhý zlomek
		double fraction2 = denominator2 == 0 ? 0 : ((knot[i + k] - t) / denominator2) * bsplineBaseFunction(i + 1, k - 1, t, knot);

		return fraction1 + fraction2;
	}

	/**
	 * Racionální B-spline bázová funkce R<sub>i,k</sub>(t).
	 * @param i Parametr v intervalu <0,počet bodů-1>.
	 * @param k Řád.
	 * @param t Parametr náležící <0,1>.
	 * @param knot Uzlový vektor o velikosti = počet bodů+k.
	 * @param points Pole bodů, ze kterého se získávají váhy.
	 */
	public static double racionalBsplineBase(int i, int k, double t, float[] knot, List<IPoint4f> points)
	{
		double sum = 0;
		for (int j = 0, n = points.size() - 1; j <= n; j++)
		{
			sum += ((double) points.get(j).getW()) * bsplineBaseFunction(j, k, t, knot);
		}
		if (sum == 0.)
		{
			return 0;
		}
		return (((double) points.get(i).getW()) * bsplineBaseFunction(i, k, t, knot)) / sum;
	}
}
