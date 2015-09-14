package napier.pedigree.model.categoriser.impl;

import org.resspecies.inheritance.model.HeritableIndividual;

import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.categoriser.AbstractCategoriser;

public class MaskCategoriser extends AbstractCategoriser<HeritableIndividual> {

	
	protected PopCheckerWrapper popCheckerContext;
	
	public MaskCategoriser (final PopCheckerWrapper popCheckerContext) {
		super ("MaskCategoriser");
		this.popCheckerContext = popCheckerContext;
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
		return popCheckerContext.isIndividualContextMasked (obj) ? getMaxValue() : getMinValue();
	}
}
