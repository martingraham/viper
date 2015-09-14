package napier.pedigree.model.impl;

import java.util.List;
import java.util.Set;

import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.MaskedGenotypeTotaliser;


public class UnfilteredMaskedGenotypeTotaliser implements
		MaskedGenotypeTotaliser {

	protected HeritablePopulation hPop;
	
	@Override
	public void calculateTotals (final HeritablePopulation hPop, final Set<SNPMarker> activeMarkers) {
		this.hPop = hPop;
	}

	@Override
	public int getMaskedGenotypeTotal (final Individual ind, final HeritablePopulation hPop) {
		if (hPop.getMaskedIndividuals().contains(ind)) {
			return hPop.getMarkerCount();
		}
		final List<String> maskedForInd = hPop.getMaskedGenotypesItoM().get(ind.getName());
		return maskedForInd == null ? 0 : maskedForInd.size();
	}

	@Override
	public int getMaskedGenotypeTotal (final SNPMarker marker, final HeritablePopulation hPop) {
		final List<String> maskedForMarker = hPop.getMaskedGenotypesMtoI().get(marker.getName());
		return maskedForMarker == null 
			? (hPop.getMaskedMarkers().contains(marker) ? hPop.getIndividualCount() - 1 : 0) 
			: maskedForMarker.size();
	}
	
	@Override
	public void clear () {
		// EMPTY
	}

}
