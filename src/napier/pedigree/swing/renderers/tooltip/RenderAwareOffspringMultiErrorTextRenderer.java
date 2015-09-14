package napier.pedigree.swing.renderers.tooltip;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.swing.renderers.base.AbstractCutoffErrorBaseRenderer;
import napier.pedigree.swing.renderers.base.MultipleItemsRenderer;
import napier.pedigree.util.HTMLLabel;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import swingPlus.graph.JGraph;
import util.Messages;
import util.collections.SingleItemSet;



public class RenderAwareOffspringMultiErrorTextRenderer extends AbstractCutoffErrorBaseRenderer {
	
   	/**
	 * 
	 */
	private static final long serialVersionUID = 3083925789431090043L;

	private static final Logger LOGGER = Logger.getLogger (RenderAwareOffspringMultiErrorTextRenderer.class);
	
	protected static final String SINGLE_COLUMN_HEADER = getPropertyString ("SingleColumnHeader");
	protected static final String MULTI_COLUMN_HEADER = getPropertyString ("MultiColumnHeader");
	
	final protected Set<Individual> singleItemSet;	// when we just want to show a tooltip for an individual

    //not final any longer
    /* If we want to change to display inference vale for single marker context
     * 
    //not final any longer
	String[] labels = {getPropertyString ("NilFromSire"), getPropertyString ("NovelAllele"), getPropertyString ("NilFromDam"), 
		bgm.getPopCheckerContext().getFocusMarker()==null ? getPropertyString ("IncompleteMarkers") :
                    getPropertyString ("InferredMarkers"), "All"};
     * 
     */
	protected String[] errorLabels = getPropertyString ("ErrorTextRendererLabels").split("\\|");
	protected final int[] errorOrdering = {2, 3, 1, 4, 5, 0}; // cos they is in the wrong order innit
	
	protected final MessageFormat multIndErrorTooltipFormat = new MessageFormat (getPropertyString ("MultIndErrorTooltipText"));
	protected final MessageFormat singleIndErrorTooltipFormat = new MessageFormat (getPropertyString ("SingleIndErrorTooltipText"));
	protected final MessageFormat singleIndLitterTextFormat = new MessageFormat (getPropertyString ("SingleIndLitterText"));
	protected final MessageFormat individualErrorFormat = new MessageFormat (getPropertyString ("IndividualErrorFormat"));
	
	
    public RenderAwareOffspringMultiErrorTextRenderer (final ErrorCollator bgm, final BoundedRangeModel brm) {
    	super (bgm, brm);
	    // Don't paint behind the component
	    setOpaque (false);
	    //setBackground (new Color (0, 0, 0, 64)); // garish colour just so we can be certain no background rendering is happening
	    this.setFont (Font.decode (Messages.getString ("napier.pedigree.graphics", "regularFont")));
	    this.setForeground (Color.black);
	    this.setHorizontalAlignment (SwingConstants.CENTER);
	    singleItemSet = new SingleItemSet<Individual> ();
	    errors = new int [ErrorCollator.ERROR_RANGE * 3];
    }
    

    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	
    	final TableCellRenderer cellRenderer = table.getDefaultRenderer (value.getClass());
    	LOGGER.debug ("Currently rendering cell with: "+cellRenderer);
    	
    	offspring = value instanceof Collection ? (Collection<Individual>)value : null; 
    	if (value instanceof Individual) {
    		singleItemSet.clear ();
    		singleItemSet.add ((Individual)value);
    		offspring = singleItemSet;
    	}
    	else if (offspring != null && offspring.size() > 1 && cellRenderer instanceof MultipleItemsRenderer) {
	    	final Collection<Individual> singleItemCollection = isolateSubCellIntoValue (table, row, column, offspring, (MultipleItemsRenderer)cellRenderer);
	    	if (singleItemCollection != null) {
	    		offspring = singleItemCollection;
	    	}
    	}
    	final Component comp = super.getTableCellRendererComponent (table, offspring, isSelected, hasFocus, row, column);
    	
