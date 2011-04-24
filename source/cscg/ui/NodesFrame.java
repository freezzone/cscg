package cscg.ui;

import cscg.model.objects.IKnotVector;
import cscg.model.objects.IObject;
import cscg.model.objects.INonUniformCurve;
import cscg.model.objects.INonUniformSurface;
import cscg.model.objects.ISurface;
import cscg.model.objects.ObjectAdapater;
import cscg.model.objects.IPoint3f;
import cscg.model.objects.IPoint4f;
import cscg.model.objects.Point3f;
import cscg.model.objects.Point4f;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * Okno pro zobrazen� a �pravu edita�n�ch uzl� objekt�.
 * @author Tom� Re�nar
 */
public class NodesFrame extends JFrame
{

	/**
	 * Poslucha� ud�lost� editovan�ho objektu.
	 */
	private final ObjectListener objectListener;
	/**
	 * Vn�j�� panel uzlov�ho vektoru k�ivky.
	 */
	private JPanel curveKnotOutPanel;
	/**
	 * Panel s uzlov�m vektorem k�ivky.
	 */
	private JScrollPane curveKnotPanel;
	/**
	 * Tabulka s uzlov�m vektorem k�ivky.
	 */
	private JTable curveKnotTable;
	/**
	 * Vn�j�� panel ��d�c�ch bod�.
	 */
	private JPanel pointsOutPanel;
	/**
	 * Panel ��d�c�ch bod�.
	 */
	private JScrollPane pointsPanel;
	/**
	 * Tabulka ��d�c�ch bod�.
	 */
	private JTable pointsTable;
	/**
	 * Editovan� objekt.
	 */
	private IObject object = null;
	/**
	 * Poslucha�i ud�lost� okna.
	 */
	private LinkedList<NodesFrameListener> listeners = new LinkedList<NodesFrameListener>();
	/**
	 * Tabulkov� model ��d�c�ch bod�.
	 */
	private PointsTableModel pointsModel;
	/**
	 * Poslucha� v�b�r� ��d�c�ch bod�.
	 */
	private PointsTableSelectionListener pointsTableSelectionListener;
	/**
	 * Tabulkov� model uzlov�ho vektoru k�ivky.
	 */
	private KnotTableModel curveKnotModel;
	/**
	 * Poslucha� uzlov�ho vektoru k�ivky.
	 */
	private final CurveKnotListener curveKnotListener;
	/**
	 * Vn�j�� panel uzlov�ho vektoru ��dk� plochy.
	 */
	private JPanel rowKnotOutPanel;
	/**
	 * Tabulka s uzlov�m vektorem ��dk� plochy.
	 */
	private JTable rowKnotTable;
	/**
	 * Panel uzlov�ho vektoru ��dk� plochy.
	 */
	private JScrollPane rowKnotPanel;
	/**
	 * Vn�j�� panel uzlov�ho vektoru sloupc� plochy.
	 */
	private JPanel colKnotOutPanel;
	/**
	 * Tabulka s uzlov�m vektorem sloupc� plochy.
	 */
	private JTable colKnotTable;
	/**
	 * Panel uzlov�ho vektoru sloupc� plochy.
	 */
	private JScrollPane colKnotPanel;
	/**
	 * Poslucha� uzlov�ho vektoru ��dk� plochy.
	 */
	private final RowKnotListener rowKnotListener;
	/**
	 * Poslucha� uzlov�ho vektoru sloupc� plochy.
	 */
	private final ColKnotListener colKnotListener;
	/**
	 * Tabulkov� model uzlov�ho ��dk� plochy.
	 */
	private KnotTableModel rowKnotModel;
	/**
	 * Tabulkov� model uzlov�ho sloupc� plochy.
	 */
	private KnotTableModel colKnotModel;

	public NodesFrame()
	{
		initGUI();

		objectListener = new ObjectListener();
		curveKnotListener = new CurveKnotListener();
		rowKnotListener = new RowKnotListener();
		colKnotListener = new ColKnotListener();
	}

