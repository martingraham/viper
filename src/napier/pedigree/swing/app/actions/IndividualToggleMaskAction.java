package napier.pedigree.swing.app.actions;

import javax.swing.Action;
import javax.swing.KeyStroke;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.swing.JGeneration;
import napier.pedigree.swing.app.PedigreeSelectionSource;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;



public class IndividualToggleMaskAction extends AbstractMaskingAction {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -833929414531051443L;
	
	
	public IndividualToggleMaskAction (final KeyStroke keyStroke, final String actionPrefix, final ErrorCollator errorModel) {
		super (keyStroke, actionPrefix, errorModel);	
	}
	

	
	public void doAction (final PedigreeSelectionSource famSelSource) {
		final HeritableIndividual hInd = (HeritableIndividual)famSelSource.getSelectedIndividual();	
		final PopCheckerWrapper popCheckerContext = errorModel.getPopCheckerContext();
		
		if (popCheckerContext.isIndividualContextMasked (hInd)) {
			popCheckerContext.contextUnmaskIndividual (hInd);
		} else {
			popCheckerContext.contextMaskIndividual (hInd);
		}
		
		tellErrorModel();
		
		final JGeneration selectedGen = famSelSource.getSelectedJGeneration();
		if (selectedGen != null) {
			((PedigreeGenerationModel)(selectedGen.getModel())).splitOffspring();
			selectedGen.repaint();
		}
	}
	
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {
		final Individual ind = famSelSource.getSelectedIndividual();
		
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
	}
	
	
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		return famSelSource.getSelectedIndividual() != null;
	}
}