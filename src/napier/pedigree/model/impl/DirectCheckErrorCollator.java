package napier.pedigree.model.impl;

import java.util.List;
import java.util.Set;

import napier.pedigree.io.ErrorStrings;
import napier.pedigree.model.ErrorMatrix;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;


public class DirectCheckErrorCollator extends DefaultErrorCollator {

	
	static private final Logger LOGGER = Logger.getLogger (DirectCheckErrorCollator.class);
	
	public DirectCheckErrorCollator () {
		this (null);
	}
	
	
	public DirectCheckErrorCollator (final HeritablePopulation hPop) {
		super (hPop);
	}
	
	
	@Override
	protected void intervention () {
		if (popCheckerContext.getPopulation() != null) {
			popCheckerContext.getPopulation().removePropertyChangeListener (this); // Property change listener in one object is multicore bottleneck, so do things differently
		}
	}
	
	
	protected void recalculateThreadless () {

		// Two ways to go. Either recalculate a subset of markers or recalculate everything.
		final Set<SNPMarker> recalcTheseMarkers = changeOnRecalc.getMarkersToChange();
		final boolean recalcTheseMarkersOnly = changeOnRecalc.onlyMarkersToChange() && !recalcTheseMarkers.isEmpty();
		final Set<SNPMarker> recalcMarkers = recalcTheseMarkersOnly ? recalcTheseMarkers : popCheckerContext.getAllMarkers();
		long nano = System.nanoTime ();
		LOGGER.info ("Recalculating everything: "+(!recalcTheseMarkersOnly));
		
		if (recalcTheseMarkersOnly) {
			// Remove marker info from error matrices. Don't need back-up matrix
			// as we're removing info, not restoring it.
			for (ErrorMatrix errorMatrix : currentErrorMapList) {
				errorMatrix.filterMarkers (recalcTheseMarkers, false, null);
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info ("markers to change: "+recalcTheseMarkers);
			}
		}
		else {
			for (ErrorMatrix errorMatrix : currentErrorMapList) {
				errorMatrix.clear ();
			}
		}
		
		currentAllErrorTotal = -1;	// Mark as uncalculated
		
		try {
			int z = 0;
			for (SNPMarker marker : recalcMarkers) {
				final List<HeritableIndividual> badInds = popCheckerContext.getPopulation().checkSingleMarkerInheritance (marker);
				if (badInds != null) {
					z++;
					if (z % 100 == 0 && LOGGER.isInfoEnabled()) {
						//LOGGER.info ("Processing Marker: "+marker);
					}
					for (HeritableIndividual badInd : badInds) {
	            		if (setInitial) {
	            			errorPutter.addError (marker, badInd, initialErrorMapList);
	            		}
	            		errorPutter.addError (marker, badInd, currentErrorMapList);
					}
				}
			}
		} catch (final Exception exc) {
			LOGGER.error (ErrorStrings.getInstance().getString("pedigreeGenotypeCheckingError"), exc);
		}
		
        nano = System.nanoTime() - nano;
        LOGGER.info("marker calc time: "+nano/1E6+" ms.");
	}
}
