package napier.pedigree.swing.app.actions;

import java.awt.event.ActionEvent;

import napier.pedigree.swing.JMarkerTable;

public class MarkerToggleMaskAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2375560043974835029L;

	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		final Object source = aEvent.getSource();
		if (source instanceof JMarkerTable) {
			System.err.println ("source: "+source+", sel: "+((JMarkerTable)source).getSelectedRows());
		}
	}

}
