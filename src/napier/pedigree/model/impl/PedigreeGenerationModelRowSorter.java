package napier.pedigree.model.impl;

import javax.swing.DefaultRowSorter;

import napier.pedigree.model.PedigreeGenerationModel;



public class PedigreeGenerationModelRowSorter<M extends PedigreeGenerationModel> extends DefaultRowSorter<M, Integer> {
	
	
    /**
     * Underlying model.
     */
    private M tableModel;
	
    
    /**
     * Creates a <code>TableRowSorter</code> with an empty model.
     */
    public PedigreeGenerationModelRowSorter () {
        this (null);
    }

    /**
     * Creates a <code>TableRowSorter</code> using <code>model</code>
     * as the underlying <code>TableModel</code>.
     *
     * @param model the underlying <code>TableModel</code> to use,
     *        <code>null</code> is treated as an empty model
     */
    public PedigreeGenerationModelRowSorter (final M model) {
    	super ();
        setModel (model);
    }
    
    
    /**
     * Sets the <code>TableModel</code> to use as the underlying model
     * for this <code>TableRowSorter</code>.  A value of <code>null</code>
     * can be used to set an empty model.
     *
     * @param model the underlying model to use, or <code>null</code>
     */
    public void setModel (final M model) {
        tableModel = model;
        setModelWrapper(new TableRowSorterModelWrapper());
    }
    
    
    /**
     * Implementation of DefaultRowSorter.ModelWrapper that delegates to a
     * TableModel.
     */
    private class TableRowSorterModelWrapper extends ModelWrapper<M,Integer> {
        public M getModel() {
            return tableModel;
        }

        public int getColumnCount() {
            return (tableModel == null) ? 0 : tableModel.getColumnCount();
        }

        public int getRowCount() {
            return (tableModel == null) ? 0 : tableModel.getRowCount();
        }

        public Object getValueAt (final int row, final int column) {
        	return (tableModel == null) ? null : tableModel.getRowPriority (row);
            //return tableModel.getValueAt(row, column);
        }

        public Integer getIdentifier (final int index) {
            return index;
        }
    }
}
