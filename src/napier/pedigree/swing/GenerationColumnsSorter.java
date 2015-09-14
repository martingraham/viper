package napier.pedigree.swing;

import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import model.matrix.AbstractMatrixTableModel;
import model.shared.MultiComparator;


import org.resspecies.model.Individual;

import swingPlus.shared.AbstractColumnsSorter;
	
/**
 * Class that sorts columns according to either values in a particular row
 * that cuts across all columns (via sort (int viewRow)) or by a set of
 * comparators (MultiComparator) that compare properties of the objects that
 * represent each column overall (via sort (Comparator<Object> objComp))
 * @author cs22
 *
 */
public class GenerationColumnsSorter extends AbstractColumnsSorter {

	protected final ColumnsSorter2 cs2 = new ColumnsSorter2 ();
	protected MultiComparator cellComparator;
	
	public GenerationColumnsSorter (final JTable jTable) {
		super (jTable);
	}
    
    public void sort (final Comparator<Object> objComp) {
    	cs2.setComparator (objComp);
    	sort (-1, cs2);
    }
    
	public void setCellComparator (final MultiComparator cellComp) {
		cellComparator = cellComp;
	}
	
	
	@Override
	public int compare (final TableColumn tc1, final TableColumn tc2) {
		final Object obj1 = jTable.getModel().getValueAt (modelRow, tc1.getModelIndex());
		final Object obj2 = jTable.getModel().getValueAt (modelRow, tc2.getModelIndex());
		return cellComparator == null ? 0 : cellComparator.compare (obj1, obj2);
	}
	
	
	public ColumnsSorter2 getColumnObjectSorter () { return cs2; }
	
	/**
	* Wrapper class for a MultiComparator for objects
	* Converts references to TableColumn to the matching column header objects in 
	* an AbstractMatrxiTableModel object
	*/
	class ColumnsSorter2 implements Comparator<TableColumn> {

		Comparator<Object> comp;
		
		public void setComparator (final Comparator<Object> comp) {
			this.comp = comp;
		}

		@Override
		public int compare (final TableColumn tc1, final TableColumn tc2) {
			final AbstractMatrixTableModel mtm = (AbstractMatrixTableModel)jTable.getModel();
			final Individual ind1 = (Individual)mtm.getColumnObject (tc1.getModelIndex());
			final Individual ind2 = (Individual)mtm.getColumnObject (tc2.getModelIndex());
			return comp.compare (ind1, ind2);
		}
	}
}
	