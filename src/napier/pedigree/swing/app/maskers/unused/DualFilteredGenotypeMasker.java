package napier.pedigree.swing.app.maskers.unused;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import swingPlus.histogram.JHistogram;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.filter.IndividualFilter;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.HistogramValueFilter;
import napier.pedigree.model.filter.impl.CompoundIndividualFilters;
import napier.pedigree.model.filter.impl.CompoundMarkerFilters;
import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.swing.AbstractHistogramListener;


public class DualFilteredGenotypeMasker extends AbstractHistogramListener {

	static final private Logger LOGGER = Logger.getLogger (DualFilteredGenotypeMasker.class);
	
	//protected JHistogram filteredIndividualErrorgram;
	protected ErrorCollator errorModel;
	
	protected Map<JHistogram, HistogramValueFilter> histogramFilters;
	
	public DualFilteredGenotypeMasker (final ErrorCollator errorModel, final JHistogram linkedHistogram) {
		super ();
		//filteredIndividualErrorgram = linkedHistogram;
		this.errorModel = errorModel;
		histogramFilters = new HashMap<JHistogram, HistogramValueFilter> ();
	}
	
	
	
	@Override
	public void doStuff (final AbstractErrorHistogram histo, final int curBottomValue, final int curTopValue) {
		if (errorModel != null) {

			final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
			final HeritablePopulation hPop = popContext.getPopulation();

			final Set<MarkerFilter> markerFilters = new HashSet<MarkerFilter> ();
			final Set<IndividualFilter> individualFilters = new HashSet<IndividualFilter> ();
			
			for (HistogramValueFilter pFilter : histogramFilters.values()) {
				if (pFilter instanceof MarkerFilter) {
					markerFilters.add ((MarkerFilter)pFilter);
				}
				else if (pFilter instanceof IndividualFilter) {
					individualFilters.add ((IndividualFilter)pFilter);
				}
			}
			
			boolean needRecalc = false;
			
			if (hPop.getMaskedGenotypesMtoI() != null
					&& !hPop.getMaskedGenotypesMtoI().isEmpty()) {
				needRecalc = true;
				//hPop.unmaskAllGenotypes(); // ooh, controversial
			}
			
			needRecalc = true;
			doMarkerFilters (markerFilters);
			doIndividualFilters (individualFilters);
				
			if (needRecalc) {
				LOGGER.info ("calling recalc from GenotypeMasker");
				errorModel.recalculate();
			}
		}	
	}
	
	
	protected void doMarkerFilters (final Set<MarkerFilter> markerFilters) {
		final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
		final HeritablePopulation hPop = popContext.getPopulation();
		
		if (!markerFilters.isEmpty()) {
			final MarkerFilter orFilter = CompoundMarkerFilters.orFilter (markerFilters);
			//final Set<SNPMarker> allMarkers = new HashSet<SNPMarker> (popContext.getAllMarkers());
			//final Set<SNPMarker> allMarkers = new HashSet<SNPMarker> (hPop.getActiveMarkers());
			//allMarkers.addAll (hPop.getMaskedMarkers());
			
			for (SNPMarker marker : hPop.getActiveMarkers()) {
				if (!orFilter.include (marker)) {
					maskBadGenotypesForMarker (marker, true);
				}
			}
		}
		
	}
	
	
	protected void doIndividualFilters (final Set<IndividualFilter> indFilters) {
		final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
		final HeritablePopulation hPop = popContext.getPopulation();
		
		if (!indFilters.isEmpty()) {
			//final IndividualFilter oneFil = indFilters.iterator().next();
			//System.err.println ("one fil: "+oneFil.toString()+", filSize: "+indFilters.size());
			final IndividualFilter orFilter = CompoundIndividualFilters.orFilter (indFilters);
			
			//final Set<HeritableIndividual> masked = hPop.getMaskedIndividuals();
			final Set<Individual> active = new HashSet<Individual> (hPop.getIndividuals());
			//active.removeAll (masked);
			
			for (Individual individual : active) {
				if (!orFilter.include (individual)) {
					maskBadGenotypesForIndividual (individual, true);
					if (LOGGER.isDebugEnabled()) {
						final List<SNPMarker> badMarkersForInd = errorModel.getCurrentAllErrorMap().getIndividualMap().get(individual);
						if (badMarkersForInd != null) {
							LOGGER.info ("masked individual "+individual.getName()+"'s genotypes");
							LOGGER.info ("has errors in "+errorModel.getCurrentAllErrorMap().getIndividualMap().get(individual));
						}
					}
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
        
        final List<Individual> inds = errorModel.getCurrentAllErrorMap().getMarkerMap().get(marker);     
        
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
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info ((mask ? "  mask" : "unmask") + " genotype ["+inds.size()+", "+inds.toString()+", "+markerName+"]");
			}
        }
	}	
	
	
	protected void maskBadGenotypesForIndividual (final Individual individual, final boolean mask) {
		final PopCheckerWrapper popContext = errorModel.getPopCheckerContext();
		final HeritablePopulation hPop = popContext.getPopulation();	
		
		final String indName = individual.getName();
		
		//hPop.get
        //where is this hash of bad inds now? - it is in the appropriate error map
        //final Set<HeritableIndividual> inds = pop.getBadIndividualsByMarkerName (marker.getName());
        
        final List<SNPMarker> markers = errorModel.getCurrentAllErrorMap().getIndividualMap().get(individual);     
        
        if (markers != null && !markers.isEmpty()) {

			if (mask) {
				for (SNPMarker marker : markers) {
					hPop.maskGenotype (indName, marker.getName());
				}
			} else {
				for (SNPMarker marker : markers) {
					hPop.unmaskGenotype (indName, marker.getName());
				}
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info ((mask ? "  mask" : "unmask") + " genotype ["+markers.size()+", "+markers.toString()+", "+indName+"]");
			}
        }
	}

	
	public void putHistogramFilter (final JHistogram histogram, final HistogramValueFilter filter) {
		histogramFilters.put (histogram, filter);
	}
}
