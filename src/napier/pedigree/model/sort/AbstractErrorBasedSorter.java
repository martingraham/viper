package napier.pedigree.model.sort;

import napier.pedigree.model.ErrorCollator;

import org.resspecies.model.Individual;

abstract public class AbstractErrorBasedSorter extends LabelledComparator<Individual> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4975224948972691893L;
	protected ErrorCollator bad;
	protected int errorType;
	
	public AbstractErrorBasedSorter (final ErrorCollator bad,
			final int errorType) {
		super ();
		this.bad = bad;
		this.errorType = errorType;
	}
	
	@Override
	public int compare (final Individual ind1, final Individual ind2) {	
		 return (ind1 == ind2 ? 0 : bad.getFilteredIndividualErrorCount (errorType, ind2) 
		 	- bad.getFilteredIndividualErrorCount (errorType, ind1));
	}
}
