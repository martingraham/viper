package napier.pedigree.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.ChangeOnNextRecalcStore;
import napier.pedigree.model.ErrorCollator;

public class DefaultChangeOnNextRecalcStore implements ChangeOnNextRecalcStore {

	protected int flags;
	protected Set<SNPMarker> markersToRecalc;
	
	public DefaultChangeOnNextRecalcStore () {
		flags = 0;
		markersToRecalc = new HashSet<SNPMarker> ();
	}
	
	@Override
	public int getFlags() {
		return flags;
	}

	@Override
	public void addFlag (final int flag) {
		flags |= flag;
	}

	@Override
	public boolean onlyMarkersToChange() {
		return flags == MARKER_CHANGE;
	}

	@Override
	public Set<SNPMarker> getMarkersToChange() {
		return markersToRecalc;
	}

	@Override
	public void addMarkersToChange (final Set<SNPMarker> markersToAdd) {
		if (markersToAdd != null && !markersToAdd.isEmpty()) {
			markersToRecalc.addAll (markersToAdd);
		}
	}

	@Override
	public void clear() {
		flags = 0;
		markersToRecalc.clear();
	}
}
