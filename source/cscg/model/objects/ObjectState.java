package cscg.model.objects;

import java.io.Serializable;

/**
 * Stavy ve kter�ch se m��e nach�zet objekt.
 * @author Tom� Re�nar
 */
public enum ObjectState implements Serializable {
	notCounted("Nevypo��t� objekt"),
	OK("Vypo��tan� objekt"),
	inputError("Objekt nelze vypo��tat pro vstupn� data");


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
