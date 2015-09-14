package napier.pedigree.swing.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.shared.MultiComparator;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.sort.BetweenOffspringSetSorters;
import napier.pedigree.model.sort.IndividualSorters;
import napier.pedigree.model.sort.TableMultiComparator;
import napier.pedigree.swing.JGeneration;


import org.resspecies.model.Individual;

/**
 * Class that acts as a holder for various Comparator objects
 * @author cs22
 *
 */
public class SorterHolder {

	
	protected MultiComparator<Individual> offspringSorters;
	protected TableMultiComparator generationSorters;
	protected Map<JGeneration, Map<Class<?>, Comparator<Individual>>> generationSpecificComparators;
	
	/**
	 * Make the comparators from the information in the JGeneration objects and the ErrorCollator object
	 * @param errorModel - object to get error data from for sorting
	 * @param generations - objects to make sorters for
	 */
	public void make (final ErrorCollator errorModel, final Collection<JGeneration> generations) {
		
		final Comparator<Individual> badGenotypeSorter = IndividualSorters.getInstance().new GenotypeSorter (errorModel);
		final Comparator<Individual> novelAlleleSorter = IndividualSorters.getInstance().new NovelAlleleSorter (errorModel);
		final Comparator<Individual> notFromDamSorter = IndividualSorters.getInstance().new NotFromDamSorter (errorModel);
		final Comparator<Individual> notFromSireSorter = IndividualSorters.getInstance().new NotFromSireSorter (errorModel);
		final Comparator<Individual> offspringNotFromDamSorter = IndividualSorters.getInstance().new OffspringTotalDamErrorSorter (errorModel);
		final Comparator<Individual> offspringNotFromSireSorter = IndividualSorters.getInstance().new OffspringTotalSireErrorSorter (errorModel);
		final Comparator<Individual> offspringAllErrorsSorter = IndividualSorters.getInstance().new OffspringAllErrorsSorter (errorModel);
		final Comparator<Individual> offspringRatioAllErrorsSorter = IndividualSorters.getInstance().new OffspringRatioAllErrorsSorter (errorModel);
		//final Comparator<Collection<Individual>> offspringCollectionAllErrorsSorter = BetweenOffspringSetSorters.getInstance().new OffspringCollectionAllErrorsSorter (errorModel);
		
		final Comparator[] withinSetSorters = new Comparator[] {
				IndividualSorters.NAME_SORTER, IndividualSorters.GENDER_SORTER,
				IndividualSorters.OFFSPRING_COUNT_SORTER, 
				badGenotypeSorter,
				notFromSireSorter,
				novelAlleleSorter,
				notFromDamSorter,
				IndividualSorters.INC_MARKER_SORTER,
				IndividualSorters.LITTER_SORTER};
		
		offspringSorters = IndividualSorters.getInstance().makeMultiComparator (withinSetSorters);

		final Comparator<?>[] betweenSetSorters = new Comparator<?>[] {
				BetweenOffspringSetSorters.OFFSPRING_SET_COUNT_SORTER, 
				BetweenOffspringSetSorters.getInstance().new OffspringCollectionAllErrorsSorter (errorModel),
				BetweenOffspringSetSorters.getInstance().new OffspringCollectionRatioAllErrorsSorter (errorModel),
				//BetweenOffspringSetSorters.GRAND_OFFSPRING_SET_COUNT_SORTER
		};

		
		final Comparator<?>[] sireBasedSorters = new Comparator<?>[] { 
				IndividualSorters.NAME_SORTER,
				badGenotypeSorter,
				IndividualSorters.getInstance().new PartnerCountSorter (generations.iterator().next()),
				IndividualSorters.OFFSPRING_COUNT_SORTER,
				//IndividualSorters.DAM_SORTER,
				//IndividualSorters.SIRE_SORTER,
				//novelAlleleSorter,
				//notFromDamSorter,
				//notFromSireSorter,
				offspringNotFromSireSorter,
				offspringAllErrorsSorter,
				offspringRatioAllErrorsSorter
		};
		
		final Comparator<?>[] damBasedSorters = new Comparator<?>[] { 
				IndividualSorters.NAME_SORTER,
				badGenotypeSorter,
				IndividualSorters.getInstance().new PartnerCountSorter (generations.iterator().next()),
				IndividualSorters.OFFSPRING_COUNT_SORTER,
				//IndividualSorters.DAM_SORTER,
				//IndividualSorters.SIRE_SORTER,
				//novelAlleleSorter,
				//notFromDamSorter,
				//notFromSireSorter,
				offspringNotFromDamSorter,
				offspringAllErrorsSorter,
				offspringRatioAllErrorsSorter
		};
		
		final List<Comparator<Object>> allSort = new ArrayList (Arrays.asList (sireBasedSorters));
		allSort.addAll (new ArrayList (Arrays.asList (damBasedSorters)));
		allSort.addAll (new ArrayList (Arrays.asList (betweenSetSorters)));
		
		final List<Integer> rowIndices = new ArrayList<Integer> ();
		for (int n = 0; n < sireBasedSorters.length; n++) {
			rowIndices.add (Integer.valueOf (ModelRowConstants.SIRE));
		}
		for (int n = 0; n < damBasedSorters.length; n++) {
			rowIndices.add (Integer.valueOf (ModelRowConstants.DAM));
		}
		for (int n = 0; n < betweenSetSorters.length; n++) {
			rowIndices.add (Integer.valueOf (ModelRowConstants.OFFSPRING));
		}
		
		generationSorters = new TableMultiComparator (allSort, rowIndices);
		
		constructGenerationSpecificSorters (generations);
	}
	
	
	/**
	 * Makes a map of generation specific comparators for certain comparator classes.
	 * At the moment only makes specific comparators for the PartnerCountSorter class
	 * @param generations - the collection of JGeneration objects to generate specific comparators for.
	 */
	public void constructGenerationSpecificSorters (final Collection<JGeneration> generations) {
		generationSpecificComparators = new HashMap<JGeneration, Map<Class<?>, Comparator<Individual>>> (generations.size());
		final IndividualSorters indSorterInstance = IndividualSorters.getInstance();
		for (JGeneration thisGen : generations) {
			final Map<Class<?>, Comparator<Individual>> classCompMap = new HashMap<Class<?>, Comparator<Individual>> ();
			generationSpecificComparators.put (thisGen, classCompMap);
			final Comparator<Individual> genSpecComp2 = indSorterInstance.new PartnerCountSorter (thisGen);
			classCompMap.put (genSpecComp2.getClass(), genSpecComp2);
		}
	}
	
	
	/**
	 * Routine that takes in a general TableMultiComparator instance and swaps in generation specific comparators
	 * for certain classes of comparator for use with that JGeneration instance
	 * see constructGenerationSpecificSorters () for details of these generation specific sorters
	 * @param mComp - general TableMultiComparator as input
	 * @param generation	-	JGeneration to insert specific comparators for
	 * @return TableMultiComparator tailored to a specific JGeneration instance
	 */
	public TableMultiComparator copyTableMultiComparator (final TableMultiComparator mComp, final JGeneration generation) {
		final Map<Class<?>, Comparator<Individual>> genSpecificComps = generationSpecificComparators.get (generation);
		final TableMultiComparator mCompCopy = new TableMultiComparator (mComp);
		final List<Comparator<Object>> compList = mCompCopy.getComparatorList();
		for (int compIndex = compList.size(); --compIndex >= 0;) {
			final Class<?> klass = compList.get(compIndex).getClass();
			final Comparator comp = genSpecificComps.get (klass);
			if (comp != null) {
				compList.set (compIndex, comp);
			}
		}
		return mCompCopy;
	}
	
	/**
	 * @return MultiComparator for sorting within groups of Individuals (offspring)
	 */
	public MultiComparator<Individual> getOffspringSorters () { return offspringSorters; }
	
	
	/**
	 * @return TableMultiComparator for comparing columns in tables (based on attributes in different rows)
	 */
	public TableMultiComparator getGenerationSorters () { return generationSorters; }
}
