package napier.pedigree.model;

import java.util.Collection;
import java.util.EventListener;

import org.resspecies.model.Individual;

import model.graph.GraphModel;


public interface PedigreeSelectionModel extends EventListener {

	/**
	 * Set error model to associate the selections with
	 * @param errorModel ErrorCollator
	 */
	public void setErrorModel (final ErrorCollator errorModel);
	
	/**
	 * Get the error model associated with this selection model
	 * @return ErrorCollator
	 */
	public ErrorCollator getErrorModel ();
	
	/**
	 * Gets the currently selected subset of Individuals and their relationships
	 * @return GraphModel of selected Individuals and parental relationships to each other
	 */
    public GraphModel getSelectedGraph();
    
    
    /**
     * Add an Individual and relatives (descendant and ancestral) and partners (if required)
     * @param ind						- Individual to add as a seed
     * @param recurseDescendantsDepth	- Number of descendant generations to search and add relatives. Integer MAX_VALUE if you want no restriction.
     * @param recurseAncestorsDepth		- Number of ancestor generations to search and add relatives. Integer MAX_VALUE if you want no restriction.
     * @param includePartners			- add ind's partners to graph if true
     */
    public void addIndividual (final Individual ind, final int recurseDescendantsDepth, final int recurseAncestorsDepth, final boolean includePartners);
    
    /**
     * Remove an individual.
     * This may not just remove that individual, but the entire subgraph that was added along with it in addIndividal () - this is how
     * DefaultPedigreeSelectionModel works.
     * @param ind						- the Individual to remove
     */
    public void removeIndividual (final Individual ind);
    
    /**
     * Removes a collection of Individuals and their associated relations
     * @param indCollection				- Collection of Individuals to remove
     */
	public void removeIndividuals (final Collection<Individual> indCollection);
    
	
	/**
	 * Clear the entire pedigree selection model
	 */
    public void clearSelection ();
    
    
    /**
     * Redo current selections, if the implementation of the interface keeps track of added selections separately
     * Often called after a remove.
     */
    public void redoSelection ();
    
    
    /**
     * Individuals added using the addIndividual method. Shouldn't count recursed individuals.
     * @return Collection of Individual objects used as seeds for the current selection state
     */
    public Collection<Individual> getSeeds();
}
