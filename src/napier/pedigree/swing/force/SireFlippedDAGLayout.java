package napier.pedigree.swing.force;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.resspecies.datasourceaware.FallBackIndividual;
import org.resspecies.model.Individual;

import swingPlus.graph.JGraph;
import swingPlus.graph.ObjectPlacement;
import swingPlus.graph.force.impl.MedianSortHoriz;

/**
 * Flips Y-coords of sire nodes and their parents after layout is complete
 * @author cs22
 *
 */
public class SireFlippedDAGLayout extends MedianSortHoriz {

	
	private static final Logger LOGGER = Logger.getLogger (SireFlippedDAGLayout.class);
	protected boolean flipEnabled;
	


	public SireFlippedDAGLayout () {
		this (SwingConstants.HORIZONTAL);
	}
	
	public SireFlippedDAGLayout (final int horizOrVert) {
		super (horizOrVert);
		flipEnabled = true;
	}
	
	@Override
	public void calculateAttractiveForces (final JGraph graph) {
		super.calculateAttractiveForces (graph);
		if (isFlipEnabled()) {
			flipSireSeeds (graph);
		}
	}
	
	protected void flipSireSeeds (final JGraph graph) {
		final List<Object> bottomLayer = layerLists.get (layerLists.size() - 1);
		if (bottomLayer != null && !bottomLayer.isEmpty()) {
			final int bottomListRank = ranks.get(bottomLayer.get(0)).intValue();
			final Set<Individual> toFlip = new HashSet<Individual> ();
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug ("bottomlayer: "+bottomLayer);
				LOGGER.debug ("layerlist count: "+layerLists.size());
				LOGGER.debug ("rank of bottom list item: "+ranks.get(bottomLayer.get(0)));
			}
			
			for (Object obj : bottomLayer) {
				if (obj instanceof Individual) {
					final Individual ind = (Individual)obj;
					final Individual sire = ind.getSire();
					if (isValidIndividual (sire)) {
						toFlip.add (sire);
						
						final Individual grandSire = sire.getSire ();
						if (isValidIndividual (grandSire) && hasSonsOnly (grandSire, graph)) {
							toFlip.add (grandSire);
						}
						
						final Individual grandDam = sire.getDam ();
						if (isValidIndividual (grandDam) && hasSonsOnly (grandDam, graph)) {
							toFlip.add (grandDam);
						}
					}
				}
			}
			
			LOGGER.debug ("to flip: "+toFlip);
			
			for (Individual flippee : toFlip) {
				flip (graph, flippee, bottomListRank);	
			}
		}
	}
	
	protected void flip (final JGraph graph, final Individual flippee, final int bottomListRank) {
		final Integer flipRankObj = ranks.get(flippee);
		if (flipRankObj != null) {
			final int flippeeRank = flipRankObj.intValue();
			final int rankDiff = bottomListRank - flippeeRank;
			final ObjectPlacement objPlacement = graph.getObjectPlacementMapping().getPlacement (flippee);
			final Point2D.Double coords = objPlacement.getLocation();
			final int coord2 = 100 + ((bottomListRank + rankDiff) * layerSep);
			coords.setLocation (orientation ? coords.getX() : coord2, orientation ? coord2 : coords.getY());
		}
	}
	
	
	protected boolean isValidIndividual (final Individual ind) {
		return ind != null && !(ind instanceof FallBackIndividual);
	}
	
	
	protected boolean hasSonsOnly (final Individual ind, final JGraph graph) {
		boolean sonsOnly = true;
		for (Individual child : ind.getOffspring()) {
			if (graph.getModel().containsNode(child) && !child.getGender().equals("M")) {
				sonsOnly = false;
				break;
			}
		}
		
		return sonsOnly;
	}
	
	
	public final boolean isFlipEnabled() {
		return flipEnabled;
	}

	public final void setFlipEnabled (final boolean newFlipEnabled) {
		flipEnabled = newFlipEnabled;
	}
}
