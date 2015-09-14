package napier.pedigree.model.impl;

import java.awt.Frame;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


import napier.pedigree.io.ErrorStrings;
import napier.pedigree.model.ChangeOnNextRecalcStore;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrix;
import napier.pedigree.model.MaskedGenotypeTotaliser;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.swing.app.EDTUpdatingTask;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;



public class DefaultErrorCollator implements ErrorCollator, PropertyChangeListener {

	static private final Logger LOGGER = Logger.getLogger (DefaultErrorCollator.class);
	
    private PropertyChangeSupport propertySupport;
	
	protected List<ErrorMatrix> initialErrorMapList;	// Errors collated when data set is first calculated
	protected List<ErrorMatrix> currentErrorMapList;	// Errors collated given current maskings of markers, individuals and genotypes
	protected List<ErrorMatrix> filteredErrorMapList;	// As currentErrorMapList, but with further Markers temporarily removed
	
	protected MaskedGenotypeTotaliser genotypeIMTotals;
	
	protected MarkerFilter markerFilter;
	protected int initialMarkerCount, initialIndividualCount, filteredMarkerCount, currentAllErrorTotal;
	protected boolean setInitial;

	protected PopCheckerWrapper popCheckerContext;
	
	protected ChangeOnNextRecalcStore changeOnRecalc;
	protected boolean recalculationNeeded, restructureNeeded;
	
	protected ErrorPutter errorPutter;
	

	public DefaultErrorCollator () {
		this (null);
	}
	
	
	public DefaultErrorCollator (final HeritablePopulation hpop) {
		super ();
		
		initialErrorMapList = new ArrayList<ErrorMatrix> ();
		currentErrorMapList = new ArrayList<ErrorMatrix> ();
		filteredErrorMapList = new ArrayList<ErrorMatrix> ();
		for (int n = 0; n < NOVEL_ALLELES + 1; n++) {
			initialErrorMapList.add (new DefaultErrorMatrix ());
			currentErrorMapList.add (new DefaultErrorMatrix ());
		}
	
		genotypeIMTotals = new DefaultMaskedGenotypeTotaliser ();
		
		initialMarkerCount = 0;
		initialIndividualCount = 0;
		filteredMarkerCount = 0;
		currentAllErrorTotal = -1; // Marks as uncalculated
		
		changeOnRecalc = new DefaultChangeOnNextRecalcStore ();
		
		propertySupport = new PropertyChangeSupport (this);
		popCheckerContext = new DefaultPopCheckerWrapper ();
		
		errorPutter = new ErrorPutter ();
		
		setModel (hpop);
	}
	
	
	@Override
	public void setModel (final HeritablePopulation hPop) {
		
		final HeritablePopulation oldPop = popCheckerContext.getPopulation();
    	if (oldPop != null) {
    		oldPop.removePropertyChangeListener (this);		
    		clear ();
    	}
		
		if (hPop != null) {
			initialMarkerCount = hPop.getActiveMarkers().size();
			initialIndividualCount = hPop.getIndividuals().size();

        	hPop.addPropertyChangeListener (this); 	
	
			popCheckerContext.setPopulation (hPop);
			popCheckerContext.setFocusMarker (null);
			
			intervention ();
			
			// Populate initial error maps
			setInitial = true;
			recalculationNeeded = true;
			recalculate ();
			setInitial = false;
		}
	}
	
	
	// hack that is overridden in multicore version of class to immediately strip out
	// that property change listener we just added to HeritablePopulation
	protected void intervention () {
		// EMPTY
	}
	
	
	// Clear all sub-objects
	protected void clear () {
		for (int n = 0; n < initialErrorMapList.size(); n++) {
			initialErrorMapList.get(n).clear();
			currentErrorMapList.get(n).clear();
		}
		
		for (int n = 0; n < filteredErrorMapList.size(); n++) {
			filteredErrorMapList.get(n).clear();
		}
		
		genotypeIMTotals.clear();
		changeOnRecalc.clear ();
	}
	
	
	
	public void recalculate () {
		LOGGER.debug ("1.in recalculate");
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info ("Markers To Recalc: "+changeOnRecalc.getMarkersToChange());
		}
		
