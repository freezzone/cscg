package cscg.controller;

import cscg.model.Model;
import cscg.ui.SettingsFrame;
import cscg.ui.SettingsListener;
import cscg.ui.View;
import java.util.EventObject;

/**
 * Implementace posluchaèe událostí okna nastavení {@link SettingsFrame}.
 * @author Tomáš Režnar
 */
public class SettingsFrameListenerImpl implements SettingsListener {

	private Model model;
	private View view;

	public SettingsFrameListenerImpl(Model model, View view)
	{
		this.model = model;
		this.view = view;
	}

	@Override
	public void eventConfirmed(EventObject evt)
	{
		view.getSettingsFrame().setVisible(false);
		//poznámka: nastavení je pøímo aplikováno do modelu, proto není pøi potvrzení provádìt další ukládání dat
	}

	@Override
	public void eventCanceled(EventObject evt)
	{
		view.getSettingsFrame().setVisible(false);
		//obnova pøedchozího nastavení
		model.getDisplayOptions().set(view.getSettingsFrame().getSettingsObjectUnchanged());
	}

}
