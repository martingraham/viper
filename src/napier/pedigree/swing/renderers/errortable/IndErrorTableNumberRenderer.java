package napier.pedigree.swing.renderers.errortable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BoundedRangeModel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.resspecies.model.Individual;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.JIndividualTable;
import napier.pedigree.util.Spectrum;

public class IndErrorTableNumberRenderer extends AbstractErrorTableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 186197938200490857L;
	
	public IndErrorTableNumberRenderer (final ErrorCollator bgm, final BoundedRangeModel greyCutoff) {
    	super (bgm, greyCutoff);
    	this.setHorizontalAlignment (SwingConstants.RIGHT);
    	this.setVerticalAlignment (SwingConstants.TOP);
    }
	
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {

    	final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column); 
    	final int modelCol = table.convertColumnIndexToModel (column);
    	Spectrum cellSpectrum = (modelCol <= 0 || modelCol > colourScalesByType.size() ? activeErrorColourScale : colourScalesByType.get (modelCol - 2));
    	final float[] csBoundaries = (modelCol <= 0 || modelCol > colourScalesByType.size() ? errorColourScaleBands : colourScaleBandsByType.get (modelCol - 2));
    	
    	if (table instanceof JIndividualTable) {
    		final JIndividualTable indTable = (JIndividualTable) table;
    		final Individual ind = indTable.getIndividual (row);
    		final int allErrorCount = errorModel.getCurrentIndividualErrorCount (ErrorCollator.ANY_ERROR, ind);
    		final boolean include = (allErrorCount >= rangeModel.getValue());
    		this.setForeground (include ? Color.black : FILTER_COLOUR);
        	cellSpectrum = (include ? cellSpectrum : greyColourScale);
    	}
    	
    	this.setBackground (returnNiceColour (((Integer)value).intValue() / ((float)errorModel.getInitialIndividualsSize()), 
	    		cellSpectrum, csBoundaries));
    	return comp;
    }
}
