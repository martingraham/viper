package napier.pedigree.model;

import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Set;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

/**
 * Helper interface that simplifies some masking requests
 * @author cs22
 *
 */
public interface PopCheckerWrapper {

	
	/**
	 * Set the HeritablePopulation object this class should be acting on
	 * @param hPop HeritablePopulation
	 */
	public void setPopulation (final HeritablePopulation hPop);
	
	
	/**
	 * A reference to the HeritablePopulation object this class is currently basing
	 * its data on.
	 * @return population HeritablePopulation
	 */
	public HeritablePopulation getPopulation ();
	
	
	/**
	 * Sets a focus Marker instance for when dealing with single Marker error issues
	 * @param marker Marker
	 */
	public void setFocusMarker (final SNPMarker marker);
	
	
	/**
	 * Gets the current focus Marker instance, null if none set
	 * @return marker Marker
	 */
	public SNPMarker getFocusMarker ();
	
	
	/**
	 * Updates a map of Individuals by genotype values for the current focus marker
	 * Usually called in response to a recalculation of the HeritablePopulation
	 * There was probably some speed issue to cache this stuff originally, but the map
	 * doesn't get used now.
	 */
	public void updateFocusMarkerGenotypes ();
	
	
	/**
	 * Gets genotypes for the current focus Marker instance, indexed in a map by Individuals.
	 * There was probably some speed issue such as caching to do this originally, but it doesn't
	 * get used now.
	 * @return Map of Individuals to genotype values
	 */
	public Map<Individual, Integer> getFocusMarkerGenotypes ();
	
	
	/**
	 * Tests if an individual is masked given the context of the markers under consideration.
	 * 
	 * For now, this just means it looks for individual masking if no focus marker is declared
	 * or a genotype masking for that individual if a focus marker is declared
	 * @param hInd - individual to test for markedness
	 * @return true if marked given the current context
	 */
	public boolean isIndividualContextMasked (final Individual hInd);
	
	
	/**
	 * Masks a whole individual if no focus marker is declared
	 * or a genotype masking for that individual if a focus marker is declared
	 * @param hInd
	 */
	public void contextMaskIndividual (final HeritableIndividual hInd);
	
	
	/**
	 * Unmasks a whole individual if no focus marker is declared
	 * or a genotype unmasking for that individual if a focus marker is declared
	 * @param hInd
	 */
	public void contextUnmaskIndividual (final HeritableIndividual hInd);
	
	
	/**
	 * Convenience method for getting all markers from a HeritablePopulation object.
	 * The same method in HeritablePopulation makes a cloned set each time its called
	 * so it may be better to cache this info at this level, or if not, just pass the
	 * call straight through to HeritablePopulation.
	 * @return Set of all SNPMarker object in current HeritablePopulation
	 */
	public Set<SNPMarker> getAllMarkers ();
	
}
