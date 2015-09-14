package napier.pedigree.swing.renderers.tooltip;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import util.Messages;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.impl.SplitLinkNodeObject;
import napier.pedigree.util.HTMLLabel;


public class SplitLinkTooltipInfoRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6194440682058137413L;

		protected Object lastValue;
		
	
	   public Component getTableCellRendererComponent (final JTable table, final Object value,
			   final boolean isSelected, final boolean hasFocus, final int row, final int column) {

		   if (value instanceof SplitLinkNodeObject && value != lastValue) {
			   final SplitLinkNodeObject slno = (SplitLinkNodeObject)value;
			   final int queryRow = (slno.getVal() == SplitLinkNodeObject.UP ? row - 1 : row + 1);
			   final Map<Object, List<Integer>> nodeSplits = slno.getObjectColumnPositions (table, queryRow);
			   slno.removeSingleOccurences (nodeSplits);
			   slno.removeEntireContiguousOccurences (nodeSplits);
			   slno.representiseOtherContiguousOccurences (nodeSplits);

			   final StringBuffer sBuffer = new StringBuffer ();
			   sBuffer.append (HTMLLabel.HTML_START);
			   if (nodeSplits.isEmpty()) {
				   sBuffer.append (Messages.getString (PropertyConstants.TEXTPROPS, "NoSplits"));
			   }
			   else {
				   for (Map.Entry<Object, List<Integer>> nodeSplit : nodeSplits.entrySet()) {
					   sBuffer.append(nodeSplit.getKey().toString()).append(": ").append(nodeSplit.getValue().size()).append(" splits<br>");
				   }
			   }
			   sBuffer.append (HTMLLabel.HTML_END);
			   this.setText (sBuffer.toString());
		   }
		   lastValue = value;
		  
		   return this;
	   }
}
