package cscg.model.objects;

import javax.swing.SpinnerNumberModel;

/**
 * Spinner model, jež vytvoří sequenci celých čísel od n po m s krokem o, a model nepřijme žádné čísla z mezer intervalů.
 * @author Tomáš Režnar
 */
public class EnumNumberSpinnerModel extends SpinnerNumberModel
{

	/**
	 * první číslo.
	 */
	int firstNumber;
	/**
	 * Aktuální číslo.
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
	 * Spinner model, jež vytvoří sequenci celých čísel od n po m s krokem o, 
	 * a model nepřijme žádné čísla z mezer intervalů.
	 * @param firstNumber První číslo sequence.
	 * @param current Defaultně zvolené číslo.
	 * @param step Krok (může být i záporný).
	 * @param limit Omezení konce sequence. Pokud je nastavena,
	 * pak poslední prvek sequence bude <= limitu pokud je krok kladný,
	 * nebo poslední prvek prvek sequence bude >= limitu když je krok záporný,
	 * nebo když je limit null, pak není sequence omezena.
	 * @throws IllegalArgumentException Když se argument nenachází v sequenci.
	 */
	public EnumNumberSpinnerModel(int firstNumber, int current, int step, Integer limit) throws IllegalArgumentException
	{
		this.firstNumber = firstNumber;
		this.current = firstNumber;
		this.step = step;
		this.limit = limit;
		if (checkValue(current) == false)
		{
			throw new IllegalArgumentException("Parametr current není součástí sequence.");
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
			throw new IllegalArgumentException("Parametr current není číslo.");
		}
		if (checkValue(value) == false)
		{
			throw new IllegalArgumentException("Parametr current není součástí sequence.");
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
	 * Zkontroluje zda objekt patří do sequence.
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
		//kontrola na začátek sequence
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
