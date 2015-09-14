package napier.pedigree.swing.app.actions;

import javax.swing.Action;
import javax.swing.KeyStroke;

import napier.pedigree.model.ErrorCollator;

import napier.pedigree.swing.app.PedigreeSelectionSource;

import org.resspecies.datasourceaware.FallBack;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;



public class IndividualToggleNullMotherAction extends AbstractMaskingAction {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -833929414531051443L;
	
	
	public IndividualToggleNullMotherAction (final KeyStroke keyStroke, final String actionPrefix, final ErrorCollator errorModel) {
		super (keyStroke, actionPrefix, errorModel);	
	}
	

	
	public void doAction (final PedigreeSelectionSource famSelSource) {
		final HeritableIndividual hInd = (HeritableIndividual)famSelSource.getSelectedIndividual();	
		//final PopCheckerWrapper popCheckerContext = errorModel.getPopCheckerContext();
		
		if (hInd.isMotherMasked()) {
			hInd.unmaskMother();
		} else {
			hInd.maskMother();
		}
		
		tellErrorModel();
		errorModel.setRestructureNeeded (true);
		
		//final JGeneration generation = famSelSource.getSelectedJGeneration();
		//final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();
		
		//JGenerationUtils.getInstance().redoGenerationFamilies (generation, hPop);
	}
	
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {
		
		if (isWorthwhileAction (famSelSource)) {
			final HeritableIndividual hInd = (HeritableIndividual)famSelSource.getSelectedIndividual();	
			final boolean currentlyNulled = hInd.isMotherMasked();
			final Individual unmaskedMother = getUnmaskedMother (hInd);
			
			final String[][] alts = {altNames, altTooltips};
			final String[] keys = {Action.NAME, Action.SHORT_DESCRIPTION};
			
			for (int n = 0; n < alts.length; n++) {
				final String[] altLabels = alts [n];
				
				if (altLabels != null && altLabels.length >= 2) {
					final int altVal = (currentlyNulled ? 1 : 0);	
					this.putValue (keys [n], altLabels [altVal]+ " " + unmaskedMother.getName());
				}
			}
		}
	}
	
	
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		final Individual ind = famSelSource.getSelectedIndividual();
		boolean worthwhile = false;
		
		if (ind instanceof HeritableIndividual) {		
			final HeritableIndividual hInd = (HeritableIndividual)ind;
			final Individual dam = getUnmaskedMother (hInd);
			worthwhile = (ind != null && dam != null && dam != FallBack.SAFE_DAM);
		}
		return worthwhile;
	}
	
	
	
	protected Individual getUnmaskedMother (final HeritableIndividual offspring) {
		final boolean maskedDam = offspring.isMotherMasked();
		return maskedDam ? ((HeritablePopulation)offspring.getPopulation()).getMaskedMaternityRelationships().get(offspring) : offspring.getDam();
	}
	
	protected boolean actionAffectsAllMarkers () {
		return true;
	}
}