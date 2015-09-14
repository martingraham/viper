package napier.pedigree.model;

import java.util.Set;

import org.resspecies.inheritance.model.SNPMarker;

/**
 * Interface to store markers that need recalculated when a recalculation next occurs
 * Done so we can chop down a full recalc to just the markers that require it
 * @author cs22
 *
 */
public interface ChangeOnNextRecalcStore {

	// Flags for things the user has changed since last recalculation. Essentially for marker only change
	// we only need to recalculate the changes for those markers.
	public final static int MARKER_CHANGE = 16, IND_CHANGE = 32, GENO_CHANGE = 64;
	
	/**
	 * @return flags value. See above for flag mask values.
	 */
	public int getFlags ();
	
	/**
	 * Logically Or a flag mask value to the current flag value
	 * @param flag - flag mask value
	 */
	public void addFlag (int flag);
	
	/**
	 * @return boolean - true if only markers are slated to change
	 */
	public boolean onlyMarkersToChange ();
	
	/**
	 * @return Set of SNPMarker objects that need recalculated
	 */
	public Set<SNPMarker> getMarkersToChange ();
	
	/**
	 * Add a bunch of markers to the current set that needs recalculated
	 * @param markersToAdd Set of SNPMarker objects to add to those that need recalculated
	 */
	public void addMarkersToChange (Set<SNPMarker> markersToAdd);
	
	
	/**
	 * Clear the marker set and flags. Usually called right after a recalculation has happened,
	 * clearing the object for the next time.
	 */
	public void clear ();
}
