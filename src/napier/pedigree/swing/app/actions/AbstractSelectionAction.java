package napier.pedigree.swing.app.actions;

import javax.swing.KeyStroke;

import napier.pedigree.model.PedigreeSelectionModel;


public abstract class AbstractSelectionAction extends AbstractFamilyCentricAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -833929414531051443L;
	
	protected PedigreeSelectionModel selectionModel;
	protected int ancestorDepth = 0, descendantDepth = 1;
	protected boolean includePartners = true;
	
	
	public AbstractSelectionAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeSelectionModel psm) {
		super (keyStroke, actionPrefix);	
		selectionModel = psm;
	}
}