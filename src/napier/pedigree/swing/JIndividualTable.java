package napier.pedigree.swing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.AbstractErrorModelTableModel;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrixEvent;

import org.resspecies.model.Individual;

import util.Messages;


public class JIndividualTable extends AbstractSelectableIndividualTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1963618302662125906L;
	
	public JIndividualTable () {
		super ();
		this.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
	}
	
	@Override
	public void setErrorModel (final ErrorCollator newErrorModel) {
		super.setErrorModel (newErrorModel);
		setModel (new ErrorIndividualTableModel (newErrorModel));
	}
	
	@Override
	public void valueChanged (final ListSelectionEvent lse) {
		//System.err.println (lse);
		if (!lse.getValueIsAdjusting()) {
			final ListSelectionModel lsm = (ListSelectionModel)lse.getSource();
			if (! lsm.isSelectionEmpty ()) {
				getPedigreeSelection().redoSelection();
				getPedigreeSelection().getSelectedGraph().addNode (getValueAt (
					lsm.isSelectedIndex (lse.getFirstIndex()) ? lse.getFirstIndex() : lse.getLastIndex(),
					0)
				);
			}
		}
		super.valueChanged (lse);
	}
	
	
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if ("filter".equals (evt.getPropertyName()) && evt.getSource() instanceof ErrorCollator) {
			repaint ();
		}
	}
}


class ErrorIndividualTableModel extends AbstractErrorModelTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5034460154047285943L;
	private final static String NAME_COLUMN_LABEL = Messages.getString (PropertyConstants.TEXTPROPS, "NameLabel");
	private final static String GENDER_COLUMN_LABEL = Messages.getString (PropertyConstants.TEXTPROPS, "GenderLabel");
	private final static int ERROR_COLUMN_START = 2;
	
	protected List<Individual> individuals;
	
	public ErrorIndividualTableModel (final ErrorCollator errorModel) {
		super (errorModel);
		makeIndividualList ();
	}
	
	@Override
	public int getRowCount() {
		return (individuals == null ? 0 : individuals.size());
	}

	@Override
	public int getColumnCount() {
		return ErrorCollator.ERROR_RANGE + ERROR_COLUMN_START;
	}
	

	@Override
	public Object getValueAt (final int rowIndex, final int columnIndex) {
		Object obj = null;
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			final Individual ind = individuals.get (rowIndex);
			
			if (ind != null) {
				if (columnIndex == AbstractSelectableIndividualTable.IND_COLUMN) {
					obj = ind;
				} else if (columnIndex == 1) {
					obj = ind.getGender();
				} else if (columnIndex >= ERROR_COLUMN_START && columnIndex < getColumnCount()) {
					// Filtered individual count as further markers may be temporarily masked
					// We want to reflect this in the individuals data table.
					obj = Integer.valueOf (errorModel.getFilteredIndividualErrorCount (columnIndex - ERROR_COLUMN_START, ind));
				}
			}
		}
		
		return obj;
	}

	@Override
	public String getColumnName (final int columnIndex) {
		if (columnIndex == AbstractSelectableIndividualTable.IND_COLUMN) {
			return NAME_COLUMN_LABEL;
		}
		else if (columnIndex == 1) {
			return GENDER_COLUMN_LABEL;
		}
		else if (columnIndex >= ERROR_COLUMN_START && columnIndex < getColumnCount()) {
			return ErrorCollator.ERROR_NAMES [columnIndex - ERROR_COLUMN_START];
		}
		return "";
	}
	
	
	@Override
    public Class<?> getColumnClass (final int columnIndex) {
    	if (columnIndex == AbstractSelectableIndividualTable.IND_COLUMN) {
    		return Individual.class;
    	} else if (columnIndex == 1) {
    		return String.class;
    	} else if (columnIndex >= ERROR_COLUMN_START && columnIndex < getColumnCount()) {
    		return Integer.class;
    	}
    	return Object.class;
    }
	

	protected void makeIndividualList () {
		if (errorModel != null) {
			//individuals = new ArrayList<Individual> (errorModel.getInitialAllErrorMap().getIndividualMap().keySet());
			individuals = new ArrayList<Individual> (errorModel.getPopCheckerContext().getPopulation().getIndividuals());
		}
	}
	
	
	@Override
	public void errorMatrixChanged (final ErrorMatrixEvent emme) {
		makeIndividualList ();
	}
}
