package napier.pedigree.swing.renderers.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import util.colour.ColorUtilities;

import napier.pedigree.swing.HistoryTree;
import napier.pedigree.undo.Memento;
import napier.pedigree.undo.impl.TreeOfListsModel;

public class HistoryListCellRenderer<T> extends JLabel implements ListCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1986813703722817083L;
	static final private Logger LOGGER = Logger.getLogger (HistoryListCellRenderer.class);
	//private final Icon commentIcon = IconCache.makeIcon (cl, "CommentIcon");

    private static final Color TREE_HASH = new Color (192, 192, 192);
    private static final Color INTERNAL_BORDER_COLOUR = new Color (128, 128, 128, 128);
    private static final Color MOUSEOVER_BORDER_COLOUR = new Color (64, 64, 64);
	protected static final Color ITEM_BACKGROUND = new Color (192, 192, 216); //Color.lightGray;
	protected static final Color ITEM_BACKGROUND_TRANS = ColorUtilities.addAlpha (ITEM_BACKGROUND, 192);
	
	private static final Border HISTORYBUTTONBORDER = BorderFactory.createMatteBorder (1, 0, 1, 0, TREE_HASH);
	private static final Border HISTORYBUTTONACTIVEBORDER = BorderFactory.createMatteBorder (2, 1, 2, 0, MOUSEOVER_BORDER_COLOUR);
	private static final Border HISTORYBUTTONACTIVERIGHTMOSTBORDER = BorderFactory.createMatteBorder (2, 1, 2, 1, MOUSEOVER_BORDER_COLOUR);
	private static final Border HISTORYBUTTONCURRENTBORDER = BorderFactory.createBevelBorder (BevelBorder.RAISED, Color.white, Color.black);
	private static final Border INTERNALSHADINGBORDER = BorderFactory.createMatteBorder (1, 1, 1, 0, INTERNAL_BORDER_COLOUR);
	private static final Border INTERNALSHADINGBORDERRIGHTMOST = BorderFactory.createMatteBorder (1, 1, 1, 1, INTERNAL_BORDER_COLOUR);
	private static final Border CHISTORYBUTTONBORDER = BorderFactory.createCompoundBorder (HISTORYBUTTONBORDER, INTERNALSHADINGBORDER);
	private static final Border CHISTORYBUTTONBORDERRIGHTMOST = BorderFactory.createCompoundBorder (HISTORYBUTTONBORDER, INTERNALSHADINGBORDERRIGHTMOST);


	protected boolean inActivePath;
	
	protected HistoryTree<T> historyTree;

    public HistoryListCellRenderer (final HistoryTree<T> hTree) {
    	super ();
	    // Don't paint behind the component
    	setOpaque (true);
    	historyTree = hTree;
    	//setMaximumSize (new Dimension (50, 60));
    }


    public Component getListCellRendererComponent (final JList list,
       final Object value, // value to display
       final int index,    // cell index
       final boolean iss,  // is selected
       final boolean chf)  // cell has focus?
       {

       final Memento<T> historyState = (Memento<T>) value;
       final boolean mouseOver = (historyTree.getMouseOverObject() == value); 
       //this.setIcon (historyState.getComment() != null ? commentIcon : (selectIndex >= 0 ? null : navigationIcon));          
       setText (value.toString());
       //LOGGER.info ("HCellText: "+getText());
         
       final TreeOfListsModel<Memento<T>> treeModel = (TreeOfListsModel<Memento<T>>)historyTree.getModel();
       final boolean last = historyState == treeModel.getLastSelected();
       final boolean rightmost = (index == list.getModel().getSize() - 1);
       final boolean ancestor = treeModel.getPathTo (treeModel.getLastSelected()).contains (historyState);
       final boolean inactive = false;
       inActivePath = ancestor;
       setBackground (ITEM_BACKGROUND);
       setForeground (Color.black);
       setBorder (getBorder (mouseOver, ancestor, last, rightmost, inactive));         
      
       return this;
  }
    
 
  
  private Border getBorder (final boolean mouseOver, final boolean ancestor, final boolean last, final boolean rightmost, final boolean inactive) {
    return (last ? HISTORYBUTTONCURRENTBORDER : 
      	(mouseOver & !inactive ? (rightmost ? HISTORYBUTTONACTIVERIGHTMOSTBORDER : HISTORYBUTTONACTIVEBORDER) :
      	(rightmost ? CHISTORYBUTTONBORDERRIGHTMOST : CHISTORYBUTTONBORDER)));
  }
  
  
  @Override
  public void paintComponent (final Graphics gContext) {
	  super.paintComponent (gContext);
	  if (!inActivePath) {
		  gContext.setColor (ITEM_BACKGROUND_TRANS);
		  gContext.fillRect (0, 0, this.getWidth(), this.getHeight());
	  }
  }
}
