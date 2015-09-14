package napier.pedigree.swing.app.actions;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultRowSorter;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.impl.DefaultPedigreeGenerationModel;
import napier.pedigree.swing.JGeneration;
import napier.pedigree.swing.app.AppUtils;
import napier.pedigree.swing.app.PedigreeSelectionSource;

import org.resspecies.inheritance.model.HeritableFamily;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

public class DetailViewAction extends AbstractFamilyCentricAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 651875448982709674L;

	protected JGeneration detailTable;
	
	public DetailViewAction (final KeyStroke keyStroke, final String actionPrefix, final JGeneration detailTable) {
		super (keyStroke, actionPrefix);	
		this.detailTable = detailTable;
	}
	

	
	public void doAction (final PedigreeSelectionSource famSelSource) {
		final Set<HeritableFamily> familySet = new HashSet<HeritableFamily> ();
		final JGeneration sourceGen = famSelSource.getSelectedJGeneration();
		final TableModel sourceModel = sourceGen.getModel();
		
		if (sourceModel instanceof PedigreeGenerationModel) {
			final PedigreeGenerationModel sourcePedGenModel = (PedigreeGenerationModel)sourceModel;
		
			if (detailTable != sourceGen && sourcePedGenModel != null) {
				for (Individual ind : famSelSource.getSelectedGroup()) {
					if (sourcePedGenModel.getSires().contains (ind) || sourcePedGenModel.getDams().contains (ind)) {
						getFamilies (ind, sourceGen, familySet);
					} else {
						final HeritableFamily hFamily = (HeritableFamily)ind.getFamily();
						if (hFamily != null) {
							familySet.add (hFamily);
						}
					}
				}
	
				if (!familySet.isEmpty()) {
					final PedigreeGenerationModel pedGenModel = new DefaultPedigreeGenerationModel (familySet, sourcePedGenModel.getGenerationIndex());
					pedGenModel.setCategoriser (sourcePedGenModel.getCategoriser());
					
					detailTable.setModel (pedGenModel);	
					((DefaultRowSorter<?, ?>)detailTable.getRowSorter()).setRowFilter (
							((DefaultRowSorter)sourceGen.getRowSorter()).getRowFilter()
					);
					detailTable.refilter();
					
					sourceGen.cloneRowHeightsTo (detailTable);
				}
			}
			
			detailTable.getTopLevelAncestor().setVisible (true);
			final JLabel detailLabel = (AppUtils.getInstance().makeHTMLLabel ("detailTableHeader", Color.gray, 
					sourceGen.getClientProperty (JGeneration.HTML_DESCRIPTOR).toString(), true));
			detailTable.setColumnHeaderView (detailLabel);
			detailTable.repaint ();
		}
	}
	
	
	public void updateAction (final PedigreeSelectionSource famSelSource) {
		// EMPTY
	}
	
	public boolean isWorthwhileAction (final PedigreeSelectionSource famSelSource) {
		return (detailTable != famSelSource.getSelectedJGeneration());
	}
	
	
	public void getFamilies (final Individual ind, final JGeneration sourceGen, 
			final Set<HeritableFamily> familySet) {
		final TableModel sourceModel = sourceGen.getModel();
		
		for (int col = 0; col < sourceGen.getColumnCount(); col++) {
			final int modelCol = sourceGen.convertColumnIndexToModel (col);
			if (ind == sourceModel.getValueAt (ModelRowConstants.SIRE, modelCol)
				|| ind == sourceModel.getValueAt (ModelRowConstants.DAM, modelCol)) {
				final List<Individual> offspring = (List<Individual>)sourceModel.getValueAt (ModelRowConstants.OFFSPRING, modelCol);
				if (!offspring.isEmpty()) {
					final HeritableFamily hFamily = ((HeritableIndividual)offspring.get(0)).getFamily();
					if (hFamily != null) {
						familySet.add (hFamily);
					}
				}
			}
		}
	}
}