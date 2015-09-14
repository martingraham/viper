package napier.pedigree.swing;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.categoriser.Categoriser;
import napier.pedigree.swing.renderers.ped.IconTextPairRenderer;
import napier.pedigree.util.PedigreeIconCache;

import swingPlus.shared.JRowHeader;
import util.Messages;


public class JGenerationRowHeader extends JRowHeader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5701579022603654485L;
	private static final Logger LOGGER = Logger.getLogger (JGenerationRowHeader.class);
	
	protected final static Properties ROWNAMES = Messages.makeProperties ("rowNames", JGenerationRowHeader.class, true);
	protected static final Map<Integer, Icon> ICONROWMAP = new HashMap<Integer, Icon> ();
	protected static final Map<Integer, String> DESCRIPTORROWMAP = new HashMap<Integer, String> ();
	static {
		ICONROWMAP.put (ModelRowConstants.DAM, PedigreeIconCache.makeIcon ("femaleIcon"));
		ICONROWMAP.put (ModelRowConstants.SIRE, PedigreeIconCache.makeIcon ("maleIcon"));
		//ICONROWMAP.put (ModelRowConstants.OFFSPRING, PedigreeIconCache.makeIcon ("offspringIcon"));
		ICONROWMAP.put (ModelRowConstants.SIRE_SPLITS, PedigreeIconCache.makeIcon ("splitLinkDownIcon"));
		ICONROWMAP.put (ModelRowConstants.DAM_SPLITS, PedigreeIconCache.makeIcon ("splitLinkUpIcon"));
			
		DESCRIPTORROWMAP.put (ModelRowConstants.DAM, ROWNAMES.get("DamRow").toString());
		DESCRIPTORROWMAP.put (ModelRowConstants.SIRE, ROWNAMES.get("SireRow").toString());
		DESCRIPTORROWMAP.put (ModelRowConstants.OFFSPRING, ROWNAMES.get("OffspringRow").toString());
		DESCRIPTORROWMAP.put (ModelRowConstants.SIRE_SPLITS, ROWNAMES.get("SireSplitRow").toString());
		DESCRIPTORROWMAP.put (ModelRowConstants.DAM_SPLITS, ROWNAMES.get("DamSplitRow").toString());
	}
	
	
	public JGenerationRowHeader () {
		super ();
		
		final MouseAdapter rowHeightListener = new RowHeightAdjusterListener ();
		this.addMouseListener (rowHeightListener);
		this.addMouseMotionListener (rowHeightListener);
		this.setRendererToolTip (new GenRowHeaderToolTip (this));
		this.setDefaultRenderer (JLabel.class, new IconTextPairRenderer ());
	}
	
	@Override
    public void setModel (final TableModel tableModel) {
		// Stops the rowheader's table model getting replaced with the associated jtable's model
		// as called by the property change listener
		//if (! (tableModel instanceof PedigreeGenerationModel)) {
		//	super.setModel (tableModel);
		//}
		super.setModel (new PedigreeRowHeaderModel ());
	}
	
	
	/**
	 * Gets the Categoriser object currently being used and held by the associated JGeneration's TableModel
	 * @return Categoriser categoriser
	 */
	protected Categoriser<?> getAssocTableCategoriser () {
		
		Categoriser<?> categoriser = null;   
		final JTable assocTable = getTable();
		
		if (assocTable != null) {
			final TableModel assocTableModel = assocTable.getModel();
					
			if (assocTableModel instanceof PedigreeGenerationModel) {
				final PedigreeGenerationModel pedGenModel = (PedigreeGenerationModel)assocTableModel;
				categoriser = pedGenModel.getCategoriser();
			}
		}
		
		return categoriser;	   
	}
	
	
	//public void tableChanged (TableModelEvent e) {
		//LOGGER.info ("tablechanged rowheader: "+e);
		//super.tableChanged(e);
	//}

	
	
   class RowHeightAdjusterListener extends MouseAdapter {
    	/**
    	 * Mouse listener to listen to mouse events for stretching a row height
    	 * in the same way a columns width can be changed
    	 */
    	
	   private int lastY = -1, startDragY = -1, oldRowHeight = -1, row = -1;
	   
		@Override
		public void mouseMoved (final MouseEvent mEvent) {
			final Point mCoord = mEvent.getPoint();
			if (mCoord.y != lastY) {
				lastY = mCoord.y;
				
				mCoord.y -= 2;
				final int row1 = getRowBoundary (mCoord);
				
				mCoord.y += 4;
				final int row2 = getRowBoundary (mCoord);
				
				JGenerationRowHeader.this.setCursor (Cursor.getPredefinedCursor (row1 == row2 
						? Cursor.DEFAULT_CURSOR : Cursor.S_RESIZE_CURSOR));
			}
		}
		
		
		public void mousePressed (final MouseEvent mEvent) {
			final Point mCoord = mEvent.getPoint();
			
			mCoord.y -= 2;
			final int row1 = getRowBoundary (mCoord);
			
			mCoord.y += 4;
			final int row2 = getRowBoundary (mCoord);
			
			startDragY = (row1 == row2) ? 1 : mCoord.y - 2;
			row = row1;
			oldRowHeight = JGenerationRowHeader.this.getRowHeight (row);
		}
		
		
		public void mouseDragged (final MouseEvent mEvent) {
			if (startDragY != -1) {
				final Point mCoord = mEvent.getPoint();
				final int newRowHeight = Math.max (6, oldRowHeight + (mCoord.y - startDragY));
				JGenerationRowHeader.this.getTable().setRowHeight (row, newRowHeight);
			}
		}
		
		public void mouseReleased (final MouseEvent mEvent) {
			if (startDragY != -1) {
				final JTable table = JGenerationRowHeader.this.getTable();
				JGenerationUtils.getInstance().revalidateSuperScrollPane (table);
			}
		}
		
		protected int getRowBoundary (final Point point) {
			return JGenerationRowHeader.this.rowAtPoint (point);
		}
    } 
   
   
   
   /**
    * Flimsy TableModel that returns a shared JLabel instance as the getValue results.
    * This is done as we want to return an Icon, or a String, or both. Just remember
    * it is shared so one getValue call wipes out a previous call, but this is ok for
    * rendering which is all we use it for.
    * Interrogates the RowHeader's associated JTable's TableModel
    * for its row count (fiddly if this model is placed into a
    * standalone class)
    * @author cs22
    *
    */
   class PedigreeRowHeaderModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6904389457958980714L;

		protected final Icon OFFSPRING_SPLIT_ICON = PedigreeIconCache.makeIcon ("offspringSplitIcon");
		protected final JLabel currentIconTextStore; // Object passed out by the table model

		
		public PedigreeRowHeaderModel () {
			super ();	
			currentIconTextStore = new JLabel ();
		}
		
		@Override
		public int getRowCount() {
			return getTable().getModel().getRowCount();	// Males, females, offspring and current split offspring rows
		}

		@Override
		public int getColumnCount() {
			return 1;	// It's a row header
		}

	    public Class<?> getColumnClass (final int columnIndex) {
	    	return columnIndex == 0 ? JLabel.class : Object.class;
	    }
		
		@Override
		public Object getValueAt (final int rowIndex, final int columnIndex) {
			if (columnIndex != 0) {
				return null;
			}
			
			// If the row has an associated icon use it, if not find a description
			final Icon icon = ICONROWMAP.get (Integer.valueOf (rowIndex));
			currentIconTextStore.setIcon (icon);
			currentIconTextStore.setText (icon == null ? getRowDescriptor (rowIndex, false) : "");
			//currentIconTextStore.setIcon (icon == null ? OFFSPRING_SPLIT_ICON : icon);
			
			return currentIconTextStore;
		}
		
		
		
		/**
		 * Generates strings based on the row index the tooltip is currently over.
	    * For non-fixed rows (i.e. split offspring rows) it must interrogate
	    * the row header's associated JGeneration and drill through to find
	    * the Categoriser object and descriptions currently being used on the
	    * generation's offspring.	
		 * @param modelRow - row in table model we want description for
		 * @param isLong - adds extra "offspring" text if set
		 * @return String with appropriate description
		 */
	   protected String getRowDescriptor (final int modelRow, final boolean isLong) {
		   String description = DESCRIPTORROWMAP.get (Integer.valueOf (modelRow));
		   
		   if (description == null) {
			   final Categoriser<?> categoriser = getAssocTableCategoriser();
			   if (categoriser != null) {
				   description = categoriser.getDescription (modelRow - ModelRowConstants.OFFSPRING_SPLIT_START)
				   		+ (isLong ? " " + DESCRIPTORROWMAP.get (ModelRowConstants.OFFSPRING) : "");
			   }
		   }
		   return description;
	   }
	}
   
   
   
   /**
    * ToolTip generator for this JGenerationRowHeader.
    * @author cs22
    *
    */
   class GenRowHeaderToolTip extends DefaultRendererToolTip {
			   
	   	/**
	   	 * 
	   	 */
	   	private static final long serialVersionUID = 1194575632404811636L;
	   	
	   	
		public GenRowHeaderToolTip (final JComponent jComponent) {
			super (jComponent);
		}

		@Override
		public void setToolTipObject (final Object obj, final int row, final int column) {
		    final String tipText = ((PedigreeRowHeaderModel)JGenerationRowHeader.this.getModel()).getRowDescriptor (
		    		JGenerationRowHeader.this.convertRowIndexToModel (row), true); 
		    label.setText (tipText);	
		    
		    if (label.getText() != null) {
				final Rectangle2D bounds = label.getFont().getStringBounds (label.getText(), frc);
				final Insets insets = label.getInsets ();
				labelSize.setSize (
					(int)bounds.getWidth() + 4 + insets.left + insets.right + 1,
					(int)bounds.getHeight() + insets.top + insets.bottom
				);
		
				this.setPreferredSize (labelSize);
		    }
		}
   }
   
}
