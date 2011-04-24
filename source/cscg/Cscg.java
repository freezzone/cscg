package cscg;

import cscg.controller.Controller;
import cscg.model.Model;
import cscg.ui.View;
import java.util.Arrays;
import javax.swing.UIManager;

/**
 * Spustitelná tøída aplikace.
 * @author Tomáš Režnar
 */
public class Cscg
{
	/**
	 * @param args Paramry aplikace:<br/>
	 * <dl>
	 *	<dt>debug</dt>
	 *	<dd>Spustí aplikaci v debug režimu, v debug režimu jsou zachycovány vybrané zprávy v debug oknì.</dd>
	 * </dl>
	 */
	public static void main(String[] args)
	{
		//vzhled musí být mìnìn døíve než dojde k deserializaci modelu
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		System.out.println(Arrays.toString(args));
		Model.setDebug((args==null?false:Arrays.binarySearch(args, "debug")!=-1));
		Model model = Model.getInstance();
		View view = new View(model);
		Controller controller = new Controller(model, view);
		controller.run();
	}
}
