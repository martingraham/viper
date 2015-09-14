package napier.pedigree.swing.renderers.base;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.JTable;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

import swingPlus.graph.JGraph;
import util.GraphicsUtil;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.util.Spectrum;

public abstract class AbstractCutoffErrorBaseRenderer extends AbstractErrorRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 676804142744335143L;
	
	
	protected BoundedRangeModel rangeModel;
	
	protected HeritableIndividual thisInd;
	protected Collection<Individual> offspring;
	protected String label;
	protected boolean isMale;
	//protected double inferredPercent;
	
	protected int[] colourSats = {0, 100, 150, 200, 255};

	protected Spectrum greyColourScale;
	protected List<Spectrum> cutoffErrorColourScales = new ArrayList<Spectrum> ();
	

	public AbstractCutoffErrorBaseRenderer (final ErrorCollator errorModel, final BoundedRangeModel rangeModel) {
		super (errorModel);
	    this.rangeModel = rangeModel;
	    
	    greyColourScale = new Spectrum (makeColourScale (Color.gray, errorColourScaleBands.length));
	    cutoffErrorColourScales.addAll (Arrays.asList (greyColourScale, greyColourScale, greyColourScale, greyColourScale,
	    		activeIncompleteColourScale, activeIncompleteColourScale, activeIncompleteColourScale, activeIncompleteColourScale));
	}
	
	public Component getTableCellRendererComponent (final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final Component comp = super.getTableCellRendererComponent (table,
				value, isSelected, hasFocus, row, column);
		
		setBorder (null);
		
		if (value instanceof HeritableIndividual) {
			thisInd = (HeritableIndividual)value;
			label = thisInd.getName();
			isMale = thisInd.getGender().equals("M");
			calculateErrorValues ();
		}
		else if (value instanceof Collection) {
	    	offspring = (Collection<Individual>)value; 
	    	this.setText (offspring.size()+"");
	    	calculateErrorValues ();
		}

		return comp;
	}
	
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		final Component comp = super.getGraphCellRendererComponent (graph,
				value, isSelected, hasFocus);
		
		this.setBorder (null);
		
		if (value instanceof HeritableIndividual) {
			thisInd = (HeritableIndividual) value;
			label = thisInd.getName();
			isMale = thisInd.getGender().equals("M");
			calculateErrorValues ();
			this.setText (label);
		}
		
		setBackground (GraphicsUtil.NULLCOLOUR);
		getBackground();
		return comp;
	}
	
	
	protected void calculateErrorValues () {
        for (int type = 0; type < errors.length; type++) {
	   	    errors [type] = errorModel.getFilteredIndividualErrorCount (type, thisInd);
    	}
	}
	
	
	protected void updateValue (final int[] values, final int index, final int newValue) {
		// EMPTY
	}
	
	protected void finaliseValues (final int[] values) {
		// EMPTY
	}
	
	
    protected Color[] makeColourSwatches (final boolean exceedCutoff) {
    	return makeColourSwatches (exceedCutoff ? colourScalesByType : cutoffErrorColourScales);
    }
}
