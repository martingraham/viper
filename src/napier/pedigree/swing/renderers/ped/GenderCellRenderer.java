package napier.pedigree.swing.renderers.ped;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.LabelUI;

import org.resspecies.inheritance.model.SNPMarker;

import util.ui.VerticalLabelUI;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.base.AbstractCutoffErrorBaseRenderer;
import napier.pedigree.util.PedigreeIconCache;



public class GenderCellRenderer extends AbstractCutoffErrorBaseRenderer {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = -4558704400697626674L;

	
	protected static final LabelUI VERTICALCAPABLE = new VerticalLabelUI (false);
	private final static Border BORDER = BorderFactory.createMatteBorder (0, 0, 0, 1, Color.lightGray);
	
	protected JLabel genotypeLabel, nameLabel;
	protected Rectangle colourBlock;
	transient protected  Stroke inferStroke = new BasicStroke (1.0f);
	
	
	public GenderCellRenderer (final ErrorCollator errorModel, final BoundedRangeModel brm) {
    	super (errorModel, brm);
    	
		colourBlock = new Rectangle ();

    	nameLabel = new JLabel ();
    	genotypeLabel = new JLabel ();
    	
    	final JLabel[] labels = {nameLabel, genotypeLabel};
    	for (JLabel label : labels) {
        	label.setBorder (null);
        	label.setOpaque (false);
        	label.setHorizontalAlignment (SwingConstants.CENTER);
    	}

    	nameLabel.setUI (VERTICALCAPABLE);
	
    	this.setLayout (new BorderLayout (0, 0));
    	this.add (nameLabel, BorderLayout.CENTER);
    	this.add (genotypeLabel, BorderLayout.NORTH);
    	
    	//genotypeLabel.setVisible (false);
    	
    	this.setBorder (BORDER);
    }
	
	
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {

    	super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
    	
    	if (thisInd != null) {
       		final String label = thisInd.getName();
       		
       		nameLabel.setIcon (thisInd.isMotherMasked () && thisInd.isFatherMasked() ? PedigreeIconCache.makeIcon ("orphanIcon") : null);
       		
    		if (label != null) {
    			this.setValue ("");
    			this.setToolTipText (label);
    			nameLabel.setText (label);
    			genotypeLabel.setText ("***");
    		} 		
        	
        	final SNPMarker focusMarker = errorModel.getPopCheckerContext().getFocusMarker();	
    		if (focusMarker != null) {
    	    	genotypeLabel.setText (thisInd.getShortGenotypeString (focusMarker));
    	    	final String compass = thisInd.getGender().equals("M") ? BorderLayout.NORTH : BorderLayout.SOUTH;
    	    	if (((BorderLayout)getLayout()).getConstraints(genotypeLabel) != compass) {
    	    		((LayoutManager2)getLayout()).removeLayoutComponent (genotypeLabel);
    	    		((LayoutManager2)getLayout()).addLayoutComponent (genotypeLabel, compass);
    	    	}
    		} 
    		
    		final boolean visNeedsChanged = (genotypeLabel.isVisible() == (focusMarker == null));
    		if (visNeedsChanged) {
    			genotypeLabel.setVisible (focusMarker != null);	
    		}
    		
    	}  

    	this.validate();
		
    	return this;
    }
    
    
    @Override
    public Dimension getMinimumSize () {
    	final Dimension dim = this.getLayout().minimumLayoutSize(this);
    	//if (errorModel.getPopCheckerContext().getFocusMarker() == null) {
    	//	dim.height -= genotypeLabel.getMinimumSize().height;
    	//}
    	
    	int height = 6 + nameLabel.getFontMetrics(nameLabel.getFont()).getHeight();
    	if (errorModel.getPopCheckerContext().getFocusMarker() != null) {
    		height += genotypeLabel.getFontMetrics(nameLabel.getFont()).getHeight();
    	}
    	
    	dim.height = height;
    	//System.err.println ("nd: "+nameLabel.getMinimumSize());
    	//System.err.println ("gd: "+genotypeLabel.getMinimumSize());
    	//System.err.println ("dim: "+dim);
    	return dim;
    }
    
    
    @Override
    public void paintComponent (final Graphics gContext) {
    	this.validate();
    	this.paintBackground (gContext);

		final boolean achievedCutoff = (errors [ErrorCollator.ANY_ERROR] >= rangeModel.getValue());
		final Color[] errorSwatches = makeColourSwatches (achievedCutoff);
		makeAndDrawShapes (gContext, this.getWidth(), this.getHeight(), errorSwatches);

		//this.validate();
		setForeground (Color.black);
    }
    
    
    protected void makeAndDrawShapes (final Graphics gContext, final int width, final int height,
    		final Color[] colourSwatches) {
    	colourBlock.setBounds (0, 3, width, height - 6);	 	
    	final Graphics2D g2d = (Graphics2D)gContext;
    	drawShape (g2d, ErrorCollator.ANY_ERROR, true, colourBlock, colourSwatches [ErrorCollator.ANY_ERROR], null, null);
    	colourBlock.setSize (colourBlock.width - 2, colourBlock.height - 1);

    	drawMaskedness (g2d, colourBlock, Color.gray);
    	drawShape (g2d, ErrorCollator.INCOMPLETE, false, colourBlock, null, colourSwatches [ErrorCollator.INCOMPLETE], inferStroke);
    }
}