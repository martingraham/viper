package napier.pedigree.swing.renderers.history;

import java.awt.Component;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import napier.pedigree.swing.HistoryTree;
import napier.pedigree.undo.Memento;
import napier.pedigree.undo.impl.HistoryJListModel;

/**
 * Each tree cell is a list, which has its own renderer in turn
 * @author Martin
 *
 */
public class HistoryTreeCellRenderer<T> extends JList implements TreeCellRenderer {

      /**
	 * 
	 */
	private static final long serialVersionUID = 1986813703722817083L;


	/**
      * Constructor.<P>
      * @param hTree {@link HistoryTree} object to associate with the cell renderer
      */
	
	public HistoryTreeCellRenderer (final HistoryTree<T> hTree) {
	    // Don't paint behind the component
    	super (new HistoryJListModel<T> ());
        setOpaque (true);
        //setMinimumSize (new Dimension (50, 20));          
		setLayoutOrientation (JList.HORIZONTAL_WRAP);
		setVisibleRowCount (1);
		setCellRenderer (new HistoryListCellRenderer<T> (hTree));
		setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
    }

      public Component getTreeCellRendererComponent (final JTree tree, final Object value, 
    		  final boolean selected, 
    		  final boolean expanded, 
    		  final boolean leaf, 
    		  final int row, 
    		  final boolean hasFocus) {
    	  
    	 if (tree instanceof HistoryTree) {
	    	 final HistoryTree<T> hTree = (HistoryTree<T>)tree;
	    	 final HistoryJListModel<T> model = (HistoryJListModel<T>)this.getModel(); 
	         if (hTree.getHistoryModel() != null && row >= 0) {
	            final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
	            final List<Memento<T>> histList = (List<Memento<T>>) dmtn.getUserObject();
	            model.setList (histList);
	            model.modelUpdated (0, histList == null ? 0 : histList.size());
	         } else if (row < 0) {
	            model.setList (null);
	         }
    	 }
         return this;
    }
}
