package napier.pedigree.swing.app;

import java.awt.Component;
import java.awt.Point;

import napier.pedigree.swing.JMarkerTable;

import org.resspecies.inheritance.model.SNPMarker;


public class ActionListMarkerPopupMenu extends ActionListPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3390902802523203023L;

	protected SNPMarker selectedMarker;
	
	
    public ActionListMarkerPopupMenu (final int activeButton) {
  		
        super (activeButton);
		
		//selectedIndividual = new SingleItemSet<Individual> ();
	}
	
	
	@Override
	protected boolean setDetails (Component comp, Point mouseCoord) {
		if (comp instanceof JMarkerTable) {
			final JMarkerTable jMarkerTable = (JMarkerTable)comp;
			final int row = jMarkerTable.rowAtPoint (mouseCoord);
			//final int column = jIndTable.columnAtPoint (mouseCoord);
			
			final SNPMarker marker = jMarkerTable.getMarker (row);
			if (marker != null) {
				selectedMarker = marker;
		    	// Update the popup's menu item labelling given context of current selection
		    	updateMenuItemActions ();
		    	title.setText (makeTitle());
				return true;
			}
			
		}
		return false;
	}

	@Override
	protected String makeTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateMenuItemActions() {
		// TODO Auto-generated method stub

	}

}
