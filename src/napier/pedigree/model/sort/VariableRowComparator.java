package napier.pedigree.model.sort;

import java.util.Comparator;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class VariableRowComparator<T> implements Comparator<T> {
	protected final Comparator<Object> comp;
	protected final int modelRow;
	protected TableModel tableModel;
	
	VariableRowComparator (final Comparator<Object> comparator, final int row) {
		this.comp = comparator;
		this.modelRow = row;
	}
	

	public void setTableModel (final TableModel tableModel) {
		this.tableModel = tableModel;
	}
	
	public int getRow () { return modelRow; }
	
	public Comparator<Object> getComparator () { return comp; }
	
	
	@Override
	public int compare (final Object obj1, final Object obj2) {
		final Object ind1 = tableModel.getValueAt (modelRow, ((TableColumn) obj1).getModelIndex());
		final Object ind2 = tableModel.getValueAt (modelRow, ((TableColumn) obj2).getModelIndex());
		if (ind1 == ind2) {
			return 0;
		}
		if (ind1 == null) {
			return 1;
		}
		else if (ind2 == null) {
			return -1;
		}
		return comp.compare (ind1, ind2);
	}
}