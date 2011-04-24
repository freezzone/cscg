package cscg.controller;

import cscg.model.Model;
import cscg.ui.SettingsFrame;
import cscg.ui.SettingsListener;
import cscg.ui.View;
import java.util.EventObject;

/**
 * Implementace poslucha�e ud�lost� okna nastaven� {@link SettingsFrame}.
 * @author Tom� Re�nar
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
		//pozn�mka: nastaven� je p��mo aplikov�no do modelu, proto nen� p�i potvrzen� prov�d�t dal�� ukl�d�n� dat
	}

	@Override
	public void eventCanceled(EventObject evt)
	{
		view.getSettingsFrame().setVisible(false);
		//obnova p�edchoz�ho nastaven�
		model.getDisplayOptions().set(view.getSettingsFrame().getSettingsObjectUnchanged());
	}

}
