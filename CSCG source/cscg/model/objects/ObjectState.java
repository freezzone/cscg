package cscg.model.objects;

import java.io.Serializable;

/**
 * Stavy ve kterých se může nacházet objekt.
 * @author Tomáš Režnar
 */
public enum ObjectState implements Serializable {
	notCounted("Nevypočítý objekt"),
	OK("Vypočítaný objekt"),
	inputError("Objekt nelze vypočítat pro vstupní data");


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
