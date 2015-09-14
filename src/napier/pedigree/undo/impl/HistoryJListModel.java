package napier.pedigree.undo.impl;

import javax.swing.AbstractListModel;
import napier.pedigree.undo.Memento;

public class HistoryJListModel<T> extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5617884226752192395L;
	private java.util.List<Memento<T>> activeList;

	public void modelUpdated (final int index0, final int index1) {
		fireContentsChanged (this, index0, index1);
	}
	
	public void setList (final java.util.List<Memento<T>> histList) {
		activeList = histList;
	}
	
	public Object getElementAt (final int index) {
		//System.out.println ("index: "+index+", size1: "+getSize()+", size2: "+activeList.size());
		return (/*historyModel != null &&*/ index >= 0 && index < getSize()) 
								? activeList.get(index) : null;
	}

	public int getSize() {
		return (/*historyModel != null && */activeList == null ? 0 : activeList.size());
	}		
}
