package napier.pedigree.model.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.PopCheckerWrapper;

public class DefaultPopCheckerWrapper implements PopCheckerWrapper {

	static private final Logger LOGGER = Logger.getLogger (DefaultPopCheckerWrapper.class);

	protected HeritablePopulation pop;
	protected SNPMarker focusMarker;
	protected Map<Individual, Integer> focusMarkerGenotypes;
	protected Set<SNPMarker> allMarkers;
	
	public DefaultPopCheckerWrapper () {
		this (null);
	}
	
	public DefaultPopCheckerWrapper (final HeritablePopulation hPop) {
		setPopulation (hPop);
		focusMarkerGenotypes = new HashMap<Individual, Integer> ();
	}
	
	
	@Override
	public void setPopulation (final HeritablePopulation hPop) {
		if (pop != hPop) {
			pop = hPop;
			allMarkers = hPop.getMarkers();
		}
	}

	@Override
	public HeritablePopulation getPopulation() {
		return pop;
	}
	

	@Override
	public void setFocusMarker (final SNPMarker marker) {
		if (marker != focusMarker) {
			focusMarker = marker;
			if (focusMarker != null) {
				focusMarkerGenotypes.clear();
				updateFocusMarkerGenotypes ();
			}
		}
	}
	
	@Override
	public SNPMarker getFocusMarker () {
		return focusMarker;
	}
		
	
	
	@Override
	public void updateFocusMarkerGenotypes () {
		for (Individual ind : pop.getIndividuals()) {
			final int genotypeVal = pop.getCheckedGenotype (getFocusMarker(), (HeritableIndividual)ind);
			focusMarkerGenotypes.put (ind, Integer.valueOf (genotypeVal));
		}
	}
	

	@Override
	public Map<Individual, Integer> getFocusMarkerGenotypes () {
		return focusMarkerGenotypes;
	}


	@Override
	public boolean isIndividualContextMasked (final Individual hInd) {
	
		boolean masked = false;
		
		if (getFocusMarker() == null) {
			masked = getPopulation().getMaskedIndividuals().contains (hInd);
		} else {
			final List<String> maskedGenotypes = getPopulation().getMaskedGenotypesItoM().get (hInd.getName());
			LOGGER.info (getFocusMarker().getName()+ " "+ hInd.getName()+" "+(maskedGenotypes == null ? "null" : maskedGenotypes.contains(getFocusMarker().getName())));
			masked = (maskedGenotypes == null ? false : maskedGenotypes.contains (getFocusMarker().getName()));
		}
		
		return masked;
	}


	@Override
	public void contextMaskIndividual (final HeritableIndividual hInd) {	
		if (getFocusMarker() == null) {
			getPopulation().maskIndividual (hInd);
		} else {
			getPopulation().maskGenotype (hInd.getName(), getFocusMarker().getName());
		}	
	}


	@Override
	public void contextUnmaskIndividual (final HeritableIndividual hInd) {
		if (getFocusMarker() == null) {
			getPopulation().unmaskIndividual (hInd);
		} else {
			getPopulation().unmaskGenotype (hInd.getName(), getFocusMarker().getName());
		}
	}
	
	@Override
	public Set<SNPMarker> getAllMarkers () {
		return (allMarkers == null ? getPopulation().getMarkers() : allMarkers);
	}
}
