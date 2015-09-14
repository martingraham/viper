package napier.pedigree.swing.renderers.ped;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import model.shared.MultiComparator;
import napier.pedigree.swing.renderers.base.AbstractOffspringSetRenderer;
import napier.pedigree.swing.renderers.base.MultipleItemsRenderer;
import napier.pedigree.swing.ui.GenerationTableUI;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

import swingPlus.shared.CellRendererPane2;
import util.Messages;




public class OffspringCollectionRenderer extends AbstractOffspringSetRenderer
										implements MultipleItemsRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3798174105063665889L;
	static final private Logger LOGGER = Logger.getLogger (OffspringCollectionRenderer.class);
	
	protected CellRendererPane2 cellPane = new CellRendererPane2 ();
	protected JTable table;
	protected boolean isSelected, hasFocus;
	protected int row, column;
	protected Rectangle rect = new Rectangle ();
	protected int globalEdgeLength; // fixed sub-cell edge length imposed if > 0
   	
	protected Color offColor;
	protected MultiComparator<Individual> offspringSort;
   	
   	// use if you want a different renderer for an object that is part of a set than when it is
   	// directly the object in a table cell.
	protected Map<Class<?>, TableCellRenderer> rendererClassCache; 
   	
	
	public OffspringCollectionRenderer (final MultiComparator<Individual> offspringSort) {
		setLayout (null);
		this.add (cellPane);
		this.offspringSort = offspringSort;
		rendererClassCache = new HashMap<Class<?>, TableCellRenderer> ();
	}
	
    @Override
	public Component getTableCellRendererComponent (final JTable table, final Object value,
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    	super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);

    	this.table = table;
    	this.row = row;
    	this.column = column;
    	this.isSelected = isSelected;
    	this.hasFocus = hasFocus;
    	int selectedCount = 0;
    	
    	/*
    	if (table instanceof JGeneration) {
    		final JGeneration generation = (JGeneration)table;
    		final Set<Object> selectedSet = generation.getSelectedSet();
    		
    		if (generation.getModel() instanceof AbstractMatrixTableModel) {
    			final AbstractMatrixTableModel matModel = (AbstractMatrixTableModel)generation.getModel();
    			final Object male = matModel.getRowObject (generation.convertRowIndexToModel (row));
    			final Object female = matModel.getColumnObject (generation.convertColumnIndexToModel (column));
    			
    			if (selectedSet.contains (male)) {
    				selectedCount++;
    			}
    			
    			if (selectedSet.contains (female)) {
    				selectedCount++;
    			}
    			
    			if (selectedCount > 0) {
    				this.isSelected = true;
    			}
    		}
    	}
    	*/
    	
    	this.hasFocus = hasFocus;
    	//setBorder (isSelected ? SELECT_SET_BORDER : MULTIEDGE_BORDER);
    	setBorder (null);
    	return this;
    }
    
    @Override
	public void paintComponent (final Graphics gContext) {
    	final int width = this.getWidth ();
    	final int height = this.getHeight ();
    	
    	final Graphics2D g2d = (Graphics2D)gContext;
		//final boolean offscreen = GraphicsUtil.isNonScreenImage (g2d);
		//g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, 
		//		/*offscreen ? RenderingHints.VALUE_ANTIALIAS_ON :*/ RenderingHints.VALUE_ANTIALIAS_ON);

    	if (offspring != null) {
	    	final int collectionSize = offspring.size();
	
	    	if (collectionSize > 0) {			
	    		final int pixWidth = (globalEdgeLength > 0 ? globalEdgeLength : 
						calcItemEdgeLength (width, height, collectionSize));
				
				if (pixWidth >= 2) {
		
					if (collectionSize > 1 && offspring instanceof List) {
						final List<Individual> offList = (List<Individual>)offspring;
						Collections.sort (offList, offspringSort);
					}
		    		
		    		double startxoffset = (width - (collectionSize * pixWidth)) / 2.0;
		    		if (startxoffset < 0.0) {
		    			startxoffset = (width % pixWidth) / 2.0;
		    		}
		    		double x = startxoffset, y = 0.0;
		    		
		    		final Iterator<Individual> offspringIterator = offspring.iterator();
		    		
		    		while (offspringIterator.hasNext ()) {	
		    			final Individual ind = offspringIterator.next ();
		    			final TableCellRenderer tcr = getDefaultRenderer (ind.getClass(), table);
		    			final Component subRenderer = tcr.getTableCellRendererComponent (table, ind, isSelected, hasFocus, row, column);
		    			cellPane.paintComponent (gContext, subRenderer, this, (int)x, (int)y, (int)pixWidth, (int)pixWidth);
		    			
		    			x += pixWidth;
		    			
		     			if (x > getWidth() - pixWidth) {
		     				x = startxoffset;
		     				y += pixWidth;
		     			} 	
		    		}
		    		cellPane.setBounds (-pixWidth, -pixWidth, 0, 0);
		    	} 
		    	else {
		    		int femaleCount = 0;
		    		final Iterator<Individual> offspringIterator = offspring.iterator();
		    		
		    		while (offspringIterator.hasNext ()) {	
		    			final Individual ind = offspringIterator.next ();
		    			if ("F".equals(ind.getGender())) {
		    				femaleCount++;
		    			}
		    		}
		    		final int femWidth = width * femaleCount / offspring.size();
		    		if (femaleCount > 0) {
		    			gContext.setColor (Color.pink);
		    			gContext.fillRect (0, 0, femWidth, height);
		    		}
		    		if (femaleCount < offspring.size()) {
		    			gContext.setColor (Color.cyan);
		    			gContext.fillRect (femWidth, 0, width - femWidth, height);
		    		}
		    		gContext.setColor (Color.darkGray);
		    		gContext.drawString (String.valueOf (offspring.size()), 1, height - 2);
		    	}
	    	}
    	}
    }
    
    public void addRenderer (final Class<?> klass, final TableCellRenderer renderer) {
    	rendererClassCache.put (klass, renderer);
    }
    
    

    
    
    protected TableCellRenderer getDefaultRenderer (final Class<?> klass, final JTable table) {
    	TableCellRenderer renderer = rendererClassCache.get (klass);
    	if (renderer == null) {
    		renderer = table.getDefaultRenderer (klass);
    	}
    	return renderer;
    }
    
    
    /**
     * SpaceFillingSetRenderer interface
     */
    @Override
    public int calcItemEdgeLength (final int width, final int height, final int collectionSize) {
	   	int pixWidth = Math.min (Math.min (height, width), (int)Math.sqrt ((width * height) / collectionSize));
	   	if (LOGGER.isDebugEnabled()) {
	   		LOGGER.debug ("initial\tdim: ("+width+", "+height+"), cr: ("+(width/pixWidth)+", "+(height/pixWidth)+"), coll: "+collectionSize+", sizeLen: "+pixWidth);
	   	}
	   	
		while (pixWidth > 0 && (width/pixWidth) * (height/pixWidth) < collectionSize) {
			final int cols = width / pixWidth;
			final int rows = height / pixWidth;
			final int npixwidth1 = width / (cols + 1);
			final int npixwidth2 = height / (rows + 1);
			
			pixWidth = Math.max (npixwidth1, npixwidth2);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug ("col rows adjusted down\tdim: ("+width+", "+height+"), cr: ("+(width/pixWidth)+", "+(height/pixWidth)+"), coll: "+collectionSize+", sizeLen: "+pixWidth);
			}
		}

    	return pixWidth;
    }
    
    @Override
    public void setGlobalItemEdgeLength (final int length) {
    	globalEdgeLength = length;
    }
    
    @Override
    public int getGlobalItemEdgeLength () { return globalEdgeLength; }
    
    @Override
    public int getObjectIndexAt (final Point mousePos, final int cellWidth,
    		final JTable table, final int vrow, final int vcolumn) {
    	
    	final Object value = table.getValueAt (vrow, vcolumn);
    	final int subCellSize = getSubCellSize (vrow, table);
    	final int count = (value instanceof Collection) ? ((Collection)value).size() : 1;
 	
		double startxoffset = (cellWidth - (count * subCellSize)) / 2.0;
		if (startxoffset < 0.0) {
			startxoffset = (cellWidth % subCellSize) / 2.0;
		}
    	
    	final int column = (int)Math.floor ((double)(mousePos.x - (int)startxoffset) / (double)subCellSize);
    	final int row = mousePos.y / subCellSize;
    	final int rowMult = cellWidth / subCellSize;
    	final int objIndex = (row * rowMult) + column;
    	return objIndex;
    }
    

	protected int getSubCellSize (final int vrow, final JTable table) {
		
		int subCellSize = 10;
		
		if (table.getUI() instanceof GenerationTableUI) {
			subCellSize = ((GenerationTableUI)table.getUI()).getCachedRowSubCellSize (vrow);
		} else {
			subCellSize = getGlobalItemEdgeLength();
		}
		
		return subCellSize;
	}
    
    @Override
	public String toString () {
    	Object obj = rendererClassCache.get (HeritableIndividual.class);
    	if (obj == null) {
    		obj = this;
    	}
		return Messages.getString ("napier.pedigree.swing.renderers.rendererNames", obj.getClass().getSimpleName());
	}
}
