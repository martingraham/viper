package napier.pedigree.swing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;

import util.Messages;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.AbstractErrorModelTableModel;
import napier.pedigree.model.ChangeOnNextRecalcStore;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrixEvent;


public class JMaskTable extends AbstractSelectableIndividualTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1963618302662125906L;

	public JMaskTable () {
		super ();
		this.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
	}
	
	@Override
	public void setErrorModel (final ErrorCollator newErrorModel) {
		super.setErrorModel (newErrorModel);
		setModel (new MaskTableModel (newErrorModel));
	}
	
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (ErrorCollator.RECALCULATED.equals (evt.getPropertyName()) && evt.getSource() instanceof ErrorCollator) {
			((MaskTableModel)getModel()).makeMaskedIndividualList ();
			this.tableChanged (new TableModelEvent (getModel()));
			//repaint ();
		}
	}
}


class MaskTableModel extends AbstractErrorModelTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5034460154047285943L;
	private final static String NAME_COLUMN_LABEL = Messages.getString (PropertyConstants.TEXTPROPS, "NameLabel");
	private final static String GENDER_COLUMN_LABEL = Messages.getString (PropertyConstants.TEXTPROPS, "GenderLabel");
	protected final static String[] MASKING_NAMES = Messages.getString (PropertyConstants.TEXTPROPS, "IndMaskLabels").split("\\|");
	protected final static int IND_COLUMN = AbstractSelectableIndividualTable.IND_COLUMN, GENDER_COLUMN = 1, 
			MASKING_COLUMN_START = 2, IS_MASKED = 2, IS_SIREREL_MASKED = 3, IS_DAMREL_MASKED = 4;
	
	protected List<HeritableIndividual> maskedIndividuals;
	
	
	public MaskTableModel (final ErrorCollator errorModel) {
		super (errorModel);
		makeMaskedIndividualList ();
	}
	
	@Override
	public int getRowCount() {
		return maskedIndividuals == null ? 0 : maskedIndividuals.size();
	}

	@Override
	public int getColumnCount() {
		return MASKING_COLUMN_START + MASKING_NAMES.length;
	}

	@Override
	public Object getValueAt (final int rowIndex, final int columnIndex) {
		Object obj = null;
		if (rowIndex >= 0 && rowIndex < getRowCount()) {
			final HeritableIndividual hInd = maskedIndividuals.get (rowIndex);
			
			if (hInd != null) {
				switch (columnIndex) {
					case IND_COLUMN:
						obj = hInd;
						break;
					case GENDER_COLUMN:
						obj = hInd.getGender();
						break;
					case IS_MASKED:
						obj = Boolean.valueOf (hInd.isMasked());
						break;
					case IS_SIREREL_MASKED:
						obj = Boolean.valueOf (hInd.isFatherMasked());
						break;
					case IS_DAMREL_MASKED:
						obj = Boolean.valueOf (hInd.isMotherMasked());
						break;
					default:
						break;
				}
			}
		}
		
		return obj;
	}
	
	
    @Override
    public void setValueAt (final Object aValue, final int rowIndex, final int columnIndex) {
    	if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0 && columnIndex < getColumnCount()) {
    		if (aValue instanceof Boolean) {
    			final HeritableIndividual hInd = (HeritableIndividual) getValueAt (rowIndex, IND_COLUMN);
    			final boolean val = ((Boolean)aValue).booleanValue();
    			boolean changed = false, relsChanged = false, curVal = false;
    			
    			switch (columnIndex) {
	    			case IS_MASKED:
	    				curVal = hInd.isMasked();
	    				if (val != curVal) {
							if (val) {
								hInd.mask();
							} else {
								hInd.unmask();
							}
							changed = true;
	    				}
						break;
					case IS_SIREREL_MASKED:
						curVal = hInd.isFatherMasked();
	    				if (val != curVal) {
							if (val) {
								hInd.maskFather();
							} else {
								hInd.unmaskFather();
							}
							changed = true;
							relsChanged = true;
	    				}
						break;
					case IS_DAMREL_MASKED:
						curVal = hInd.isMotherMasked();
	    				if (val != curVal) {
							if (val) {
								hInd.maskMother();
							} else {
								hInd.unmaskMother();
							}
							changed = true;
							relsChanged = true;
	    				}
						break;
					default:
						break;
    			}
    			
    			if (changed) {
    				errorModel.setRecalculationNeeded (true, ChangeOnNextRecalcStore.IND_CHANGE);
    			}
    			if (relsChanged) {
    				errorModel.setRestructureNeeded (true);
    			}
    		}
    	}
    }

	@Override
	public String getColumnName (final int columnIndex) {
		if (columnIndex == IND_COLUMN) {
			return NAME_COLUMN_LABEL;
		}
		else if (columnIndex == GENDER_COLUMN) {
			return GENDER_COLUMN_LABEL;
		}
		else if (columnIndex >= MASKING_COLUMN_START && columnIndex < getColumnCount()) {
			return MASKING_NAMES [columnIndex - MASKING_COLUMN_START];
		}
		return "";
	}
	
	
	@Override
    public Class<?> getColumnClass (final int columnIndex) {
    	if (columnIndex == IND_COLUMN) {
    		return Individual.class;
    	} else if (columnIndex == GENDER_COLUMN) {
    		return String.class;
    	} else if (columnIndex >= MASKING_COLUMN_START && columnIndex < getColumnCount()) {
    		return Boolean.class;
    	}
    	return Object.class;
    }
    
    
    @Override 
    public boolean isCellEditable (final int row, final int column) { 
    	return column >= MASKING_COLUMN_START;
    } 
	
	
	protected void makeMaskedIndividualList () {
		if (errorModel != null) {
			final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();
			
			final Set<HeritableIndividual> maskedInds = new HashSet<HeritableIndividual> (hPop.getMaskedIndividuals());	// Masked Individuals		
			final Set<HeritableIndividual> maskedSireInds = hPop.getMaskedPaternityRelationships().keySet();
			final Set<HeritableIndividual> maskedDamInds = hPop.getMaskedMaternityRelationships().keySet();
			maskedInds.addAll (maskedSireInds);
			maskedInds.addAll (maskedDamInds);
			
			if (maskedInds != null) {
				maskedIndividuals = new ArrayList<HeritableIndividual> (maskedInds);
			}
		}
	}
	
	@Override
	public void errorMatrixChanged (final ErrorMatrixEvent emme) {
		makeMaskedIndividualList ();
	}
}