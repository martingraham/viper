package napier.pedigree.model;

import java.util.Set;

import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

/**
 * Interface to calculate masked genotype totals for individuals and markers.
 * - For marker totals we count masked individuals as +1
 * - But for individual totals we don't count masked markers (only the active marker set)
 * - And I can't remember why we reached that decision.
 * Done 'cos caching is way quicker than querying HeritablePopulation every time we need this info
 * @author Martin
 *
 */
public interface MaskedGenotypeTotaliser {

	/**
	 * calculate the appropriate masked genotype counts for all Markers and Individuals in a population
	 * @param hPop - HeritablePopulation concerned
	 * @param activeMarkers - set of non-masked Markers
	 */
	public void calculateTotals (final HeritablePopulation hPop, final Set<SNPMarker> activeMarkers);
	
	/**
	 * Work out masked genotypes for an Individual
	 * @param ind - Individual concerned
	 * @param hPop - HeritablePopulation concerned
	 * @return int - number of masked genotypes for that Individual over the active marker set
	 */
	public int getMaskedGenotypeTotal (final Individual ind, final HeritablePopulation hPop);
	
	/**
	 * Work out masked genotypes for a Marker
	 * @param marker - Marker concerned
	 * @param hPop - HeritablePopulation concerned
	 * @return int - number of masked genotypes for that Marker over all Individuals (masked Individuals count as masked genotypes too)
	 */
	public int getMaskedGenotypeTotal (final SNPMarker marker, final HeritablePopulation hPop);
	
	/**
	 * Clear this model
	 */
	public void clear ();
}
