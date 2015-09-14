package napier.pedigree.model.filter;

import org.resspecies.model.Individual;

public interface IndividualFilter {

	/**
	 * Simple interface that decides whether or not to include
	 * an individual in operations.
	 * @param ind - Individual object
	 * @return boolean - true to include
	 */
	public boolean include (final Individual ind);
}
