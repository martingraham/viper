package napier.pedigree.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

public class MultiCoreErrorMatrix extends DefaultErrorMatrix {

	
	public MultiCoreErrorMatrix () {
		super ();
	}
	
	public void addError (final SNPMarker marker, final Individual ind) {
		List<Individual> badIndsForMarker = markerMap.get (marker);
		if (badIndsForMarker == null) {
			badIndsForMarker = new ArrayList<Individual> ();
			synchronized (markerMap) {
				markerMap.put (marker, badIndsForMarker);
			}
		}
		
		synchronized (badIndsForMarker) {
			badIndsForMarker.add (ind);
		}
		
		List<SNPMarker> badMarkersForInd = individualMap.get (ind);
		if (badMarkersForInd == null) {
			badMarkersForInd = new ArrayList<SNPMarker> ();
			synchronized (individualMap) {
				individualMap.put (ind, badMarkersForInd);
			}
		}
		
		synchronized (badMarkersForInd) {
			badMarkersForInd.add (marker);
		}
		
		//super.addError (marker, ind);
	}
}
