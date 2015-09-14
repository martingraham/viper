package napier.pedigree.swing.app.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import util.Messages;
import util.StringUtils;

import napier.pedigree.util.PedigreeIconCache;


public abstract class PropertyPrefixBasedAction extends AbstractAction implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1865016357316715163L;

	protected String textPropertyFile = "napier.pedigree.swing.app.frameLabel";
	protected String accString;
	protected String actionPrefix;
	protected String[] altNames;
	protected String[] altTooltips;
	
	
	
	public PropertyPrefixBasedAction () {
		super ();
		this.addPropertyChangeListener (this);
	}
	
	public PropertyPrefixBasedAction (final KeyStroke keyStroke, final String actionPrefix) {
		this ();
		setup (keyStroke, actionPrefix);	
	}
	
	
	protected void setup (final KeyStroke keyStroke, final String actionPrefix) {
		
		final Icon handleIcon = PedigreeIconCache.makeIcon (actionPrefix + "Icon");
		this.actionPrefix = actionPrefix;
		
		putValue (Action.NAME, Messages.getString (textPropertyFile, actionPrefix + "ActionName"));
 		putValue (Action.MNEMONIC_KEY, Integer.valueOf (keyStroke.getKeyCode()));
		putValue (Action.ACCELERATOR_KEY, keyStroke);
		putValue (Action.SMALL_ICON, handleIcon);
		//Below line now poked into life in the propertyChange method when ACCELERATOR_KEY is set
   		//putValue (Action.SHORT_DESCRIPTION, Messages.getString (textPropertyFile, actionPrefix + "ActionTooltip") + accString); 
		putValue ("LargeIcon", handleIcon);	
		
		final String altNameString = Messages.getString (textPropertyFile, actionPrefix + "ActionNames");
		if (altNameString != null && altNameString.charAt(0) != Messages.ERROR_CHAR) {
			altNames = altNameString.split("\\|");
		}
	}
	
	public void setTextPropertyFile (final String newTextPropertyFile) {
		textPropertyFile = newTextPropertyFile;
	}
	

	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals (Action.ACCELERATOR_KEY)) {
			accString = StringUtils.getInstance().makeAccString (this);
			putValue (Action.SHORT_DESCRIPTION, Messages.getString (textPropertyFile, actionPrefix + "ActionTooltip") + accString);
			makeAltTooltips ();
		}
	}
	
	
	
	protected void makeAltTooltips () {
		final String altTooltipString = Messages.getString (textPropertyFile, actionPrefix + "ActionTooltips");
		
		if (altTooltipString != null && altTooltipString.charAt(0) != Messages.ERROR_CHAR) {
			altTooltips = altTooltipString.split("\\|");
			
			for (int n = 0; n < altTooltips.length; n++) {
				altTooltips [n] = altTooltips [n] + accString;
			}
		}
	}
}