	/**
	 * Inicializace GUI prvk�.
	 */
	private void initGUI()
	{
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setTitle("Editace objektu");
		setResizable(true);
		setLayout(new GridBagLayout());

		//panel s knotValues vektorem k�ivky
		curveKnotOutPanel = new JPanel(new BorderLayout());
		curveKnotOutPanel.setBorder(BorderFactory.createTitledBorder("Uzlov� vektor"));
		curveKnotOutPanel.setMinimumSize(new Dimension(50, 100));
		curveKnotTable = new JTable();
		curveKnotTable.setTableHeader(null);
		curveKnotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		curveKnotTable.setFillsViewportHeight(true);
		curveKnotTable.setRowSelectionAllowed(false);
		curveKnotTable.setCellSelectionEnabled(true);
		curveKnotPanel = new JScrollPane(curveKnotTable);
		curveKnotPanel.setColumnHeader(null);
		curveKnotOutPanel.add(curveKnotPanel, BorderLayout.CENTER);

		//panel s knotValues vektorem ��dk� plochy
		rowKnotOutPanel = new JPanel(new BorderLayout());
		rowKnotOutPanel.setBorder(BorderFactory.createTitledBorder("Uzlov� vektor ��dk� plochy"));
		rowKnotOutPanel.setMinimumSize(new Dimension(50, 100));
		rowKnotTable = new JTable();
		rowKnotTable.setTableHeader(null);
		rowKnotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowKnotTable.setFillsViewportHeight(true);
		rowKnotTable.setRowSelectionAllowed(false);
		rowKnotTable.setCellSelectionEnabled(true);
		rowKnotPanel = new JScrollPane(rowKnotTable);
		rowKnotPanel.setColumnHeader(null);
		rowKnotOutPanel.add(rowKnotPanel, BorderLayout.CENTER);

		//panel s knotValues vektorem sloupc� plochy
		colKnotOutPanel = new JPanel(new BorderLayout());
		colKnotOutPanel.setBorder(BorderFactory.createTitledBorder("Uzlov� vektor sloupc� plochy"));
		colKnotOutPanel.setMinimumSize(new Dimension(50, 100));
		colKnotTable = new JTable();
		colKnotTable.setTableHeader(null);
		colKnotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		colKnotTable.setFillsViewportHeight(true);
		colKnotTable.setRowSelectionAllowed(false);
		colKnotTable.setCellSelectionEnabled(true);
		colKnotPanel = new JScrollPane(colKnotTable);
		colKnotPanel.setColumnHeader(null);
		colKnotOutPanel.add(colKnotPanel, BorderLayout.CENTER);

		//panel s body
		pointsOutPanel = new JPanel(new BorderLayout());
		pointsOutPanel.setBorder(BorderFactory.createTitledBorder("��d�c� body"));
		pointsTable = new JTable();
		pointsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pointsTable.setFillsViewportHeight(true);
		pointsTable.setColumnSelectionAllowed(false);
		pointsTableSelectionListener = new PointsTableSelectionListener();
		pointsTable.getSelectionModel().addListSelectionListener(pointsTableSelectionListener);
		pointsPanel = new JScrollPane(pointsTable);
		pointsOutPanel.add(pointsPanel);

		//panel s tla��tkem pro zav�en� okna
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton("Zav��t okno");
		closePanel.add(closeButton);

		//vlo�en� panel� do okna
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;

		gbc.gridy = 0;
		add(curveKnotOutPanel, gbc);
		gbc.gridy++;
		add(rowKnotOutPanel, gbc);
		gbc.gridy++;
		add(colKnotOutPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 1;
		add(pointsOutPanel, gbc);

		gbc.gridy++;
		gbc.weighty = 0;
		add(closePanel, gbc);


		setObject(null);//nastaven� na zobrazen� ��dn�ho objektu

		closeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				NodesFrame.this.setVisible(false);
			}
		});
	}

	/**
	 * P�id�n� poslucha�e.
	 */
	public void addListener(NodesFrameListener l)
	{
		listeners.add(l);
	}

	/**
	 * Odebr�n� poslucha�e.
	 */
	public void removeListener(NodesFrameListener l)
	{
		listeners.remove(l);
	}

	/**
	 * Nastav� okno pro zobrazen� vlastnost� objektu.
	 * @param o Objekt nebo null.
	 */
	public void setObject(IObject o)
	{
		if (object != null)//z p�vodn�ho objektu odeberu poslucha�e
		{
			object.removeObjectListener(objectListener);
			if (o instanceof INonUniformCurve)
			{
				((INonUniformCurve) o).getKnot().removeKnotListener(curveKnotListener);
			}
			if (o instanceof INonUniformSurface)
			{
				((INonUniformSurface) o).getRowKnot().removeKnotListener(rowKnotListener);
				((INonUniformSurface) o).getColumnKnot().removeKnotListener(colKnotListener);
			}
		}

		object = o;
		curveKnotTable.setEnabled(false);
		rowKnotTable.setEnabled(false);
		colKnotTable.setEnabled(false);
		pointsTable.setVisible(false);
		pointsTable.setEnabled(false);
		curveKnotOutPanel.setVisible(false);
		rowKnotOutPanel.setVisible(false);
		colKnotOutPanel.setVisible(false);
		if (o != null)
		{
			if (o instanceof INonUniformCurve)
			{
				curveKnotModel = new KnotTableModel(((INonUniformCurve) o).getKnot());
				((INonUniformCurve) o).getKnot().addKnotListener(curveKnotListener);
				curveKnotTable.setModel(curveKnotModel);
				curveKnotTable.setEnabled(true);
				curveKnotOutPanel.setVisible(true);
			}
			if (o instanceof INonUniformSurface)
			{
				//��dky
				rowKnotModel = new KnotTableModel(((INonUniformSurface) o).getRowKnot());
				((INonUniformSurface) o).getRowKnot().addKnotListener(rowKnotListener);
				rowKnotTable.setModel(rowKnotModel);
				rowKnotTable.setEnabled(true);
				rowKnotOutPanel.setVisible(true);
				//sloupce
				colKnotModel = new KnotTableModel(((INonUniformSurface) o).getColumnKnot());
				((INonUniformSurface) o).getColumnKnot().addKnotListener(colKnotListener);
				colKnotTable.setModel(colKnotModel);
				colKnotTable.setEnabled(true);
				colKnotOutPanel.setVisible(true);
			}
			pointsTable.setVisible(true);
			pointsTable.getSelectionModel().removeListSelectionListener(pointsTableSelectionListener); //proti zacyklen�
			int[] sel = object.getSelectedPointsIndexes();
			pointsModel = new PointsTableModel(o.getPoints());
			pointsTable.setModel(pointsModel);//mus� b�t a� po object.getSelectedPointsIndexes()
			pointsTable.clearSelection();
			for (int i : sel)
			{
				pointsTable.addRowSelectionInterval(i, i);
			}
			pointsTable.getSelectionModel().addListSelectionListener(pointsTableSelectionListener);
			pointsTable.setEnabled(true);
			object.addObjectListener(objectListener);
		}
	}

	/**
	 * Nastaven� bod� v tabulce bod� na "slected" dle aktu�ln�ho v�b�ru v editoru.
	 */
	private void updatePointsSelection()
	{
		pointsTable.setEnabled(false);
		pointsTable.getSelectionModel().removeListSelectionListener(pointsTableSelectionListener); //proti zacyklen�
		int[] sel = object.getSelectedPointsIndexes();
		pointsTable.clearSelection();
		for (int i : sel)
		{
			pointsTable.addRowSelectionInterval(i, i);
		}
		pointsTable.getSelectionModel().addListSelectionListener(pointsTableSelectionListener);
		pointsTable.setEnabled(true);
	}

	/**
	 * Z�sk�n� objektu je� je v okn� editov�n.
	 */
	public IObject getObject()
	{
		return object;
	}

	/**
	 * Model tabulky pro zobrazen� bod�.
	 */
	class PointsTableModel extends AbstractTableModel
	{

		/**
		 * body
		 */
		protected List<IPoint3f> points;

		public PointsTableModel(List<IPoint3f> points)
		{
			if (points == null)
			{
				throw new NullPointerException("Zadejte pole bod�.");
			}
			this.points = points;
		}

		@Override
		public String getColumnName(int column)
		{
			String name;
			switch (column)
			{
				case 0:
					name = "Po�ad�";
					break;
				case 1:
					name = "x";
					break;
				case 2:
					name = "y";
					break;
				case 3:
					name = "z";
					break;
				case 4:
					name = "w";
					break;
				default:
					name = super.getColumnName(column);
			}
			return name;
		}

		@Override
		public int getRowCount()
		{
			return points.size();
		}

		@Override
		public int getColumnCount()
		{
			if (points.isEmpty())
			{
				return 4;
			} else
			{
				if (points.get(0) instanceof IPoint4f)
				{
					return 5;
				} else
				{
					return 4;
				}
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			IPoint3f p = points.get(rowIndex);
			Object val;
			switch (columnIndex)
			{
				case 0:
					if (object instanceof ISurface)
					{
						ISurface s = (ISurface) object;
						val = (rowIndex / s.rows() + 1) + "." + (rowIndex % s.rows() + 1);
					} else
					{
						val = (rowIndex + 1);
					}
					break;
				case 1:
					val = p.getX();
					break;
				case 2:
					val = p.getY();
					break;
				case 3:
					val = p.getZ();
					break;
				case 4:
					val = ((IPoint4f) p).getW();
					break;
				default:
					throw new IndexOutOfBoundsException("Neplatn� sloupec " + columnIndex + ".");
			}
			return val;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 0 ? false : true;//nult� index je po�ad� bodu
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return columnIndex == 0 ? String.class : Float.class;//nult� index je po�ad� bod�
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if (aValue == null)
			{
				aValue = 0f;
			}
			IPoint3f newPoint;
			IPoint3f oldPoint = points.get(rowIndex);
			if (getColumnCount() == 5)//body s v�hou
			{
				newPoint = new Point4f(
				  columnIndex != 1 ? oldPoint.getX() : (Float) aValue,
				  columnIndex != 2 ? oldPoint.getY() : (Float) aValue,
				  columnIndex != 3 ? oldPoint.getZ() : (Float) aValue,
				  columnIndex != 4 ? ((IPoint4f) oldPoint).getW() : (Float) aValue);
			} else
			{
				newPoint = new Point3f(
				  columnIndex != 1 ? oldPoint.getX() : (Float) aValue,
				  columnIndex != 2 ? oldPoint.getY() : (Float) aValue,
				  columnIndex != 3 ? oldPoint.getZ() : (Float) aValue);
			}
			for (NodesFrameListener l : listeners)
			{
				l.eventPointChanged(object, oldPoint, newPoint);
			}
		}

		/**
		 * Zm�na bodu.
		 * @param index Index bodu/��dku.
		 * @param point Upraven� bod.
		 */
		protected void changePoint(int index, IPoint3f point)
		{
			points.set(index, point);
			fireTableRowsUpdated(index, index);
		}
	}

	/**
	 * Poslucha� aktu�ln�ho vybran�ho objektu.
	 */
	private class ObjectListener extends ObjectAdapater
	{

		@Override
		public void eventPointAdded(IObject o, IPoint3f p, int index)
		{
			setObject(o);
		}

		@Override
		public void eventPointChanged(IObject o, IPoint3f p, int index)
		{
			pointsModel.changePoint(index, p);
		}

		@Override
		public void eventPointRemoved(IObject o, IPoint3f p, int index)
		{
			setObject(o);
		}

		@Override
		public void eventPointSelectionChanged(IObject o)
		{
			updatePointsSelection();
		}

		@Override
		public void eventPointsChanged(IObject o)
		{
			setObject(o);
			eventPointSelectionChanged(o);//znovu vyberu body
		}

		@Override
		public void eventSizeChanged(IObject o)
		{
			setObject(o);
		}
	}

	/**
	 * Poslucha� v�b�ru bod� v tabulce bod�.
	 */
	private class PointsTableSelectionListener implements ListSelectionListener
	{

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			for (NodesFrameListener l : listeners)
			{
				l.eventPointSelectionChanged(object, pointsTable.getSelectedRows());
			}
		}
	}

	/**
	 * Tabulkov� model pro tabulku s knotValues vektory.
	 */
	private class KnotTableModel extends AbstractTableModel
	{

		/**
		 * Pole vektoru.
		 */
		Float[] knotValues;
		/**
		 * Po�et sloupc�.
		 */
		private final int cols = 15;
		/**
		 * Knot vektor.
		 */
		private final IKnotVector knot;

		private KnotTableModel(IKnotVector knot)
		{
			if (knot == null)
			{
				throw new NullPointerException();
			}
			this.knotValues = knot.getValues();
			this.knot = knot;
		}

		@Override
		public int getRowCount()
		{
			return knotValues.length / cols + (knotValues.length % cols != 0 ? 1 : 0);
		}

		@Override
		public int getColumnCount()
		{
			return cols;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			int index = rowIndex * cols + columnIndex;
			if (index < knotValues.length)
			{
				return knotValues[index];
			} else
			{
				return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			int index = rowIndex * cols + columnIndex;
			if (index < knotValues.length)
			{
				return true;
			} else
			{
				return false;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return Float.class;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			int index = rowIndex * cols + columnIndex;
			if (aValue == null)
			{
				aValue = 0f;
			}
			for (NodesFrameListener l : listeners)
			{
				l.eventChangeKnotNode(object, knot, index, (Float) aValue);
			}
		}

		/**
		 * Zm�na slo�ky vektoru.
		 * @param index Index slo�ky.
		 * @param value Upraven� slo�ka.
		 */
		protected void changeValue(int index, float value)
		{
			knotValues[index] = value;
			fireTableRowsUpdated(index / cols, index % cols);
		}
	}

	/**
	 * Poslucha� zm�n knotValues vektoru k�ivky.
	 */
	private class CurveKnotListener implements cscg.model.objects.KnotListener
	{

		@Override
		public void eventKnotChanged(IKnotVector source)
		{
			curveKnotModel = new KnotTableModel(source);
			curveKnotTable.setEnabled(false);
			curveKnotTable.setModel(curveKnotModel);
			curveKnotTable.setEnabled(true);
		}
	}

	/**
	 * Poslucha� zm�n vektoru ��dku.
	 */
	private class RowKnotListener implements cscg.model.objects.KnotListener
	{

		@Override
		public void eventKnotChanged(IKnotVector source)
		{
			rowKnotModel = new KnotTableModel(source);
			rowKnotTable.setEnabled(false);
			rowKnotTable.setModel(rowKnotModel);
			rowKnotTable.setEnabled(true);
		}
	}

	/**
	 * Poslucha� zm�n vektoru sloupce.
	 */
	private class ColKnotListener implements cscg.model.objects.KnotListener
	{

		@Override
		public void eventKnotChanged(IKnotVector source)
		{
			colKnotModel = new KnotTableModel(source);
			colKnotTable.setEnabled(false);
			colKnotTable.setModel(colKnotModel);
			colKnotTable.setEnabled(true);
		}
	}
}
