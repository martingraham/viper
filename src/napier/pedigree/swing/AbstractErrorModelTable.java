package napier.pedigree.swing;

import java.awt.Component;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import napier.pedigree.model.AbstractErrorModelTableModel;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrixEvent;

import swingPlus.shared.ScaledTable;

//import swingPlus.matrix.JScaledTableHeader;


public abstract class AbstractErrorModelTable extends ScaledTable implements PropertyChangeListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3010344526071291735L;

	
    public AbstractErrorModelTable () {
        this (null, null, null);
    }

    public AbstractErrorModelTable (final TableModel tableModel) {
        this (tableModel, null, null);
    }

    public AbstractErrorModelTable (final TableModel tableModel, final TableColumnModel columnModel) {
        this (tableModel, columnModel, null);
    }

    public AbstractErrorModelTable (final TableModel tableModel, final TableColumnModel columnModel, final ListSelectionModel selectionModel) {
    	super (tableModel, columnModel, selectionModel);

		this.setRendererToolTip (new ColumnisedRendererToolTip (this));
    }
	
	

	public void setErrorModel (final ErrorCollator newErrorModel) {
		if (getErrorModel() != null) {
			getErrorModel().getPropertyChangeSupport().removePropertyChangeListener (this);
		}
		newErrorModel.getPropertyChangeSupport().addPropertyChangeListener (this);
	}
	
	public ErrorCollator getErrorModel () { return ((AbstractErrorModelTableModel)getModel()).getErrorModel(); }
	
	
	@Override
	public void setModel (final TableModel model) {
		if (! (model instanceof AbstractErrorModelTableModel)) {
			throw new IllegalArgumentException (this.getClass().getName()+" must use TableModel instance inherited from "+AbstractErrorModelTableModel.class.getName());
		}
		super.setModel (model);
	}
	
	
    @Override
    protected TableModel createDefaultDataModel() {
    	return new AbstractErrorModelTableModel  ((ErrorCollator)null) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -7367401439752371241L;

			@Override
			public void errorMatrixChanged (final ErrorMatrixEvent emme) {
				// EMPTY
			}

			@Override
			public int getRowCount() {
				return 0;
			}

			@Override
			public int getColumnCount() {
				return 0;
			}

			@Override
			public Object getValueAt (final int rowIndex, final int columnIndex) {
				return null;
			}
		};
    }
    
    protected class ColumnisedRendererToolTip extends DefaultRendererToolTip {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4515541711478345112L;
    	
		public ColumnisedRendererToolTip(JComponent jComponent) {
			super(jComponent);
		}
		
		@Override
		public void setToolTipObject (final Object obj, final int row, final int column) {
	        if ((column != -1) && (row != -1)) {
	            final TableCellRenderer renderer = getCellRenderer (row, column);
	            final Component component = prepareRenderer (renderer, row, column);
	            if (component instanceof JComponent) {
	            	final String val = ((JComponent)component).getToolTipText();
	            	label.setText (val);
	            } 
	        } 
	        
	        if (label.getText() == null) {
	        	label.setText (obj.toString());	
	        }
	        
	        if (label.getText() != null) {
	        	label.setText (AbstractErrorModelTable.this.getColumnName(column)+": "+label.getText());
	        }
	        
	        if (label.getText() != null) {
				final Rectangle2D bounds = label.getFont().getStringBounds (label.getText(), frc);
				final Insets insets = label.getInsets ();
				labelSize.setSize (
					(int)bounds.getWidth() + 4 + insets.left + insets.right + 1,
					(int)bounds.getHeight() + insets.top + insets.bottom
				);
	
				this.setPreferredSize (labelSize);
	        }
		}
    }
}
