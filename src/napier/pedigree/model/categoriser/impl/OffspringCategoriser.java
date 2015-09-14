package napier.pedigree.model.categoriser.impl;

import org.resspecies.inheritance.model.HeritableIndividual;

import napier.pedigree.model.categoriser.AbstractCategoriser;

public class OffspringCategoriser extends AbstractCategoriser<HeritableIndividual> {

	public OffspringCategoriser () {
		super ("OffspringCategoriser");
	}
	
	@Override
	public int getMinValue() {
		return 0;
	}

	@Override
	public int getMaxValue() {
		return 1;
	}
	
	
	@Override
	public int categorise (final HeritableIndividual obj) {
		return obj.getOffspring().isEmpty() ? getMinValue() : getMaxValue();
	}
}
