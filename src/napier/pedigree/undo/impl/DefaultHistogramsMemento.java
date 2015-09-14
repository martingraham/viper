package napier.pedigree.undo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoundedRangeModel;

import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.undo.Memento;

public class DefaultHistogramsMemento implements Memento<Collection<AbstractErrorHistogram>> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7722737867821460461L;
	
	protected Map<BoundedRangeModel, List<Integer>> modelValueMap;
	
	
	public DefaultHistogramsMemento () {
		super ();
		modelValueMap = new HashMap <BoundedRangeModel, List<Integer>> ();
	}
	
	@Override
	public void make (final Collection<AbstractErrorHistogram> histograms) {
		
		final Set<BoundedRangeModel> rangeModels = new HashSet<BoundedRangeModel> ();
		for (AbstractErrorHistogram histogram : histograms) {
			rangeModels.add (histogram.getModel());
		}		
		
		for (BoundedRangeModel brm : rangeModels) {
			final List<Integer> vals = new ArrayList<Integer> ();
			// store in the order that setRangeProperties wants them back in
			vals.addAll ((List<Integer>)Arrays.asList (brm.getValue(), brm.getExtent(), brm.getMinimum(), brm.getMaximum()));
			modelValueMap.put (brm, vals);
		}
	}

	@Override
	public void restore (final Collection<AbstractErrorHistogram> histograms) {
		
		final Set<BoundedRangeModel> rangeModels = new HashSet<BoundedRangeModel> ();
		for (AbstractErrorHistogram histogram : histograms) {
			rangeModels.add (histogram.getModel());
			// to stop histogram firing events to errorcollator etc when the model is restored
			// as masking state is restored with another memento class
			histogram.blockAbstractHistogramListeners (true); 
		}	
		
		for (BoundedRangeModel brm : rangeModels) {
			final List<Integer> vals = modelValueMap.get (brm);
			if (vals != null) {
				brm.setRangeProperties (vals.get(0), vals.get(1), vals.get(2), vals.get(3), false);
			}
		}
		
		// allow events to affect these histograms again
		for (AbstractErrorHistogram histogram : histograms) {
			histogram.blockAbstractHistogramListeners (false);
		}
	}

	@Override
	public BitSet compare (final Collection<AbstractErrorHistogram> histograms) {
		final BitSet differences = new BitSet ();
		return differences;
	}
}
