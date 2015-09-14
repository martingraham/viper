package napier.pedigree.model.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.MaskedGenotypeTotaliser;

public class DefaultMaskedGenotypeTotaliser implements MaskedGenotypeTotaliser {

	protected Map<Individual, Integer> individualTotals;
	protected Map<SNPMarker, Integer> markerTotals;
	protected int activeMarkerSize;
	
	public DefaultMaskedGenotypeTotaliser () {
		individualTotals = new HashMap <Individual, Integer> ();
		markerTotals = new HashMap <SNPMarker, Integer> ();
	}
	
	@Override
	public void calculateTotals (final HeritablePopulation hPop, final Set<SNPMarker> onlyTheseMarkers) {
		clear ();
		
		if (hPop != null) {
			/**
			 * Do Marker to Individual masked genotype counts; counting masked individuals as plus one masking per marker
			 * Being careful to remove double counts i.e. a masked marker and a masked genotype at that marker should only count as 1.
			 */
			final TreeSet<HeritableIndividual> maskedInds = hPop.getMaskedIndividuals();
			final TreeMap<String, List<String>> genotypeMtoIMap = hPop.getMaskedGenotypesMtoI();
			final TreeSet<SNPMarker> activeMarkers = hPop.getActiveMarkers();
			for (SNPMarker marker : activeMarkers) {
				final List<String> maskedGenoInds = genotypeMtoIMap.get(marker.getName());
	
				if (maskedGenoInds != null) {
					int total = (maskedInds == null ? 0 : maskedInds.size());
					total += maskedGenoInds.size();
					
					if (maskedInds != null) {
						// remove double counts
						for (String maskedGenoIndName : maskedGenoInds) {
							final Individual maskedGenoInd = hPop.getIndividualForName (maskedGenoIndName);
							if (maskedInds.contains (maskedGenoInd)) {
								total--;
							}
						}
					}
					markerTotals.put (marker, Integer.valueOf (total));
				}
			}
			
			/**
			 * Do Individual to Marker masked genotype counts, but dont count masked genotypes if the marker is fully masked
			 */
			final Set<SNPMarker> maskedMarkers = hPop.getMaskedMarkers();
			final TreeMap<String, List<String>> genotypeItoMMap = hPop.getMaskedGenotypesItoM();
			activeMarkerSize = activeMarkers.size();
			for (Individual ind : hPop.getIndividuals()) {
				final HeritableIndividual hInd = (HeritableIndividual)ind;
				
				if (!hInd.isMasked()) {
					// If individual isn't masked
					final List<String> maskedGenoMarkers = genotypeItoMMap.get (ind.getName());
					
					if (maskedGenoMarkers != null) {
						int lessThese = 0;
						
						if (maskedMarkers != null) {
							// don't count masked genotypes where marker is fully masked
							for (String maskedGenoMarkerName : maskedGenoMarkers) {
								final SNPMarker maskedGenoMarker = hPop.getMarkerByName (maskedGenoMarkerName);
								if (maskedMarkers.contains (maskedGenoMarker)) {
									lessThese++;
								}
							}
						}
						
						individualTotals.put (ind, Integer.valueOf (maskedGenoMarkers.size() - lessThese));
					}
				} 
			}
		}
	}



	@Override
	public int getMaskedGenotypeTotal (final Individual ind, final HeritablePopulation hPop) {
		final Integer intObj = individualTotals.get (ind);
		return intObj == null
			? (hPop.getMaskedIndividuals().contains(ind) ? activeMarkerSize : 0)
			: intObj.intValue();
	}

	@Override
	public int getMaskedGenotypeTotal (final SNPMarker marker, final HeritablePopulation hPop) {
		final Integer intObj = markerTotals.get (marker);
		return intObj == null 
			? (hPop.getMaskedMarkers().contains(marker) ? hPop.getIndividualCount() - 1 : hPop.getMaskedIndividuals().size()) 
			: intObj.intValue();
	}
	
	@Override
	public void clear () {
		individualTotals.clear();
		markerTotals.clear();
	}
}
