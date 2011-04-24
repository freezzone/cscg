package cscg;

import cscg.controller.Controller;
import cscg.model.Model;
import cscg.ui.View;
import java.util.Arrays;
import javax.swing.UIManager;

/**
 * Spustiteln� t��da aplikace.
 * @author Tom� Re�nar
 */
public class Cscg
{
	/**
	 * @param args Paramry aplikace:<br/>
	 * <dl>
	 *	<dt>debug</dt>
	 *	<dd>Spust� aplikaci v debug re�imu, v debug re�imu jsou zachycov�ny vybran� zpr�vy v debug okn�.</dd>
	 * </dl>
	 */
	public static void main(String[] args)
	{
		//vzhled mus� b�t m�n�n d��ve ne� dojde k deserializaci modelu
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
