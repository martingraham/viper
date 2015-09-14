package napier.pedigree.swing.renderers.base;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Arrays;

import javax.swing.BoundedRangeModel;

import napier.pedigree.model.ErrorCollator;

public class HexagonalGlyphRenderer extends AbstractCutoffErrorBaseRenderer {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8447798997746809249L;
	
	protected Polygon hexBorder;
	protected Stroke hexStroke = new BasicStroke (1.0f);
	protected Polygon upTriangle, downTriangle;
	protected Rectangle centralRect;
	protected Stroke lineStroke;
	
	public HexagonalGlyphRenderer (final ErrorCollator bgm, final BoundedRangeModel rangeModel) {
		super (bgm, rangeModel);
		
	    upTriangle = new Polygon ();
	    downTriangle = new Polygon ();
	    centralRect = new Rectangle ();
	    hexBorder = new Polygon ();
	    lineStroke = null; //new BasicStroke (2.0f);
	}

	
	
    @Override
	public void paintComponent (final Graphics gContext) {
    	this.paintBackground (gContext);
    	
    	if (offspring == null || !offspring.isEmpty()) {
		    final int height = this.getHeight();
		    final int width = this.getWidth();
		    
		    //((Graphics2D)gContext).setRenderingHint (RenderingHints.KEY_ANTIALIASING,  
		    //         RenderingHints.VALUE_ANTIALIAS_ON); 
	     
		    final boolean exceedCutoff = (errors [ErrorCollator.ANY_ERROR] >= rangeModel.getValue());
	    	final Color[] colourSwatches = makeColourSwatches (exceedCutoff);
	    	
	    	makeAndDrawShapes (gContext, width, height, colourSwatches);
	    	
		    if (width > 25 && height > 13) {
		    	super.paintComponent (gContext);
		    }
    	}
    }
	
	
    protected void makeAndDrawShapes (final Graphics gContext, final int width, final int height,
    		final Color[] colourSwatches) {
    	// Up Triangle defined left-to-right
    	upTriangle.reset();
    	upTriangle.addPoint (0, height / 3);
    	upTriangle.addPoint (width / 2, -1);
    	upTriangle.addPoint (width, height / 3);

    	// Down Triangle defined right-to-left
    	downTriangle.reset();
    	downTriangle.addPoint (width, (height / 3) * 2);
    	downTriangle.addPoint (width / 2, height);
    	downTriangle.addPoint (0, (height / 3) * 2);
    	// This makes it easy to join the point arrays together so they make a hexagon for drawing a 
    	// border round the entire representation
    	
    	centralRect.setBounds (0, height / 3, width, height / 3);
    	
		//hexBorder.reset ();
		int[] xpoints = Arrays.copyOf (upTriangle.xpoints, 6);
		int[] ypoints = Arrays.copyOf (upTriangle.ypoints, 6);
		System.arraycopy (downTriangle.xpoints, 0, xpoints, 3, 3);
		System.arraycopy (downTriangle.ypoints, 0, ypoints, 3, 3);
		xpoints[2]--;
		xpoints[3]--;
		ypoints[4]--;
		//ypoints[1]++;
		
		ypoints[0]--;
		ypoints[2]--;
		hexBorder.xpoints = xpoints;
		hexBorder.ypoints = ypoints;
		hexBorder.npoints = xpoints.length;
	
    	
    	final Graphics2D g2d = (Graphics2D)gContext;
    	
    	drawShape (g2d, downTriangle, colourSwatches [ErrorCollator.BAD_DAM], Color.black, lineStroke);
    	drawShape (g2d, centralRect, colourSwatches [ErrorCollator.NOVEL_ALLELES], Color.black, lineStroke);
    	drawShape (g2d, upTriangle, colourSwatches [ErrorCollator.BAD_SIRE], Color.black, lineStroke);	
    	drawShape (g2d, ErrorCollator.MASKED_DAM, false, downTriangle, colourSwatches [ErrorCollator.MASKED_DAM], Color.black, lineStroke);
    	drawShape (g2d, ErrorCollator.MASKED_SIRE, false, upTriangle, colourSwatches [ErrorCollator.MASKED_SIRE], Color.black, lineStroke);

		drawMaskedness ((Graphics2D)gContext, hexBorder, colourSwatches [ErrorCollator.INCOMPLETE]);
		drawShape ((Graphics2D)gContext, ErrorCollator.INCOMPLETE, false, hexBorder, null, colourSwatches [ErrorCollator.INCOMPLETE], hexStroke);
    }
}
