package napier.pedigree.model.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import model.shared.MultiComparator;

public class TableMultiComparator extends MultiComparator<TableColumn> {

	/**
	 * MultiComparators work on a set row, but by extending the class we can set
	 * each comparator to be performed on the objects in a different row.
	 * Thus we could sort a table by say the objects in row 1, if they are the same, then by
	 * the objects in row 3 and so on
	 * NB The aim of this of course is to eventually sort the columns
	 */
	protected TableModel tableModel;
	
	/**
	 * Empty constructor. Can fill layer with setComparatorList / setRowsOrColumnsList method.
	 */
	public TableMultiComparator () { super (); }
	
	public TableMultiComparator (final List<Comparator<Object>> comparatorList,
			final List<Integer> rowList) {
		this (null, comparatorList, rowList);
	}
	
	
	public TableMultiComparator (final TableModel tModel, 
			final List<Comparator<Object>> comparatorList,
			final List<Integer> rowList) {
		super (comparatorList);		
		
		final List<Comparator<Object>> vRowCompList = new ArrayList<Comparator<Object>> ();
		for (int listIndex = 0; listIndex < Math.min (comparatorList.size(), rowList.size()); listIndex++) {
			vRowCompList.add (new VariableRowComparator<Object> (comparatorList.get(listIndex), rowList.get(listIndex).intValue()));
		}
		setTableModel (tModel);
		this.comparatorList = vRowCompList;
	}
	
	public TableMultiComparator (final TableMultiComparator multiComp) {
		super (multiComp);
		setTableModel (multiComp.getTableModel());
	}
	
	
	public final TableModel getTableModel() {
		return tableModel;
	}

	public final void setTableModel (final TableModel newTableModel) {
		tableModel = newTableModel;
		for (int listIndex = 0; listIndex < comparatorList.size(); listIndex++) {
			final Comparator<Object> comp = comparatorList.get (listIndex);
			if (comp instanceof VariableRowComparator<?>) {
				((VariableRowComparator<?>)comp).setTableModel (newTableModel);
			}
		}
	}

	
	@Override
	public int compare (final TableColumn tc1, final TableColumn tc2) {
		
		// Whack the null check here and the Comparators in the comparatorList
		// don't need to keep re-checking
		if (tc1 == null) {
			return tc2 == null ? 0 : 1;
		}
		else if (tc2 == null) {
			return -1;
		}
		int diff = 0;
		for (int index = 0, listSize = getComparatorList().size(); index < listSize && diff == 0; index++) {
			//final VariableRowComparator<Object> comp = (VariableRowComparator<Object>)getComparatorList().get(index);
			//comp.setTableModel (tableModel);
			diff = getComparatorList().get(index).compare (tc1, tc2);
		}
		return diff;
	}
}
