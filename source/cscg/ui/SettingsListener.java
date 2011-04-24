package cscg.ui;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Posluchaè okna pro editaci nastavení¨.
 * @author Tomáš Režnar
 */
public interface SettingsListener extends EventListener
{

	/**
	 * Událost potvrzení nastavení.
	 */
	public void eventConfirmed(EventObject evt);

	/**
	 * Událost zrušení nastavení.
	 */
	public void eventCanceled(EventObject evt);
}
