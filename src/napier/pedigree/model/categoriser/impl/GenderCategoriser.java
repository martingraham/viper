package napier.pedigree.model.categoriser.impl;

import org.resspecies.inheritance.model.HeritableIndividual;

import napier.pedigree.model.categoriser.AbstractCategoriser;

public class GenderCategoriser extends AbstractCategoriser<HeritableIndividual> {

	public GenderCategoriser () {
		super ("GenderCategoriser");
	}
	
	@Override
	public int getMinValue() {
		return 0;
	}

	@Override
	public int getMaxValue() {
		return 2;
	}
	
	@Override
	public int categorise (final HeritableIndividual obj) {
		final String gender = obj.getGender ();
		return "M".equals(gender) ? getMinValue(): ("F".equals(gender) ? getMaxValue() : 1);
	}
}
