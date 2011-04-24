package cscg.ui;

import java.util.LinkedList;



/**
 * Okno s filtrovan�m textov�m v�stupem pro debugging.
 * @author Tom� Re�nar
 */
public class TextOutputFrame extends javax.swing.JFrame
{
	/**
	 * Zobrazen� text.
	 */
	private String content="";

	/**
	 * Uzdr�ov�n� posledn�ch n�kolika z�zanm� pro mo�nost filtrace.
	 */
	private LinkedList<String> lastEntries=new LinkedList<String>();

	/**
	 * Po�et polo�ek ukl�dan�ch v filtru.
	 */
	static final int FILTER_SIZE=10;

	/**
	 * Pozastaven� vypisov�n� textu do GUI prvku.
	 */
	private boolean pause=false;

	/** Creates new form TextOutputFrame */
	public TextOutputFrame()
	{
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        contentTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        pauseButton = new javax.swing.JButton();
        filterBox = new javax.swing.JCheckBox();

        setMinimumSize(new java.awt.Dimension(640, 480));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(800, 600));

        contentTextArea.setColumns(20);
        contentTextArea.setEditable(false);
        contentTextArea.setRows(5);
        contentTextArea.setTabSize(4);
        jScrollPane1.setViewportView(contentTextArea);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        pauseButton.setText("Pozastavit");
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(pauseButton);

        filterBox.setSelected(true);
        filterBox.setText("filtrovat opakuj�c� se posledn� z�znam");
        jPanel1.add(filterBox);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * Pou�it� tla��tka pro pauzu.
	 * @param evt
	 */
	private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pauseButtonActionPerformed
	{//GEN-HEADEREND:event_pauseButtonActionPerformed
		synchronized(this)
		{
			if(pause)
			{
				pause=false;
				pauseButton.setText("Pozastavit");
				contentTextArea.setText(content);
			}
			else
			{
				pause=true;
				pauseButton.setText("Pokra�ovat");
			}
		}
	}//GEN-LAST:event_pauseButtonActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		java.awt.EventQueue.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				new TextOutputFrame().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea contentTextArea;
    private javax.swing.JCheckBox filterBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton pauseButton;
    // End of variables declaration//GEN-END:variables

	/**
	 * P�id� nov� ��dek na konec.
	 */
	public void addLine(String text)
	{
		synchronized(this)
		{
			if(filterBox.isSelected()==false || lastEntries.contains(text)==false)
			{
				lastEntries.addFirst(text);
				//p�i zapln�n� filtru, odstran�m nejstar�� polo�ku
				if(lastEntries.size()>FILTER_SIZE)
				{
					lastEntries.removeLast();
				}
				//p�id�n� textu
				content+="\n"+text;
				if(pause==false)
				{
					contentTextArea.setText(content);
				}
			}
		}
	}
}
