package napier.pedigree.swing.app.maskers;

import java.util.NavigableMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.SNPMarker;

import swingPlus.histogram.JHistogram;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.impl.MaskMarkerOperator;
import napier.pedigree.model.filter.impl.MaskMarkerOperatorTest;
import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.swing.AbstractHistogramListener;


public class MarkerMasker extends AbstractHistogramListener {

	static final private Logger LOGGER = Logger.getLogger (MarkerMasker.class);
	
	protected JHistogram linkedHistogram;
	protected ErrorCollator errorModel;
	protected MarkerFilter markerFilter;
	
	public MarkerMasker (final ErrorCollator errorModel, final JHistogram linkedHistogram, final MarkerFilter mFilter) {
		super ();
		this.linkedHistogram = linkedHistogram;
		this.errorModel = errorModel;
		this.markerFilter = mFilter;
	}
	
	@Override
	public void doStuff (final AbstractErrorHistogram histo, final int curBottomValue, final int curTopValue) {
		
		final NavigableMap<Integer, Number> histoData = histo.getBinnedData();
		final NavigableMap<Integer, Number> dataSubset = histoData.headMap 
				(Integer.valueOf (curBottomValue), true);

		final MaskMarkerOperator mmo = new MaskMarkerOperatorTest ();
		//final MaskMarkerOperator mmo = new MaskMarkerOperator ();
		mmo.mask (errorModel, markerFilter);
		
		errorModel.recalculate();
		
		if (linkedHistogram != null) {
			linkedHistogram.setData (dataSubset);
		}
	}
	
	
	/**
	 * 'Cos SNPMarker toString doesn't output anything human readable
	 * @param markers - set of markers to turn into a String
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
