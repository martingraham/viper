package napier.pedigree.model.categoriser.impl;

import org.resspecies.inheritance.model.HeritableIndividual;

import napier.pedigree.model.categoriser.AbstractCategoriser;

public class NullParentCategoriser extends AbstractCategoriser<HeritableIndividual> {

	
	public NullParentCategoriser () {
		super ("NullParentCategoriser");
	}
	
	@Override
	public int getMinValue() {
		return 0;
	}

	@Override
	public int getMaxValue() {
		return 3;
	}
	
	@Override
	public int categorise (final HeritableIndividual obj) {
		final boolean nullFather = obj.isFatherMasked();
		final boolean nullMother = obj.isMotherMasked();
		return ((nullMother ? 1 : 0) << 1) + (nullFather ? 1 : 0);
	}
}
