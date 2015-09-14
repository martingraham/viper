package napier.pedigree.model.categoriser.impl;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.PopCheckerWrapper;
import napier.pedigree.model.categoriser.AbstractCategoriser;

public class SingleMarkerInferredCategoriser extends AbstractCategoriser<HeritableIndividual> {

	protected PopCheckerWrapper errorModel;
	
	public SingleMarkerInferredCategoriser (final PopCheckerWrapper popCheckerContext) {
		super ("SingleMarkerInferredCategoriser");
		this.errorModel = popCheckerContext;
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
	public int categorise (final HeritableIndividual hInd) {
		//final Genotype geno = obj.getGenotype (bgm.getFocusMarker());
		final SNPMarker focusMarker = errorModel.getFocusMarker();
		if (focusMarker == null) {
			return getMinValue();	
		}
		return errorModel.getPopulation().genotypeIsInferred (focusMarker, hInd) ? getMaxValue() : getMinValue();
	}
}
