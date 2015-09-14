package napier.pedigree.model.sort;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.shared.MultiComparator;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.JGeneration;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

import util.collections.PropertyChangeArrayList;



public class IndividualSorters {

	public final static Comparator<Individual> NAME_SORTER = new IndividualNameSorter ();
	public final static Comparator<Individual> GENDER_SORTER = new IndividualGenderSorter ();
	public final static Comparator<Individual> OFFSPRING_COUNT_SORTER = new IndividualOffspringCountSorter ();
	public final static Comparator<Individual> MULTICELL_OFFSPRING_COUNT_SORTER = new IndividualMultiCellOffspringCountSorter ();
	public final static Comparator<Individual> DAM_SORTER = new IndividualByDamSorter ();
	public final static Comparator<Individual> SIRE_SORTER = new IndividualBySireSorter ();
	public final static Comparator<Individual> INC_MARKER_SORTER = new IncompleteMarkerCount ();
	public final static Comparator<Individual> LITTER_SORTER = new LitterSorter ();
	
	private static final IndividualSorters INSTANCE = new IndividualSorters ();
	
	private IndividualSorters () {}
	
	public static final IndividualSorters getInstance () { return INSTANCE; }
	
	
	public MultiComparator makeMultiComparator (final Comparator<Object>[] comparators) {
		final List<Comparator<Object>> compList = 
			new PropertyChangeArrayList<Comparator<Object>> (Arrays.asList (comparators));
		return new MultiComparator (compList);
	}
	
