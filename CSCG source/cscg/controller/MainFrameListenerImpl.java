package cscg.controller;

import cscg.model.Model;
import cscg.model.Project;
import cscg.model.objects.IObject;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPointsAddable;
import cscg.ui.MainFrame;
import cscg.ui.MainFrameListener;
import cscg.ui.SettingsFrame;
import cscg.ui.View;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Implementace posluchače hlavního okna {@link MainFrame}.
 * @author Tomáš Režnar
 */
public class MainFrameListenerImpl implements MainFrameListener
{

	private Model model;
	private View view;

	public MainFrameListenerImpl(Model model, View view)
	{
		this.model = model;
		this.view = view;
	}

	@Override
	public void eventExit()
	{
		view.destroy();
		try
		{
			Model.persistInstance();
		} catch (IOException ex)
		{
			Logger.getLogger(Controller.class.getName()).log(Level.WARNING, null, ex);
		}
		System.exit(0);
	}

	@Override
	public void eventCloseProject(Project project)
	{
		model.removeProject(project);
	}

	@Override
	public void eventNewProject()
	{
		model.addProject(new Project());
	}

	@Override
	public void eventSetViewportCombination(Project project, boolean display3D)
	{
		project.setDisplayAllViewports(display3D);
	}

	@Override
	public void eventSetWorkingProject(int index)
	{
		model.setWorkingProject(index);
	}

	@Override
	public void eventSetSelectedObject(Project project, int index)
	{
		try
		{
			project.setSelectedObject(index);
		} catch (NullPointerException ex)
		{
		}//při zavření posledního projektu
	}

	@Override
	public void eventMovePointRelative(Project project, IObject object, IPoint3f point, float x, float y, float z)
	{
		object.movePointRelative(point, x, y, z);
	}

	@Override
	public void eventMovePointsRelative(Project project, IObject object, List<IPoint3f> points, float x, float y, float z)
	{
		for(IPoint3f p:points)
		{
			object.movePointRelative(p, x, y, z);
		}
	}

	@Override
	public void eventMovePointAbsolute(Project project, IObject object, IPoint3f point, float x, float y, float z)
	{
		object.movePointTo(point, x, y, z);
	}

	@Override
	public void eventDeleteSelectedPointsFromObject(Project project, IObject object)
	{
		if (object == null || (object instanceof IPointsAddable) == false)
		{
			return;
		}
		IPointsAddable oa = (IPointsAddable) object;
		for (IPoint3f p : object.getSelectedPoints())
		{
			oa.removePoint(p);
		}
		//automaticky vyberu posledni bod objektu do vyberu
		try
		{
			IPoint3f last = object.getPoint(object.getPointsCount() - 1);
			object.setSelectedPoint(last);
		} //když už neexistuje zadny bod
		catch (IndexOutOfBoundsException e)
		{
		}
	}

	@Override
	public void eventAddPoint(Project project, IObject object, IPoint3f point)
	{
		if (object == null || (object instanceof IPointsAddable) == false)
		{
			if (object != null)
			{
				object.cancelSelectedPoints();
			}
			return;
		}
		IPoint3f newPoint;
		IPointsAddable oa = (IPointsAddable) object;
		if (object.getLastSelectedPoint() == null)
		{
			newPoint = oa.addPointBefore(point, null);
		} else
		{
			newPoint = oa.addPointAfter(point, object.getLastSelectedPoint());
		}
		object.setSelectedPoint(newPoint);
	}

	@Override
	public void eventObjectRename(Project project, IObject object, String text)
	{
		try
		{
			object.setName(text);
		} //k vyjímce dojde pokud se view pokusí přejmenovat neexistující objekt (může se stát kvůli více vláknům)
		catch (Exception e)
		{
		}
	}

	@Override
	public void eventAddObject(Project project, String className)
	{
		//pokusím se objekt vytvořit
		try
		{
			IObject o = (IObject) Class.forName(className).newInstance();
			project.addObject(o);
			project.setSelectedObject(o);
			//přesun objektu na střed vieportu 1
			o.translateTo(project.getViewport1Projection().getCenterInOptimalDistance());
		} catch (Exception ex)
		{
			model.debugPrintException(ex);
		}
	}

	@Override
	public void eventMoveUpObject(Project project, IObject object)
	{
		project.moveObject(true, object);
	}

	@Override
	public void eventMoveDownObject(Project project, IObject object)
	{
		project.moveObject(false, object);
	}

	@Override
	public void eventSetObjectVisibility(Project project, IObject object, boolean visible)
	{
		object.setVisible(visible);
	}

	@Override
	public void eventSetObjectExlusiveVisibility(Project project, IObject object, boolean visible)
	{
		project.setExlusiveVisibility(visible);
	}

	@Override
	public void eventRemoveObject(Project project, IObject object)
	{
		project.removeObject(object);
	}

	@Override
	public void eventRotateObject(Project project, IObject object, IPoint3f pivot, IPoint3f axes, float angle)
	{
		object.rotate(pivot, axes, angle);
	}

	@Override
	public void eventMoveObject(Project project, IObject object, IPoint3f vector)
	{
		object.translateBy(vector);
	}

	@Override
	public void eventSetEditingMode(Project project, boolean editingMode)
	{
		project.setEditorInEditingMode(editingMode);
	}

