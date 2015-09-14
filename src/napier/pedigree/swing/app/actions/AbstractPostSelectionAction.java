package napier.pedigree.swing.app.actions;

import javax.swing.KeyStroke;

import napier.pedigree.model.PedigreeSelectionModel;
import napier.pedigree.swing.app.PedigreeSelectionSource;


public abstract class AbstractPostSelectionAction extends AbstractSelectionAction {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4758925984809661365L;

	public AbstractPostSelectionAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeSelectionModel psm) {
		super (keyStroke, actionPrefix, psm);	
	}
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {
		// EMPTY
	}
	   		
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		return this.selectionModel.getSelectedGraph().getNodeCount() > 0;
	}
}