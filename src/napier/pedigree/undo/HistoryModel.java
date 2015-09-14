package napier.pedigree.undo;

import napier.pedigree.undo.impl.TreeOfListsModel;



public interface HistoryModel<T> {

	TreeOfListsModel<Memento<T>> getHistoryModelTree ();
	
	void addHistoryState (final T dataSource);
	
	void setOriginator (final T dataSource);
	
	void restoreHistoryState (final Memento<T> historyState);
}
