package napier.pedigree.model;

import java.util.Collection;

import javax.swing.table.TableModel;

import napier.pedigree.model.categoriser.Categoriser;

import org.resspecies.inheritance.model.HeritableFamily;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

/**
 * Interface for a TableModel that takes data corresponding to the families that make
 * a bridge between two generations.
 * @author cs22
 *
 */
public interface PedigreeGenerationModel extends TableModel, PolygamyState {

	
	/**
	 * Make the model from a collection of HeritableFamily objects.
	 * @param families
	 */
	public void make (final Collection<HeritableFamily> families);
	
	
	/**
	 * remake the model using cached HeritableFamily objects
	 * i.e. the ones last used to call make (Collection<HeritableFamily>)
	 */
	public void remake ();
	
	/**
	 * @return index of generation of supplied HeritableFamily set. Set in constructor.
	 */
	public int getGenerationIndex();
	
	/**
	 * @return Collection of sire Individuals for this generation
	 */
	public Collection<Individual> getSires();
	
	/**
	 * @return Collection of dam Individuals for this generation
	 */
	public Collection<Individual> getDams();
	
	
	/**
	 * Set new categoriser for splitting offspring into separate rows
	 * @param newCategoriser
	 */
	public void setCategoriser (final Categoriser<HeritableIndividual> newCategoriser);
	
	
	/**
	 * @return the current Catgeoriser for splitting offspring
	 */
	public Categoriser<HeritableIndividual> getCategoriser ();
	
	
	/**
	 * Do the splitting of offspring using the current Categoriser
	 */
	public void splitOffspring ();
	
	/**
	 * Test if there are offspring that match a particular value for the current Categoriser object
	 * i.e. test if the associated row is empty
	 * @param categoryValue
	 * @return boolean showing whether offspring occur in this category value at all
	 */
	public boolean areSplitOffspringPresent (final int categoryValue);
	
	
	/**
	 * Used in row sorting. Used to make sure that the DAM related rows (dams and dam split row) 
	 * are judged of lower value than any other rows, and thus always drawn at the bottom of the 
	 * table when used in conjunction with a PedigreeGenerationModelRowSorter object;
	 * This is needed as split offspring rows are at the end of the TableModel in row order,
	 * but we want them drawn before the dam related rows.
	 * @param modelRow
	 * @return priority of the current row
	 */
	public Integer getRowPriority (final int modelRow);
}
