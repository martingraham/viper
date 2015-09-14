package napier.pedigree.swing.renderers.ped;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.LabelUI;
import javax.swing.table.DefaultTableCellRenderer;

import util.ui.VerticalLabelUI;



public class IconTextPairRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2852935264793627807L;
	protected static final LabelUI VERTICALCAPABLE = new VerticalLabelUI (false, true);

	private final static Color BACKGROUND = new Color (224, 224, 224);
	
	
	public IconTextPairRenderer () {
		super ();
		//this.setBorder (BorderFactory.createRaisedBevelBorder ());
		this.setBackground (BACKGROUND);
		this.setUI (VERTICALCAPABLE);
		this.setHorizontalAlignment (SwingConstants.CENTER);
	}
	
	
    public Component getTableCellRendererComponent (final JTable table, final Object value,
    		final boolean isSelected, final boolean hasFocus, final int row, final int column) {
 
    	//table
    	final Component comp = super.getTableCellRendererComponent (table, value, false, hasFocus, row, column);

    	if (value instanceof JLabel) {
    		final JLabel jlabel = (JLabel)value;
    		this.setIcon (jlabel.getIcon());
    		this.setText (jlabel.getText());
    	}
    	
    	else if (value instanceof Icon) {
    		this.setIcon ((Icon)value);
    	}
    	
    	else if (value instanceof String) {
    		this.setText ((String)value);
    	}
    	
    	return comp;
    }
}