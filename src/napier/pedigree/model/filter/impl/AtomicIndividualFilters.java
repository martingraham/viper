package napier.pedigree.model.filter.impl;

import javax.swing.BoundedRangeModel;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.filter.IndividualFilter;
import napier.pedigree.model.filter.HistogramValueFilter;

import org.resspecies.model.Individual;

public class AtomicIndividualFilters {

	
	
	
	/**
	 * IndividualFilter that excludes all individuals with more than a given number of errors.
	 * @author cs22
	 *
	 */
	static public class CutoffIndividualFilter implements IndividualFilter, HistogramValueFilter {

		protected BoundedRangeModel rangeModel;
		protected ErrorCollator errorModel;
		protected boolean onFilteredData;
		
		public CutoffIndividualFilter (final ErrorCollator errorModel, final BoundedRangeModel brm, final boolean onFilteredData) {
			rangeModel = brm;
			this.errorModel = errorModel;
			this.onFilteredData = onFilteredData;
		}
		
		@Override
		public boolean include (final Individual ind) {
			return (onFilteredData ? errorModel.getCurrentAllErrorMap() : errorModel.getInitialAllErrorMap()).errorCount(ind) < rangeModel.getValue();
		}
		
		public boolean include (final int value) {
			return value < rangeModel.getValue();
		}
		
		public String toString () {
			return this.getClass() + ", filter val: "+rangeModel.getValue();
		}
	}
	
	
	
	
	/**
	 * IndividualFilter that excludes all individuals whose number of errors fall in a certain range.
	 * @author cs22
	 *
	 */
	static public class RangeIndividualFilter extends CutoffIndividualFilter {
		
		public RangeIndividualFilter (final ErrorCollator errorModel, final BoundedRangeModel brm, final boolean onFilteredData) {
			super (errorModel, brm, onFilteredData);
		}
		
		@Override
		public boolean include (final Individual individual) {
			final int errorCount = (onFilteredData ? errorModel.getCurrentAllErrorMap() : errorModel.getInitialAllErrorMap()).errorCount(individual);
			return errorCount < rangeModel.getValue() || errorCount >= rangeModel.getValue() + rangeModel.getExtent();
		}
		
		@Override
		public boolean include (final int value) {
			return value < rangeModel.getValue() || value >= rangeModel.getValue() + rangeModel.getExtent();
		}
	}
	
	
	/**
	 * IndividualFilter that excludes all individuals with less than or equal to a given number of errors.
	 * @author cs22
	 *
	 */
	static public class InverseCutoffIndividualFilter extends CutoffIndividualFilter {

		
		public InverseCutoffIndividualFilter (final ErrorCollator errorModel, final BoundedRangeModel brm, final boolean onFilteredData) {
			super (errorModel, brm, onFilteredData);
		}
		
		@Override
		public boolean include (final Individual individual) {
			return !super.include (individual);
		}
		
		public boolean include (final int value) {
			return !super.include (value);
		}
	}
}
