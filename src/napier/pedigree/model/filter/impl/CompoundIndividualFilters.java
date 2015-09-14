package napier.pedigree.model.filter.impl;

import java.util.Collection;

import org.resspecies.model.Individual;

import napier.pedigree.model.filter.IndividualFilter;


public class CompoundIndividualFilters {

	
    public static IndividualFilter orFilter (final Collection<IndividualFilter> filters) {
        return new OrIndividualFilter (filters);
    }
    
    public static IndividualFilter andFilter (final Collection<IndividualFilter> filters) {
        return new AndIndividualFilter (filters);
    }
    
    public static IndividualFilter notFilter (final IndividualFilter filter) {
        return new NotIndividualFilter (filter);
    }
    
    
	
	static class AndIndividualFilter implements IndividualFilter {
		
		Collection<IndividualFilter> filterCollection;
		
		AndIndividualFilter (final Collection<IndividualFilter> filters) {
			filterCollection = filters;
		}
		
		@Override
		public boolean include (final Individual individual) {
			for (IndividualFilter filter : filterCollection) {
				if (!filter.include (individual)) {
					return false;
				}
			}
			return true;
		}
	}
	
	
	
	static class OrIndividualFilter implements IndividualFilter {
		
		Collection<IndividualFilter> filterCollection;
		
		OrIndividualFilter (final Collection<IndividualFilter> filters) {
			filterCollection = filters;
		}
		
		@Override
		public boolean include (final Individual individual) {
			for (IndividualFilter filter : filterCollection) {
				if (filter.include (individual)) {
					return true;
				}
			}
			return false;
		}
	}
	
	
	
	static class NotIndividualFilter implements IndividualFilter {
		
		IndividualFilter filter;
		
		NotIndividualFilter (final IndividualFilter filter) {
			this.filter = filter;
		}
		
		@Override
		public boolean include (final Individual individual) {
			return !filter.include (individual);
		}
	}
}
