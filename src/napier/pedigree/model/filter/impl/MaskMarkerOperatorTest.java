package napier.pedigree.model.filter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.filter.MarkerFilter;


public class MaskMarkerOperatorTest extends MaskMarkerOperator {

	static final private Logger LOGGER = Logger.getLogger (MaskMarkerOperatorTest.class);
	
    protected static Map<Individual, List<SNPMarker>> incompleteGenotypesItoM = new HashMap<Individual, List<SNPMarker>>();
    protected static Map<Individual, List<SNPMarker>> maskedIncompleteGenotypesItoM = new HashMap<Individual, List<SNPMarker>>();


	
	
	public void mask (final ErrorCollator errorModel, final MarkerFilter mFilter, final Set<SNPMarker> optionalMarkerSet) {
		
		final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();
		
		final Set<SNPMarker> active = hPop.getActiveMarkers();
		final Set<SNPMarker> masked = new HashSet<SNPMarker> (hPop.getMaskedMarkers()); // otherwise we end up affecting the set held in the HeritablePopulation
		
		deepCopyMaps (hPop);
		
		// Optional marker set can be used to whittle down the number of markers to mask and unmask against
		// if no set is supplied the full marker set is used
		if (optionalMarkerSet != null) {
			active.retainAll (optionalMarkerSet);
			masked.retainAll (optionalMarkerSet);
		}
	
		
		final HashSet<SNPMarker> toMask = new HashSet<SNPMarker> ();
		for (SNPMarker marker : active) {
			if (! mFilter.include (marker)) {
				toMask.add (marker);
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.info ("markers toMask: "+printMarkerSet (toMask));
		}

		final HashSet<SNPMarker> toUnmask = new HashSet<SNPMarker> ();
		for (SNPMarker marker : masked) {
			if (mFilter.include (marker)) {
				toUnmask.add (marker);
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.info ("markers unMask: "+printMarkerSet (toUnmask));
		}
		
		long nano = System.nanoTime ();
		hPop.maskMarkers (toMask);
		nano = System.nanoTime() - nano;
		LOGGER.info("Hpop. To mask "+(toMask.size() * hPop.getIndividualCount())+" genotypes ("+toMask.size()+" markers by "
				+hPop.getIndividualCount()+" inds) = "+(nano/1E6)+" ms.");
		
		nano = System.nanoTime ();
		maskMarkers (hPop, toMask);
		nano = System.nanoTime() - nano;
		LOGGER.info("New .To mask "+(toMask.size() * hPop.getIndividualCount())+" genotypes ("+toMask.size()+" markers by "
				+hPop.getIndividualCount()+" inds) = "+(nano/1E6)+" ms.");

		nano = System.nanoTime ();
		hPop.unmaskMarkers (toUnmask);
		nano = System.nanoTime() - nano;
		LOGGER.info("Hpop .To unmask "+(toUnmask.size() * hPop.getIndividualCount())+" genotypes ("+toUnmask.size()+" markers by "
				+hPop.getIndividualCount()+" inds) = "+(nano/1E6)+" ms.");

		nano = System.nanoTime ();
		unmaskMarkers (hPop, toUnmask);
		nano = System.nanoTime() - nano;
		LOGGER.info("New .To unmask "+(toUnmask.size() * hPop.getIndividualCount())+" genotypes ("+toUnmask.size()+" markers by "
				+hPop.getIndividualCount()+" inds) = "+(nano/1E6)+" ms.");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("calling recalc from MarkerMasker");
		}
		
		deepCompareMaps (hPop);
	}
	
	
    /**
     * Masking markers has the effect of removing them from the active Set of Markers 
     * (and storing them for later retrieval). Fires PropertyChange event of type 
     * MASK_MARKERS forwarding the list of Markers to mask;
     * @param marker 
     */
    public void maskMarkers (HeritablePopulation hPop, Collection<SNPMarker> markers) {
        
        if (markers==null || markers.isEmpty()) {
            return;
        }

        //this.maskedMarkers.addAll(markers);
        
        //masking a marker is the same as deleting it - so i want to remove any 
        //consideration of it from the dataset at all - this means the masked markers
        //should be removed from the map of I -> M incompleteness so they are not used to calculate stats
        
        //update the incomplete I->M hash only
        //(as any masked marker will be incomplete for all animals we dont need to track the actual animals)
        HeritableIndividual hind = null;
        
        for (Individual ind: hPop.getIndividuals()) {
            hind = (HeritableIndividual) ind;
        
            if (this.incompleteGenotypesItoM.containsKey(hind)) {
            	this.incompleteGenotypesItoM.get(hind).removeAll (markers);
            	
                if (! this.maskedIncompleteGenotypesItoM.containsKey(hind)) {
                    this.maskedIncompleteGenotypesItoM.put(hind, new ArrayList<SNPMarker>());
                }
                
                final Set<SNPMarker> temp = new HashSet<SNPMarker> (markers);
                temp.removeAll (this.maskedIncompleteGenotypesItoM.get(hind));
                this.maskedIncompleteGenotypesItoM.get(hind).addAll(temp);
            }
        }
        
       // this.nonmaskedMarkersCount = this.markerCount - this.maskedMarkers.size();
        //this.propertySupport.firePropertyChange(MASK_MARKERS, null, markers);
    }
    
    
    
    /**
     * Unmasking markers has the effect of removing them from the store of masked markers and 
     * returning them to the active Set of Markers. Fires PropertyChange event of type 
     * UNMASK_MARKERS forwarding the list of Markers to unmask;
     * @param markers 
     */
    public void unmaskMarkers(HeritablePopulation hPop, Collection<SNPMarker> markers) {

        if (markers==null || markers.isEmpty()) {
            return;
        }
    	
        //this.maskedMarkers.removeAll(markers);
        
        //unmasking a marker returns it from deletions and we now want to 
        //consider it again in the dataset - se we need to reset the values for 
        //this marker acoss the population, 
        //we may also need to take account of whether it is in the maps of 
        //masked genotypes - but at the momen i will ignore this as the 
        //incompleteness is only  used in the summary view 
        
        //update the incomplete I->M hash only
        //(as any masked marker will be incomplete for all animals we dont need to track theactual animals)
        
        HeritableIndividual hind = null;
        
        
            
//            //this shouldnt be necessary if we never actually remove the data from this map
//            if (this.originalIncompleteGenotypesMtoI.containsKey(m) ) {
//                this.incompleteGenotypesMtoI.put(m,this.originalIncompleteGenotypesMtoI.get(m) );
//            }

        for (Individual ind: hPop.getIndividuals()) {
            hind = (HeritableIndividual) ind;
            
            //switch to using a partial copy of masked genotypes
            if (this.maskedIncompleteGenotypesItoM.containsKey(hind)) {
            	//for (SNPMarker m: markers) {
            		
            		final Set<SNPMarker> temp = new HashSet<SNPMarker> (this.maskedIncompleteGenotypesItoM.get(hind));
            		// In retainAll the argument collection is looped through with contains(), so best to make a set the argument
            		temp.retainAll (markers); // whittle temp set down to unmasked markers that are present in the masked incomplete map
            		this.maskedIncompleteGenotypesItoM.get(hind).removeAll (temp); // remove them
            		
            		temp.removeAll (this.incompleteGenotypesItoM.get(hind));	// further whittle the temp set down to the ones that aren't in the incomplete map
            		this.incompleteGenotypesItoM.get(hind).addAll (temp);		// add them
            	//}
            }
        }
  
       //this.nonmaskedMarkersCount = this.markerCount - this.maskedMarkers.size();
        //this.propertySupport.firePropertyChange(UNMASK_MARKERS, null, markers);
    }
	
    
    public void deepCopyMaps (final HeritablePopulation hPop) {
    	final Map<Individual, List<SNPMarker>> oldMap = hPop.getIncompleteGenotypesItoM();
    	incompleteGenotypesItoM.clear ();
    	
    	for (Individual ind : oldMap.keySet()) {
    		List<SNPMarker> oldMarkerList = oldMap.get(ind);
    		incompleteGenotypesItoM.put (ind, new ArrayList<SNPMarker> (oldMarkerList));
    	}
    }
    
    
    public void deepCompareMaps (final HeritablePopulation hPop) {
    	final Map<Individual, List<SNPMarker>> oldMap = hPop.getIncompleteGenotypesItoM();
    	final Map<Individual, List<SNPMarker>> newMap = incompleteGenotypesItoM;
    	
    	LOGGER.info("---------------------------------------");
    	LOGGER.info("Old map size: "+oldMap.size()+"\t\tNew map size: "+newMap.size());
    	for (Individual ind : oldMap.keySet()) {
    		List<SNPMarker> oldMarkerList = oldMap.get(ind);
    		List<SNPMarker> newMarkerList = newMap.get(ind);
    		boolean equals = oldMarkerList.equals(newMarkerList);
    		if (!equals) {
    			Set<SNPMarker> oldMarkerSet = new HashSet<SNPMarker> (oldMarkerList);
    			boolean contains = (oldMarkerList.size() == newMarkerList.size());
    			for (int n = 0 ; n < newMarkerList.size() && contains; n++) {
    				contains &= (oldMarkerSet.contains (newMarkerList.get(n)));
    			}
    			
    			Set<SNPMarker> newMarkerSet = new HashSet<SNPMarker> (newMarkerList);
    			for (int n = 0 ; n < oldMarkerList.size() && contains; n++) {
    				contains &= (newMarkerSet.contains (oldMarkerList.get(n)));
    			}
    			
    			LOGGER.info (ind+", old: "+oldMarkerList.size()+", new: "+newMarkerList.size()+(contains? ", different order" : ", different elements"));
    		}
    	}
    	
    }

}
