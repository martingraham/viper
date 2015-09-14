package napier.pedigree.swing.renderers.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.BoundedRangeModel;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

import model.graph.Edge;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.ped.IndividualCutoffMultiErrorRenderer;

import swingPlus.graph.GraphEdgeRenderer;
import swingPlus.graph.JGraph;
import util.GraphicsUtil;


public class GraphInheritanceErrorEdgeRenderer extends IndividualCutoffMultiErrorRenderer
				implements GraphEdgeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6392147847581347187L;
	
	static final private Stroke NEW_STROKE = new BasicStroke (2.0f);
	static final private Color LINE_COLOUR = Color.gray; //ColorUtilities.addAlpha (Color.gray, 128);

	public int fromX, fromY, toX, toY;
	public Individual parentNode;
	public Edge edge;
	
	public GraphInheritanceErrorEdgeRenderer (final ErrorCollator bgm, final BoundedRangeModel brm) {
    	super (bgm, brm);
    	
    	setBackground (GraphicsUtil.NULLCOLOUR); 
    }
	

	@Override
	public Component getGraphEdgeRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus, final int x1, final int y1, final int x2, final int y2) {
		
		fromX = x1;
		fromY = y1;
		toX = x2;
		toY = y2;
		
		edge = null;
		parentNode = null;
		
		if (value instanceof Edge) {
			edge = (Edge)value;
			final Object node2 = edge.getNode2();
			
			if (node2 instanceof HeritableIndividual) {
				thisInd = (HeritableIndividual)node2;
				parentNode = (Individual)edge.getNode1();
				calculateErrorValues ();
			}
		}
		
		return this;
	}

	
	@Override
    protected void makeAndDrawShapes (final Graphics gContext, final int width, final int height,
    		final Color[] colourSwatches) {
		
    	final Graphics2D g2d = (Graphics2D)gContext;
		translateToOrigin (g2d);
		g2d.setStroke (NEW_STROKE);
		g2d.setColor (LINE_COLOUR);

		if (parentNode != null) {
			final boolean sire = parentNode.getGender().equals("M");
			g2d.setColor (sire ? colourSwatches [ErrorCollator.BAD_SIRE] : colourSwatches [ErrorCollator.BAD_DAM]);
		}

		g2d.drawLine (fromX, fromY, toX, toY);
    }
    
	public void translateToOrigin (final Graphics graphics) {
		graphics.translate (-(this.getX()), -(this.getY()));
		//g.translate (-(this.getX() + insets.left), -(this.getY() + insets.top));
	}
}
