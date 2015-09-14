package napier.pedigree.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import napier.pedigree.model.AbstractErrorMatrix;
import napier.pedigree.model.ErrorMatrix;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;


public class DefaultErrorMatrix extends AbstractErrorMatrix {

	static private final Logger LOGGER = Logger.getLogger (DefaultErrorMatrix.class);
	
	
	protected Map<SNPMarker, List<Individual>> markerMap;
	protected Map<Individual, List<SNPMarker>> individualMap;
	
	public DefaultErrorMatrix () {
		super ();
		markerMap = new HashMap <SNPMarker, List<Individual>> ();
		individualMap = new HashMap <Individual, List<SNPMarker>> ();
	}
	
	public void clear () {
		markerMap.clear ();
		individualMap.clear ();
	}
	
	
	public void addError (final SNPMarker marker, final Individual ind) {
		List<Individual> badIndsForMarker = markerMap.get (marker);
		if (badIndsForMarker == null) {
			badIndsForMarker = new ArrayList<Individual> ();
			markerMap.put (marker, badIndsForMarker);
		}
		
		badIndsForMarker.add (ind);
		
		List<SNPMarker> badMarkersForInd = individualMap.get (ind);
		if (badMarkersForInd == null) {
			badMarkersForInd = new ArrayList<SNPMarker> ();
			individualMap.put (ind, badMarkersForInd);
		}
		
		badMarkersForInd.add (marker);
		
		super.addError (marker, ind);
	}
	
	
	@Override
	public int errorCount (final SNPMarker marker) {
		final List<Individual> inds = markerMap.get (marker);
		return (inds == null ? 0 : inds.size());
	}
	
	@Override
	public int errorCount (final Individual ind) {
		final List<SNPMarker> markers = individualMap.get (ind);
		return (markers == null ? 0 : markers.size());
	}
	
	@Override
	public Map<SNPMarker, List<Individual>> getMarkerMap () { return markerMap; }
	
	@Override
	public Map<Individual, List<SNPMarker>> getIndividualMap () { return individualMap; }

	
    //new by trevor to allow  genotypes to be added all at once
    //nb the map of incomplete genotypes  in the Population uses a Set to 
    //simplify updates, so the parameter here matches this and must be converted to a List
    //however it might be better to use the same signature of map so can just
    //use the original Map by reference
    @Override
    public void setMarkerMap (final Map<SNPMarker, List<Individual>> map) {
    	markerMap = map;
        //markerMap.clear();
        //markerMap.putAll(map);
    	//markerMap = new HashMap<SNPMarker, List<Individual>>();
        //for (SNPMarker m : map.keySet()) {
        //    markerMap.put(m, new ArrayList(map.get(m)));
        //}
        
    }
    
    //new by trevor to allow  genotypes to be added all at once
    //nb the map of incomplete genotypes  in the Population uses a Set to simplify 
    //updates, so the parameter here matches this and must be converted to a List
    //however it might be better to use the same signature of map so can just
    //use the original Map by reference        
    @Override
    public void setIndividualMap (final Map<Individual, List<SNPMarker>> map) {
    	individualMap = map;
        //individualMap = new HashMap<Individual, List<SNPMarker>>();
        //for (HeritableIndividual i : map.keySet()) {
        //    individualMap.put(i, new ArrayList(map.get(i)));
        //}
        
    }
    
    
    @Override
    public ErrorMatrix makeDeepCopy () {
        final Map<SNPMarker, List<Individual>> clonedMarkerMap = new HashMap<SNPMarker, List<Individual>>();
        for (Entry<SNPMarker, List<Individual>> entry : markerMap.entrySet()) {
            clonedMarkerMap.put (entry.getKey(), new ArrayList<Individual> (entry.getValue()));
        }
        
        final Map<Individual, List<SNPMarker>> clonedIndividualMap = new HashMap<Individual, List<SNPMarker>>();
        for (Entry<Individual, List<SNPMarker>> entry : individualMap.entrySet()) {
            clonedIndividualMap.put (entry.getKey(), new ArrayList<SNPMarker> (entry.getValue()));
        }
        
        final ErrorMatrix clonedMatrix = new DefaultErrorMatrix ();
        clonedMatrix.setMarkerMap (clonedMarkerMap);
        clonedMatrix.setIndividualMap (clonedIndividualMap);
        
        return clonedMatrix;
    }
	
    
    
	@Override
	public void filterMarkers (final Collection<? extends SNPMarker> maskMarkers, 
			final boolean filter, final ErrorMatrix backupMatrix) {
		for (SNPMarker maskMarker : maskMarkers) {
			filterMarker (maskMarker, filter, backupMatrix);
		}
	}
	

	@Override
	public void filterMarker (final SNPMarker marker, final boolean include,
			final ErrorMatrix backupMatrix) {
		
		final List<Individual> individuals = markerMap.get(marker);
		
		if (include) {		
			if (individuals == null) {
				// Find which individuals contain this marker...
				final List<Individual> affectedInds = backupMatrix.getMarkerMap().get (marker); // copy?
				//markerMap.put (marker, affectedInds);
				
				// Loop through the filtered marker collections for these Individuals and add the marker
				if (affectedInds != null) {
					markerMap.put (marker, affectedInds);
					
					for (Individual affectedInd : affectedInds) {
						List<SNPMarker> markersForInd = individualMap.get (affectedInd);
						if (markersForInd == null) {
							markersForInd = new ArrayList<SNPMarker> ();
							individualMap.put (affectedInd, markersForInd);
						}
						markersForInd.add (marker);
					}
				}
			}
		}
		else {
			if (individuals != null) {
				
				// Remove this marker. Find which individuals contain this marker...
				final List<Individual> affectedInds = markerMap.remove (marker);
				
				// Loop through the filtered marker collections for these Individuals and remove the marker
				if (affectedInds != null) {
					for (Individual affectedInd : affectedInds) {
						final Collection<SNPMarker> markersForInd = individualMap.get (affectedInd);
						
						if (markersForInd != null) {
							//LOGGER.debug ("removing marker from ind: "+affectedInd);
							markersForInd.remove (marker);
						}
					}
				}
			}
		}
	}
}
