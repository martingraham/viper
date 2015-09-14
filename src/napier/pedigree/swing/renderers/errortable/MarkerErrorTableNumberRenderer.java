package napier.pedigree.swing.renderers.errortable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.JMarkerTable;
import napier.pedigree.util.Spectrum;


public class MarkerErrorTableNumberRenderer extends AbstractErrorTableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 186197938200490857L;

	protected boolean filteredMarker;
	
	
	public MarkerErrorTableNumberRenderer (final ErrorCollator bgm) {
    	super (bgm, null);
    	this.setHorizontalAlignment (SwingConstants.RIGHT);
    	this.setVerticalAlignment (SwingConstants.TOP);

    }
	
	@Override
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	
    	final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column); 
    	final int modelCol = table.convertColumnIndexToModel (column);
    	
    	filteredMarker = true;
    	if (table instanceof JMarkerTable) {
    		final JMarkerTable markerTable = (JMarkerTable) table;
    		final SNPMarker marker = markerTable.getMarker (row);
    		filteredMarker = errorModel.isFilteredMarker (marker);
    		filteredMarker = filteredMarker && (!errorModel.getPopCheckerContext().getPopulation().getMaskedMarkers().contains(marker));
    		this.setForeground (filteredMarker ? Color.black : FILTER_COLOUR);
    	}	
    	
    	final Spectrum cellSpectrum = (modelCol <= 0 || modelCol > colourScalesByType.size() ? activeErrorColourScale : colourScalesByType.get (modelCol - 1));
    	final float[] csBoundaries = (modelCol <= 0 || modelCol > colourScalesByType.size() ? errorColourScaleBands : colourScaleBandsByType.get (modelCol - 1));
    	
    	this.setBackground (returnNiceColour (((Integer)value).intValue() / ((float)errorModel.getInitialIndividualsSize()), 
	    		filteredMarker ?  cellSpectrum : greyColourScale, csBoundaries));
    	return comp;
    }
}
