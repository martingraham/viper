package napier.pedigree.swing.app.actions;

import javax.swing.Action;
import javax.swing.KeyStroke;

import napier.pedigree.model.ErrorCollator;

import napier.pedigree.swing.app.PedigreeSelectionSource;

import org.resspecies.datasourceaware.FallBack;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;



public class IndividualToggleNullFatherAction extends AbstractMaskingAction {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -833929414531051443L;
	
	
	public IndividualToggleNullFatherAction (final KeyStroke keyStroke, final String actionPrefix, final ErrorCollator errorModel) {
		super (keyStroke, actionPrefix, errorModel);	
	}
	

	
	public void doAction (final PedigreeSelectionSource famSelSource) {
		final HeritableIndividual hInd = (HeritableIndividual)famSelSource.getSelectedIndividual();	
		//final PopCheckerWrapper popCheckerContext = errorModel.getPopCheckerContext();
		
		if (hInd.isFatherMasked()) {
			hInd.unmaskFather();
		} else {
			hInd.maskFather();
		}
		
		tellErrorModel ();
		errorModel.setRestructureNeeded (true);
		
		
		//final JGeneration generation = famSelSource.getSelectedJGeneration();
		//final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();
		
		//JGenerationUtils.getInstance().redoGenerationFamilies (generation, hPop);
	}
	
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {
		
		if (isWorthwhileAction (famSelSource)) {
			final HeritableIndividual hInd = (HeritableIndividual)famSelSource.getSelectedIndividual();	
			final boolean currentlyNulled = hInd.isFatherMasked();
			final Individual unmaskedFather = getUnmaskedFather (hInd);
			
			final String[][] alts = {altNames, altTooltips};
			final String[] keys = {Action.NAME, Action.SHORT_DESCRIPTION};
			
			for (int n = 0; n < alts.length; n++) {
				final String[] altLabels = alts [n];
				
				if (altLabels != null && altLabels.length >= 2) {
					final int altVal = (currentlyNulled ? 1 : 0);	
					this.putValue (keys [n], altLabels [altVal]+ " " + unmaskedFather.getName());
				}
			}
		}
	}
	
	
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		final Individual ind = famSelSource.getSelectedIndividual();
		boolean worthwhile = false;
		
		if (ind instanceof HeritableIndividual) {		
			final HeritableIndividual hInd = (HeritableIndividual)ind;
			final Individual sire = getUnmaskedFather (hInd);
			worthwhile = (ind != null && sire != null && sire != FallBack.SAFE_SIRE);
		}
		return worthwhile;
	}
	
	
	
	protected Individual getUnmaskedFather (final HeritableIndividual offspring) {
		final boolean maskedSire = offspring.isFatherMasked();
		return maskedSire ? ((HeritablePopulation)offspring.getPopulation()).getMaskedPaternityRelationships().get(offspring) : offspring.getSire();
	}
	
	
	protected boolean actionAffectsAllMarkers () {
		return true;
	}
}