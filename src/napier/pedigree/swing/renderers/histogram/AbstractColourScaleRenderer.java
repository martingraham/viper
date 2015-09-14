package napier.pedigree.swing.renderers.histogram;


import java.awt.Color;

import napier.pedigree.swing.renderers.base.ErrorColourableRenderer;
import napier.pedigree.util.Spectrum;

import swingPlus.histogram.BarRenderer;
import util.Messages;
import util.colour.ColorUtilities;

/**
 * does what it says on the tin.
 * A) Has a colour scale similar to other list/pedigree renderers
 * b) Is linked directly to a IntMarkerFilter that decides whether to draw greyed out
 * or using the colour scale
 * @author cs22
 *
 */
public abstract class AbstractColourScaleRenderer implements BarRenderer, ErrorColourableRenderer {

	protected Spectrum activeColourScale;
	protected float[] colourScaleBoundaries = {1.0f, 0.5f, 0.1f, 0.0f};
	protected Color excludedColour;
	protected Color zeroColour = new Color (224, 224, 224);

	public AbstractColourScaleRenderer () {
		activeColourScale = new Spectrum ();
		excludedColour = Color.gray;
	}
	
	public void setErrorColourScale (final Color[] newScale) {
		activeColourScale.setColours (newScale);
	}
	
	public void setIncompleteColourScale (final Color[] newScale) {
		// Don't use incomplete colouring here
	}
	
	public Color[] makeColourScale (final Color baseColour, final int graduations) {
		final Color[] spectrum = new Color [graduations];
		for (int n = 0; n < graduations; n++) {
			spectrum [n] = ColorUtilities.mixColours (zeroColour, baseColour, 1.0f / (float)graduations * (float)(n));
		}
		return spectrum;
	}
	
    protected Color returnNiceColour (final float val, final Spectrum colourSpectrum, final float[] colourScaleCutoffs) {
    	final int indexLimit = Math.min (colourScaleBoundaries.length, colourSpectrum.getSize());
    	for (int index = 1; index < indexLimit; index++) {
    		if (val > colourScaleBoundaries [index]) {
    			return colourSpectrum.getColour (index - 1);
    		}
    	}
    	return zeroColour;
    }
	
	@Override
	public String toString () {
		return Messages.getString ("napier.pedigree.swing.renderers.rendererNames", getClass().getSimpleName());
	}
}
