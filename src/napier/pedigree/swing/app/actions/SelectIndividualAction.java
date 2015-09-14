package napier.pedigree.swing.app.actions;

import javax.swing.KeyStroke;

import org.resspecies.model.Individual;

import napier.pedigree.model.PedigreeSelectionModel;
import napier.pedigree.swing.app.PedigreeSelectionSource;



public class SelectIndividualAction extends AbstractSelectionAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -833929414531051443L;
	
	
	public SelectIndividualAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeSelectionModel psm, 
			final int descendantsDepth, final int ancestorsDepth, final boolean includePartners) {
		super (keyStroke, actionPrefix, psm);	
		this.descendantDepth = descendantsDepth;
		this.ancestorDepth = ancestorsDepth;
		this.includePartners = includePartners;
	}
	
	public void doAction (final PedigreeSelectionSource famSelSource) {
		final Individual ind = famSelSource.getSelectedIndividual();
		selectionModel.addIndividual (ind, descendantDepth, ancestorDepth, includePartners);
	}
	
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {
		//final Individual ind = famSelSource.getSelectedIndividual();
		
		/*
		if (ind != null) {
			final HeritableIndividual hInd = (HeritableIndividual)ind;	
			final PopCheckerWrapper popCheckerContext = errorModel.getPopCheckerContext();
			final boolean multipleMarkersActive = (popCheckerContext.getFocusMarker() == null);
			final boolean currentlyMasked = popCheckerContext.isIndividualContextMasked (hInd);
			
			final String[][] alts = {altNames, altTooltips};
			final String[] keys = {Action.NAME, Action.SHORT_DESCRIPTION};
			
			for (int n = 0; n < alts.length; n++) {
				final String[] altLabels = alts [n];
				
				if (altLabels != null && altLabels.length >= 4) {
					final int altVal = multipleMarkersActive ? (currentlyMasked ? 1 : 0) : (currentlyMasked ? 3 : 2);	
					this.putValue (keys [n], altLabels [altVal]);
				}
			}
		}
		*/
	}
	
	
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		return famSelSource.getSelectedIndividual() != null;
		//return famSelSource.getSelectedGroup().size() <= 1;
	}
}