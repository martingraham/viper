package napier.pedigree.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.resspecies.datasourceaware.FallBackIndividual;
import org.resspecies.model.Individual;

import util.collections.SingleItemSet;

import model.graph.AbstractGraphModel;
import model.graph.Edge;
import model.graph.impl.EdgeInstance;
import napier.pedigree.model.AbstractPedigreeSelectionModel;


public class DefaultPedigreeSelectionModel extends AbstractPedigreeSelectionModel {

	protected Set<Seed> seedData;


	public DefaultPedigreeSelectionModel () {
		super ();
		seedData = new HashSet<Seed> ();
	}
	
	
	@Override
	public void addIndividual (final Individual ind, final int recurseDescendantsDepth, 
			final int recurseAncestorsDepth, final boolean includePartners) {
		final Seed newSeed = new Seed (ind, recurseDescendantsDepth, recurseAncestorsDepth, includePartners);
		if (!seedData.contains (newSeed)) {
			seedData.add (newSeed);
			seeds.add (ind);
			select (newSeed, newSeed.getInd());
			selectionChanged ();
		}
	}

	
	@Override
	public void removeIndividual (final Individual ind) {
		final Set<Individual> sis = new SingleItemSet<Individual> ();
		sis.add (ind);
		removeIndividuals (sis);
	}
	
	
	@Override
	public void removeIndividuals (final Collection<Individual> indCollection) {
		final Iterator<Seed> iter = seedData.iterator();
		boolean modified = false;
		
		while (iter.hasNext()) {
			final Seed seed = iter.next();
			if (indCollection.contains (seed.getInd())) {
				iter.remove ();
				modified = true;
				seeds.remove (seed.getInd());
			}
		}
		
		if (modified) {
			redoSelection ();
		}
	}
	
	
	@Override
	public void clearSelection() {
		super.clearSelection ();
		seedData.clear ();
	}
	
	
	@Override
	public void redoSelection () {
		super.clearSelection(); // super.clearSelection() as we don't want to clear seedData
		
		for (Seed seed : seedData) {
			seeds.add (seed.getInd());
			select (seed, seed.getInd());
		}
		
		selectionChanged ();
	}
	
	
	
	protected void select (final Seed newSeed, final Individual currentInd) {
		this.getSelectedGraph().addNode (currentInd);
		if (newSeed.getAncestorsDepth() > 0) {
			selectRecurseUp (currentInd, currentInd, newSeed.getAncestorsDepth());
		}
		if (newSeed.getDescendantsDepth() > 0) {
			selectRecurseDown (currentInd, currentInd, newSeed.getDescendantsDepth(), newSeed.includePartners);
		}
	}
	
	
	protected void selectRecurseUp (final Individual currentInd, final Individual lastInd, final int depth) {
		if (currentInd != null && !(currentInd instanceof FallBackIndividual)) {
			if (currentInd != lastInd) {
				this.getSelectedGraph().addNode (currentInd);
				addRelationship (currentInd, lastInd);
			}
			final int newDepth = depth - 1;
			if (newDepth >= 0) {
				selectRecurseUp (currentInd.getSire(), currentInd, newDepth);
				selectRecurseUp (currentInd.getDam(), currentInd, newDepth);
			}
		}
	}
	
	protected void selectRecurseDown (final Individual currentInd, final Individual lastInd, final int depth, final boolean addOtherParentsFirstRecursion) {
		if (currentInd != null && !(currentInd instanceof FallBackIndividual)) {
			if (currentInd != lastInd) {
				this.getSelectedGraph().addNode (currentInd);
				addRelationship (currentInd, lastInd);
			}
			final int newDepth = depth - 1;
			//System.err.println ("currentInd: "+currentInd.getName()+", newdepth: "+newDepth);
			if (newDepth >= 0) {
				final Collection<? extends Individual> offspring = currentInd.getOffspring();
				for (Individual child : offspring) {
					selectRecurseDown (child, currentInd, newDepth, false);
					
					if (addOtherParentsFirstRecursion) {
						final Individual otherParent = (child.getSire() == currentInd ? child.getDam() : child.getSire());
						if (otherParent != null && !(otherParent instanceof FallBackIndividual)) {
							this.getSelectedGraph().addNode (otherParent);
							selectRecurseDown (child, otherParent, 0, false);
						}
					}
				}
			}
		}
	}
	
	protected void addRelationship (final Individual currentInd, final Individual lastInd) {
		final Edge edge = new EdgeInstance (currentInd, lastInd, Boolean.TRUE);
		if (! this.getSelectedGraph().containsEdge (edge)) {
			this.getSelectedGraph().addEdge (edge);
		}
	}
	
	
	public void selectionChanged () {
		((AbstractGraphModel)selectedSubGraph).fireGraphStructureChanged();
	}
	
	
	class Seed {

		Individual ind;
		int descendantsDepth, ancestorsDepth;
		boolean includePartners;
		
		public Seed (final Individual ind, final int recurseDescendantsDepth, final int recurseAncestorsDepth, final boolean includePartners) {
			this.ind = ind;
			descendantsDepth = recurseDescendantsDepth;
			ancestorsDepth = recurseAncestorsDepth;
			this.includePartners = includePartners;
		}
		
		public final Individual getInd() {
			return ind;
		}

		public final int getDescendantsDepth() {
			return descendantsDepth;
		}

		public final int getAncestorsDepth() {
			return ancestorsDepth;
		}

		public final boolean isIncludePartners() {
			return includePartners;
		}

		private DefaultPedigreeSelectionModel getOuterType() {
			return DefaultPedigreeSelectionModel.this;
		}	
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ancestorsDepth;
			result = prime * result + descendantsDepth;
			result = prime * result + (includePartners ? 1231 : 1237);
			result = prime * result + ((ind == null) ? 0 : ind.hashCode());
			return result;
		}

		@Override
		public boolean equals (final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Seed other = (Seed) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (ancestorsDepth != other.ancestorsDepth) {
				return false;
			}
			if (descendantsDepth != other.descendantsDepth) {
				return false;
			}
			if (includePartners != other.includePartners) {
				return false;
			}
			if (ind == null) {
				if (other.ind != null) {
					return false;
				}
			} else if (!ind.equals(other.ind)) {
				return false;
			}
			return true;
		}
	}
}
