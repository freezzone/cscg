package cscg.model.objects;

/**
 * Rozšíření bodu o vlastnost váhy bodu.
 * @author Tomáš Režnar
 */
public interface IPoint4f extends IPoint3f
{

	/**
	 * Získání váhy bodu.
	 */
	public float getW();
}
