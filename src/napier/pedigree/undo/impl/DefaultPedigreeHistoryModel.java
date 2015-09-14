package napier.pedigree.undo.impl;

import java.util.ArrayList;
import java.util.BitSet;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.app.PedigreeFrame;
import napier.pedigree.undo.Memento;
import napier.pedigree.undo.HistoryModel;


public class DefaultPedigreeHistoryModel implements HistoryModel<PedigreeFrame> {

	private static final Logger LOGGER = Logger.getLogger (DefaultPedigreeHistoryModel.class);
	
	protected TreeOfListsModel<Memento<PedigreeFrame>> treeModel;
	protected PedigreeFrame pFrame;
	protected boolean adjusting;
	
	public DefaultPedigreeHistoryModel () {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode ();
   	 	root.setUserObject (new ArrayList<Memento<PedigreeFrame>> ());
   	 	treeModel = new TreeOfListsModel<Memento<PedigreeFrame>> (root);
   	 	setAdjusting (false);
	}

	@Override
	public TreeOfListsModel<Memento<PedigreeFrame>> getHistoryModelTree() {
		return treeModel;
	}

	@Override
	public void addHistoryState (final PedigreeFrame pFrame) {
		if (!isAdjusting()) { // stops re-adding of selections via restoreHistoryState
			final Memento<PedigreeFrame> historyState = new DefaultPedigreeMemento();
			historyState.make (pFrame);
			LOGGER.debug ("hpop: "+pFrame.getModel().getGenotypedPopulation().getMaskedMarkers().toString());
			getHistoryModelTree().add (historyState);
		}
	}
	
	
	@Override
	public void restoreHistoryState (final Memento<PedigreeFrame> historyState) {
		setAdjusting (true);
    	if (pFrame != null) {
    		LOGGER.info ("about to restore "+historyState.toString());
    		final ErrorCollator errorModel = pFrame.getErrorCollator();
    			
    		// Flag if restoring this history state means restructuring the pedigree
    		final BitSet difference = historyState.compare (pFrame);
    		if (!difference.isEmpty()) {
    			errorModel.setRestructureNeeded (true);
    		}

			getHistoryModelTree().setLastSelected (historyState);
			long now = System.currentTimeMillis();
    		historyState.restore (pFrame);
    		LOGGER.debug ("Time for restore: "+(System.currentTimeMillis() - now)+" ms.");
    		errorModel.recalculate();
    	}
		setAdjusting (false);
	}
	
	
	/*
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (ErrorCollator.STORE_HISTORY.equals (evt.getPropertyName())) {
			addHistoryState ((HeritablePopulation)evt.getNewValue());
		}
	}
	*/

	@Override
	public void setOriginator (final PedigreeFrame pFrame) {
		this.pFrame = pFrame;
	}
	


	public final boolean isAdjusting() {
		return adjusting;
	}

	public final void setAdjusting (final boolean adjusting) {
		this.adjusting = adjusting;
	}
}
