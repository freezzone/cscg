package cscg.model.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Uzlový vektor ploch.
 * @author Tomáš Režnar
 */
public class SurfaceKnotVector<Parent extends INonUniformSurface> implements IKnotVector<Parent>
{

	/**
	 * Uzlový vektor.
	 */
	private final ArrayList<Float> knot = new ArrayList<Float>();
	/**
	 * Posluchaèi knot vektoru.
	 */
	private transient LinkedList<KnotListener> knotListeners;
	/**
	 * Objekt ke kterému se váže uzlový vektor.
	 */
	private final Parent parent;
	/**
	 * True pokud je uzlový vektor pro øádky, false pro sloupce.
	 */
	private boolean forRows;

	/**
	 * @param parentObject Plocha, kterou uzlový vektor definuje.
	 * @param forRows True pokud je uzlový vektor pro øádky, false pro sloupce.
	 */
	public SurfaceKnotVector(Parent parentObject, boolean forRows)
	{
		parent = parentObject;
		this.forRows = forRows;
		initListeners();
		initTransients();
		generateKnot();
	}

	/**
	 * Inicializace transientních vlastností.
	 */
	private void initTransients()
	{
		parent.addObjectListener(new ParentListener());
		parent.addDegreeListener(new ParentDegreeListener());
	}

	/**
	 * Inicializace seznamu posluchaèù.
	 */
	private void initListeners()
	{
		knotListeners = new LinkedList<KnotListener>();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		initListeners();
		in.defaultReadObject();
		initTransients();
	}

	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.defaultWriteObject();
	}

	@Override
	public synchronized Float[] getValues()
	{
		return knot.toArray(new Float[0]);
	}

	@Override
	public float[] getTransformedValues()
	{
		float[] knotArray = new float[knot.size()];
		float knotMin = knot.get(0),
		  knotMax = knot.get(knot.size() - 1);
		int i = 0;
		for (Float p : knot)
		{
			knotArray[i] = (p.floatValue() - knotMin) / (knotMax - knotMin);
			i++;
		}
		return knotArray;
	}

	@Override
	public synchronized Float getValue(int index)
	{
		return knot.get(index);
	}

	@Override
	public synchronized void setValue(int index, float value)
	{
		if (index < knot.size() && value != knot.get(index).floatValue())
		{
			//kontrola validnosti hodnoty
			checkKnotValue(index, value);
			knot.set(index, value);
			for (KnotListener l : knotListeners)
			{
				l.eventKnotChanged(this);
			}
		}
	}

	/**
	 * Vygenerování defaultního knot vektoru. POZOR-pøepíše stávající knot vektor.
	 */
	public final synchronized void generateKnot()
	{
		knot.clear();
		int order = parent.getDegree() + 1;
		int points = forRows ? parent.rows() : parent.columns();
		int i, k;
		for (i = 0; i < order; i++)
		{
			knot.add(new Float(0));
		}
		for (i = 0; i < points - order; i++)
		{
			knot.add(new Float(i + 1));
		}
		for (k = i + 1, i = 0; knot.size() < points + order; i++)
		{
			knot.add(new Float(k));
		}

		for (KnotListener l : knotListeners)
		{
			l.eventKnotChanged(this);
		}
	}

	/**
	 * Validace hodnoty prvku knot vektoru. Pokud je hodnota platná nedojde k vyhození vyjímky.
	 * @throws IllegalArgumentException Neplatná hodnota
	 * @throws IndexOutOfBoundsException Neplatný index
	 */
	private synchronized void checkKnotValue(int index, float value) throws IllegalArgumentException
	{
		Float prev = null;
		Float next = null;
		if (index != 0)//není první
		{
			prev = knot.get(index - 1);
		}
		if (index != knot.size() - 1)//není poslední
		{
			next = knot.get(index + 1);
		}
		//kontrola neklesající posloupnosti
		if ((prev != null && prev > value)
		  || (next != null && next < value))
		{
			throw new IllegalArgumentException("Uzlový vektor musí být neklesající posloupnost.");
		}
		//kontrola výskytu shodných prvkù za sebou maximálnì v poètu rovném stupni køivky
		if ((prev != null && prev == value)//pøedchozí bod je stejný
		  || (next != null && next == value)//následující bod je stejný
		  )
		{
			int countEquals = 1;//1 za samotný prvek
			int lastEqualPrev = index;//index pøedchozí nejvdálenìjšího shodného prvku
			int lastEqualNext = index;//index následujícího nejvdálenìjšího shodného prvku
			//pøedchozí stejné uzly
			if (prev != null && prev == value)
			{
				for (lastEqualPrev = index - 1; lastEqualPrev >= 0; lastEqualPrev--)
				{
					if (knot.get(lastEqualPrev) == value)
					{
						countEquals++;
						continue;
					} else
					{
						break;
					}
				}
				lastEqualPrev++;
			}
			//následující stejné uzly
			if (next != null && next == value)
			{
				for (lastEqualNext = index + 1; lastEqualNext < knot.size(); lastEqualNext++)
				{
					if (knot.get(lastEqualNext) == value)
					{
						countEquals++;
						continue;
					} else
					{
						break;
					}
				}
				lastEqualNext--;
			}

			if (countEquals > parent.getDegree() + 1)//více než povolený poèet shodných hodnot
			{
				throw new IllegalArgumentException("Uzlový vektor nesmí obsahovat více než n shodných vnitøních složek"
				  + " nebo více než n+1 shodných prvních a posledních složek, kde n je stupeò køivky.");
			}

			//n+1 shodných složek mùže být pouze u okrajù vektoru
			if (countEquals == parent.getDegree() + 1
			  && (lastEqualPrev != 0 && lastEqualNext != knot.size() - 1))
			{
				throw new IllegalArgumentException("Uzlový vektor nesmí obsahovat více než n shodných vnitøních složek"
				  + " nebo více než n+1 shodných prvních a posledních složek, kde n je stupeò køivky.");
			}
		}
	}

	@Override
	public synchronized void addKnotListener(KnotListener knotListener)
	{
		knotListeners.add(knotListener);
	}

	@Override
	public synchronized void removeKnotListener(KnotListener knotListener)
	{
		knotListeners.remove(knotListener);
	}

	@Override
	public int length()
	{
		return knot.size();
	}

	@Override
	public Parent getParent()
	{
		return parent;
	}

	/**
	 * Sledování zmìn bodù v objektu.
	 */
	private class ParentListener extends ObjectAdapater
	{

		@Override
		public void eventPointAdded(IObject o, IPoint3f p, int index)
		{
			generateKnot();
		}

		@Override
		public void eventPointRemoved(IObject o, IPoint3f p, int index)
		{
			generateKnot();
		}

		@Override
		public void eventSizeChanged(IObject o)
		{
			generateKnot();
		}
	}

	/**
	 * Sledování zmìn stupnì objektu.
	 */
	private class ParentDegreeListener implements DegreeListener
	{

		@Override
		public void degreeChanged(IDegree source)
		{
			generateKnot();
		}
	}
}
