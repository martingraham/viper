package napier.pedigree.swing.renderers.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import swingPlus.graph.DefaultGraphEdgeRenderer;

public class SimpleLineRenderer extends DefaultGraphEdgeRenderer {

	/**
	 * 
	 */
	static private final long serialVersionUID = -4658788303212160988L;
	static private final Stroke NEW_STROKE = new BasicStroke (3.0f);
	static protected final Color LINECOLOUR = new Color (192, 192, 192, 128);
	@Override
	public void paintComponent (final Graphics graphics) {
		if (obj != null) {
			translateToOrigin (graphics);
			((Graphics2D)graphics).setStroke (NEW_STROKE);
			graphics.setColor (LINECOLOUR);
			//final int diffx = toX - fromX;
			//final int diffy = toY - fromY;
			graphics.drawLine (fromX, fromY, toX, toY);
		}
	}
}
