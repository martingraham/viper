package napier.pedigree.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.PedigreeSelectionModel;
import napier.pedigree.swing.app.AppUtils;
import napier.pedigree.swing.force.DamFlippedDAGLayout;
import napier.pedigree.swing.force.SireFlippedDAGLayout;
import napier.pedigree.swing.renderers.base.AbstractErrorRenderer;
import napier.pedigree.swing.renderers.tooltip.RenderAwareOffspringMultiErrorTextRenderer;
import napier.pedigree.util.ModelScopeWindow;

import org.apache.log4j.Logger;
import org.resspecies.datasourceaware.FallBackIndividual;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

import swingPlus.graph.GraphEdgeRenderer;
import swingPlus.graph.GraphRendererToolTip;
import swingPlus.graph.JGraph;
import swingPlus.graph.force.impl.MedianSortHoriz;
import swingPlus.graph.force.impl.NullRepulsiveForceCalculator;
import swingPlus.graph.force.impl.OneOffPositionUpdater;
import swingPlus.graph.force.PositionUpdater;
import swingPlus.graph.force.RepulsiveForceCalculationInterface;
import swingPlus.shared.MyFrame;
import util.Messages;

import model.graph.GraphModel;
import model.graph.GraphModelEvent;
import model.graph.GraphModelListener;
import model.graph.impl.SymmetricGraphInstance;

