package napier.pedigree.swing.renderers.ped;

import javax.swing.BoundedRangeModel;

import napier.pedigree.model.ErrorCollator;


public class OffspringMultiMaxErrorRenderer extends OffspringMultiErrorRenderer {

	
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 4729402491978632158L;

	public OffspringMultiMaxErrorRenderer (final ErrorCollator bgm, final BoundedRangeModel brm) {
    	super (bgm, brm);
    }
      
   
	@Override
	protected void updateValue (final int[] values, final int index, final int newValue) {
		values [index] = Math.max (values [index], newValue);
	}


	@Override
	protected void finaliseValues (final int[] values) {
		// EMPTY
	}
}
