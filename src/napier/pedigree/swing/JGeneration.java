package napier.pedigree.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultRowSorter;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.graph.GraphModelEvent;
import model.graph.GraphModelListener;
import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.PedigreeSelectable;
import napier.pedigree.model.PedigreeSelectionModel;
import napier.pedigree.model.impl.FireSuppressedListSelectionModel;
import napier.pedigree.model.impl.SplitLinkNodeObject;
import napier.pedigree.model.impl.PedigreeGenerationModelRowSorter;
import napier.pedigree.model.sort.TableMultiComparator;
import napier.pedigree.swing.renderers.ped.SplitLinkNodeRenderer;

import org.apache.log4j.Logger;

import swingPlus.shared.AbstractColumnsSorter;
import swingPlus.shared.ColumnSortableTable;
import swingPlus.shared.JBasicMatrix;
import swingPlus.shared.border.OrthogonalTitlesBorder;
import swingPlus.shared.tooltip.AbstractRendererToolTip;



public class JGeneration extends JBasicMatrix implements ColumnSortableTable, GraphModelListener, PedigreeSelectable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2444978298282376523L;
	private static final Logger LOGGER = Logger.getLogger (JGeneration.class);
	private static final String UI_CLASS_ID = "GenerationTableUI";
	static {
		UIManager.put (UI_CLASS_ID, "napier.pedigree.swing.ui.GenerationTableUI");
	}
	private final static Color ROW_HEADER_BACK = new Color (224, 224, 255);
	private final static TableCellRenderer SPLIT_LINK_RENDERER = new SplitLinkNodeRenderer();
	private final static int ROW_HEADER_WIDTH = 24;
	private final static int[] ROW_HEIGHTS = {48, 20, 96, 20, 48};
	
	public static final String HTML_DESCRIPTOR = "HTML_Descriptor";
	
	private final Border lineBorder = BorderFactory.createLineBorder (Color.black, 3);
	//private final Border titleBorder = BorderFactory.createTitledBorder (lineBorder, "ToolTip");
	private final Border titleBorder = new OrthogonalTitlesBorder (lineBorder, "ToolTip");
	
	protected AbstractColumnsSorter colSort;
	protected TableMultiComparator colSort2;
	protected PedigreeSelectionModel pedigreeSelection;

	protected boolean toggleSelection, extendSelection;
	
	
	protected boolean rowSizeInitialised = false;
	protected int[] rowHeightsWhenPopulated;	// Model indexed row size cache
	protected BitSet unpopulatedRows;			// Model indexed row contents monitor
	
	
	
	

	public JGeneration() {
        this (null, null, null);
    }

    public JGeneration (final TableModel tableModel) {
        this (tableModel, null, null);
    }

    public JGeneration (final TableModel tableModel, final TableColumnModel columnModel) {
        this (tableModel, columnModel, null);
    }

    public JGeneration (final TableModel tableModel, final TableColumnModel columnModel,
    		final ListSelectionModel selectionModel) {
    	super (tableModel, columnModel, selectionModel);
    	
		axesLabel.setVisible (false);
		setRowColumnHeaderDefaults ();
		
		final AbstractRendererToolTip niceToolTip = new PedigreeRendererToolTip (this);
    	setRendererToolTip (niceToolTip);
    	niceToolTip.setBorder (titleBorder);
		
    	//this.getRowHeader().setVisible(false);
    	//this.getColumnHeader().setVisible(false);
    	setRowHeight (64);
    	initialiseRowSizes ();
    	
    	this.setShowVerticalLines (false);
    	
		//if (ui instanceof AnimatedTableUI) {
			//((AnimatedTableUI)ui).setColumnAnimationEnabled (false);
			//((AnimatedTableUI)ui).setRowAnimationEnabled (false);
		//}
		
		setColumnSelectionAllowed (true);
		setRowSelectionAllowed (true);
		this.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		this.setBorder (null);
		
		this.addMouseListener (new ToolTipSustainer ());
    }
    
    @Override
    public String getUIClassID() {
        return UI_CLASS_ID;
    }

    
    
    protected void initializeLocalVars () {
    	super.initializeLocalVars ();
    	
    	setAutoResizeMode (JTable.AUTO_RESIZE_LAST_COLUMN);
    	
    	this.setGridColor (new Color (208, 208, 208));
    	this.setCellSelectionEnabled (true);
    		
    	setColumnsSorter (new GenerationColumnsSorter (this));
		//setRowSorter (new TableRowSorter<TableModel> (getModel()));	
		setDefaultRenderer (SplitLinkNodeObject.class, SPLIT_LINK_RENDERER);
    }

    
    
    public void setModel (final TableModel model) {
    	
    	unpopulatedRows = new BitSet ();
		
    	super.setModel (model);
    	
    	if (model instanceof PedigreeGenerationModel) {
    		setRowSorter (new PedigreeGenerationModelRowSorter<PedigreeGenerationModel> ((PedigreeGenerationModel)getModel()));
    	}
    	
		// Stick old row sizes back in if available
		restoreRowSizes ();
    }
    
    
    
    protected void initialiseRowSizes () {
    	if (!rowSizeInitialised && getRowCount() > 4) {
    		rowSizeInitialised = true;
    		for (int n = 0; n < Math.min (ROW_HEIGHTS.length, getRowCount()); n++) {
    			setRowHeight (n, ROW_HEIGHTS[n]);
    		}
    	}
    }
    
    
    
    /**
     * Fill individual row sizes with the cache of sizes in rowHeightsWhenPopulated
     */
    protected void restoreRowSizes () {   	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug ("row sizes Bf restore: "+Arrays.toString (rowHeightsWhenPopulated));
    	}
    	
		for (int viewRow = 0; viewRow < getRowCount(); viewRow++) {
			final int modelRow = this.convertRowIndexToModel (viewRow);
			final int rowSize = (rowHeightsWhenPopulated != null && rowHeightsWhenPopulated.length > modelRow && 
					rowHeightsWhenPopulated [modelRow] > 0 ? rowHeightsWhenPopulated [modelRow] : this.getRowHeight());
			
			if (rowSize > 0) {
				setRowHeight (viewRow, rowSize);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug ("view index: "+viewRow+", row size: "+rowSize);
				}
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("row sizes Af restore: "+Arrays.toString (rowHeightsWhenPopulated));
		}
    }
    
    
    
    protected void calculateUnpopulatedRows () {
		unpopulatedRows.clear();
		if (getModel() instanceof PedigreeGenerationModel) {
			final PedigreeGenerationModel pgm = (PedigreeGenerationModel)getModel();
			for (int modelRowIndex = ModelRowConstants.OFFSPRING_SPLIT_START; modelRowIndex < pgm.getRowCount(); modelRowIndex++) {
				final boolean emptyRow = (!pgm.areSplitOffspringPresent (modelRowIndex - ModelRowConstants.OFFSPRING_SPLIT_START));
				unpopulatedRows.set (modelRowIndex, emptyRow);
			}
		}
    }
    
    
    /**
     * recalculates which rows have nothing in and then calls restore row sizes and forces a relayout
     */
    public void redoRowHeights () {
		
    	calculateUnpopulatedRows ();	
		restoreRowSizes ();
		JGenerationUtils.getInstance().revalidateSuperScrollPane (this);
		
		if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug ("MRow size: "+getModel().getRowCount()+", vrow size: "+this.getRowCount());
    		LOGGER.debug ("upop: "+unpopulatedRows);
		}
    }
    
    
    
    
    protected void setRowColumnHeaderDefaults () {
		//final TableCellRenderer axesGenderRenderer = new GenderHeaderRenderer ();
		
		final JGenerationRowHeader jRowHeader = new JGenerationRowHeader (); //(JRowHeader)getRowHeader();
		this.setRowHeader (jRowHeader);
		jRowHeader.setModel (jRowHeader.new PedigreeRowHeaderModel());
		jRowHeader.setRowSelectionAllowed (false);
		jRowHeader.setBackground (ROW_HEADER_BACK);
		jRowHeader.getColumnModel().getColumn(0).setWidth (ROW_HEADER_WIDTH);

		//jRowHeader.setShowGrid (false);
		//final AnimatedTableUI atui3 = new AnimatedTableUI ();
		//jRowHeader.setUI (atui3);
		//atui3.setColumnAnimationEnabled (false);
		//atui3.setRowAnimationEnabled (false);
    }
    
    
    

	/*
    @Override
	public void createDefaultColumnsFromModel() {
    	super.createDefaultColumnsFromModel();
    	// Hide the first column, which is the row list
		//this.getColumnModel().removeColumn (this.getColumnModel().getColumn(0));
		//attachComparatorsToColumns ();
    }
  	*/
    
    public void attachComparatorsToColumns (final Comparator indColComp) {
		if (this.getRowSorter() instanceof TableRowSorter) {
			final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)this.getRowSorter();
			for (int n = this.getColumnCount(); --n > 0;) {
				trs.setComparator (n, indColComp);
			}
			//trs.setComparator (0, ((MatrixTableModel)getModel()).getRowMetrics());
		}
    }
    
    
    /*
     * Eviscerated tableChanged method for TableModelEvents. Used to do stuff for deleting/inserting rows
     * but scrapped all that after it kept chucking errors (some of which were in the swing jtable code and not mine!)
     * (non-Javadoc)
     * @see javax.swing.JTable#tableChanged(javax.swing.event.TableModelEvent)
     */
    @Override
    public void tableChanged (final TableModelEvent tme) {
    	//System.err.println ("TME: y1: "+tme.getFirstRow()+"\ty2: "+tme.getLastRow()+"\tx: "+tme.getColumn()+"\ttype: "+tme.getType());
    	if (tme.getType() == TableModelEvent.UPDATE || tme.getType() == TableModelEvent.INSERT || tme.getType() == TableModelEvent.DELETE) {
    		if (getModel() instanceof PedigreeGenerationModel) {
	    		//final int[] rowSizeCache = cacheRowSizes ();
	    		//if (this.getUI() != null) { 
	    		//	((AnimatedTableUI)this.getUI()).setColumnAnimationEnabled(false);
	    		//}
	    		//this.clearSelection();
	    		super.tableChanged (tme);
	    		//if (getRowHeader() != null) {
	    		//	getRowHeader().tableChanged (tme);
	    		//}
	    		
	    		redoRowHeights ();
	    		//if (this.getUI() != null) { 
	    		//	((AnimatedTableUI)this.getUI()).setColumnAnimationEnabled(true);
	    		//}
    		}
    		
    		JGenerationUtils.getInstance().revalidateSuperScrollPane (this);
    	} else {
    		//System.err.println ("Table: "+this.getHeaderLabel().getText());
    		super.tableChanged (tme);
    		if (getRowHeader() != null) {
    			getRowHeader().tableChanged (tme);
    		}
    	}
    }
    
    
    /**
     * Returns the default selection model object, which is
     * a <code>DefaultListSelectionModel</code>.  A subclass can override this
     * method to return a different selection model object.
     *
     * @return the default selection model object
     * @see javax.swing.DefaultListSelectionModel
     */
    @Override
    protected ListSelectionModel createDefaultSelectionModel() {
    	//return super.createDefaultSelectionModel ();
        return new FireSuppressedListSelectionModel();
    }
    
    /**
     * Returns the default column model object, which is
     * a <code>DefaultTableColumnModel</code>.  A subclass can override this
     * method to return a different column model object.
     *
     * @return the default column model object
     * @see javax.swing.table.DefaultTableColumnModel
     */
    @Override
    protected TableColumnModel createDefaultColumnModel() {
        final TableColumnModel tcm = super.createDefaultColumnModel();
        tcm.setSelectionModel (new FireSuppressedListSelectionModel());
        return tcm;
    }
    
    
    
    @Override
    public void setRowHeight (final int row, final int height) {
    	
    	final int modelRow = this.convertRowIndexToModel (row);
    	final int possEmptyRowHeight = modelRow >= ModelRowConstants.OFFSPRING_SPLIT_START && unpopulatedRows.get (modelRow) ? 10 : height;
    	super.setRowHeight (row, possEmptyRowHeight);
    	if (getRowHeader() != null) {
    		getRowHeader().setRowHeight (row, possEmptyRowHeight);
    	}
     	
		if (rowHeightsWhenPopulated == null) {
			rowHeightsWhenPopulated = new int [modelRow + 1];	
		}
		
		if (rowHeightsWhenPopulated.length <= modelRow) {
			rowHeightsWhenPopulated = Arrays.copyOf (rowHeightsWhenPopulated, modelRow * 2);
		}
    	
    	//if (!unpopulatedRows.get (modelRow)) {
    	//	rowHeightsWhenPopulated [modelRow] = height;
    	//} else if (rowHeightsWhenPopulated [modelRow] == 0) {
    		rowHeightsWhenPopulated [modelRow] = height;
    	//}
    	
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.info ("Row: "+row+", mrow: "+modelRow+", height: "+height);
    	}
    }
    
    
    public void cloneRowHeightsTo (final JGeneration otherGen) {
		for (int row = 0; row < getRowCount(); row++) {
			otherGen.setRowHeight (row, getRowHeight (row));
		}
		
		otherGen.unpopulatedRows = (BitSet)unpopulatedRows.clone();
		otherGen.rowHeightsWhenPopulated = Arrays.copyOf (
				rowHeightsWhenPopulated, rowHeightsWhenPopulated.length);
    }
    

    
    
    public void setColumnHeaderView (final JComponent jcomp) {
    	final Container parent = getParent();
    	if (parent instanceof JViewport) {
    		final Container grandParent = parent.getParent();
    		if (grandParent instanceof JScrollPane) {
    			final JScrollPane scrollPane = (JScrollPane)grandParent;
    			scrollPane.setColumnHeaderView (jcomp);
    		}
    	}
    }
    
    public void configureEnclosingScrollPane () {
    	super.configureEnclosingScrollPane();
    	setColumnHeaderView (null);
    }
    /*
    public void repaint (long tm, int x, int y, int width, int height) {
    	System.err.println ("repaint tm: "+tm+", x: "+x+", y: "+y+", w: "+width+", h: "+height);
    	Thread.dumpStack();
    	super.repaint (tm, x, y, width, height);
    }
    */
    
    
    /**
     * Pedigree Selection methods
     */
    
    public PedigreeSelectionModel getPedigreeSelection() {
		return pedigreeSelection;
	}

	public void setPedigreeSelection (final PedigreeSelectionModel newModel) {
		if (pedigreeSelection != newModel) {
			if (pedigreeSelection != null && pedigreeSelection.getSelectedGraph() != null) {
				pedigreeSelection.getSelectedGraph().removeGraphModelListener (this);
			}
			
			pedigreeSelection = newModel;
			
			if (pedigreeSelection != null && pedigreeSelection.getSelectedGraph() != null) {
				pedigreeSelection.getSelectedGraph().addGraphModelListener (this);
			}
		}
	}
	

	@Override
	public void graphChanged (final GraphModelEvent gmEvent) {
		if (gmEvent.getType() == GraphModelEvent.UPDATE || gmEvent.getType() == GraphModelEvent.CLEAR) {
			repaint ();
		}
	}

	
	@Override
	public void changeSelection (final int rowIndex, final int columnIndex, final boolean toggle, final boolean extend) {
		setToggleSelection (toggle);
		setExtendSelection  (extend);
		super.changeSelection (rowIndex, columnIndex, toggle, extend);
	}
		   
	public boolean isToggleSelection() {
		return toggleSelection;
	}

	public void setToggleSelection (final boolean toggleSelection) {
		this.toggleSelection = toggleSelection;
	}

	public boolean isExtendSelection() {
		return extendSelection;
	}

	public void setExtendSelection (final boolean extendSelection) {
		this.extendSelection = extendSelection;
	}

	
	/**
	 * Renderer methods
	 */
	
	
	@Override
    public TableCellRenderer getCellRenderer (final int row, final int column) {
    	final Object obj = getValueAt (row, column);
    	return getCellRenderer (obj, row, column);
    	//return getCellRenderer (Collections.EMPTY_LIST, row, column);
    }
    
    
    public TableCellRenderer getCellRenderer (final Object value, final int row, final int column) {
    	TableCellRenderer tcr = null;

    	//System.err.println ("viewRows: "+this.getRowCount());
    	if (value != null) {
    		//if (! ((Collection<?>)value).isEmpty()) {
    			tcr = getDefaultRenderer (value.getClass());
    		//}
    	}
    	
    	//System.err.println ("obj: "+value+", "+(value == null ? "null" : value.getClass())+", tcr: "+tcr);
    	
    	if (tcr == null) {
    		tcr = super.getCellRenderer (row, column);
    	}

    	return tcr;
    }
    
    
    @Override
    public Component prepareRenderer (final TableCellRenderer renderer, final int row, final int column) {
    	final Object value = getValueAt (row, column);
        return prepareRenderer (renderer, value, row, column);
    }
    
   
    
    public Component prepareRenderer (final TableCellRenderer renderer, final Object value,
    		final int row, final int column) {

        boolean isSelected = false;
        boolean hasFocus = false;

        // Only indicate the selection and focused cell if not printing
        if (!isPaintingForPrint()) {
            isSelected = isCellSelected(row, column);
            final boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
            final boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
            hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
        }

        return renderer.getTableCellRendererComponent (this, value,
	                                              isSelected, hasFocus,
	                                              row, column);
    }
    
    /**
     * Method that combines getCellRenderer and prepareRenderer so that
     * only one call to getValueAt (row, column) is needed. This is beneficial
     * in JMatrix as the MatrixTableModel is a graph and getting edges between 
     * two nodes is often not a single operation.
     * @param row       the row of the cell to render, where 0 is the first row
     * @param column    the column of the cell to render, where 0 is the first column
     * @return          the <code>Component</code> under the event location
     */
    public Component getAndPrepareCellRenderer (final int row, final int column) {
    	final Object value = getValueAt (row, column);
    	final TableCellRenderer tcr = getCellRenderer (value, row, column);
    	return prepareRenderer (tcr, value, row, column);
    }
    
    
    
    
    
    /**
     * Changes the filter used in the RowSorters for both
     * the JGeneration and associated RowHeader
     * @param activeRowFilter - filter to use
     */
    public void filter (final RowFilter activeRowFilter) {
		final SortKey skey = new SortKey (0, SortOrder.DESCENDING);
    	final List<SortKey> keyList = Arrays.asList (skey);
		    		
		final RowSorter<?> rowSorter = getRowSorter ();
		if (rowSorter instanceof DefaultRowSorter) {
			final DefaultRowSorter<?, ?> defRowSorter = (DefaultRowSorter<?, ?>)rowSorter;
			defRowSorter.setRowFilter (activeRowFilter);
		}
		
		final RowSorter<?> headerRowSorter = getRowHeader().getRowSorter ();
		if (headerRowSorter instanceof DefaultRowSorter) {
			final DefaultRowSorter<?, ?> defHeaderRowSorter = (DefaultRowSorter<?, ?>)headerRowSorter;
			defHeaderRowSorter.setRowFilter (activeRowFilter);
		}

		if (rowSorter != null) {
			rowSorter.setSortKeys (keyList);
		}
    }
    
    
    
    /**
     * Refilters using the current filter.
     * Used when the filter might not have changed but the conditions
     * used to calculate inclusion/exclusion may have.
     */
    public void refilter () {
    	RowFilter activeRowFilter = null;
    	
    	final RowSorter<?> rowSorter = getRowSorter ();	
		if (rowSorter instanceof DefaultRowSorter) {
			final DefaultRowSorter<?, ?> drs = (DefaultRowSorter<?, ?>)rowSorter;
			activeRowFilter = drs.getRowFilter();
		}
		
		filter (activeRowFilter);
    }

    
    
    
    /**
     * Sorter methods
     */

	@Override
	public AbstractColumnsSorter getColumnsSorter() {
		return colSort;
	}
	
	
	public void setColumnsSorter (final AbstractColumnsSorter aColSort) {
		colSort = aColSort;
	}
	
    
	
    public void sortColumns (final TableMultiComparator rowMultiplexMultiComparator) {
    	colSort2 = rowMultiplexMultiComparator;
    	resortColumns ();
    }
    
    
    public void resortColumns () {
    	colSort2.setTableModel (this.getModel());
    	getColumnsSorter().sort (1, colSort2);
    	//this.repaint();
    }
    
    
	
	@Override
	public void paint (final Graphics graphics) {
		final Graphics2D g2d = (Graphics2D)graphics;
		g2d.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint (graphics);
	}
    
    
    
	static class ToolTipSustainer extends MouseAdapter {
	    /**
	     * {@inheritDoc}
	     */
	    public void mouseEntered (final MouseEvent mEvent) {
	    	ToolTipManager.sharedInstance().setDismissDelay (10000);
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public void mouseExited (final MouseEvent mEvent) {
	    	ToolTipManager.sharedInstance().setDismissDelay (4000);
	    }
	}
}
