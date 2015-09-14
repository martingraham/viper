package napier.pedigree.undo.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;


public class TreeOfListsModel<T> extends DefaultTreeModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2562059184370342079L;
	private static final Logger LOGGER = Logger.getLogger (TreeOfListsModel.class);
	
	protected T lastSelected;
	
	
	public TreeOfListsModel (final TreeNode root) {
		super (root);
	}
	
    public TreeOfListsModel (final TreeNode root, final boolean asksAllowsChildren) {
        super (root, asksAllowsChildren);
    }



	
	public void add (final T listItem) {

		//changed = true;

    		DefaultMutableTreeNode dmtn = find ((DefaultMutableTreeNode)getRoot(), lastSelected);
    		if (dmtn == null) {
    			dmtn = (DefaultMutableTreeNode)getRoot();
    		}
    		final List<T> itemList = (List<T>) dmtn.getUserObject();
    		final int index = itemList.indexOf (lastSelected);
    		
    		if (index == itemList.size() - 1) {
    			if (dmtn.getChildCount() == 0) {
    				itemList.add (listItem);
    				nodeChanged (dmtn);
    			} else {
    				final List<T> newBranch = new ArrayList<T> ();
    				newBranch.add (listItem);
    				final DefaultMutableTreeNode node = new DefaultMutableTreeNode (newBranch);
    				insertNodeInto (node, dmtn, dmtn.getChildCount());
    			}
    		} else {
    			final List<T> afterSplit = new ArrayList<T> (itemList.subList (index + 1, itemList.size()));
				dmtn.setUserObject (new ArrayList<T> (itemList.subList (0, index + 1)));		
   				final DefaultMutableTreeNode afterSplitNode = new DefaultMutableTreeNode (afterSplit);   			
								
				final Enumeration<DefaultMutableTreeNode> childEnum = dmtn.children();
				final List<DefaultMutableTreeNode> splitList = new ArrayList<DefaultMutableTreeNode> ();
				while (childEnum.hasMoreElements()) {
					splitList.add (childEnum.nextElement());
				}

				for (final DefaultMutableTreeNode childNode : splitList) {
					removeNodeFromParent(childNode);
					insertNodeInto (childNode, afterSplitNode, afterSplitNode.getChildCount());
				}
				
    			final List<T> newBranch = new ArrayList<T> ();
				newBranch.add (listItem);
				final DefaultMutableTreeNode node = new DefaultMutableTreeNode (newBranch);
				
				insertNodeInto (afterSplitNode, dmtn, dmtn.getChildCount());
				insertNodeInto (node, dmtn, dmtn.getChildCount());
				
				nodeStructureChanged(dmtn);
    		}
    		
            setLastSelected (listItem);
            LOGGER.info ("Last selected: "+lastSelected+", dmtn: "+dmtn);
            
            //outputTreeModel (this);
		//redoAncestrySet ();
	//}
    }


	public DefaultMutableTreeNode find (final DefaultMutableTreeNode dmtn, final T maskState) {
		final List<T> itemList = (List<T>) dmtn.getUserObject();    	
		
		if (itemList != null && itemList.contains (maskState)) {
			return dmtn;
		}	    	
		
		for (int n = 0; n < dmtn.getChildCount(); n++) {
			final DefaultMutableTreeNode dmtn2 = find ((DefaultMutableTreeNode)dmtn.getChildAt (n), maskState);
			if (dmtn2 != null) {
				return dmtn2;
			}
		}
		
		return null;
	}
	
	
	
	public List<T> getPathTo (final T listItem) {
		
		DefaultMutableTreeNode dmtn = find ((DefaultMutableTreeNode)getRoot(), listItem);
		if (dmtn == null) { 
			dmtn = (DefaultMutableTreeNode)getRoot();
		}
		List<T> pathList = (List<T>) dmtn.getUserObject();
		int index = pathList.indexOf (listItem);
    	
    	final List<T> backList = new ArrayList<T> ();
		backList.add (listItem);
		
		T pathItem = listItem;
			
		while (pathItem != null && dmtn != null) {
			index--;
			if (index < 0) {
				dmtn = (DefaultMutableTreeNode) dmtn.getParent();
				if (dmtn != null) {
					pathList = (List<T>) dmtn.getUserObject();
					index = pathList.size() - 1;
				} else {
					pathItem = null;
				}
			}
			
			if (index >= 0) {
				pathItem = pathList.get(index);			
				backList.add (pathItem);
			}
			//index--;
		}
		
		return backList;
    }
	
	public int getListsSize () {
		return getListsSize ((DefaultMutableTreeNode)this.getRoot());
	}
	
	protected int getListsSize (final DefaultMutableTreeNode tNode) {
		int size =  ((List<T>)tNode.getUserObject()).size();
		for (int child = 0; child < tNode.getChildCount(); child++) {
			size += getListsSize ((DefaultMutableTreeNode)tNode.getChildAt(child));
		}
		return size;
	}

	public T getLastSelected () {
		return lastSelected;
	}
	
	public void setLastSelected (final T listObj) {
		lastSelected = listObj;
	}
	
	
    public static void outputTreeModel (final TreeModel treeModel) {
    	final DefaultTreeModel dtm = (DefaultTreeModel)treeModel;
    	if (dtm != null) {
	    	final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) dtm.getRoot();  	
	    	final StringBuilder sb = new StringBuilder ();
	    	outputTreeNode (dmtn, 0, sb);
	    	
	    	LOGGER.info (sb.toString());
    	}
    }
    
    public static void outputTreeNode (final DefaultMutableTreeNode dmtn, final int treeDepth, final StringBuilder sBuilder) {
    	for (int n = treeDepth; --n >= 0;) {
    		sBuilder.append ("\t");
    	}
    	sBuilder.append (dmtn.getUserObject());
    	sBuilder.append ("\n");
    	
    	if (!dmtn.isLeaf()) {
    		final Enumeration enumer = dmtn.children();
    		while (enumer.hasMoreElements()) {
    			final DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) enumer.nextElement();
    			outputTreeNode (nextNode, treeDepth + 1, sBuilder);
    		}
    	}
    }
}
