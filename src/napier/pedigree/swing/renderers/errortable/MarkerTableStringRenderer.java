package napier.pedigree.swing.renderers.errortable;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.ButtonModel;
import javax.swing.JTable;
import javax.swing.RepaintManager;
import javax.swing.SwingConstants;
import javax.swing.text.View;

import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.filter.MarkerFilter;


public class MarkerTableStringRenderer extends AbstractErrorTableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2984808199537154677L;

	protected MarkerFilter markerFilter;
	protected ButtonModel expandToIndsModel;

	public MarkerTableStringRenderer (final ErrorCollator bgm, final MarkerFilter markerFilter,
			final ButtonModel expandToIndsModel) {
		super (bgm, null);
		this.setVerticalAlignment (SwingConstants.TOP);
		this.markerFilter = markerFilter;
		this.expandToIndsModel = expandToIndsModel;
	}

	
	@Override
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    	
    	int rowHeight = table.getRowHeight();
    	this.setForeground (Color.black);
    	
    	if (value instanceof SNPMarker) {
    		final SNPMarker marker = (SNPMarker)value;
    		
    		if (!markerFilter.include (marker)) {
    			this.setForeground (Color.gray);
    		}
    		//System.err.println ("marker: "+marker.getName()+", filt: "+markerFilter.getClass()+", v: "+errorModel.getInitialAllErrorMap().errorCount(marker)+", i: "+markerFilter.include (marker));
    		
	    	boolean htmlText = false;
	    	
	    	if (isSelected && expandToIndsModel.isSelected()) {
	    		final List<Individual> badIndividuals = errorModel.getCurrentAllErrorMap().getMarkerMap().get(marker);     
	    		
	    		if (badIndividuals != null && ! badIndividuals.isEmpty()) {
		    		final StringBuilder htmlList = new StringBuilder ("<HTML>"+marker.getName()+"<TABLE border=\"0\" cellspacing =\"0\" cellpadding=\"0\">");
		    		if (badIndividuals != null && !badIndividuals.isEmpty()) {
			    		for (Individual badIndividual : badIndividuals) {
			    			htmlList.append("<TR><TD height=\"10\"><font size=2>").append(badIndividual.getName()).append("</font></td></tr>");
			    		}
		    		}
		    		htmlList.append ("</TABLE></HTML>");
		    		this.setText (htmlList.toString());	
		    		
		    	    final View htmlView = (View) this.getClientProperty ("html");		
		    		rowHeight = (int)htmlView.getPreferredSpan (View.Y_AXIS);
		    		htmlText = true;
	    		} 
	    	}
	    	
	    	if (!htmlText) {
	    		this.setText (marker.getName());
	    	}
    	
	    	this.setToolTipText (marker.getName());	// Make tooltip output marker name as text, not class name + instance info that toString() produces
	    	//comp.setBackground (isSelected ? Color.yellow : Color.white);
	    	
	    	comp.setBackground (marker == errorModel.getPopCheckerContext().getFocusMarker()
	    				? Color.yellow : Color.white);	
    	}
    	
    	table.setRowHeight (row, rowHeight);
    	// stops the row resizing kicking off an endless call of repaint/draw/rowheight/repaint etc
    	RepaintManager.currentManager(table).markCompletelyClean(table);
    	return comp;
    }
}
