package napier.pedigree.swing;

import org.resspecies.model.Individual;

import model.graph.GraphModelEvent;
import model.graph.GraphModelListener;
import napier.pedigree.model.PedigreeSelectable;
import napier.pedigree.model.PedigreeSelectionModel;


public abstract class AbstractSelectableIndividualTable extends AbstractErrorModelTable implements GraphModelListener, PedigreeSelectable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2678692350166428176L;
	protected final static int IND_COLUMN = 0;
	
	protected PedigreeSelectionModel pedigreeSelection;
	
	public void setPedigreeSelection (final PedigreeSelectionModel newModel) {
		if (pedigreeSelection != newModel) {
			if (pedigreeSelection != null && pedigreeSelection.getSelectedGraph() != null) {
				pedigreeSelection.getSelectedGraph().removeGraphModelListener (this);
			}
			
			pedigreeSelection = newModel;
			
			if (pedigreeSelection != null && pedigreeSelection.getSelectedGraph() != null) {
				pedigreeSelection.getSelectedGraph().addGraphModelListener (this);
			}
		}
	}
	
	@Override
	public PedigreeSelectionModel getPedigreeSelection () {
		return pedigreeSelection;
	}
	
	@Override
	public void graphChanged (final GraphModelEvent gmEvent) {
		if (gmEvent.getType() == GraphModelEvent.UPDATE || gmEvent.getType() == GraphModelEvent.CLEAR) {
			repaint ();
		}
	}
	
	
	public Individual getIndividual (final int viewRow) {
		final int modelRow = this.convertRowIndexToModel (viewRow);
		return (Individual)getModel().getValueAt (modelRow, IND_COLUMN);
	}
}