		final SwingWorker<Void, Void> swork = new SwingWorker<Void, Void> () {
			@Override
			protected Void doInBackground() {
				setProgress (1);
				LOGGER.debug ("do 1. in doInBackground");
				recalculateThreadless();
				LOGGER.debug ("do 1. exit doInBackground");
				return null;
			}
			
	        /**
	         * Executed in EDT
	         */
	        @Override
	        protected void done() {
	        	postCalculationTasks();
	        	LOGGER.debug ("done. post recalculated property firing");
	        }
		};
		
		LOGGER.debug ("2. is EDT: "+SwingUtilities.isEventDispatchThread());
		

		
		if (SwingUtilities.isEventDispatchThread()) {
			final Window[] windows = Window.getWindows();
			final Frame frame = (Frame)windows[0];
			new EDTUpdatingTask (frame, swork, new String[] {"null", "Recalculating"}).doIt();
		} else {
			recalculateThreadless();
			postCalculationTasks ();
			LOGGER.debug ("non EDT route");
		}
		
		if (restructureNeeded) {
			propertySupport.firePropertyChange (RESTRUCTURED, Boolean.TRUE, Boolean.FALSE);
		}
		
		setRecalculationNeeded (false, 0);
		setRestructureNeeded (false);
	}
	
	
	
	protected void recalculateThreadless () {
		// Two ways to go. Either recalculate a subset of markers or recalculate everything.
		final Set<SNPMarker> recalcTheseMarkers = changeOnRecalc.getMarkersToChange();
		final boolean recalcTheseMarkersOnly = changeOnRecalc.onlyMarkersToChange() && !recalcTheseMarkers.isEmpty();
        long nano = System.nanoTime ();
		
		if (recalcTheseMarkersOnly) {
			// Remove marker info from error matrices. Don't need back-up matrix
			// as we're removing info, not restoring it.
			for (ErrorMatrix errorMatrix : currentErrorMapList) {
				errorMatrix.filterMarkers (recalcTheseMarkers, false, null);
			}
			currentAllErrorTotal = -1;	// Mark as uncalculated
			
			try {
				for (SNPMarker marker : recalcTheseMarkers) {
					popCheckerContext.getPopulation().checkSingleMarkerInheritance (marker);
				}
			} catch (final Exception exc) {
				LOGGER.error (ErrorStrings.getInstance().getString("pedigreeGenotypeCheckingError"), exc);
			}
		}
		else {
			for (ErrorMatrix errorMatrix : currentErrorMapList) {
				errorMatrix.clear ();
			}
			currentAllErrorTotal = -1;	// Mark as uncalculated
	
			try {
				popCheckerContext.getPopulation().completeInheritanceCheck();
			} catch (final Exception exc) {
				LOGGER.error (ErrorStrings.getInstance().getString("pedigreeGenotypeCheckingError"), exc);
			}
		}
		
        nano = System.nanoTime() - nano;
        LOGGER.info("marker calc time: "+nano/1E6+" ms.");
	}
	
	
	protected void postCalculationTasks () {
		propertySupport.firePropertyChange (STORE_HISTORY, Boolean.TRUE, Boolean.FALSE);
		filter ();
		propertySupport.firePropertyChange (RECALCULATED, Boolean.TRUE, Boolean.FALSE);
		propertySupport.firePropertyChange (REPAINT_NEEDED, false, true);
	}
	
	
	@Override
	public void setRecalculationNeeded (final boolean isNeeded, final int maskingFlag) {
		setRecalculationNeeded (isNeeded, maskingFlag, Collections.EMPTY_SET);
	}
	
	@Override
	public void setRecalculationNeeded (final boolean isNeeded, final int maskingFlag, 
			final Set<SNPMarker> affectedMarkers) {
		final boolean oldValue = recalculationNeeded;
		recalculationNeeded = isNeeded;
		if (isNeeded) {
			changeOnRecalc.addFlag (maskingFlag);
			changeOnRecalc.addMarkersToChange (affectedMarkers);
		} else {
			changeOnRecalc.clear();
		}
		// Property change call currently used to tell recalculate button to wake up
		propertySupport.firePropertyChange (ErrorCollator.RECALC_NEEDED, oldValue, recalculationNeeded);
		propertySupport.firePropertyChange (ErrorCollator.REPAINT_NEEDED, false, true);
	}


	
	@Override
	public void setRestructureNeeded (final boolean isNeeded) {
		final boolean oldValue = restructureNeeded;
		restructureNeeded = isNeeded;
		// Property change call currently does nowt
		propertySupport.firePropertyChange (ErrorCollator.RESTRUC_NEEDED, oldValue, restructureNeeded);
	}

	
	
	
	@Override
	public void filter () {
		
		filteredErrorMapList.clear ();
		for (int index = 0; index < currentErrorMapList.size(); index++) {
			filteredErrorMapList.add (new DefaultErrorMatrix ());
		}
		
		filteredMarkerCount = 0;
		if (markerFilter != null) {
			for (SNPMarker marker : popCheckerContext.getPopulation().getActiveMarkers()) {
				final boolean include = markerFilter.include (marker);
				filterMarker (marker, include);
				if (include) { filteredMarkerCount++; }
			}
			LOGGER.info ("inc markers "+filteredMarkerCount);
			
			propertySupport.firePropertyChange (FILTER, Boolean.TRUE, Boolean.FALSE);
			LOGGER.info ("post filter property firing");
		}
		
		genotypeIMTotals.calculateTotals (popCheckerContext.getPopulation(), null);
	}
	
	
	@Override
	public int getInitialIndividualErrorCount (final int errorMapIndex, final Individual ind) {
		final HeritableIndividual hInd = (HeritableIndividual)ind;
		if (errorMapIndex == ErrorCollator.INCOMPLETE) {
			final SNPMarker focusMarker = popCheckerContext.getFocusMarker();
			return (focusMarker == null)
				? popCheckerContext.getPopulation().countOfIncompleteGenotypes (hInd)
				: (hInd.wasGenotypeIncomplete(focusMarker) ? 1 : 0);
		}
		else if (errorMapIndex == ErrorCollator.MASKED_GENO) {
			return genotypeIMTotals.getMaskedGenotypeTotal (ind, popCheckerContext.getPopulation());
		}
		else if (errorMapIndex == ErrorCollator.MASKED_SIRE) {
			return hInd.isFatherMasked() ? initialMarkerCount : 0;
		}
		else if (errorMapIndex == ErrorCollator.MASKED_DAM) {
			return hInd.isMotherMasked() ? initialMarkerCount : 0;
		}
		final ErrorMatrix errorMap = initialErrorMapList.get (errorMapIndex);
		return getIndividualErrorCount (errorMap, ind);
	}

	
	@Override
	public int getCurrentIndividualErrorCount (final int errorMapIndex, final Individual ind) {
		if (errorMapIndex == ErrorCollator.INCOMPLETE || errorMapIndex >= ErrorCollator.MASKED_SIRE) {
			return getInitialIndividualErrorCount (errorMapIndex, ind);
		}
		else if (errorMapIndex == ErrorCollator.MASKED_GENO) {
			final SNPMarker focusMarker = popCheckerContext.getFocusMarker();
			final int masked = genotypeIMTotals.getMaskedGenotypeTotal (ind, popCheckerContext.getPopulation());
			if (focusMarker == null) {
				return masked;
			}
			final HeritablePopulation hPop = popCheckerContext.getPopulation();
			final List<String> maskedForInd = hPop.getMaskedGenotypesItoM().get (ind.getName());
			return ((maskedForInd == null || !maskedForInd.contains (focusMarker.getName())) && !hPop.getMaskedIndividuals().contains(ind) ? 0 : 1);
		}
		final ErrorMatrix errorMap = currentErrorMapList.get (errorMapIndex);
		return getIndividualErrorCount (errorMap, ind);
	}
	
	
	@Override
	public int getFilteredIndividualErrorCount (final int errorMapIndex, final Individual ind) {
		if (errorMapIndex == ErrorCollator.INCOMPLETE || errorMapIndex >= ErrorCollator.MASKED_SIRE) {
			return getInitialIndividualErrorCount (errorMapIndex, ind);
		}
		else if (errorMapIndex == ErrorCollator.MASKED_GENO) {
			final SNPMarker focusMarker = popCheckerContext.getFocusMarker();
			final int masked = genotypeIMTotals.getMaskedGenotypeTotal (ind, popCheckerContext.getPopulation());
			if (focusMarker == null) {
				return masked;
			}
			final HeritablePopulation hPop = popCheckerContext.getPopulation();
			final List<String> maskedForInd = hPop.getMaskedGenotypesItoM().get (ind.getName());
			return ((maskedForInd == null || !maskedForInd.contains (focusMarker.getName())) && !hPop.getMaskedIndividuals().contains(ind) ? 0 : 1);
		}
		final ErrorMatrix errorMap = filteredErrorMapList.get (errorMapIndex);
		return getIndividualErrorCount (errorMap, ind);
	}
	
	
	protected int getIndividualErrorCount (final ErrorMatrix errorMap, final Individual ind) {
		return errorMap.errorCount (ind);
	}
	
		
	@Override
	public int getInitialMarkerErrorCount (final int errorMapIndex, final SNPMarker marker) {
		if (errorMapIndex == ErrorCollator.INCOMPLETE) {
			if (popCheckerContext.getPopulation().getIncompleteGenotypesMtoI().get(marker) == null) {
				return 0;
			} else {
				return popCheckerContext.getPopulation().countOfIncompleteGenotypes (marker);
			}
		}
		else if (errorMapIndex == ErrorCollator.MASKED_GENO) {
			return genotypeIMTotals.getMaskedGenotypeTotal (marker, popCheckerContext.getPopulation());
		}
		final ErrorMatrix errorMap = initialErrorMapList.get (errorMapIndex);
		return getMarkerErrorCount (errorMap, marker);
	}
	
	
	@Override
	public int getCurrentMarkerErrorCount (final int errorMapIndex, final SNPMarker marker) {
		if (errorMapIndex == ErrorCollator.INCOMPLETE || errorMapIndex == ErrorCollator.MASKED_GENO) {
			return getInitialMarkerErrorCount (errorMapIndex, marker);
		}
		final ErrorMatrix errorMap = currentErrorMapList.get (errorMapIndex);
		return getMarkerErrorCount (errorMap, marker);
	}
	
	
	protected int getMarkerErrorCount (final ErrorMatrix errorMap, final SNPMarker marker) {
		return errorMap.errorCount (marker);
	}

	
	@Override
	public int getInitialMarkerSize () { return initialMarkerCount; }

	@Override
	public int getInitialIndividualsSize () { return initialIndividualCount; }
	
	@Override
	public int getFilteredMarkerSize () { return filteredMarkerCount; }
	
	
	@Override
	public ErrorMatrix getInitialAllErrorMap () { return getInitialErrorMap (ErrorCollator.ANY_ERROR); }

	@Override
	public ErrorMatrix getCurrentAllErrorMap () { return getCurrentErrorMap (ErrorCollator.ANY_ERROR); }
	
	
	@Override
	public int getCurrentAllErrorTotal () {
		if (currentAllErrorTotal < 0) { // Lazy calculate
			currentAllErrorTotal = 0;
			final Set<Individual> inds = getCurrentAllErrorMap().getIndividualMap().keySet();
			for (Individual ind : inds) {
				currentAllErrorTotal += getCurrentAllErrorMap().errorCount(ind);
			}
		}
		
		return currentAllErrorTotal;
	}
	/*
	
	public void add (final CollatedErrorModel otherBgm) {
		final Map<Individual, Integer> otherMap = otherBgm.getErrorMap (otherBgm.ERROR_COUNT);
		for (Map.Entry<Individual, Integer> entry : otherMap.entrySet()) {
			incGenotypeErrorCount (this.badCountPerIndividual, entry.getKey(), entry.getValue().intValue());
		}
		
		final Map[] errorMaps = {getErrorMap (BAD_DAM), getErrorMap (BAD_SIRE),
				getErrorMap (NOVEL_ALLELES), getErrorMap (NOVEL_MARKERS)};
		final Map[] otherErrorMaps = {otherBgm.getErrorMap (BAD_DAM), otherBgm.getErrorMap (BAD_SIRE),
				otherBgm.getErrorMap (NOVEL_ALLELES), otherBgm.getErrorMap (NOVEL_MARKERS)};
		
		for (int index = 0; index < errorMaps.length; index++) {
			final Map<Individual, Integer> errorMap = (Map<Individual, Integer>)errorMaps [index];
			final Map<Individual, Integer> otherErrorMap = (Map<Individual, Integer>)otherErrorMaps [index];
			
			for (Map.Entry<Individual, Integer> entry : otherErrorMap.entrySet()) {
				incGenotypeErrorCount (errorMap, entry.getKey(), entry.getValue().intValue());
			}
		}
		
		System.err.println (this.toString());
	}
	
	*/
	

	
	protected int getErrorMapCount () {
		return initialErrorMapList.size();
	}
	
	
	@Override
	public ErrorMatrix getInitialErrorMap (final int errorMapIndex) {

		if (errorMapIndex >= 0 && errorMapIndex < getErrorMapCount()) {
			return initialErrorMapList.get(errorMapIndex);
		}

		return null;
	}
	
	
	@Override
	public ErrorMatrix getCurrentErrorMap (final int errorMapIndex) {

		if (errorMapIndex >= 0 && errorMapIndex < getErrorMapCount()) {
			return currentErrorMapList.get(errorMapIndex);
		}

		return null;
	}
	
	

	protected void filterMarker (final SNPMarker marker, final boolean include) {
		for (int index = 0; index < currentErrorMapList.size(); index++) {
			filteredErrorMapList.get(index).filterMarker (
					marker, include, currentErrorMapList.get (index));
		}
	}
	
	
	
	@Override
	public void setFilter (final MarkerFilter newMarkerFilter) {
		markerFilter = newMarkerFilter;
	}
	
	@Override
	public boolean isFilteredMarker (final SNPMarker marker) {
		return markerFilter.include (marker);
	}

		
	
	@Override
	public PopCheckerWrapper getPopCheckerContext () { return popCheckerContext; }

	
	
	// used to fire property changes from here to elsewhere
	@Override
	public PropertyChangeSupport getPropertyChangeSupport () {
		return propertySupport;
	}
	
	
	// Listens to property changes from elsewhere. Reciprocal of above.
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();

        //adding a treeset of bad individuals to the treemap of bad markers to bad inds
        if (HeritablePopulation.BAD_MARKER_INDIVIDUALS.equals (propertyName)) {
      
        	if (LOGGER.isInfoEnabled()) {
        		LOGGER.info (Thread.currentThread()+" "+evt.getNewValue());
        	}
        	
        	final AbstractMap.SimpleEntry<SNPMarker, List<HeritableIndividual>> entry = 
        			(AbstractMap.SimpleEntry<SNPMarker, List<HeritableIndividual>>) evt.getNewValue();
           // final Set<Map.Entry<SNPMarker, Set<HeritableIndividual>>> entries = badMarkers.entrySet();
            
           // for (Map.Entry<SNPMarker, Set<HeritableIndividual>> entry : entries) {
            	final SNPMarker marker = entry.getKey();
            	
            	for (HeritableIndividual ind : entry.getValue()) {

            		if (setInitial) {
            			addInitialError (marker, ind);
            		}
            		addCurrentError (marker, ind);
            	}
           // }
            	
           // Possible route to update things that listen to error matrices (like marker tables)
           //for (ErrorMatrix emm : currentErrorMapList) {
        	//   if (emm instanceof AbstractErrorMatrix) {
        	//	   ((AbstractErrorMatrix)emm).fireErrorMatrixChanged (new ErrorMatrixEvent (emm));
        	//   }
           //}
        }
	}
	
	
	protected void addInitialError (final SNPMarker marker, final HeritableIndividual ind) {
		errorPutter.addError (marker, ind, initialErrorMapList);
	}
	
	
	protected void addCurrentError (final SNPMarker marker, final HeritableIndividual ind) {
		errorPutter.addError (marker, ind, currentErrorMapList);
	}
	
		
	
	
	public String toString () {
		final StringBuilder sBuilder = new StringBuilder ();
		final String newLine = System.getProperty ("line.separator");
		
		sBuilder.append ("Error List" + newLine);
		final List<ErrorMatrix>[] mapListArray = new List[] {initialErrorMapList, currentErrorMapList};
		final String[] descriptors = {"Initial Errors", "Current Errors"};
		
		for (int n = 0; n < mapListArray.length; n++) {
			final List<ErrorMatrix> mapList = mapListArray [n];
			final Set<Map.Entry<Individual, List<SNPMarker>>> entrySet = mapList.get(ANY_ERROR).getIndividualMap().entrySet();
			sBuilder.append (descriptors[n]).append(newLine);
			
			for (Map.Entry<Individual, List<SNPMarker>> entry : entrySet) {
				final Individual key = entry.getKey();
				final String name = key.getName();
				sBuilder.append (name+" has "+entry.getValue().size()+" total errors.\t");
				
				int val = mapList.get(BAD_DAM).errorCount(key);
				if (val > 0) {
					sBuilder.append (val+" bad dams.\t");
				}
				val = mapList.get(BAD_SIRE).errorCount(key);
				if (val > 0) {
					sBuilder.append (val+" bad sires.\t");
				}
				val = mapList.get(NOVEL_ALLELES).errorCount(key);
				if (val > 0) {
					sBuilder.append (val+" novelAlleles.\t");	
				}
				sBuilder.append (newLine);
			}
		}
		
		return sBuilder.toString();
	}
	
	
	
	/**
	 * Outputs the current difference between initial and filtered maps. For manual error checking.
	 */
	public String compareMaps () {
		
		final ErrorMatrix initialMap = this.getInitialAllErrorMap();
		final ErrorMatrix currentMap = this.getCurrentAllErrorMap();
		final StringBuilder sBuilder = new StringBuilder ();
		final String newLine = System.getProperty ("line.separator");
		
		sBuilder.append (newLine + "Individual Differences" + newLine);
		final Map<Individual, List<SNPMarker>> indInitialMap = initialMap.getIndividualMap();
		final Map<Individual, List<SNPMarker>> indCurrentMap = currentMap.getIndividualMap();
		
		final Set<Individual> initInd = indInitialMap.keySet();
		for (Individual ind : initInd) {
			final List<SNPMarker> initMarkers = indInitialMap.get(ind);
			final List<SNPMarker> currentMarkers = indCurrentMap.get(ind);
			if (currentMarkers == null || ! initMarkers.containsAll (currentMarkers)) {
				sBuilder.append ("Difference: "+ind+" init: "+initMarkers+newLine+"\t\t current: "+currentMarkers);
			}
		}
			
		sBuilder.append (newLine + "Marker Differences" + newLine);
		final Map<SNPMarker, List<Individual>> marInitialMap = initialMap.getMarkerMap();
		final Map<SNPMarker, List<Individual>> marCurrentMap = currentMap.getMarkerMap();
		
		final Set<SNPMarker> initMarker = marInitialMap.keySet();
		for (SNPMarker marker : initMarker) {
			final List<Individual> initInds = marInitialMap.get(marker);
			final List<Individual> currentInds = marCurrentMap.get(marker);
			if (currentInds == null || ! initInds.containsAll (currentInds)) {
				sBuilder.append ("Difference: "+marker+" init: "+initInds+newLine+"\t\t current: "+currentInds);
			}
		}
		
		return sBuilder.toString();
	}
	
	

	/**
	 * Put the addError method in a separate class so we can call multiple instances of it in the multicore version of this class.
	 * @author cs22
	 *
	 */
	protected class ErrorPutter {
		
		protected void addError (final SNPMarker marker, final HeritableIndividual ind, final List<ErrorMatrix> errorMapList) {

			if (ind.isGenotypeInconsistent (marker)) {
					
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug ("all errors: "+marker.getName()+" ind: "+ind.getName()+", dam: "+ind.getDam().getName());
				}
				errorMapList.get(ErrorCollator.ANY_ERROR).addError (marker, ind);

				if (ind.genotypeHasNovelAllele (marker)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug ("novel allele: "+marker.getName()+" ind: "+ind.getName()+", dam: "+ind.getDam().getName());
					}
					errorMapList.get(ErrorCollator.NOVEL_ALLELES).addError (marker, ind);
				}
				
				if (ind.genotypeHasNilFromDam (marker)) { // changed from truth.ternary.TRUE, check new behaviour
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug ("bad dam marker: "+marker.getName()+" ind: "+ind.getName()+", dam: "+ind.getDam().getName());
					}
					errorMapList.get(ErrorCollator.BAD_DAM).addError (marker, ind);
					LOGGER.debug ("finished getErrorMap");
				}
				
				
				if (ind.genotypeHasNilFromSire (marker)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug ("bad sire marker: "+marker.getName()+" ind: "+ind.getName()+" "+", sire: "+ind.getSire().getName());
					}
					errorMapList.get(ErrorCollator.BAD_SIRE).addError (marker, ind);
				}	
			}	
		}
	}
}
