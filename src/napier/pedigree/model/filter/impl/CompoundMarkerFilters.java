package napier.pedigree.model.filter.impl;

import java.util.Collection;

import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.HistogramValueFilter;


public class CompoundMarkerFilters {

	
    public static MarkerFilter orFilter (final Collection<MarkerFilter> filters) {
        return new OrMarkerFilter (filters);
    }
    
    public static MarkerFilter andFilter (final Collection<MarkerFilter> filters) {
        return new AndMarkerFilter (filters);
    }
    
    public static MarkerFilter notFilter (final MarkerFilter filter) {
        return new NotMarkerFilter (filter);
    }
    
    
	
	static class AndMarkerFilter implements MarkerFilter, HistogramValueFilter {
		
		Collection<MarkerFilter> filterCollection;
		
		AndMarkerFilter (final Collection<MarkerFilter> filters) {
			filterCollection = filters;
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			for (MarkerFilter filter : filterCollection) {
				if (!filter.include (marker)) {
					return false;
				}
			}
			return true;
		}
		
		
		@Override
		public boolean include (final int value) {
			for (MarkerFilter filter : filterCollection) {
				if (!((HistogramValueFilter)filter).include (value)) {
					return false;
				}
			}
			return true;
		}
	}
	
	
	
	static class OrMarkerFilter implements MarkerFilter, HistogramValueFilter {
		
		Collection<MarkerFilter> filterCollection;
		
		OrMarkerFilter (final Collection<MarkerFilter> filters) {
			filterCollection = filters;
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			for (MarkerFilter filter : filterCollection) {
				if (filter.include (marker)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public boolean include (final int value) {
			for (MarkerFilter filter : filterCollection) {
				if (((HistogramValueFilter)filter).include (value)) {
					return true;
				}
			}
			return false;
		}
	}
	
	
	
	static class NotMarkerFilter implements MarkerFilter, HistogramValueFilter {
		
		MarkerFilter filter;
		
		NotMarkerFilter (final MarkerFilter filter) {
			this.filter = filter;
		}
		
		@Override
		public boolean include (final SNPMarker marker) {
			return !filter.include (marker);
		}
		
		@Override
		public boolean include (final int value) {
			return !((HistogramValueFilter)filter).include (value);
		}
	}
}