public class GraphFrame extends MyFrame implements ModelScopeWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5419300419311995104L;
	private final static Logger LOGGER = Logger.getLogger (GraphFrame.class);
	private static AbstractErrorRenderer nodeRenderer, edgeRenderer;
	
	protected JGraph jgraph;
	protected JCheckBox sireFlipCheckBox;
	protected MessageFormat titleTemplate = new MessageFormat (Messages.getString (PropertyConstants.TEXTPROPS, "GraphFrameTitleText"));
	protected GraphModel gModel;
	protected GraphModelListener gListener;
	
	public GraphFrame () {
		this (null);
	}
	
	public GraphFrame (final PedigreeSelectionModel pedSelection) {
		super ();
		
		this.setName ("GraphView");
		this.setAlwaysOnTop (true);
		final DamFlippedDAGLayout pedSort = new DamFlippedDAGLayout ();
		pedSort.setMinObjSep (128);
		pedSort.setLayerSep (256);
		final RepulsiveForceCalculationInterface nullRepulse = new NullRepulsiveForceCalculator ();
		final PositionUpdater pedigreeUpdater = new FitNodesToView ();
		
		jgraph = new JGraph (null, nullRepulse, pedSort, pedigreeUpdater);
		setModel (pedSelection, null);
		jgraph.setShowEdges (true);
		jgraph.setBackground (Color.white);
		//jgraph.getUI().setScale (1.0f, new Point(0, 0));
	
		sireFlipCheckBox = new JCheckBox ();
		sireFlipCheckBox.setSelected (pedSort.isFlipEnabled());
		sireFlipCheckBox.setText (Messages.getString (PropertyConstants.TEXTPROPS, "SireFlipCheckBoxText"));
		
		final Dimension frameSize = new Dimension (480, 320);
		final String propertyFrameSize = Messages.getString (PropertyConstants.TEXTPROPS, "GraphFrameSize");
		if (propertyFrameSize.charAt(0) != Messages.ERROR_CHAR) {
			AppUtils.getInstance().populateDimension2 (propertyFrameSize, frameSize);
		}
		this.setSize (frameSize);
	
		
		this.getContentPane().add (jgraph, BorderLayout.CENTER);
		this.getContentPane().add (sireFlipCheckBox, BorderLayout.SOUTH);
		
		
		sireFlipCheckBox.addChangeListener (
			new ChangeListener () {
				@Override
				public void stateChanged (final ChangeEvent cEvent) {
					if (cEvent.getSource() == sireFlipCheckBox) {
						pedSort.setFlipEnabled (sireFlipCheckBox.isSelected());
						pedSort.reset();
						//jgraph.getModel().addGraphModelListener (this);
						jgraph.restartWorker();
					}
				}	
			}
		);
		
		this.addWindowListener (
			new WindowAdapter () {
				 public void windowClosing (final WindowEvent wEvent) {
					 if (gModel != null && gListener != null) {
						 gModel.removeGraphModelListener (gListener);
						 LOGGER.debug ("yo im closing "+wEvent);
					 }
				 }
			}
		);
		
		this.addWindowStateListener(
				new WindowAdapter () {
					@Override
					public void windowStateChanged (final WindowEvent wEvent) {
						LOGGER.debug ("Window state event gets through");
					}
				}
		);
		
		setVisible (true);
	}
	
	static public void setRenderers (final AbstractErrorRenderer[] graphRenderers) {
		nodeRenderer = graphRenderers [0];
		edgeRenderer = graphRenderers [1];
		edgeRenderer.setBorder (BorderFactory.createEmptyBorder (4, 4, 4, 4));
		nodeRenderer.setPreferredSize (new Dimension (64, 64));
	}
	
	
	public void setModel (final PedigreeSelectionModel pedSelection, final BoundedRangeModel colourCutOff) {
		if (pedSelection != null) {
			gModel = pedSelection.getSelectedGraph();
			final ErrorCollator errorModel = pedSelection.getErrorModel();
			
			jgraph.setModel (setup (pedSelection.getSeeds()));
			//jgraph.setModel (gModel);
			final BoundedRangeModel brm = colourCutOff == null ? new DefaultBoundedRangeModel() : colourCutOff;
			
			jgraph.setDefaultNodeRenderer (HeritableIndividual.class, nodeRenderer);
			jgraph.setDefaultEdgeRenderer (Boolean.class, (GraphEdgeRenderer)edgeRenderer);
			
			
			final RenderAwareOffspringMultiErrorTextRenderer tooltipRenderer = new RenderAwareOffspringMultiErrorTextRenderer (errorModel, brm);
			((GraphRendererToolTip)jgraph.createToolTip()).addRenderer (HeritableIndividual.class, tooltipRenderer);
	
			//System.err.println ("Node renderer: "+nodeRenderer.isOpaque()+", bcol: "+nodeRenderer.getBackground());
			
			this.setTitle (titleTemplate.format (new Object[] {pedSelection.getSeeds().toString()}));
			
			//jgraph.getModel().addGraphModelListener(
			gListener = new GraphModelListener () {
				@Override
				public void graphChanged (final GraphModelEvent graphModelEvent) {
					if (graphModelEvent.getType() == GraphModelEvent.UPDATE) {
						//jgraph.getModel().removeGraphModelListener (this);
						jgraph.getModel().clear();
						jgraph.getFilteredModel().clear();
						jgraph.getModel().addEdges (setup (pedSelection.getSeeds()).getEdges());
						jgraph.updateFilteredModel();
						GraphFrame.this.setTitle (titleTemplate.format (new Object[] {pedSelection.getSeeds().toString()}));
						((MedianSortHoriz)jgraph.getAttractiveForceCalculator()).reset();
						//jgraph.getModel().addGraphModelListener (this);
						jgraph.restartWorker();
					}
				}
			};
			gModel.addGraphModelListener (gListener);
			
			jgraph.restartWorker();
		}
	}
	
	
	
	protected GraphModel setup (final Collection<Individual> inds) {
		final GraphModel graph = new SymmetricGraphInstance ();
		for (Individual ind : inds) {
			addIndividual (ind, graph);
		}
		return graph;
	}
	
	protected void addIndividual (final Individual ind, final GraphModel graph) {
		
		graph.addNode (ind);
		final boolean sire = ind.getGender().equals("M");
		
		final Collection<? extends Individual> offspring = ind.getOffspring();
		if (offspring != null) {
			for (Individual child : offspring) {
				if (graph.addNode (child)) {
					graph.addEdge (ind, child, Boolean.TRUE);
					
					final Individual otherParent = sire ? child.getDam() : child.getSire();
					graph.addNode (otherParent);
					graph.addEdge (otherParent, child, Boolean.TRUE);
					addParents (otherParent, graph);
				}
			}
		}
		
		addParents (ind, graph);
	}
	
	protected void addParents (final Individual ind, final GraphModel graph) {
		if (ind.getSire() != null && !(ind.getSire() instanceof FallBackIndividual)) {
			graph.addNode (ind.getSire());
			graph.addEdge (ind.getSire(), ind, Boolean.TRUE);
		}
		if (ind.getDam() != null && !(ind.getDam() instanceof FallBackIndividual)) {
			graph.addNode (ind.getDam());
			graph.addEdge (ind.getDam(), ind, Boolean.TRUE);
		}
	}
	
	
	
	/**
	 * Updates position of dag after layout to ensure all nodes are visible in window
	 * @author cs22
	 *
	 */
	static class FitNodesToView extends OneOffPositionUpdater {
		@Override
		public void updatePositions (final JGraph graph) {
	    	graph.fitTo (graph.getModel().getNodes());
		}
	}
}
