package napier.pedigree.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import util.Messages;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.swing.renderers.history.HistoryTreeCellRenderer;
import napier.pedigree.undo.HistoryModel;
import napier.pedigree.undo.Memento;
import napier.pedigree.undo.impl.TreeOfListsModel;

public class HistoryTree<T> extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3392884591314378794L;
	private static final Logger LOGGER = Logger.getLogger (HistoryTree.class);
	private static final String UI_CLASS_ID = "IndentTreeUI";
	static {
		UIManager.put (UI_CLASS_ID, "napier.pedigree.swing.ui.IndentTreeUI");
	}
	
    protected Object mouseOverIndex = null;
    protected HistoryModel<T> historyModel;
    

	public HistoryTree() {
		super (new DefaultTreeModel (new DefaultMutableTreeNode (
			Messages.getString (PropertyConstants.TEXTPROPS, "HistoryTreeStartupText")))
		); // otherwise you get the default sports/colours swing Jtree
		extra();
	}

	protected void extra () {
		final MouseInputAdapter mia = new HistoryTreeMouseInputListener ();
		addMouseListener (mia);
	    addMouseMotionListener (mia);
	    //historyJTree.setShowsRootHandles (false);
	    setExpandsSelectedPaths (true);
	    //setUI (new IndentTreeUI());
	    getPreferredScrollableViewportSize();
	}
	
	
    @Override
    public String getUIClassID() {
        return UI_CLASS_ID;
    }
    
    
    // Sets history model, not a tree model
    public void setModel (final HistoryModel<T> newHistoryModel) {
    	if (historyModel != newHistoryModel) {
        	
    		if (historyModel != null) {
    			historyModel = null;
    			super.setModel (null);
    			setToolTipText ("no model...");
    		}

    		historyModel = newHistoryModel;
    		
    		if (historyModel != null) {
    			setModel (historyModel.getHistoryModelTree());
    			setCellRenderer (new HistoryTreeCellRenderer<T> (this));
    			expandAll ();
    	    	recenter ();
    		}	
    	}
    	else {
    		setModel (historyModel.getHistoryModelTree());
    		expandAll ();
        	recenter ();
    	}

    }
    
    public HistoryModel<T> getHistoryModel () { return historyModel; }

	
	@Override
	public void collapsePath (final TreePath path) {
		final TreeNode tNode = (TreeNode)path.getLastPathComponent();
		boolean containsCurrentPath = false;
		final Enumeration nodeEnum = tNode.children();
		while (nodeEnum.hasMoreElements() && !containsCurrentPath) {
			final TreeNode child = (TreeNode) nodeEnum.nextElement();
			final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)child;
			final List<Memento<T>> dList = (List<Memento<T>>)dmtn.getUserObject();
			if (!dList.isEmpty()) {
    			final Memento<T> him = dList.get(0);
    			final TreeOfListsModel<Memento<T>> treeModel = (TreeOfListsModel<Memento<T>>)getModel();
    			containsCurrentPath = (/*historyModel != null &&*/ treeModel.getPathTo (treeModel.getLastSelected()).contains (him));	
			}
			//System.out.println ("dlist: "+dList);
		}
		
		if (!containsCurrentPath) {
			super.collapsePath (path);
		}
	}
	
	
    protected void recenter () {
    	final TreeOfListsModel<Memento<T>> tModel = historyModel.getHistoryModelTree();
    	final Memento<T> him = tModel.getLastSelected ();
    	//((IndentTreeUI)historyJTree.getUI()).updateSize();
    	
    	if (him != null) {
    		final DefaultMutableTreeNode dmtn = tModel.find ((DefaultMutableTreeNode)tModel.getRoot(), him);
    		final TreeNode[] nodes = tModel.getPathToRoot (dmtn);
    		final TreePath tPath = new TreePath (nodes);
    		//if (!isSource) {
    			scrollPathToVisible (tPath);
    		//}
    		//isSource = false;
    	}
    	
    	repaint ();
    }
    
    
    public void expandAll () {
    	//System.out.println ("histrows: "+historyJTree.getRowCount());
    	
    	for (int i = 0; i < getRowCount(); i++) {
    		//System.out.println ("Count: "+i+" of "+historyJTree.getRowCount());
    		final TreePath tPath = getPathForRow(i);
    		final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tPath.getLastPathComponent();
    		final List<Memento<T>> dList = (List<Memento<T>>) dmtn.getUserObject();
    		if (!dList.isEmpty()) {
    			final Memento<T> him = dList.get(0);
    			final TreeOfListsModel<Memento<T>> treeModel = historyModel.getHistoryModelTree();
    			if (treeModel.getPathTo (treeModel.getLastSelected()).contains (him)) {
    				expandRow(i);
    			}
    		}	
    	}
    }


    public Object getMouseOverObject() {
        return mouseOverIndex;
    }

    protected void setMouseOverObject (final Object value) {
        mouseOverIndex = value;
    }
	
	
	class HistoryTreeMouseInputListener extends MouseInputAdapter {
		
		private static final int COMMENT_LENGTH = 10, COMMENT_EXPANDED_LENGTH = 2500;
		private MouseEvent lastMouseMovedEvent;
		
		private final javax.swing.Timer expandTooltipTimer = new Timer (1000,
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					if (getMouseOverObject() instanceof Memento) {
						final Memento<T> him = (Memento<T>)getMouseOverObject();
						//if (him.getComment() != null && him.getComment().length() >= COMMENT_LENGTH && lastMouseMovedEvent != null) {
						//	String tooltip = generateEventText (him, COMMENT_EXPANDED_LENGTH);
						//	JComponent tree = (JComponent)lastMouseMovedEvent.getComponent();
						//	tree.setToolTipText (tooltip);
						//	tree.dispatchEvent (lastMouseMovedEvent);
						//}
					}
				}
			}	
		);
		
		HistoryTreeMouseInputListener () {
			super ();
			expandTooltipTimer.setRepeats (false);
		}
        
		Memento<T> getItem (final JTree tree, final TreePath tPath, final Point point) {
			final TreeNode tNode = (tPath == null ? null : (TreeNode) tPath.getLastPathComponent());
            final Rectangle rect = tree.getPathBounds (tPath);
            Memento<T> him = null;
            
            if (tNode != null) {
            	final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tNode;
            	final Object userObj = dmtn.getUserObject();
            	if (userObj instanceof List) {
	            	final java.util.List<Memento<T>> histList = (List<Memento<T>>) dmtn.getUserObject();  		
		            
		            final int xx = point.x - rect.x;
		            final int size = histList.size();
		            final int index = xx * size / rect.width;
		            him = histList.get (index);
            	}
            }
            return him;
		}
		
        @Override
		public void mouseExited (final MouseEvent mEvent) {
            if (getMouseOverObject() != null) {
                setMouseOverObject (null);
                expandTooltipTimer.stop();
                setToolTipText (null);
                lastMouseMovedEvent = null;
                //repaint();
                UIManager.put ("ToolTip.background", UIManager.getLookAndFeelDefaults().get ("ToolTip.background")); 
            }
        }
        
        @Override
        public void mouseEntered (final MouseEvent mEvent) {
	        //LOGGER.info ("Entering History Tree");
        }

        @Override
		public void mouseMoved (final MouseEvent mEvent) {
            final JTree tree = (JTree) mEvent.getSource();
            final TreePath tPath = tree.getPathForLocation (mEvent.getX(), mEvent.getY());           
            final Memento<T> him = getItem (tree, tPath, mEvent.getPoint());
            
            if (him == null) {
            	mouseExited (mEvent);
            }
            else if (him != getMouseOverObject()) { 
            	lastMouseMovedEvent = mEvent;
            	setMouseOverObject (him);  
                ToolTipManager.sharedInstance().setInitialDelay (500);             
                expandTooltipTimer.restart();            
            }
        }
        
        @Override
        public void mousePressed (final MouseEvent mEvent) {
            final JTree tree = (JTree) mEvent.getSource();
            final TreePath tPath = tree.getPathForLocation(mEvent.getX(), mEvent.getY());           
            final Memento<T> him = getItem (tree, tPath, mEvent.getPoint());
            expandTooltipTimer.stop();
            
            if (him != null) {
	            if (mEvent.getButton() == MouseEvent.BUTTON1) {
	            	historyModel.restoreHistoryState (him);
	            }
	            //else if (mEvent.getButton() == MouseEvent.BUTTON3) {
	            	//HistoryPopupMenu.getInstance().setDetails (HistoryPanel.this, him);
	            	//new TextEntryDialog (UserInterface.getUserInterface(), him, assocModel);
	           // }
            }
        }
	}
}
