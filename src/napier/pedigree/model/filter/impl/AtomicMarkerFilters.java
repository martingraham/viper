package napier.pedigree.model.filter.impl;

import javax.swing.BoundedRangeModel;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.HistogramValueFilter;

import org.resspecies.inheritance.model.SNPMarker;

public class AtomicMarkerFilters {

	
	/**
	 * MarkerFilter that excludes everything except the error model's current focus marker
	 * @author cs22
	 *
	 */
	static public class SpecificMarkerFilter implements MarkerFilter {

		PopCheckerWrapper popCheckerContext;
		
		public SpecificMarkerFilter (final PopCheckerWrapper errorModel) {
			this.popCheckerContext = errorModel;
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			return popCheckerContext.getFocusMarker() == null || popCheckerContext.getFocusMarker() == marker;
		}
	}
	
	
	/**
	 * MarkerFilter that excludes all markers with more than a given number of errors.
	 * @author cs22
	 *
	 */
	static public class CutoffMarkerFilter implements HistogramValueFilter, MarkerFilter {

		protected BoundedRangeModel rangeModel;
		protected ErrorCollator errorModel;
		protected boolean onFilteredData;
		
		public CutoffMarkerFilter (final ErrorCollator errorModel, final BoundedRangeModel brm, boolean onFilteredData) {
			rangeModel = brm;
			this.errorModel = errorModel;
			this.onFilteredData = onFilteredData;
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			return include ((onFilteredData ? errorModel.getCurrentAllErrorMap() : errorModel.getInitialAllErrorMap()).errorCount(marker));
		}
		
		@Override
		public boolean include (final int value) {
			return value < rangeModel.getValue();
		}
	}
	
	
	
	
	/**
	 * MarkerFilter that excludes all markers whose number of errors fall in a certain range.
	 * @author cs22
	 *
	 */
	static public class RangeMarkerFilter extends CutoffMarkerFilter {
		
		public RangeMarkerFilter (final ErrorCollator errorModel, final BoundedRangeModel brm, final boolean onFilteredData) {
			super (errorModel, brm, onFilteredData);
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			final int errorCount = (onFilteredData ? errorModel.getCurrentAllErrorMap() : errorModel.getInitialAllErrorMap()).errorCount(marker);
			return include (errorCount);
		}
		
		@Override
		public boolean include (final int value) {
			return value < rangeModel.getValue() || value >= rangeModel.getValue() + rangeModel.getExtent();
		}
	}
	
	
	
	
	
	/**
	 * MarkerFilter that excludes all markers with less than or equal to a given number of errors.
	 * @author cs22
	 *
	 */
	static public class InverseCutoffMarkerFilter extends CutoffMarkerFilter {

		
		public InverseCutoffMarkerFilter (final ErrorCollator errorModel, final BoundedRangeModel brm, final boolean onFilteredData) {
			super (errorModel, brm, onFilteredData);
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			// do not invert here, as it gets inverted via the include (int value) method below
			return super.include (marker);
		}
		
		@Override
		public boolean include (final int value) {
			return !super.include (value) || (value == 0); // (value == 0) : don't want to exclude markers with no errors
		}
	}

}
