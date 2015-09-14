package napier.pedigree.model.sort;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import napier.pedigree.model.ErrorCollator;

import org.resspecies.model.Individual;



public class BetweenOffspringSetSorters {

	public final static Comparator<Collection<Individual>> GRAND_OFFSPRING_SET_COUNT_SORTER = new GrandOffspringSetCountSorter ();
	public final static Comparator<Collection<Individual>> OFFSPRING_SET_COUNT_SORTER = new OffspringSetCountSorter ();
	
	private static final BetweenOffspringSetSorters INSTANCE = new BetweenOffspringSetSorters ();
	
	private BetweenOffspringSetSorters () {}
	
	public static final BetweenOffspringSetSorters getInstance () { return INSTANCE; }
	
	
	static class GrandOffspringSetCountSorter extends LabelledComparator<Collection<Individual>> implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7248706042124138640L;

		@Override
		public int compare (final Collection<Individual> coll1, final Collection<Individual> coll2) {
			
			int total1 = 0;
			for (Individual ind1 : coll1) {
				total1 += ind1.getOffspring().size();
			}
			
			int total2 = 0;
			for (Individual ind2 : coll2) {
				total2 += ind2.getOffspring().size();
			}
			return total2 - total1;
		}
	}
	
	
	static class OffspringSetCountSorter extends LabelledComparator<Collection<Individual>> implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3667053871402462221L;

		@Override
		public int compare (final Collection<Individual> coll1, final Collection<Individual> coll2) {
			if (coll1 == null) {
				return coll2 == null ? 0 : 1;
			}
			else if (coll2 == null) {
				return -1;
			}
			return coll2.size() - coll1.size();
		}
	}
	
	
	abstract class AbstractErrorBasedCollectionSorter extends LabelledComparator<Collection<Individual>> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4151025825926545533L;
		protected ErrorCollator bad;
		protected int errorType;
		
		public AbstractErrorBasedCollectionSorter (final ErrorCollator bad,
				final int errorType) {
			super ();
			this.bad = bad;
			this.errorType = errorType;
		}
		
		@Override
		public int compare (final Collection<Individual> coll1, final Collection<Individual> coll2) {
			if (coll1 == null) {
				return coll2 == null ? 0 : 1;
			}
			else if (coll2 == null) {
				return -1;
			}
			
			return 0;
		}
	}
	
	
	class OffspringCollectionTotalErrorSorter extends AbstractErrorBasedCollectionSorter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8130106634367603672L;

		public OffspringCollectionTotalErrorSorter (final ErrorCollator bad,
				final int errorType) {
			super (bad, errorType);
		}	
		
		protected int sumErrors (final Collection<Individual> offspring) {
			final Iterator<? extends Individual> iter = offspring.iterator ();
			int total = 0;
			while (iter.hasNext()) {
				final Individual child = iter.next();
				total += bad.getFilteredIndividualErrorCount (errorType, child);
			}
			return total;
		}
			
		@Override
		public int compare (final Collection<Individual> coll1, final Collection<Individual> coll2) {	
			final int val = super.compare (coll1, coll2);
			if (val == 0 && coll1 != null) {
				return (coll1 == coll2 ? 0 : sumErrors (coll2) - sumErrors (coll1));
			}
			return val;
		}
	}
	
	
	public class OffspringCollectionAllErrorsSorter extends OffspringCollectionTotalErrorSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3370258809762342603L;

		public OffspringCollectionAllErrorsSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.ANY_ERROR);
		}
	}
	
	
	
	public class OffspringCollectionRatioAllErrorsSorter extends OffspringCollectionTotalErrorSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3370258809762342603L;

		public OffspringCollectionRatioAllErrorsSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.ANY_ERROR);
		}
		
		@Override
		public int compare (final Collection<Individual> coll1, final Collection<Individual> coll2) {	
			final int val = super.compare (coll1, coll2);
			if (val == 0 && coll1 != null) {
				double ratioCompare = ((double)sumErrors (coll2) / (double)coll2.size()) 
						 - ((double)sumErrors (coll1) / (double)coll1.size());
				
				return (ratioCompare == 0.0 ? 0 : (ratioCompare > 0.0 ? 1 : -1));
			}
			return val;
		}
	}
}
