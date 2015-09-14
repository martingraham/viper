package napier.pedigree.swing.renderers.ped;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingConstants;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.base.HexagonalGlyphRenderer;

import org.resspecies.model.Individual;

import util.Messages;



public class OffspringMultiErrorRenderer extends HexagonalGlyphRenderer {
	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;


	
	
    public OffspringMultiErrorRenderer (final ErrorCollator bgm, final BoundedRangeModel brm) {
    	super (bgm, brm);
	    // Don't paint behind the component
	    setOpaque (false);
	    setBackground (Color.red); // garish colour just so we can be certain no background rendering is happening
	    this.setFont (Font.decode (Messages.getString ("napier.pedigree.graphics", "regularFont")));
	    this.setForeground (Color.black);
	    this.setHorizontalAlignment (SwingConstants.CENTER);
    }
    

    @Override
    protected void calculateErrorValues () {
    	//for (int n = 0; n < errors.length; n++) {
    	//	errors [n] = 0;
    	//}
    	Arrays.fill (errors, 0);
    		
    	for (Individual thisInd : offspring) {
	    	for (int type = 0; type < errors.length; type++) {
		   	    updateValue (errors, type, errorModel.getFilteredIndividualErrorCount (type, thisInd));
	    	}
		}
	    
	    finaliseValues (errors);
	    
	    //final Genotype geno = thisInd.getGenotype (bgm.getFocusMarker());
		//final boolean noFocusMarkerSet = (errorModel.getPopCheckerContext().getFocusMarker() == null);
	    //inferredPercent = (double) errors [ErrorCollator.INCOMPLETE] / 
		//	(noFocusMarkerSet ? (double)errorModel.getInitialMarkerSize() : 1.0);
    }
    

	@Override
	protected void updateValue (final int[] values, final int index, final int newValue) {
		 values [index] += newValue;
	}


	@Override
	protected void finaliseValues (int[] values) {
	    for (int type = 0; type < values.length; type++) {
	    	//if (values [type] > 20) {
	    	//	System.err.println ("value: "+values[type]+" over "+offspring.size()+" offspring.");
	    	//}
	    	values [type] = (int)Math.ceil ((double)values [type] / (double)offspring.size());
	    }
	}
}
