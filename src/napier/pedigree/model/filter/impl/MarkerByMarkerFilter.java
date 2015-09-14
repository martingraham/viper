package napier.pedigree.model.filter.impl;

import java.util.HashSet;
import java.util.Set;

import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.HistogramValueFilter;

public class MarkerByMarkerFilter implements MarkerFilter, HistogramValueFilter {

	
	protected Set<SNPMarker> selectedMaskedMarkers;
	
	public MarkerByMarkerFilter () {
		selectedMaskedMarkers = new HashSet<SNPMarker> ();
	}
	
	@Override
	public boolean include (final SNPMarker marker) {
		return ! selectedMaskedMarkers.contains (marker);
	}
	
	
	public boolean add (final SNPMarker marker) {
		return selectedMaskedMarkers.add (marker);
	}

	public boolean remove (final SNPMarker marker) {
		return selectedMaskedMarkers.remove (marker);
	}
	
	public Set<SNPMarker> getAllMaskedMarkers () {
		return selectedMaskedMarkers;
	}

	/**
	 * Since this filter doesn't deal in ranges for histograms, we just return 'true' for any includes queries
	 */
	@Override
	public boolean include (int value) {
		return true;
	}
}
