package napier.pedigree.model.impl;

import javax.swing.DefaultListSelectionModel;

public class FireSuppressedListSelectionModel extends DefaultListSelectionModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 687924872295317027L;

	protected boolean suppressed;
    /**
     * Notifies <code>ListSelectionListeners</code> that the value
     * of the selection, in the closed interval <code>firstIndex</code>,
     * <code>lastIndex</code>, has changed.
     * 
     * Stops all firing of events if both valueIsAdjusting and suppressed are true
     * This is valuable if a table<->table selection translation ends up with
     * setting or clearing selections at multiple points in one ListSelectionModel.
     * We don't want to be generating multiple calls to listeners, as even valueIsAdjusting()
     * is ignored by some listeners.
     * Note: In the case of the pedigree vis, we are setting both column and row selections so any repaint
     * would end up being the whole table anyways, so partial repaints calculated when valueIsAdjusting=true
     * in JTable's valueChanged code are superfluous.
     */
    protected void fireValueChanged (final int firstIndex, final int lastIndex, final boolean isAdjusting) {
    	if (!getValueIsAdjusting () || !isSuppressed ()) {
    		super.fireValueChanged(firstIndex, lastIndex, getValueIsAdjusting());
    	}
    }
    
	public boolean isSuppressed() {
		return suppressed;
	}
	
	public void setSuppressed (final boolean suppressed) {
		this.suppressed = suppressed;
	}
}
