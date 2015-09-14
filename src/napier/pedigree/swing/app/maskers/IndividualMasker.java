package napier.pedigree.swing.app.maskers;

import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;

import swingPlus.histogram.JHistogram;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.swing.AbstractHistogramListener;


public class IndividualMasker extends AbstractHistogramListener {

	static final private Logger LOGGER = Logger.getLogger (IndividualMasker.class);
	
	protected JHistogram filteredIndividualErrorgram;
	protected ErrorCollator errorModel;
	
	public IndividualMasker (final ErrorCollator errorModel, final JHistogram linkedHistogram) {
		super ();
		filteredIndividualErrorgram = linkedHistogram;
		this.errorModel = errorModel;
	}
	
	
	@Override
	public void doStuff (final AbstractErrorHistogram histo, final int curBottomValue, final int curTopValue) {
		
			
		final NavigableMap<Integer, Number> histoData = histo.getBinnedData();
		final NavigableMap<Integer, Number> dataSubset = histoData.headMap 
			(Integer.valueOf (curBottomValue), true);
		filteredIndividualErrorgram.setData (dataSubset);
		final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();
		
		
		final Set<HeritableIndividual> masked = hPop.getMaskedIndividuals();
		final Set<Individual> active = new HashSet<Individual> (hPop.getIndividuals());
		active.removeAll (masked);
		
		final HashSet<HeritableIndividual> toMask = new HashSet<HeritableIndividual> ();
		for (Individual ind : active) {
			if (errorModel.getInitialIndividualErrorCount (ErrorCollator.ANY_ERROR, ind) > curBottomValue && active.contains (ind)) {
				toMask.add ((HeritableIndividual)ind);
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("inds toMask: "+toMask);
		}

		final HashSet<HeritableIndividual> toUnmask = new HashSet<HeritableIndividual> ();
		for (HeritableIndividual ind : masked) {
			if (errorModel.getInitialIndividualErrorCount (ErrorCollator.ANY_ERROR, ind) <= curBottomValue && masked.contains (ind)) {
				toUnmask.add (ind);
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("inds unMask: "+toUnmask);
		}
		
		for (HeritableIndividual ind : toMask) {
			hPop.maskIndividual (ind);
		}
		
		for (HeritableIndividual ind : toUnmask) {
			hPop.unmaskIndividual (ind);
		}
		
		errorModel.recalculate();
	}	
}
