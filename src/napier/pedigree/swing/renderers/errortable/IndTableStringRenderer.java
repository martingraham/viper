package napier.pedigree.swing.renderers.errortable;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.ButtonModel;
import javax.swing.JTable;
import javax.swing.RepaintManager;
import javax.swing.SwingConstants;
import javax.swing.text.View;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PedigreeSelectable;
import napier.pedigree.model.PedigreeSelectionModel;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.filter.IndividualFilter;
import napier.pedigree.swing.renderers.base.AbstractErrorRenderer;


public class IndTableStringRenderer extends AbstractErrorTableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2984808199537154677L;

	protected IndividualFilter filter;
	protected final ButtonModel showGenotypeButtonModel;
	protected final ButtonModel expandToMarkersModel;

	public IndTableStringRenderer (final ErrorCollator bgm, final BoundedRangeModel greyCutoff, final IndividualFilter rangeOnly,
			final ButtonModel showGenoButtonModel, final ButtonModel expandToMarkersModel) {
		super (bgm, greyCutoff);
		this.setVerticalAlignment (SwingConstants.TOP);
		filter = rangeOnly;
		showGenotypeButtonModel = showGenoButtonModel;
		this.expandToMarkersModel = expandToMarkersModel;
	}

	@Override
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	final Component comp = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    	int rowHeight = table.getRowHeight();
    	boolean extraSelected = isSelected;
    	
    	if (value instanceof Individual) {	
    		final Individual ind = (Individual)value;
    		this.setForeground (filter.include (ind) ? Color.black : FILTER_COLOUR);
    	
    		final PopCheckerWrapper pcw = errorModel.getPopCheckerContext();
    		final SNPMarker focusMarker = pcw.getFocusMarker();
    		if (focusMarker != null && showGenotypeButtonModel.isSelected()) {
    			final int genotype = pcw.getPopulation().getCheckedGenotype (focusMarker, (HeritableIndividual)ind);
    			final String geno = focusMarker.getLongGenotypeString (genotype);
    			this.setText (geno+" "+getText());
    		}
    		
    		if (isSelected && expandToMarkersModel.isSelected()) {
	    		final List<SNPMarker> badMarkers = errorModel.getCurrentAllErrorMap().getIndividualMap().get(ind);     
	    		
	    		if (badMarkers != null && ! badMarkers.isEmpty()) {
		    		final StringBuilder htmlList = new StringBuilder ("<HTML>"+this.getText()+"<TABLE border=\"0\" cellspacing =\"0\" cellpadding=\"0\">");
		    		for (SNPMarker badMarker : badMarkers) {
		    			htmlList.append("<TR><TD height=\"10\"><font size=2>").append(badMarker.getName()).append("</font></td></tr>");
		    		}
		    		htmlList.append ("</TABLE></HTML>");
		    		this.setText (htmlList.toString());	
		    		
		    	    final View htmlView = (View) this.getClientProperty ("html");		
		    		rowHeight = (int)htmlView.getPreferredSpan(View.Y_AXIS);
	    		}
    		}
    		
    		if (table instanceof PedigreeSelectable) {
    			final PedigreeSelectionModel psm = ((PedigreeSelectable)table).getPedigreeSelection();
    			if (psm != null && psm.getSelectedGraph() != null) {
    				extraSelected = isSelected | psm.getSelectedGraph().containsNode (ind);
    			}
    		}
        	
        	final int allErrorCount = errorModel.getInitialIndividualErrorCount (ErrorCollator.ANY_ERROR, ind);
        	final boolean include = (allErrorCount >= rangeModel.getValue());
        	this.setForeground (include ? Color.black : FILTER_COLOUR);
    	}
    	
    	comp.setBackground (extraSelected ? AbstractErrorRenderer.getSelectedColour() : Color.white);
    	table.setRowHeight (row, rowHeight);
    	// stops the row resizing kicking off an endless call of repaint/draw/rowheight/repaint etc
    	RepaintManager.currentManager(table).markCompletelyClean(table);
    	return comp;
    }
}
