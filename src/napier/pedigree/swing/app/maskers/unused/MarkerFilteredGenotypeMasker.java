package napier.pedigree.swing.app.maskers.unused;

import java.util.List;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import swingPlus.histogram.JHistogram;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.HistogramValueFilter;
import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.swing.AbstractHistogramListener;


public class MarkerFilteredGenotypeMasker extends AbstractHistogramListener {

	static final private Logger LOGGER = Logger.getLogger (MarkerFilteredGenotypeMasker.class);
	
	//protected JHistogram filteredIndividualErrorgram;
	protected ErrorCollator errorModel;
	
	public MarkerFilteredGenotypeMasker (final ErrorCollator errorModel, final JHistogram linkedHistogram) {
		super ();
		//filteredIndividualErrorgram = linkedHistogram;
		this.errorModel = errorModel;
	}
	
	
	@Override
	public void doStuff (final AbstractErrorHistogram histo, final int curBottomValue, final int curTopValue) {
		if (errorModel != null) {
				
			final HistogramValueFilter filter = histo.getHistogramValueFilter();
			final MarkerFilter markerFilter = (MarkerFilter)filter;
			final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
			final HeritablePopulation hPop = popContext.getPopulation();
			
			boolean needRecalc = false;
			
			if (filter != null) {
				if (hPop.getMaskedGenotypesMtoI() != null
						&& !hPop.getMaskedGenotypesMtoI().isEmpty()) {
					needRecalc = true;
					//hPop.unmaskAllGenotypes(); // ooh, controversial
				}

				LOGGER.info ("Genotype masking/unmasking "+curTopValue+" "+lastTopVal);
				
				//if (histo.getExtent() != 0) {
					for (SNPMarker marker : popContext.getPopulation().getActiveMarkers()) {
						if (!markerFilter.include (marker)) {
							maskBadGenotypesForMarker (marker, true);
						}
					}
					needRecalc = true;
				//}
					
				
				if (needRecalc) {
					//System.err.println ("calling recalc from GenotypeMasker");
					errorModel.recalculate();
				}
			}
		}	
	}
	
	protected void maskBadGenotypesForMarker (final SNPMarker marker, final boolean mask) {
		final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
		final HeritablePopulation hPop = popContext.getPopulation();	
		
		final String markerName = marker.getName();
		
		//hPop.get
        //where is this hash of bad inds now? - it is in the appropriate error map
        //final Set<HeritableIndividual> inds = pop.getBadIndividualsByMarkerName (marker.getName());
        
        final List<Individual> inds = errorModel.getInitialAllErrorMap().getMarkerMap().get(marker);     
        
        if (inds != null && !inds.isEmpty()) {

			if (mask) {
				for (Individual ind : inds) {
					hPop.maskGenotype (ind.getName(), markerName);
				}
			} else {
				for (Individual ind : inds) {
					hPop.unmaskGenotype (ind.getName(), markerName);
				}
			}
			
			LOGGER.info ((mask ? "  mask" : "unmask") + " genotype ["+inds.size()+", "+inds.toString()+", "+markerName+"]");
		}
	}	

}
