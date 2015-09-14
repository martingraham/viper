package napier.pedigree.model.categoriser.impl;

import org.resspecies.inheritance.model.HeritableIndividual;

import napier.pedigree.model.categoriser.AbstractCategoriser;

public class IdentityCategoriser extends AbstractCategoriser<HeritableIndividual> {

	public IdentityCategoriser () {
		super ("IdentityCategoriser");
	}
	
	@Override
	public int getMinValue() {
		return 0;
	}

	@Override
	public int getMaxValue() {
		return 0;
	}
	
	@Override
	public int categorise (final HeritableIndividual obj) {
		return getMaxValue();
	}
}
