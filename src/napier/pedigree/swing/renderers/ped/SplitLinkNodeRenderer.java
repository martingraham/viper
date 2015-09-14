package napier.pedigree.swing.renderers.ped;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;

import util.colour.ColorUtilities;

import napier.pedigree.model.impl.SplitLinkNodeObject;
import napier.pedigree.swing.JGeneration;
import napier.pedigree.swing.renderers.base.AbstractErrorRenderer;
import napier.pedigree.swing.renderers.base.AbstractOffspringSetRenderer;


public class SplitLinkNodeRenderer extends AbstractOffspringSetRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6194440682058137413L;

		protected Map<Object, List<Integer>> nodeSplits;
		protected int direction;
		
		protected Color[] colours = {ColorUtilities.addAlpha (Color.lightGray, 128),
				ColorUtilities.addAlpha (Color.gray, 128),
				ColorUtilities.addAlpha (Color.darkGray, 128),
				ColorUtilities.addAlpha (Color.black, 128)
		};
		protected Color darkSelectedColour;
		
		protected Stroke stroke = new BasicStroke (2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		protected JTable refTable;
		
	
	   public Component getTableCellRendererComponent (final JTable table, final Object value,
			   final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		   if (value instanceof SplitLinkNodeObject) {
			   final SplitLinkNodeObject slno = (SplitLinkNodeObject)value;
			   final int queryRow = (slno.getVal() == SplitLinkNodeObject.UP ? row - 1 : row + 1);
			   nodeSplits = slno.getObjectColumnPositions (table, queryRow);   
			   direction = slno.getVal();
			   slno.removeSingleOccurences (nodeSplits);
			   slno.removeEntireContiguousOccurences (nodeSplits);
			   slno.representiseOtherContiguousOccurences (nodeSplits);
			   final int rowHeight = table.getRowHeight (row);
			   final int newRowHeight = (nodeSplits.isEmpty() ? 6 : Math.min (50, Math.max (20, (4 * nodeSplits.size()))));
			   if (rowHeight != newRowHeight) {
				   table.setRowHeight (row, newRowHeight);
			   }
		   }
		   refTable = table;
		   return this;
	   }

		public void paintComponent (final Graphics gContext) {
	    	//int width = this.getWidth ();
			final int height = this.getHeight ();
		    //((Graphics2D)gContext).setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
		   //          RenderingHints.VALUE_ANTIALIAS_ON); 
	    	//gContext.drawString (nodeSplits.toString(), 0, 13);
	    	
	    	final int count = nodeSplits.size();
	    	final int vseperation = height / (count + 1);
	    	int iterCount = 0;
	    	final int[] columnMiddles = getColumnMiddles ();
	    	
	    	darkSelectedColour = AbstractErrorRenderer.getSelectedColour().darker();
	    	   	
			final Set<Map.Entry<Object, List<Integer>>> entries = nodeSplits.entrySet ();
			final Iterator<Map.Entry<Object, List<Integer>>> iter = entries.iterator();
			while (iter.hasNext()) {			
				final Map.Entry<Object, List<Integer>> entry = iter.next();
				final List<Integer> columns = entry.getValue();
				final boolean selected = ((JGeneration)refTable).getPedigreeSelection().getSelectedGraph().containsNode (entry.getKey());
				drawLinks ((Graphics2D)gContext, columns, columnMiddles,
						vseperation * (iterCount + 1), iterCount, direction, selected);
				iterCount++;
			}
		}
		
		
		protected int[] getColumnMiddles () {
			final int[] colxpos = new int [refTable.getColumnCount()];
			int xtotal = 0;
			for (int columnIndex = 0; columnIndex < refTable.getColumnCount(); columnIndex++) {
				final int colWidth = refTable.getColumnModel().getColumn(columnIndex).getWidth();
				colxpos [columnIndex] = xtotal + (colWidth / 2);
				xtotal += colWidth;
			}
			return colxpos;
		}
		
		
		protected void drawLinks (final Graphics2D g2D, final List<Integer> columns, 
				final int[] columnMiddles, final int vHeight, final int iterCount,
				final int direction, final boolean selected) {
			final Stroke oldStroke = g2D.getStroke();
			g2D.setStroke (stroke);
			g2D.setColor (selected ? darkSelectedColour : colours [iterCount % colours.length]);
			
			//}
			//g2D.drawString (Arrays.toString (columnMiddles), 0, 28);
			//g2D.drawString (vHeight+"", 0, 42);
			
			final int columnIndex1 = columns.get(0).intValue();
			final int columnIndex2 = columns.get(columns.size() - 1).intValue();
			final int x1 = columnMiddles [columnIndex1];
			final int x2 = columnMiddles [columnIndex2];
			final int arcWidth = x2 - x1;
			final int arcMajRadius = arcWidth / 2;
			
			for (int listIndex = 1; listIndex < columns.size() - 1; listIndex++) {
				final int columnIndex = columns.get(listIndex).intValue();
				final int x = columnMiddles [columnIndex];
				final int ellx = Math.abs (x - (x1 + arcMajRadius));
				final double brad = (double)vHeight;
				final double arad = (double)arcMajRadius;
				final double elly = Math.sqrt ((brad * brad) * (1.0 - ((ellx * ellx) / (arad * arad))));
				final int ellHeight = (int)elly;
				if (direction == SplitLinkNodeObject.UP) {
					//g2D.fillRoundRect (x - 5, -10, 10, vHeight + 10, 5, 5);
					g2D.drawLine (x, -2, x, ellHeight);
				} else {
					//g2D.fillRoundRect (x - 5, -10, 10, vHeight + 10, 5, 5);
					g2D.drawLine (x, this.getHeight() - ellHeight, x, this.getHeight() + 2);
				}
			}
			

			if (direction == SplitLinkNodeObject.UP) {
				//g2D.fillRoundRect (x1 - 5, vHeight - 10, x2 - x1 + 10, 10, 5, 5);
				//g2D.drawLine (x1, vHeight, x2, vHeight);
				g2D.drawArc (x1, -vHeight, arcWidth, vHeight * 2, 0, -180);
			} else {
				g2D.drawArc (x1, this.getHeight() - vHeight, arcWidth, vHeight * 2, 0, 180);
				//g2D.fillRoundRect (x1 - 5, this.getHeight() - vHeight, x2 - x1 + 10, 10, 5, 5);
			}
			
			g2D.setStroke (oldStroke);
		}
}
