package cscg.ui;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Poslucha� okna pro editaci nastaven�.
 * @author Tom� Re�nar
 */
public interface SettingsListener extends EventListener
{

	/**
	 * Ud�lost potvrzen� nastaven�.
	 */
	public void eventConfirmed(EventObject evt);

	/**
	 * Ud�lost zru�en� nastaven�.
	 */
	public void eventCanceled(EventObject evt);
}
