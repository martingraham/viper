package napier.pedigree.swing.renderers.errortable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BoundedRangeModel;
import javax.swing.JTable;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.base.AbstractCutoffErrorBaseRenderer;


public abstract class AbstractErrorTableRenderer extends AbstractCutoffErrorBaseRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5557285251088994370L;

	protected final static Color FILTER_COLOUR = new Color (0, 0, 0, 128);
	
	
	public AbstractErrorTableRenderer (final ErrorCollator bgm, final BoundedRangeModel greyCutoff) {
    	super (bgm, greyCutoff);
    	setForeground (Color.black);
    }
	
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	
    	return super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    }
    
	protected void calculateErrorValues () {
		// EMPTY
	}
	
	protected void updateValue (final int[] values, final int index, final int newValue) {
		// EMPTY
	}
	
	protected void finaliseValues (final int[] values) {
		// EMPTY
	}
}
