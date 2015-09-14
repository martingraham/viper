package napier.pedigree.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.resspecies.datasourceaware.FallBack;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;

public final class PopulateOffspring {
	
	private final static PopulateOffspring INSTANCE = new PopulateOffspring ();
	
	private PopulateOffspring () {}
	
	
	public static PopulateOffspring getInstance() { return INSTANCE; }
	
	

	public void populateAllOffspring (final HeritablePopulation hPop) {
		
		final Collection<Individual> inds = hPop.getIndividuals();
		final Set<Individual> childless = new HashSet<Individual> (inds);
		
		for (Individual ind : inds) {
			final Individual sire = ind.getSire ();
			if (sire != null && sire != FallBack.SAFE_SIRE) {
				sire.addOffspring (ind);
				childless.remove (sire);
			}
			final Individual dam = ind.getDam ();
			if (dam != null && dam != FallBack.SAFE_DAM) {
				dam.addOffspring (ind);
				childless.remove (dam);
			}
		}
		
		for (Individual notAParent : childless) {
			notAParent.addOffspring (FallBack.SAFE_INDIVIDUAL);
		}
	}
}
