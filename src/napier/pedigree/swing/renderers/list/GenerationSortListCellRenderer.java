package napier.pedigree.swing.renderers.list;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import napier.pedigree.model.sort.VariableRowComparator;
import napier.pedigree.util.PedigreeIconCache;

public class GenerationSortListCellRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4482022377933723016L;
	
	private final static Icon MALE_ICON = PedigreeIconCache.makeIcon ("maleIcon");
	private final static Icon FEMALE_ICON = PedigreeIconCache.makeIcon ("femaleIcon");
	private final static Icon OFFSPRING_ICON = PedigreeIconCache.makeIcon ("offspringIcon");
	private final static Icon[] ICONARRAY = {MALE_ICON, OFFSPRING_ICON, FEMALE_ICON};
	
	@Override
	public Component getListCellRendererComponent (final JList list,
	        final Object value, // value to display
	        final int index,    // cell index
	        final boolean iss,  // is selected
	        final boolean chf)  // cell has focus?
    {
         super.getListCellRendererComponent (list, value, index, iss, chf);
         if (value instanceof VariableRowComparator<?>) {
        	 final VariableRowComparator<?> vrComp = (VariableRowComparator<?>)value;
        	 final int modelTableRow = vrComp.getRow() - 1;
        	 if (modelTableRow >= 0 && modelTableRow < ICONARRAY.length) {	
        		 setIcon (ICONARRAY [modelTableRow]);
        	 }
        	 setText (vrComp.getComparator().toString());
        	 
         } else {
        	 setText (Integer.toString(index+1)+" "+getText());
         }
         return this;
    }
}
