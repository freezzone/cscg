package cscg.model.objects;

import java.io.Serializable;

/**
 * Stavy ve kterých se mùže nacházet objekt.
 * @author Tomáš Režnar
 */
public enum ObjectState implements Serializable {
	notCounted("Nevypoèítý objekt"),
	OK("Vypoèítaný objekt"),
	inputError("Objekt nelze vypoèítat pro vstupní data");


	private String text;

	ObjectState(String text)
	{
		this.text=text;
	}
	
	@Override
	public String toString()
	{
		return getText();
	}

	/**
	 * @return text stavu
	 */
	public String getText()
	{
		return text;
	}


}
