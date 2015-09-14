package napier.pedigree.model.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import napier.pedigree.io.ErrorStrings;
import napier.pedigree.model.ErrorMatrix;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import util.threads.LatchRunnable;
import util.threads.ParallelCollectionProcess;

public class MultiCoreErrorCollator extends DefaultErrorCollator {

	
	static private final Logger LOGGER = Logger.getLogger (MultiCoreErrorCollator.class);
	
	public MultiCoreErrorCollator () {
		this (null);
	}
	
	
	public MultiCoreErrorCollator (final HeritablePopulation hPop) {
		super (hPop);
	}
	
	
	@Override
	protected void intervention () {
		System.err.println ("hello");
		if (popCheckerContext.getPopulation() != null) {
			popCheckerContext.getPopulation().removePropertyChangeListener (this); // Property change listener in one object is multicore bottleneck, so do things differently
		}
		
		for (int n = 0; n < initialErrorMapList.size(); n++) {
			initialErrorMapList.get(n).clear();
			currentErrorMapList.get(n).clear();
		}
		
		initialErrorMapList.clear();
		currentErrorMapList.clear();
		
		for (int n = 0; n < NOVEL_ALLELES + 1; n++) {
			initialErrorMapList.add (new MultiCoreErrorMatrix ());
			currentErrorMapList.add (new MultiCoreErrorMatrix ());
		}
	}
	
	
	
	protected void recalculateThreadless () {
		// Two ways to go. Either recalculate a subset of markers or recalculate everything.
		final HeritablePopulation pop = this.getPopCheckerContext().getPopulation();
		final Set<SNPMarker> recalcTheseMarkers = changeOnRecalc.getMarkersToChange();
		final boolean recalcTheseMarkersOnly = changeOnRecalc.onlyMarkersToChange() && !recalcTheseMarkers.isEmpty();
		final Set<SNPMarker> recalcMarkers = recalcTheseMarkersOnly ? recalcTheseMarkers : pop.getActiveMarkers();
		
		if (recalcTheseMarkersOnly) {
			// Remove marker info from error matrices. Don't need back-up matrix
			// as we're removing info, not restoring it.
			for (ErrorMatrix errorMatrix : currentErrorMapList) {
				errorMatrix.filterMarkers (recalcTheseMarkers, false, null);
			}
		}
		else {
			for (ErrorMatrix errorMatrix : currentErrorMapList) {
				errorMatrix.clear ();
			}
		}
		
		currentAllErrorTotal = -1;	// Mark as uncalculated
		
		LOGGER.info (recalcMarkers);
		
        long nano = System.nanoTime ();
        
        final ParallelMarkerCalc pnf = new ParallelMarkerCalc (pop);
        pnf.doParallel (recalcMarkers, null);
        
        nano = System.nanoTime() - nano;
        LOGGER.info("marker calc time: "+nano/1E6+" ms.");
        
        final ErrorMatrix overallErrorMap = getInitialAllErrorMap();
        final Collection<List<Individual>> errors = overallErrorMap.getMarkerMap().values();
        LOGGER.info (errors);
	}
	
	
	class ParallelMarkerCalc extends ParallelCollectionProcess {

		HeritablePopulation hPop;
		
		public ParallelMarkerCalc (final HeritablePopulation hPop) {
			super ();
			this.hPop = hPop;
		}

		@Override
		public LatchRunnable makeSubListProcess (final List<?> subList, final CountDownLatch cLatch) {
			return new CalcMarkers (hPop, subList, cLatch);
		}
		
		@Override
		public void addPartialResult (final Object mergedResult, final Object partialResult) {
			// Empty
		}
	}
	
	
	class CalcMarkers extends LatchRunnable {
		Collection<SNPMarker> markers;
		HeritablePopulation hPop;
		ErrorPutter ePutter;
		
		CalcMarkers (final HeritablePopulation hPop, final Collection<?> markers, final CountDownLatch latch) {
			super (latch);
			this.markers = (Collection<SNPMarker>)markers;
			this.hPop = hPop;
			ePutter = new ErrorPutter();
		}
		
		public void run () {
			try {
				for (SNPMarker marker : markers) {
					final List<HeritableIndividual> badInds = popCheckerContext.getPopulation().checkSingleMarkerInheritance (marker);
					if (badInds != null) {
						for (HeritableIndividual badInd : badInds) {
		            		if (setInitial) {
		            			ePutter.addError (marker, badInd, initialErrorMapList);
		            		}
		            		ePutter.addError (marker, badInd, currentErrorMapList);
						}
					}
				}
			} catch (final Exception exc) {
				LOGGER.error (ErrorStrings.getInstance().getString("pedigreeGenotypeCheckingError"), exc);
			}
	        super.run ();
		}

		public Object getResult () { return null; }
	}
}
