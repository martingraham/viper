package napier.pedigree.swing.renderers.histogram;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.SwingConstants;

import napier.pedigree.swing.AbstractErrorHistogram;

import swingPlus.histogram.JHistogram;


public class CountHeightRenderer extends AbstractColourScaleRenderer {

	protected double baseProp; // Proportion of height used to indicate presence of data rather than amount of data
	
	public CountHeightRenderer (final double baseProp) {
		super ();
		this.baseProp = Math.max (0.0, Math.min (1.0, baseProp));
	}
	
	@Override
	public void drawBar (final JHistogram histo, final Graphics graphics, final int x, final int y,
			final int width, final int height, final int dataValue, final double dataValueCount) {
    	graphics.setColor (getBarColour ((AbstractErrorHistogram)histo, dataValue));
    	if (histo.getOrientation() == SwingConstants.HORIZONTAL) {
    		final double baseHeight = (height + y) * baseProp;
    		final int joinY =  y + height - (int)baseHeight;
    		final int newY = (int)(y * (1.0 - baseProp));
    		graphics.fillRect (x, newY, width, joinY - newY);
    		//graphics.setColor (graphics.getColor().darker());
    		graphics.fillRect (x, joinY + 1, width, (int)baseHeight - 1);
    	} else {
    		final double baseWidth = (width + x) * baseProp;
    		final int joinX =  x + width - (int)baseWidth;
    		final int newX = (int)(x * (1.0 - baseProp));
    		graphics.fillRect (newX, y, joinX - newX, height);
    		//graphics.setColor (graphics.getColor().darker());
    		graphics.fillRect (joinX + 1, y, (int)baseWidth - 1, height);
    	}
	}
 
    protected Color getBarColour (final AbstractErrorHistogram histogram, final int dataValue) {
    	final Color shade = returnNiceColour ((float)dataValue / (float)histogram.getMaximum(), activeColourScale, colourScaleBoundaries);
    	//final Color shade = activeColourScale.getColour(0);
    	return (histogram.getHistogramValueFilter() != null && 
    			histogram.getHistogramValueFilter().include (dataValue)) 
    			? shade : excludedColour;
    }
}
