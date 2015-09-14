package napier.pedigree.swing.app.actions;

import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;



import napier.pedigree.swing.app.PedigreeSelectionSource;


public abstract class AbstractFamilyCentricAction extends PropertyPrefixBasedAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1865016357316715163L;

	
	
	public AbstractFamilyCentricAction (final KeyStroke keyStroke, final String actionPrefix) {
		super (keyStroke, actionPrefix);	
	}
	
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		final Object source = aEvent.getSource();

		if (source instanceof PedigreeSelectionSource) {
			doAction ((PedigreeSelectionSource)source);
		}
		else if (source instanceof Container) {
			Container con = (Container)source;
			while (con != null && !(con instanceof PedigreeSelectionSource)) {
				con = con.getParent();
			}
			if (con instanceof PedigreeSelectionSource) {
				doAction ((PedigreeSelectionSource)con);
			}
		}
	}
	
	
	/**
	 * Extend this action in subclasses to use FamilySelectionSource data fields
	 * @param famSelSource
	 */
	public abstract void doAction (PedigreeSelectionSource famSelSource);
	
	
	/**
	 * Extend this action in subclasses to use FamilySelectionSource data fields
	 * to customise aspects of Actions such as labelling
	 * @param famSelSource
	 */
	public abstract void updateAction (PedigreeSelectionSource famSelSource);
	
	
	
	/**
	 * Works out whether this action is worthwhile given the data in the PedigreeSelectionSource
	 * e.g. a masking action on a group of individuals that are already masked isn't worthwhile
	 * @param famSelSource
	 */
	public abstract boolean isWorthwhileAction (PedigreeSelectionSource famSelSource);
}