	@Override
	public void eventExportImage(Project project, BufferedImage image)
	{
		try
		{
			File saveFile = view.askForImageExportFile();
			if (saveFile != null
			  && (saveFile.exists() == false
			  || JOptionPane.showConfirmDialog(
			  view.getMainFrame(),
			  "Soubor již existuje, chcete vybraný soubor přepsat?", "Pozor",
			  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION))
			{
				ImageIO.write(image, "PNG", saveFile);
			}
		} catch (IOException ex)
		{
			model.debugPrintException(ex);
			JOptionPane.showMessageDialog(view.getMainFrame(), "Obrázek se nepodařilo uložit.");
		} catch (Exception ex)
		{
			model.debugPrintException(ex);
			JOptionPane.showMessageDialog(view.getMainFrame(), "Při exportu došlo k chybě.");
		}
	}

	@Override
	public void eventOpenProject()
	{
		try
		{
			File file = view.askForProjectOpenFile(null);
			if (file == null) //uživatel nevybral soubor
			{
				return;
			}

			ObjectInputStream s = new ObjectInputStream(new FileInputStream(file));
			Project p = (Project) s.readObject();
			model.addProject(p);//přidání projektu
			//znovu vložení objektů, kvůli registraci posluchačů
			for (IObject o : p.getObjects())
			{
				p.removeObject(o);
				p.addObject(o);
			}
			s.close();
		} catch (ClassNotFoundException ex)
		{
			model.debugPrintException(ex);
			JOptionPane.showMessageDialog(view.getMainFrame(), "Soubor se nepodařilo otevřít.");
		} catch (IOException ex)
		{
			model.debugPrintException(ex);
			JOptionPane.showMessageDialog(view.getMainFrame(), "Soubor se nepodařilo otevřít.");
		}
	}

	@Override
	public void eventSaveProject(Project project)
	{
		try
		{
			String path = project.getFile();
			File file;
			if (path == null)
			{
				file = view.askForProjectSaveFile(null);
				if (file == null //uživatel nevybral soubor
				  || (file.exists()
				  && JOptionPane.showConfirmDialog(
				  view.getMainFrame(),
				  "Soubor již existuje, chcete vybraný soubor přepsat?", "Pozor",
				  JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION))
				{
					return;
				}
			} else
			{
				file = new File(path);
			}
			ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(file));
			s.writeObject(project);
			s.close();
			project.setFile(file.getPath());
		} catch (IOException ex)
		{
			model.debugPrintException(ex);
			JOptionPane.showMessageDialog(view.getMainFrame(), "Projekt se nepodařilo uložit.");
		}
	}

	@Override
	public void eventSaveAsProject(Project project)
	{
		try
		{
			String path = project.getFile();
			File oldFile = path == null ? null : new File(path);
			File file;
			file = view.askForProjectSaveFile(oldFile);
			if (file == null //uživatel nevybral soubor
			  || (file.exists()
			  && JOptionPane.showConfirmDialog(
			  view.getMainFrame(),
			  "Soubor již existuje, chcete vybraný soubor přepsat?", "Pozor",
			  JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION))
			{
				return;
			}
			ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(file));
			s.writeObject(project);
			s.close();
			project.setFile(file.getPath());
		} catch (IOException ex)
		{
			model.debugPrintException(ex);
			JOptionPane.showMessageDialog(view.getMainFrame(), "Projekt se nepodařilo uložit.");
		}
	}

	@Override
	public void eventShowAxes(Project project, boolean showAxes)
	{
		project.setShowAxes(showAxes);
	}

	@Override
	public void eventEditPoint(Project project, IObject object, IPoint3f oldPoint, IPoint3f newPoint)
	{
		object.editPoint(oldPoint, newPoint);
	}

	@Override
	public void eventSetSelectedPointByIndex(Project project, IObject object, int[] selectedPoints)
	{
		object.setSelectedPoints(selectedPoints);
	}

	@Override
	public void eventShowInformationText(Project project, boolean showInformationText)
	{
		project.setShowInformationText(showInformationText);
	}

	@Override
	public void eventShowOrientationIcon(Project project, boolean showOrientationIcon)
	{
		project.setShowOrientationIcon(showOrientationIcon);
	}

	@Override
	public void eventOpenNodesFrame(Project project, IObject object)
	{
		view.getNodesFrame().setVisible(true);
		view.setNodesFrameTo(object);
	}

	@Override
	public void eventOpenSettingsFrame()
	{
		SettingsFrame settingsFrame = view.getSettingsFrame();
		if (settingsFrame.isVisible() == false)//pokud je okno zavřené, nastavím mu objekt s nastavením
		{
			settingsFrame.setSettingsObject(model.getDisplayOptions());
		}
		settingsFrame.setVisible(true);
	}

	@Override
	public void eventCancelPointsSelection(Project project, IObject object)
	{
		object.cancelSelectedPoints();
	}

	@Override
	public void eventAddPointToSelection(Project project, IObject object, IPoint3f point)
	{
		object.addSelectedPoint(point);
	}

	@Override
	public void eventSetPointAsSelection(Project project, IObject object, IPoint3f point)
	{
		object.setSelectedPoint(point);
	}
}
