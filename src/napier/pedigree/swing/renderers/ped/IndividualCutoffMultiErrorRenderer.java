package napier.pedigree.swing.renderers.ped;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BoundedRangeModel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.base.HexagonalGlyphRenderer;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.SNPMarker;


import swingPlus.graph.JGraph;
import util.Messages;


public class IndividualCutoffMultiErrorRenderer extends HexagonalGlyphRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;
	
	
	
	public IndividualCutoffMultiErrorRenderer (final ErrorCollator bgm, final BoundedRangeModel brm) {
    	super (bgm, brm);
	    // Don't paint behind the component
	    setOpaque (false); // remove for background colours
	    setBackground (Color.white); // garish colour just so we can be certain no background rendering inputStream happening
	    this.setFont (Font.decode (Messages.getString ("napier.pedigree.graphics", "regularFont")));
	    setForeground (Color.black);
	    this.setHorizontalAlignment (SwingConstants.CENTER);
    }

	public Component getTableCellRendererComponent (final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final Component comp = super.getTableCellRendererComponent (table,
				value, isSelected, hasFocus, row, column);
		
		setPossibleSingleMarkerValueLabel (value);
		return comp;
	}
	
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		final Component comp = super.getGraphCellRendererComponent (graph,
				value, isSelected, hasFocus);
		
		setPossibleSingleMarkerValueLabel (value);
		return comp;
	}
	
	
	public void setPossibleSingleMarkerValueLabel (final Object value) {
		if (value instanceof HeritableIndividual) {	
			final SNPMarker focusMarker = errorModel.getPopCheckerContext().getFocusMarker();
			if (focusMarker != null) {
				//System.err.println ("bgm: "+bgm.getFocusMarker().getName());
				this.setText (thisInd.getShortGenotypeString (focusMarker));
				label = this.getText();
				//final List<Marker> indInfMarkerList = bgm.getErrorMap (CollatedErrorModel.INFERRED).getIndividualMap().get(ind);
			}
		}
	}
}
