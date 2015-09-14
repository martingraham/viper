package napier.pedigree.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

// Crashes in DefaultRowSorter.convertRowIndexToView

public class TableFilterDeleteTest {
	
	static public void main (final String[] args) {
		new TableFilterDeleteTest ();
	}
	
	public TableFilterDeleteTest () {

		JFrame jframe = new JFrame ();
		final DummyTableModel tModel = new DummyTableModel ();
		final JTable table = new JTable (tModel);
			
		final TableRowSorter<DummyTableModel> trs = new TableRowSorter<DummyTableModel> (tModel);
		final OddRowFilter orf = new OddRowFilter ();
		trs.setRowFilter (orf);
		table.setRowSorter (trs);
		
		JButton deleteButton = new JButton ("Delete Row");
		deleteButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (ActionEvent e) {
					table.clearSelection();
					tModel.deleteARow();
					trs.setRowFilter (orf);
				}		
			}
		);
		
		
		jframe.setSize (500, 500);
		jframe.getContentPane().add (new JScrollPane (table));
		jframe.getContentPane().add (deleteButton, BorderLayout.SOUTH);
		
		jframe.setVisible (true);
	}
	
	
	class OddRowFilter extends RowFilter<TableModel, Integer> {

		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int row = entry.getIdentifier().intValue();
			return (row % 2) == 0;
		}
	}
	
	
	
	class DummyTableModel extends AbstractTableModel {

		int rows = 10;
		
		@Override
		public int getRowCount() {
			return rows;
		}

		@Override
		public int getColumnCount() {
			return 10;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return Integer.valueOf (rowIndex * columnIndex);
		}
		
		
		public void deleteARow () {
			if (rows > 2) {
				this.fireTableRowsDeleted (rows - 1, rows - 1);
				rows -= 2;
			}
		}
	}
}
