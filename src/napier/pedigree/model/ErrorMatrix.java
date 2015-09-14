package napier.pedigree.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

/**
 * Model that stores dual complementary hash maps of markers --> list of individuals 
 * and individuals --> list of markers.
 * These represent the specific errors for a given error type 
 * (i.e. we have multiple ErrorMatrixModels, one for errors to sire, one for errors to dam etc, 
 * a set for initial errors, a set for current errors etc)
 * @author cs22
 *
 */
public interface ErrorMatrix {
	
	/**
	 * Add an error for a particular Marker and Individual combo
	 * @param marker - SNPMarker
	 * @param ind - Individual
	 */
	public void addError (final SNPMarker marker, final Individual ind);
	
	/**
	 * Count the errors associated with a Marker
	 * i.e. the size of the Individual list associated with the Marker
	 * @param marker - Marker
	 * @return errorCount
	 */
	public int errorCount (final SNPMarker marker);
	
	/**
	 * Count the errors associated with an Individual
	 * i.e. the size of the Marker list associated with the Individual
	 * @param ind - Individual
	 * @return errorCount
	 */
	public int errorCount (final Individual ind);
	
	
	/**
	 * get the entire map of Markers --> Lists of Individuals
	 * @return Map of Marker --> Lists of Individuals
	 */
	public Map<SNPMarker, List<Individual>> getMarkerMap ();
	
	/**
	 * get the entire map of Individuals --> Lists of Markers
	 * @return Map of Individuals --> Lists of Markers
	 */
	public Map<Individual, List<SNPMarker>> getIndividualMap ();
	
    
    /**
    * new by trevor to allow  genotypes to be added all at once
    * nb the map of incomplete genotypes  in the Population uses a Set to simplify 
    * updates, so the parameter here matches this and must be converted to a List
    * however it might be better to use the same signature of map so can just
    * use the original Map by reference
    * @param map - the map of errors, marker to Set of individuals
    */
   public void setMarkerMap (Map<SNPMarker, List<Individual>> map);
   
   /**
    * new by trevor to allow  genotypes to be added all at once
    * nb the map of incomplete genotypes  in the Population uses a Set to simplify 
    * updates, so the parameter here matches this and must be converted to a List
    * however it might be better to use the same signature of map so can just
    * use the original Map by reference
    * @param map - the map of errors, marker to Set of individuals
    */
   public void setIndividualMap (Map<Individual, List<SNPMarker>> map);
           

	
	/**
	 * Empty model of all data
	 */
	public void clear ();
	

	
	/**
	 * Make a deep copy
	 */
	public ErrorMatrix makeDeepCopy ();
	
	
	
	/**
	 * Convenience method for multiple calls to filterMarker
	 * @param maskMarkers - Markers in question
	 * @param include - true to include, false to exclude
	 */
	public void filterMarkers (final Collection<? extends SNPMarker> maskMarkers, final boolean include, 
			final ErrorMatrix backupMatrix);
	
	/**
	 * Include/exclude a given marker in/out from the filtered marker map
	 * @param maskMarker - Marker in question
	 * @param include - true to include, false to exclude
	 */
	public void filterMarker (final SNPMarker maskMarker, final boolean include, 
			final ErrorMatrix backupMatrix);	
	
	
	/*
	 * No need for filterIndividual / filterIndividuals methods
	 * If an individual is masked it reports zero errors anyways
	 * plus recalculating is always necessary if an individual is masked
	 * as errors can get shifted between individuals. Errors can't be
	 * shifted between markers so markers can be safely filtered without
	 * recalculating.
	 */
	
	
    /**
     * Adds a listener to the list that is notified each time a change
     * to the data model occurs.
     *
     * @param	emml		the ErrorMatrixModelListener
     */
    public void addErrorMatrixModelListener (ErrorMatrixListener emml);

    /**
     * Removes a listener from the list that is notified each time a
     * change to the data model occurs.
     *
     * @param	emml		the ErrorMatrixModelListener
     */
    public void removeErrorMatrixModelListener (ErrorMatrixListener emml);
}
