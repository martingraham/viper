package napier.pedigree.swing.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.TableCellRenderer;

import org.resspecies.model.Individual;

import napier.pedigree.swing.renderers.base.MultipleItemsRenderer;

import ui.MultiCellSpanTableUI;


public class GenerationTableUI extends MultiCellSpanTableUI {

	
	protected Map<Integer, Integer> rowSubCellSizeCache;
	
	public static ComponentUI createUI (final JComponent comp) {
		return new GenerationTableUI();
	}
	
	
	/**
	 * Precalculate minimum size of subcells in all cells in a row and pass
	 * that minimum value to the necessary renderer
	 */
	@Override
	protected void preRowCalculation (final int vrow) {
		
		int subCellEdgeLength = Integer.MAX_VALUE;
		if (rowSubCellSizeCache == null) {
			rowSubCellSizeCache = new HashMap<Integer, Integer> ();
		}
		
		for (int col = 0; col < table.getColumnCount(); col++) {
			final TableCellRenderer tcr = table.getCellRenderer (vrow, col);
			
			if (tcr instanceof MultipleItemsRenderer) {
				int cellWidth = table.getColumnModel().getColumn(col).getWidth();
				int cellHeight = table.getRowHeight (vrow);
	            final int colMargin = Math.min (table.getColumnModel().getColumnMargin(), cellWidth);
				final int rowMargin = Math.min (table.getRowMargin(), cellHeight);
	            cellWidth -= colMargin;
	            cellHeight -= rowMargin;
	            
	            final Object value = table.getValueAt (vrow, col);
	            final Collection<?> offspring = value instanceof Collection ? (Collection<Individual>)value : null;
	            if (offspring != null && !offspring.isEmpty()) {
	            	final MultipleItemsRenderer spaceFillRenderer = (MultipleItemsRenderer)tcr;
	            	subCellEdgeLength = Math.min (subCellEdgeLength, spaceFillRenderer.calcItemEdgeLength (cellWidth, cellHeight, offspring.size()));
	            	spaceFillRenderer.setGlobalItemEdgeLength (subCellEdgeLength);
	            	//System.err.println ("pixwidth: "+subCellEdgeLength);
	            }
			}
		}
		
		rowSubCellSizeCache.put (Integer.valueOf (vrow), Integer.valueOf (subCellEdgeLength));
	}
	
	
	public int getCachedRowSubCellSize (final int vrow) {
		final Integer val = rowSubCellSizeCache.get (Integer.valueOf (vrow));
		return (val == null ? -1 : Integer.valueOf (val));
	}
}
