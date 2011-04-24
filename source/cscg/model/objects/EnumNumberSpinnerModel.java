package cscg.model.objects;

import javax.swing.SpinnerNumberModel;

/**
 * Spinner model, je� vytvo�� sequenci cel�ch ��sel od n po m s krokem o, a model nep�ijme ��dn� ��sla z mezer interval�.
 * @author Tom� Re�nar
 */
public class EnumNumberSpinnerModel extends SpinnerNumberModel
{

	/**
	 * prvn� ��slo.
	 */
	int firstNumber;
	/**
	 * Aktu�ln� ��slo.
	 */
	int current;
	/**
	 * Krok.
	 */
	int step;
	/**
	 * Limit.
	 */
	Integer limit;

	/**
	 * Spinner model, je� vytvo�� sequenci cel�ch ��sel od n po m s krokem o, 
	 * a model nep�ijme ��dn� ��sla z mezer interval�.
	 * @param firstNumber Prvn� ��slo sequence.
	 * @param current Defaultn� zvolen� ��slo.
	 * @param step Krok (m��e b�t i z�porn�).
	 * @param limit Omezen� konce sequence. Pokud je nastavena,
	 * pak posledn� prvek sequence bude <= limitu pokud je krok kladn�,
	 * nebo posledn� prvek prvek sequence bude >= limitu kdy� je krok z�porn�,
	 * nebo kdy� je limit null, pak nen� sequence omezena.
	 * @throws IllegalArgumentException Kdy� se argument nenach�z� v sequenci.
	 */
	public EnumNumberSpinnerModel(int firstNumber, int current, int step, Integer limit) throws IllegalArgumentException
	{
		this.firstNumber = firstNumber;
		this.current = firstNumber;
		this.step = step;
		this.limit = limit;
		if (checkValue(current) == false)
		{
			throw new IllegalArgumentException("Parametr current nen� sou��st� sequence.");
		}
	}

	@Override
	public Object getValue()
	{
		return current;
	}

	@Override
	public void setValue(Object value)
	{
		if ((value instanceof Number) == false)
		{
			throw new IllegalArgumentException("Parametr current nen� ��slo.");
		}
		if (checkValue(value) == false)
		{
			throw new IllegalArgumentException("Parametr current nen� sou��st� sequence.");
		}
		current = ((Number) value).intValue();
		fireStateChanged();
	}

	@Override
	public Object getNextValue()
	{
		Integer next = current + step;
		return checkValue(next) ? next : null;
	}

	@Override
	public Object getPreviousValue()
	{
		Integer next = current - step;
		return checkValue(next) ? next : null;
	}

	/**
	 * Zkontroluje zda objekt pat�� do sequence.
	 */
	private boolean checkValue(Object check)
	{
		if((check instanceof Number)==false)
		{
			return false;
		}
		Number num=(Number)check;
		double value=num.doubleValue();
		//kontrola limitu
		if (limit != null && value > limit && step >= 0)
		{
			return false;
		}
		if (limit != null && value < limit && step <= 0)
		{
			return false;
		}
		//kontrola na za��tek sequence
		if (value < firstNumber && step > 0)
		{
			return false;
		}
		if (value > firstNumber && step < 0)
		{
			return false;
		}
		//kontrola na prvek sequence
		if ((value - firstNumber) % step != 0)
		{
			return false;
		}

		return true;
	}
}
