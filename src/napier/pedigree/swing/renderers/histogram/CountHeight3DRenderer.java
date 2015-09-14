package napier.pedigree.swing.renderers.histogram;

import java.awt.Graphics;

import javax.swing.SwingConstants;

import napier.pedigree.swing.AbstractErrorHistogram;

import swingPlus.histogram.JHistogram;


public class CountHeight3DRenderer extends CountHeightRenderer {
	
	public CountHeight3DRenderer (final double baseProp) {
		super (baseProp);
	}
	
	@Override
	public void drawBar (final JHistogram histo, final Graphics graphics, final int x, final int y,
			final int width, final int height, final int dataValue, final double dataValueCount) {
    	graphics.setColor (getBarColour ((AbstractErrorHistogram)histo, dataValue));
    	if (histo.getOrientation() == SwingConstants.HORIZONTAL) {
    		final double baseHeight = (height + y) * baseProp;
    		final int joinY =  y + height - (int)baseHeight;
    		final int newY = (int)(y * (1.0 - baseProp));
    		graphics.fill3DRect (x, newY, width, joinY - newY, true);
    		//graphics.setColor (graphics.getColor().darker());
    		graphics.fillRect (x, joinY + 1, width, (int)baseHeight - 1);
    	} else {
    		final double baseWidth = (width + x) * baseProp;
    		final int joinX =  x + width - (int)baseWidth;
    		final int newX = (int)(x * (1.0 - baseProp));
    		graphics.fill3DRect (newX, y, joinX - newX, height, true);
    		//graphics.setColor (graphics.getColor().darker());
    		graphics.fillRect (joinX + 1, y, (int)baseWidth - 1, height);
    	}
	}
}
