package napier.pedigree.swing.app;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.table.TableCellRenderer;

import org.resspecies.inheritance.model.HeritableIndividual;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.categoriser.Categoriser;
import napier.pedigree.model.categoriser.impl.GenderCategoriser;
import napier.pedigree.model.categoriser.impl.IdentityCategoriser;
import napier.pedigree.model.categoriser.impl.MaskCategoriser;
import napier.pedigree.model.categoriser.impl.NullParentCategoriser;
import napier.pedigree.model.categoriser.impl.OffspringCategoriser;
import napier.pedigree.model.categoriser.impl.SingleMarkerInferredCategoriser;
import napier.pedigree.swing.renderers.base.AbstractErrorRenderer;
import napier.pedigree.swing.renderers.base.ErrorColourableRenderer;
import napier.pedigree.swing.renderers.errortable.IndErrorTableNumberRenderer;
import napier.pedigree.swing.renderers.errortable.MarkerErrorTableNumberRenderer;
import napier.pedigree.swing.renderers.graph.GraphIndividualNovelAlleleErrorRenderer;
import napier.pedigree.swing.renderers.graph.GraphInheritanceErrorEdgeRenderer;
import napier.pedigree.swing.renderers.histogram.AbstractColourScaleRenderer;
import napier.pedigree.swing.renderers.histogram.BinPresenceRenderer;
import napier.pedigree.swing.renderers.histogram.CountHeight3DRenderer;
import napier.pedigree.swing.renderers.histogram.CountHeightRenderer;
import napier.pedigree.swing.renderers.histogram.DefaultHistogramRenderer;
import napier.pedigree.swing.renderers.ped.GenderCellRenderer;
import napier.pedigree.swing.renderers.ped.IndividualCutoffMultiErrorRenderer;
import napier.pedigree.swing.renderers.ped.OffspringCollectionRenderer;
import napier.pedigree.swing.renderers.ped.OffspringMultiErrorRenderer;
import napier.pedigree.swing.renderers.ped.OffspringMultiMaxErrorRenderer;

public class RendererHolder {
	
	protected TableCellRenderer[] familyRenderers;
	protected Set<ErrorColourableRenderer> errorColourableRenderers;
	protected AbstractErrorRenderer[] genderRenderers;
	protected AbstractErrorRenderer[] graphRenderers;
	protected AbstractColourScaleRenderer[] errorgramRenderers;
	protected OffspringCollectionRenderer setRenderer;
	protected MarkerErrorTableNumberRenderer markerErrorTableRenderer;
	protected IndErrorTableNumberRenderer indErrorTableRenderer;
	
	protected Categoriser<HeritableIndividual>[] categorisers;

	
	public void make (final ErrorCollator errorModel, 
			final SorterHolder sorterObj,
			final BoundedRangeModel rangeModel) {
		setRenderer = new OffspringCollectionRenderer (sorterObj.getOffspringSorters());
		final OffspringMultiErrorRenderer aggregateErrorRenderer = new OffspringMultiErrorRenderer (errorModel, rangeModel);
		final OffspringMultiMaxErrorRenderer aggregateErrorRenderer2 = new OffspringMultiMaxErrorRenderer (errorModel, rangeModel);
	
		
		final IndividualCutoffMultiErrorRenderer individualRenderer = 
			new IndividualCutoffMultiErrorRenderer (errorModel, rangeModel);
		setRenderer.addRenderer (HeritableIndividual.class, individualRenderer);
		setRenderer.addRenderer (AbstractCollection.class, setRenderer);
		
		
		final GenderCellRenderer cellGenderRenderer = new GenderCellRenderer (errorModel, rangeModel);
		//final GenderCellRenderer cellGenderRenderer3 = new GenderCellRenderer (errorModel, rangeModel);

		final GraphIndividualNovelAlleleErrorRenderer graphIndividualRenderer = 
			new GraphIndividualNovelAlleleErrorRenderer (errorModel, rangeModel);
		final GraphInheritanceErrorEdgeRenderer graphEdgeRenderer = new GraphInheritanceErrorEdgeRenderer (errorModel, rangeModel);

		markerErrorTableRenderer = new MarkerErrorTableNumberRenderer (errorModel);
		indErrorTableRenderer = new IndErrorTableNumberRenderer (errorModel, rangeModel);
		
		
		
		familyRenderers = new TableCellRenderer[] {setRenderer, aggregateErrorRenderer, aggregateErrorRenderer2};
		
		errorgramRenderers = new AbstractColourScaleRenderer[] {
				new BinPresenceRenderer (), new CountHeightRenderer (0.2), new CountHeight3DRenderer (0.2), new DefaultHistogramRenderer () 
		};
		
		genderRenderers = new AbstractErrorRenderer[] {cellGenderRenderer}; //, cellGenderRenderer3};
		
		graphRenderers = new AbstractErrorRenderer[] {graphIndividualRenderer, graphEdgeRenderer};
		
		errorColourableRenderers = new HashSet<ErrorColourableRenderer> ();
		errorColourableRenderers.addAll (Arrays.asList (new ErrorColourableRenderer[] {individualRenderer, 
				aggregateErrorRenderer, aggregateErrorRenderer2,
				markerErrorTableRenderer, indErrorTableRenderer}));
		errorColourableRenderers.addAll (Arrays.asList (errorgramRenderers));
		errorColourableRenderers.addAll (Arrays.asList (genderRenderers));
		errorColourableRenderers.addAll (Arrays.asList (graphRenderers));
	
		categorisers = new Categoriser[] {new IdentityCategoriser (), new GenderCategoriser (), 
				new OffspringCategoriser (),
				new MaskCategoriser (errorModel.getPopCheckerContext()),
				new NullParentCategoriser ()
		};
	}
	
	
	public TableCellRenderer[] getFamilyRenderers () {
		return familyRenderers;
	}
	
	public AbstractErrorRenderer[] getGenderRenderers () {
		return genderRenderers;
	}
	
	public Set<ErrorColourableRenderer> getColourableRenderers () {
		return errorColourableRenderers;
	}
	
	public AbstractErrorRenderer[] getGraphRenderers () {
		return graphRenderers;
	}
	
	public Categoriser<HeritableIndividual>[] getCategorisers () {
		return categorisers;
	}
	
	public AbstractColourScaleRenderer[] getErrorgramRenderers () {
		return errorgramRenderers;
	}
	
	public OffspringCollectionRenderer getSetRenderer () {
		return setRenderer;
	}
	
	public MarkerErrorTableNumberRenderer getMarkerErrorTableRenderer () {
		return markerErrorTableRenderer;
	}
	
	public IndErrorTableNumberRenderer getIndErrorTableRenderer () {
		return indErrorTableRenderer;
	}
	
	public void addColourableRenderer (final ErrorColourableRenderer colourableRenderer) {
		errorColourableRenderers.add (colourableRenderer);
	}
}
