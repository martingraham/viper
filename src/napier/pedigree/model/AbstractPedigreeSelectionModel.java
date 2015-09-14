package napier.pedigree.model;

import java.util.Collection;
import java.util.HashSet;

import org.resspecies.model.Individual;

import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;



public abstract class AbstractPedigreeSelectionModel implements PedigreeSelectionModel {

	//static final private Logger LOGGER = Logger.getLogger (AbstractPedigreeSelectionModel.class);

	
	protected GraphModel selectedSubGraph;
	protected ErrorCollator errorModel;
	protected Collection<Individual> seeds;
	
	
	public AbstractPedigreeSelectionModel () {
		selectedSubGraph = new SymmetricGraphInstance ();
		seeds = new HashSet<Individual> ();
	}
	
	@Override
	public void setErrorModel (final ErrorCollator newErrorModel) {
		errorModel = newErrorModel;
	}
	
	@Override
	public ErrorCollator getErrorModel () {
		return errorModel;
	}

	@Override
    public GraphModel getSelectedGraph() {
		return selectedSubGraph;
	}
    

	@Override
    public Collection<Individual> getSeeds() { return seeds; }


	@Override
	public void clearSelection() {
		selectedSubGraph.clear ();
		seeds.clear ();
	}
}
