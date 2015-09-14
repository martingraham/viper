package napier.pedigree.swing.app;

import java.util.Collection;

import napier.pedigree.swing.JGeneration;
import org.resspecies.model.Individual;

/**
 * Interface that marks an object as being able to perform some sort of selection on a generation,
 * often just as simple as sending a single Individual object to some other object
 * @author cs22
 *
 */
public interface PedigreeSelectionSource {

	/**
	 * The individual selected by this interface
	 * @return an Individual object
	 */
	public Individual getSelectedIndividual ();
	
	/**
	 * The group selected by this interface
	 * @return a Collection of Individual objects
	 */
	public Collection<Individual> getSelectedGroup ();
	
	/**
	 * The generation widget this selection occurred in
	 * @return the JGeneration where this selection occurred
	 */
	public JGeneration getSelectedJGeneration ();
}
