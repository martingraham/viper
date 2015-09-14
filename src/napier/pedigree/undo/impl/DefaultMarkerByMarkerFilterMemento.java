package napier.pedigree.undo.impl;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.filter.impl.MarkerByMarkerFilter;
import napier.pedigree.swing.JMarkerTable;
import napier.pedigree.undo.Memento;


public class DefaultMarkerByMarkerFilterMemento implements Memento<JMarkerTable> {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9140466118715358973L;
	protected Set<SNPMarker> handMaskedMarkers;
	
	
	public DefaultMarkerByMarkerFilterMemento () {
		super ();
		handMaskedMarkers = new HashSet<SNPMarker> ();
	}
	
	@Override
	public void make (final JMarkerTable markerTable) {
		final MarkerByMarkerFilter mFilter = markerTable.getMarkerFilter();
		handMaskedMarkers = new HashSet<SNPMarker> (mFilter.getAllMaskedMarkers());
	}

	@Override
	public void restore (final JMarkerTable markerTable) {
		final MarkerByMarkerFilter mFilter = markerTable.getMarkerFilter();
		mFilter.getAllMaskedMarkers().clear();
		mFilter.getAllMaskedMarkers().addAll (handMaskedMarkers);
	}

	@Override
	public BitSet compare (final JMarkerTable markerTable) {
		final BitSet differences = new BitSet ();
		return differences;
	}
}