	// If nullTest() != 0, then return nullTest() - 2;
	static int nullTest (final Object obj1, final Object obj2) {
		if (obj1 == null) {
			return obj2 == null ? 2 : 3;
		}
		return obj2 == null ? 1 : 0;
	}
	
	
	static class IndividualNameSorter extends LabelledComparator<Individual> {	
		/**
		 * 
		 */
		private static final long serialVersionUID = -1732323411472610978L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			return Individual.NameableComparator.compare (ind1, ind2);
		}
	}
	
	
	static class IndividualGenderSorter extends LabelledComparator<Individual> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1573087290893733619L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			return ind1.getGender().compareTo (ind2.getGender());
		}
	}
	
	
	static class IndividualOffspringCountSorter extends LabelledComparator<Individual> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4195381567072962215L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			return ind2.getOffspring().size() - ind1.getOffspring().size();
		}
	}
	
	
	/**
	 * This is needed as when two sires/dams have the same number offspring, they are sometimes mixed amongst
	 * each other when they both span several columns
	 * @author cs22
	 *
	 */
	static class IndividualMultiCellOffspringCountSorter extends LabelledComparator<Individual> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5714548384469679505L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			final int diff = ind2.getOffspring().size() - ind1.getOffspring().size();
			return diff == 0 ? NAME_SORTER.compare(ind1, ind2) : diff;
		}
	}
	
	static class IndividualByDamSorter extends LabelledComparator<Individual> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -107491422841212301L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			return Individual.IdentifiableComparator.compare (ind1.getDam(), ind2.getDam());
		}
	}
	
	
	static class IndividualBySireSorter extends LabelledComparator<Individual> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5861436732267642240L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			return Individual.IdentifiableComparator.compare (ind1.getSire(), ind2.getSire());
		}
	}
	
	
	
	abstract class AbstractGenerationAwareSorter extends LabelledComparator<Individual> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1828554680689854039L;
		protected JGeneration generation;
		
		
		public AbstractGenerationAwareSorter (final JGeneration generation) {
			super ();
			setJGeneration (generation);
		}
		
		public final void setJGeneration (final JGeneration generation) {
			this.generation = generation;
		}
	}
	
	/**
	 * Sorts by the position the individuals partners are in regarding their current
	 * sort
	 * @author cs22
	 *
	 */
	public class PartnerCountSorter extends AbstractGenerationAwareSorter {

		/**
		 * 
		 */
		private static final long serialVersionUID = -694740372591289926L;

		public PartnerCountSorter (final JGeneration generation) {
			super (generation);
		}
		
		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			final Collection<Individual> indOffSpring1 = (Collection<Individual>)ind1.getOffspring ();
			final Collection<Individual> indOffSpring2 = (Collection<Individual>)ind2.getOffspring ();
			//final AbstractMatrixTableModel matrixModel = (AbstractMatrixTableModel)generation.getModel();
			
			Iterator<Individual> iter1 = indOffSpring1.iterator();
			//System.err.println ("ind1: "+ind1+", ind1 offspring: "+indOffSpring1.toString());
			final Individual firstChild1 = iter1.next();
			final boolean ind1IsSire = (firstChild1.getSire() == ind1);
			
			iter1 = indOffSpring1.iterator();
			final Iterator<Individual> iter2 = indOffSpring2.iterator();
			
			final Set<Individual> partners = new HashSet<Individual> ();
			
			Individual lastPartner = null;
			while (iter1.hasNext()) {
				final Individual child = iter1.next ();
				final Individual partner = ind1IsSire ? child.getDam() : child.getSire();
				if (partner != lastPartner) {
					partners.add (partner);
					lastPartner = partner;
				}
			}
			final int count1 = partners.size();
			
			
			lastPartner = null;
			partners.clear ();
			
			while (iter2.hasNext()) {
				final Individual child = iter2.next ();
				final Individual partner = ind1IsSire ? child.getDam() : child.getSire();
				if (partner != lastPartner) {
					partners.add (partner);
					lastPartner = partner;
				}
			}
			final int count2 = partners.size();
			
			return count2 - count1;
		}
	}
	
	
	

	
	public class GenotypeSorter extends AbstractErrorBasedSorter {	
		/**
		 * 
		 */
		private static final long serialVersionUID = -8229900939078207253L;

		public GenotypeSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.ANY_ERROR);
		}
	}
	
	
	public class NovelAlleleSorter extends AbstractErrorBasedSorter {	
		/**
		 * 
		 */
		private static final long serialVersionUID = 6942389251454777067L;

		public NovelAlleleSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.NOVEL_ALLELES);
		}
	}
	
	
	public class NotFromDamSorter extends AbstractErrorBasedSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3312282673557156823L;

		public NotFromDamSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.BAD_DAM);
		}
	}
	
	
	public class NotFromSireSorter extends AbstractErrorBasedSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7826600332547744028L;

		public NotFromSireSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.BAD_SIRE);
		}
	}
	
	
	class OffspringTotalErrorSorter extends AbstractErrorBasedSorter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8130106634367603672L;

		public OffspringTotalErrorSorter (final ErrorCollator bad,
				final int errorType) {
			super (bad, errorType);
		}	
		
		protected int sumErrors (final Individual ind) {
			final Collection<? extends Individual> offspring = ind.getOffspring();
			final Iterator<? extends Individual> iter = offspring.iterator ();
			int total = 0;
			while (iter.hasNext()) {
				final Individual child = iter.next();
				total += bad.getFilteredIndividualErrorCount (errorType, child);
			}
			return total;
		}
			
		@Override
		public int compare (final Individual ind1, final Individual ind2) {	
			 return (ind1 == ind2 ? 0 : sumErrors (ind2) - sumErrors (ind1));
		}
	}
	
	public class OffspringTotalSireErrorSorter extends OffspringTotalErrorSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7163189608339957706L;

		public OffspringTotalSireErrorSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.BAD_SIRE);
		}
	}
	
	public class OffspringTotalDamErrorSorter extends OffspringTotalErrorSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8732055051841003924L;

		public OffspringTotalDamErrorSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.BAD_DAM);
		}
	}
	
	public class OffspringAllErrorsSorter extends OffspringTotalErrorSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3370258809762342603L;

		public OffspringAllErrorsSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.ANY_ERROR);
		}
	}
	
	
	public class OffspringRatioAllErrorsSorter extends OffspringTotalErrorSorter {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3370258809762342603L;

		public OffspringRatioAllErrorsSorter (final ErrorCollator bad) {
			super (bad, ErrorCollator.ANY_ERROR);
		}
		
		@Override
		public int compare (final Individual ind1, final Individual ind2) {	
			if (ind1 == ind2) {
				return 0;
			}
			
			double ratioCompare = ((double)sumErrors (ind2) / ind2.getOffspring().size()) 
					 - ((double)sumErrors (ind1) / ind1.getOffspring().size());
			
			return (ratioCompare == 0.0 ? 0 : (ratioCompare > 0.0 ? 1 : -1));
		}
	}
	
	
	
	
	class OffspringMaxErrorSorter extends AbstractErrorBasedSorter {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2903130810774926654L;

		public OffspringMaxErrorSorter (final ErrorCollator bad,
				final int errorType) {
			super (bad, errorType);
		}
		
		protected int maxErrors (final Individual ind) {
			final Collection<? extends Individual> offspring = ind.getOffspring();
			final Iterator<? extends Individual> iter = offspring.iterator ();
			int total = 0;
			while (iter.hasNext()) {
				final Individual child = iter.next();
				total = Math.max (total, bad.getFilteredIndividualErrorCount (errorType, child));
			}
			return total;
		}
			
		@Override
		public int compare (final Individual ind1, final Individual ind2) {	
			 return (ind1 == ind2 ? 0 : maxErrors (ind2) - maxErrors (ind1));
		}
	}
	
	
	static class IncompleteMarkerCount extends LabelledComparator<Individual> implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2064108858678702332L;
		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			final boolean isHind1 = (ind1 instanceof HeritableIndividual);
			final boolean isHind2 = (ind2 instanceof HeritableIndividual);
			if (isHind1 && !isHind2) {
				return 1;
			}
			if (!isHind1 && isHind2) {
				return -1;
			}
			if (!isHind1 && !isHind2) {
				return 0;
			}
			return ((HeritableIndividual)ind1).getCountOfIncompleteGenotypesSet() - ((HeritableIndividual)ind2).getCountOfIncompleteGenotypesSet();
		}
	}
	
	
	static class LitterSorter extends LabelledComparator<Individual> implements Serializable  {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8330440499518282082L;

		@Override
		public int compare (final Individual ind1, final Individual ind2) {
			final String litter1 = ind1.getLitter();
			final String litter2 = ind2.getLitter();
			return litter1 == null ? (litter2 == null ? 0 : -1) : (litter2 == null ? 1 : litter1.compareTo (litter2));
		}
	}
}