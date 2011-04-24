package cscg.model.objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Uzlov� vektor ploch.
 * @author Tom� Re�nar
 */
public class SurfaceKnotVector<Parent extends INonUniformSurface> implements IKnotVector<Parent>
{

	/**
	 * Uzlov� vektor.
	 */
	private final ArrayList<Float> knot = new ArrayList<Float>();
	/**
	 * Poslucha�i knot vektoru.
	 */
	private transient LinkedList<KnotListener> knotListeners;
	/**
	 * Objekt ke kter�mu se v�e uzlov� vektor.
	 */
	private final Parent parent;
	/**
	 * True pokud je uzlov� vektor pro ��dky, false pro sloupce.
	 */
	private boolean forRows;

	/**
	 * @param parentObject Plocha, kterou uzlov� vektor definuje.
	 * @param forRows True pokud je uzlov� vektor pro ��dky, false pro sloupce.
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
	 * Inicializace transientn�ch vlastnost�.
	 */
	private void initTransients()
	{
		parent.addObjectListener(new ParentListener());
		parent.addDegreeListener(new ParentDegreeListener());
	}

	/**
	 * Inicializace seznamu poslucha��.
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
	 * Vygenerov�n� defaultn�ho knot vektoru. POZOR-p�ep�e st�vaj�c� knot vektor.
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
	 * Validace hodnoty prvku knot vektoru. Pokud je hodnota platn� nedojde k vyhozen� vyj�mky.
	 * @throws IllegalArgumentException Neplatn� hodnota
	 * @throws IndexOutOfBoundsException Neplatn� index
	 */
	private synchronized void checkKnotValue(int index, float value) throws IllegalArgumentException
	{
		Float prev = null;
		Float next = null;
		if (index != 0)//nen� prvn�
		{
			prev = knot.get(index - 1);
		}
		if (index != knot.size() - 1)//nen� posledn�
		{
			next = knot.get(index + 1);
		}
		//kontrola neklesaj�c� posloupnosti
		if ((prev != null && prev > value)
		  || (next != null && next < value))
		{
			throw new IllegalArgumentException("Uzlov� vektor mus� b�t neklesaj�c� posloupnost.");
		}
		//kontrola v�skytu shodn�ch prvk� za sebou maxim�ln� v po�tu rovn�m stupni k�ivky
		if ((prev != null && prev == value)//p�edchoz� bod je stejn�
		  || (next != null && next == value)//n�sleduj�c� bod je stejn�
		  )
		{
			int countEquals = 1;//1 za samotn� prvek
			int lastEqualPrev = index;//index p�edchoz� nejvd�len�j��ho shodn�ho prvku
			int lastEqualNext = index;//index n�sleduj�c�ho nejvd�len�j��ho shodn�ho prvku
			//p�edchoz� stejn� uzly
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
			//n�sleduj�c� stejn� uzly
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

			if (countEquals > parent.getDegree() + 1)//v�ce ne� povolen� po�et shodn�ch hodnot
			{
				throw new IllegalArgumentException("Uzlov� vektor nesm� obsahovat v�ce ne� n shodn�ch vnit�n�ch slo�ek"
				  + " nebo v�ce ne� n+1 shodn�ch prvn�ch a posledn�ch slo�ek, kde n je stupe� k�ivky.");
			}

			//n+1 shodn�ch slo�ek m��e b�t pouze u okraj� vektoru
			if (countEquals == parent.getDegree() + 1
			  && (lastEqualPrev != 0 && lastEqualNext != knot.size() - 1))
			{
				throw new IllegalArgumentException("Uzlov� vektor nesm� obsahovat v�ce ne� n shodn�ch vnit�n�ch slo�ek"
				  + " nebo v�ce ne� n+1 shodn�ch prvn�ch a posledn�ch slo�ek, kde n je stupe� k�ivky.");
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
	 * Sledov�n� zm�n bod� v objektu.
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
	 * Sledov�n� zm�n stupn� objektu.
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
