package napier.pedigree.swing.app.actions;

import java.util.Collection;

import javax.swing.KeyStroke;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.PopCheckerWrapper;

import napier.pedigree.swing.app.PedigreeSelectionSource;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;



public class FamilyUnmaskAction extends AbstractMaskingAction {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -833929414531051443L;
	
	
	public FamilyUnmaskAction (final KeyStroke keyStroke, final String actionPrefix, final ErrorCollator errorModel) {
		super (keyStroke, actionPrefix, errorModel);	
	}
	

	
	public void doAction (final PedigreeSelectionSource famSelSource) {
		final PopCheckerWrapper popCheckerContext = errorModel.getPopCheckerContext();
		for (Individual ind : famSelSource.getSelectedGroup()) {
			popCheckerContext.contextUnmaskIndividual ((HeritableIndividual)ind);
		}
		
		tellErrorModel();
		((PedigreeGenerationModel)(famSelSource.getSelectedJGeneration().getModel())).splitOffspring();
		famSelSource.getSelectedJGeneration().repaint();
	}
	
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {	
		this.setEnabled (isWorthwhileAction (famSelSource));
	}
	
	
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		boolean allUnmaskedAlready = true;	
		final Collection<Individual> selectedGroup = famSelSource.getSelectedGroup();
		
		if (selectedGroup != null && selectedGroup.size() > 1) {
			final PopCheckerWrapper popCheckerContext = errorModel.getPopCheckerContext();	
			for (Individual ind : famSelSource.getSelectedGroup()) {
				//((HeritableIndividual)ind);
				allUnmaskedAlready &= (! popCheckerContext.isIndividualContextMasked ((HeritableIndividual)ind));
			}
		}
		
		return !allUnmaskedAlready;
	}
}