package napier.pedigree.model.filter.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.filter.MarkerFilter;


public class MaskMarkerOperator {

	static final private Logger LOGGER = Logger.getLogger (MaskMarkerOperator.class);
	
	
	public void mask (final ErrorCollator errorModel, final MarkerFilter mFilter) {
		mask (errorModel, mFilter, null);
	}

	
	
	public void mask (final ErrorCollator errorModel, final MarkerFilter mFilter, final Set<SNPMarker> optionalMarkerSet) {
		
		final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();
		
		final Set<SNPMarker> active = hPop.getActiveMarkers();
		final Set<SNPMarker> masked = new HashSet<SNPMarker> (hPop.getMaskedMarkers()); // otherwise we end up affecting the set held in the HeritablePopulation
		
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
		
		hPop.maskMarkers (toMask);
		hPop.unmaskMarkers (toUnmask);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("calling recalc from MarkerMasker");
		}
	}
	
	
	/**
	 * 'Cos SNPMarker toString doesn't output anything human readable
	 * @param markers set of markers
	 * @return String containing names of all markers in set
	 */
	protected String printMarkerSet (final Set<SNPMarker> markers) {
		final StringBuilder sBuilder = new StringBuilder ("[");
		for (SNPMarker marker : markers) {
			sBuilder.append (marker.getName()+", ");
		}
		sBuilder.append ("]");
		return sBuilder.toString ();
	}
}