    	this.setText (makeHTMLTable (offspring));
         //setBorder (isSelected ? SELECT_BORDER : null);
 		return comp;
    }
    
    
	@Override
	public Component getGraphCellRendererComponent (final JGraph graph, final Object value,
			final boolean isSelected, final boolean hasFocus) {
		if (value instanceof Individual) {
    		singleItemSet.clear ();
    		singleItemSet.add ((Individual)value);
    		offspring = singleItemSet;
    	}
		final Component comp = super.getGraphCellRendererComponent (graph, offspring, isSelected, hasFocus);
		this.setText (makeHTMLTable (offspring));
		return comp;
	}
    
	
	protected String makeHTMLTable (final Collection<Individual> offspring) {
    	calculateErrorValues ();

        /*
         * if we want to show different stats for summary versus single marekr context
         
        labels[3] = bgm.getPopCheckerContext().getFocusMarker()==null ? getPropertyString ("IncompleteMarkers") :
            getPropertyString ("InferredMarkers");
         
         */
        
    	final int activeMarkerCount = errorModel.getFilteredMarkerSize();
    	//final int activeMarkerCount = errorModel.getPopCheckerContext().getPopulation().getActiveMarkers().size();
    	//System.err.println ("bgm: "+markerCount+", bgm2: "+bgm.getErrorMap (CollatedErrorModel.ANY_ERROR).);
    	final int SNPCount = activeMarkerCount * offspring.size();
    	
    	final StringBuilder strBuffer = new StringBuilder ();
    	strBuffer.append (HTMLLabel.HTML_START);
    	final boolean singleInd = (offspring.size() == 1);
    	
    	if (activeMarkerCount > 0) {
    		if (singleInd) {
    			final Individual ind = offspring.iterator().next();
    			final SNPMarker focusMarker = errorModel.getPopCheckerContext().getFocusMarker();
    	    	final Object[] args = {Integer.valueOf (activeMarkerCount), ind.getGender(), ind.getName(), 
    	    			(focusMarker == null ? "" : ((HeritableIndividual)ind).getShortGenotypeString (focusMarker))};
    	    	strBuffer.append (singleIndErrorTooltipFormat.format (args));
    	    	if (ind.getLitter() != null && ind.getLitter().length() > 0) {
    	    		args[0] = ind.getLitter();
        	    	strBuffer.append (singleIndLitterTextFormat.format (args));
    	    	}
    		} else {
    	    	final Object[] args = {Integer.valueOf (activeMarkerCount), Integer.valueOf (offspring.size())};
    	    	strBuffer.append (multIndErrorTooltipFormat.format (args));
    		}

	
	    	strBuffer.append ("<TABLE>");
			strBuffer.append (offspring.size() > 1 ? MULTI_COLUMN_HEADER : SINGLE_COLUMN_HEADER);
	
	    	for (int count = 0; count < errorLabels.length - 1; count++) {
	    		final int rightIndex = errorOrdering [count];
	    		final String label = errorLabels [rightIndex];
	    		strBuffer.append ("<TR><TD>").append(label).append("</TD>");
	    		if (offspring.size() > 1) {
	    			addStringBuilderNumber (strBuffer, errors[rightIndex], activeMarkerCount);
	    		}
	    		addStringBuilderNumber (strBuffer, errors[rightIndex + errorLabels.length], activeMarkerCount);
	    		if (offspring.size() > 1) {
	    			final int total = errors[rightIndex + (errorLabels.length * 2)];
	    			addStringBuilderNumber (strBuffer, total, SNPCount);
	    		}
	    		strBuffer.append ("</TR>");
	    	}
	
	    	strBuffer.append ("</TABLE>");
    	}
    	
    	strBuffer.append (HTMLLabel.HTML_END);
    	
    	return strBuffer.toString();
	}
	
    protected void addStringBuilderNumber (final StringBuilder sBuffer, final int errorCount, 
    		final int allMarkerLocCount) {
		final int totalperCent = (errorCount * 100) / allMarkerLocCount;	 
		final Object[] args = {Integer.valueOf (errorCount), Integer.valueOf (totalperCent)};
		sBuffer.append (individualErrorFormat.format (args));	 
    }
    
    
    /**
     * If tooltip is over a table cell drawn with a renderer that displays a set of objects,
     * then make the tooltip render the individual object that the mouse pointer is currently over.
     * This needs the table to be drawn with a GenerationTableUI to work successfully.
     * @param table	- JTable
     * @param row	- row of cell
     * @param column	- column of cell
     * @param spaceFiller	- the set renderer of the table cell the tooltip is to provide info for
     */
    protected Collection<Individual> isolateSubCellIntoValue (final JTable table, final int row, final int column, 
    		final Collection<Individual> group, final MultipleItemsRenderer spaceFiller) {
		
		// Convert table mouse point to cell point
		final Point rendererMouseOffset = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen (rendererMouseOffset, table);
		final Rectangle cellBounds = table.getCellRect (row, column, false);
		rendererMouseOffset.translate (-cellBounds.x, -cellBounds.y);
		
		// Isolate the object in the set the mouse is currently over
		final int objectIndex = spaceFiller.getObjectIndexAt (rendererMouseOffset, cellBounds.width,
				table, row, column);

		// Push that object into a single item set instance and make that the focus of the tooltip
		if (objectIndex >= 0 && objectIndex < group.size()) {
			singleItemSet.clear ();
			final Iterator<Individual> iter = group.iterator();
			Object obj = null;
			int index = 0;
			while (iter.hasNext() && index <= objectIndex) {
				index++;
				obj = iter.next();
			}
    		singleItemSet.add ((Individual)obj);
    		return singleItemSet;
		}
		return null;
    }
    
    
    @Override
	public void paintComponent (final Graphics gContext) {
	    ((Graphics2D)gContext).setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
	             RenderingHints.VALUE_ANTIALIAS_ON); 
	    //this.paintBackground (gContext);
	    super.paintComponent (gContext);
    }
    
    
    
    @Override
	protected void calculateErrorValues () {
    	final int[] valuesAvg = new int [errors.length / 3];
    	final int[] valuesMax = new int [valuesAvg.length];
    	final int[] valuesTotal = new int [valuesAvg.length];
	

    	//note to martin from trevor
        //this was broken in the version of 26/08
        //i have reverted it to look like the previous version i had that was apparently ok 
	    for (Individual thisInd : offspring) {
	    	for (int type = 0; type < valuesAvg.length; type++) {
	    		final int thisTypeIndErrorCombo = errorModel.getFilteredIndividualErrorCount (type, thisInd);
		   	    updateValue (valuesAvg, type, thisTypeIndErrorCombo);
		   	    updateValue2 (valuesMax, type, thisTypeIndErrorCombo);
		   	    //valuesTotal [type] = valuesAvg [type];
	    	}
	    	
	    	System.arraycopy (valuesAvg, 0, valuesTotal, 0, valuesAvg.length);
		}
	    
	    finaliseValues (valuesAvg);
	    errors = concat (valuesAvg, concat (valuesMax, valuesTotal));
	}

    
	@Override
	protected void updateValue (final int[] values, final int index, final int newValue) {
		 values [index] += newValue;
	}


	protected void updateValue2 (final int[] values, final int index, final int newValue) {
		values [index] = Math.max (values [index], newValue);
	}


	@Override
	protected void finaliseValues (final int[] values) {
	    for (int type = 0; type < values.length; type++) {
	    	//if (values [type] > 20) {
	    	//	System.err.println ("value: "+values[type]+" over "+offspring.size()+" offspring.");
	    	//}
	    	values [type] = (int)Math.ceil ((double)values [type] / (double)offspring.size());
	    }
	}
	
	
	public int[] concat (final int[] first, final int[] second) { 
		final int[] result = Arrays.copyOf (first, first.length + second.length); 
		System.arraycopy (second, 0, result, first.length, second.length); 
		return result; 
	} 
}
