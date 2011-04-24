package cscg.model.objects;

import javax.swing.SpinnerNumberModel;

/**
 * Spinner model, je vytvoøí sequenci celıch èísel od n po m s krokem o, a model nepøijme ádné èísla z mezer intervalù.
 * @author Tomáš Renar
 */
public class EnumNumberSpinnerModel extends SpinnerNumberModel
{

	/**
	 * první èíslo.
	 */
	int firstNumber;
	/**
	 * Aktuální èíslo.
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
	 * Spinner model, je vytvoøí sequenci celıch èísel od n po m s krokem o, 
	 * a model nepøijme ádné èísla z mezer intervalù.
	 * @param firstNumber První èíslo sequence.
	 * @param current Defaultnì zvolené èíslo.
	 * @param step Krok (mùe bıt i zápornı).
	 * @param limit Omezení konce sequence. Pokud je nastavena,
	 * pak poslední prvek sequence bude <= limitu pokud je krok kladnı,
	 * nebo poslední prvek prvek sequence bude >= limitu kdy je krok zápornı,
	 * nebo kdy je limit null, pak není sequence omezena.
	 * @throws IllegalArgumentException Kdy se argument nenachází v sequenci.
	 */
	public EnumNumberSpinnerModel(int firstNumber, int current, int step, Integer limit) throws IllegalArgumentException
	{
		this.firstNumber = firstNumber;
		this.current = firstNumber;
		this.step = step;
		this.limit = limit;
		if (checkValue(current) == false)
		{
			throw new IllegalArgumentException("Parametr current není souèástí sequence.");
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
			throw new IllegalArgumentException("Parametr current není èíslo.");
		}
		if (checkValue(value) == false)
		{
			throw new IllegalArgumentException("Parametr current není souèástí sequence.");
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
	 * Zkontroluje zda objekt patøí do sequence.
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
		//kontrola na zaèátek sequence
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
