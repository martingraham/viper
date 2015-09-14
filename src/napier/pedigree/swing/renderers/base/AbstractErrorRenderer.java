package napier.pedigree.swing.renderers.base;


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.resspecies.model.Individual;

import swingPlus.graph.GraphCellRenderer;
import swingPlus.graph.JGraph;
import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;

import model.graph.GraphModel;
import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.JGeneration;
import napier.pedigree.util.Spectrum;




public abstract class AbstractErrorRenderer extends DefaultTableCellRenderer2
						implements GraphCellRenderer, TableCellRenderer, ErrorColourableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7309358973700865595L;
	static protected Color selectedColour = new Color (255, 255, 0, 128);
	
	protected static final float MAGIC_NUMBER = 0.58f;
	
	protected ErrorCollator errorModel;
	protected int[] errors;
	
	protected Color noneColour = new Color (240, 240, 240, 255);
	protected Color zeroColour = new Color (224, 224, 224);
	
	protected Color defaultErrorColour = Color.red;
	protected Spectrum activeErrorColourScale;
	protected float[] errorColourScaleBands = {1.0f, 0.5f, 0.1f, 0.0f};

	protected Color defaultIncompleteColour = Color.blue;
	protected Spectrum activeIncompleteColourScale;
	protected float[] incompleteColourScaleBands = {1.0f, 0.99f, 0.5f, 0.1f, 0.0f};
	protected TexturePaint[] maskedTextures;
	
	protected List<Spectrum> colourScalesByType = new ArrayList<Spectrum> ();
	protected List<float[]> colourScaleBandsByType = new ArrayList<float[]> ();

	
	public AbstractErrorRenderer (final ErrorCollator errorModel) {
    	super ();
	    this.errorModel = errorModel;
	    errors = new int [ErrorCollator.ERROR_RANGE + 2]; // +2 errors for masked sire and dam conditions

	    activeErrorColourScale = new Spectrum (makeColourScale (defaultErrorColour, errorColourScaleBands.length));
	    activeIncompleteColourScale = new Spectrum (makeColourScale (defaultIncompleteColour, incompleteColourScaleBands.length));

	    colourScalesByType.addAll (Arrays.asList (activeErrorColourScale, activeErrorColourScale, activeErrorColourScale, activeErrorColourScale,
	    		activeIncompleteColourScale, activeIncompleteColourScale, activeIncompleteColourScale, activeIncompleteColourScale));
	    colourScaleBandsByType.addAll (Arrays.asList (errorColourScaleBands, errorColourScaleBands, errorColourScaleBands,
	    		errorColourScaleBands, incompleteColourScaleBands, incompleteColourScaleBands, incompleteColourScaleBands, incompleteColourScaleBands));
    }
	
	
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	setBackground (null);
    	
    	if (table instanceof JGeneration) {
    		final JGeneration generation = (JGeneration)table;
    		if (value instanceof Individual) {
    			isIndividualSelected (generation.getPedigreeSelection().getSelectedGraph(), value);
    		}
    		else if (value instanceof Collection) {
    			boolean setSelected = false;
    			final Iterator<?> iter = ((Collection<?>)value).iterator();
    			while (iter.hasNext() && !setSelected) {
    	 			setSelected = isIndividualSelected (generation.getPedigreeSelection().getSelectedGraph(), iter.next());
    			}
    		}
    	}
    	return super.getTableCellRendererComponent (table, value, false, false, row, column);
    }
    
    
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		//obj = value;
		return this;
	}
	
	
	public boolean isIndividualSelected (final GraphModel graph, final Object value) {
		final boolean indSelected = (value instanceof Individual) && graph.containsNode (value);
		setBackground (indSelected ? selectedColour : null);
		return indSelected;
	}
	
    public void paintBackground (final Graphics gContext) {
    	if (getBackground() != null && !getBackground().equals (this.getParent().getBackground())) {
    		gContext.setColor (getBackground());
    		gContext.fillRect (0, 0, this.getWidth(), this.getHeight());
    	}
    }
    
    
    protected Color[] makeColourSwatches (final List<Spectrum> currentErrorColourScales) {
    	final float markerCount = (errorModel == null) ? 1.0f : (float)errorModel.getInitialMarkerSize() * MAGIC_NUMBER;	
    	final Color[] swashes = new Color [errors.length];
    	
    	for (int swashIndex = 0; swashIndex < errors.length; swashIndex++) {
    		final Spectrum colourScale = currentErrorColourScales.get (swashIndex);
    		final float[] csBands = colourScaleBandsByType.get (swashIndex);
           	swashes [swashIndex] = returnNiceColour (Math.min (1.0f, (float)errors[swashIndex] / markerCount), 
           			colourScale, csBands);
    	}
    	return swashes;
    }
	
	
    protected Color returnNiceColour (final float val, final Spectrum colourSpectrum, final float[] colourScaleBands) {
    	final int indexLimit = Math.min (colourScaleBands.length, colourSpectrum.getSize());
    	final int index = getColourBandIndex (val, colourScaleBands, indexLimit);
    	return index < 0 ? noneColour : colourSpectrum.getColour (index);
    }
    
    
    protected int getColourBandIndex (final float val, final float[] csBands, final int maxIndex) {
    	for (int index = 1; index < maxIndex; index++) {
    		if (val > csBands [index]) {
    			return index - 1;
    		}
    	}
    	return -1;
    }
    
    
	public void setIncompleteColourScale (final Color[] newScale) {
		activeIncompleteColourScale.setColours (newScale);
		maskedTextures = MaskTextures.getInstance().makeTextures (newScale);
	}
	
    
	public void setErrorColourScale (final Color[] newScale) {
		activeErrorColourScale.setColours (newScale);
	}
	
	public Color[] makeColourScale (final Color baseColour, final int numberOfBands) {
		final Color[] spectrum = new Color [numberOfBands];
		final Color mixColour = (baseColour.getAlpha() == 0 ? GraphicsUtil.NULLCOLOUR : zeroColour);
		for (int n = 0; n < numberOfBands; n++) {
			spectrum [n] = ColorUtilities.mixColoursAndAlpha (mixColour, baseColour, 1.0f / (float)numberOfBands * (float)(n));
		}
		return spectrum;
	}
	
    
	
	
	protected void drawShape (final Graphics2D g2d, final int errorType, final boolean drawZeroValues,
			final Shape shape, final Color fillColor, 
			final Color edgeColor, final Stroke edgeStroke) {
		if (drawZeroValues || errors [errorType] > 0) {
			drawShape (g2d, shape, fillColor, edgeColor, edgeStroke);
		}
	}
	
    
	protected void drawShape (final Graphics2D g2d, final Shape shape, final Color fillColour, 
			final Color edgeColour, final Stroke edgeStroke) {
		if (fillColour != null) {
			g2d.setColor (fillColour);
			g2d.fill (shape);
		}
		
		if (edgeStroke != null) {
			final Stroke oldStroke = g2d.getStroke ();
			g2d.setStroke (edgeStroke);
			g2d.setColor (edgeColour);
			g2d.draw (shape);
			g2d.setStroke (oldStroke);
		}
	}
	
	
	/**
	 * Texture fill a shape with a texture. The texture is obtained from a colour value which we use to get an index 
	 * to an array of textures (currently the textures are a stippledrange of alpha'ed values of one colour)
	 * @param gContext - Graphics to draw to
	 * @param shape - Shape to fill with the texture
	 * @param incompleteColour - 
	 */
	protected void drawMaskedness (final Graphics2D gContext, final Shape shape, final Color incompleteColour) {
		if (errors [ErrorCollator.MASKED_GENO] > 0) {
			final Paint oldPaint = gContext.getPaint();
			final float maskedRate = errors [ErrorCollator.MASKED_GENO] / (float)errorModel.getInitialMarkerSize() * MAGIC_NUMBER;
			final int bandIndex = getColourBandIndex (maskedRate, incompleteColourScaleBands,
					Math.min (incompleteColourScaleBands.length, maskedTextures.length));
		    gContext.setPaint (maskedTextures [bandIndex]);
		    gContext.fill (shape);
		    gContext.setPaint (oldPaint);
		}
	}
	
	
	
	abstract protected void calculateErrorValues ();
	
	abstract protected void updateValue (final int[] values, final int index, final int newValue);
	
	abstract protected void finaliseValues (final int[] values);
	
	
	
	@Override
	public String toString () {
		return Messages.getString ("napier.pedigree.swing.renderers.rendererNames", getClass().getSimpleName());
	}
	
	static public String getPropertyString (final String key) {
		return Messages.getString (PropertyConstants.TEXTPROPS, key);
	}
	
	static public void setSelectedColour (final Color newSelectedColour) {
		selectedColour = newSelectedColour;
	}
	
	static public Color getSelectedColour () {
		return selectedColour;
	}
}
