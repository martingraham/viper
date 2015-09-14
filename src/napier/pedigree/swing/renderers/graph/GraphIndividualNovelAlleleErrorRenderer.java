package napier.pedigree.swing.renderers.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BoundedRangeModel;

import swingPlus.graph.GraphCellRenderer;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.ped.IndividualCutoffMultiErrorRenderer;



public class GraphIndividualNovelAlleleErrorRenderer extends IndividualCutoffMultiErrorRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;

	protected Ellipse2D circle;
	protected Rectangle2D square;
	protected Polygon diamond;
	protected Stroke shapeBorder;


	public GraphIndividualNovelAlleleErrorRenderer (final ErrorCollator bgm, final BoundedRangeModel brm) {
    	super (bgm, brm);
	    circle = new Ellipse2D.Double (0.0, 0.0, 10.0, 10.0);
	    square = new Rectangle2D.Double (0.0, 0.0, 10.0, 10.0);
	    diamond = new Polygon (new int[] {0, 5, 10, 5}, new int[] {5, 0, 5, 10}, 4);
	    shapeBorder = new BasicStroke (1.5f);
    }

	/**
	 * Copy Constructor
	 * @param rendererToCopy - equivalent renderer to copy fields from
	 */
	public GraphIndividualNovelAlleleErrorRenderer (final GraphCellRenderer rendererToCopy) {
    	this (((GraphIndividualNovelAlleleErrorRenderer)rendererToCopy).errorModel, ((GraphIndividualNovelAlleleErrorRenderer)rendererToCopy).rangeModel);
    }

	
    protected void makeAndDrawShapes (final Graphics gContext, final int width, final int height,
    		final Color[] colourSwatches) {
    	final int minDim = Math.min (width - 1, height - 1);
    	circle.setFrame (0, 0, minDim, minDim);
    	square.setFrame (0, 0, minDim, minDim);
    	//diamond.
    	final Graphics2D g2d = (Graphics2D)gContext;
    	//drawShape (g2d, isMale ? square : circle, colourSwatches [ErrorCollator.ANY_ERROR], Color.darkGray, shapeBorder);
    	drawShape (g2d, isMale ? square : circle, colourSwatches [ErrorCollator.ANY_ERROR], colourSwatches [ErrorCollator.INCOMPLETE], shapeBorder);
    	drawMaskedness (g2d, isMale ? square : circle, colourSwatches [ErrorCollator.INCOMPLETE]);
    }
}
