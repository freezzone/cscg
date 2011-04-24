package cscg.controller;

import cscg.model.Model;
import cscg.model.ModelAdapter;
import cscg.model.Project;
import cscg.ui.TextOutputFrame;
import cscg.ui.View;
import java.util.Locale;

/**
 * Hlavn� t��da kontrol�ru, star� se o vytvo�en� a registraci poslucha�� pohledu.
 * @author Tom� Re�nar
 */
public class Controller{
	private final Model model;
	private final View view;
	private final TextOutputFrame debugFrame;

	public Controller(Model model, View view)
	{
		Locale.setDefault(Locale.FRENCH);
		this.model=model;
		this.view=view;
		//p�id�n� poslucha�e pro v�pis debug informac�
		if(Model.isDebug())
		{
			debugFrame=new TextOutputFrame();
			model.addModelListener(new ModelAdapter(){

				@Override
				public void eventDebugPrintLn(String text)
				{
					debugFrame.setVisible(true);
					debugFrame.addLine("\n-----------------------------------------------------------------------\n"+text);
					System.out.println("\n-----------------------------------------------------------------------\nDebug info:\n"
						+ text);
				}

			});
		}
		else
		{
			debugFrame=null;
		}

		//poslucha� modelu
		model.addModelListener(new ModelListenerImpl(model, view));

		//p�id�n� poslucha�� oken
		view.getMainFrame().addListener(new MainFrameListenerImpl(model, view));
		view.getNodesFrame().addListener(new NodesFrameListenerImpl(model, view));
		view.getSettingsFrame().addListener(new SettingsFrameListenerImpl(model, view));
	}
	/**
	 * Spu�t�n� aplikace
	 */
	public void run() {
		view.getMainFrame().setVisible(true);
		if(model.getProjects().length==0)
		{
			model.addProject(new Project());//po spu�t�n� se vytvo�� nov� �loha
		}
	}
}
