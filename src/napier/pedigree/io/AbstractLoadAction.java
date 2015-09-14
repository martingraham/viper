package napier.pedigree.io;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import util.Messages;

import napier.pedigree.swing.app.PedigreeFrame;
import napier.pedigree.swing.app.actions.PropertyPrefixBasedAction;


public abstract class AbstractLoadAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4665628974897844026L;

	static final private Logger LOGGER = Logger.getLogger (AbstractLoadAction.class);

	protected PedigreeFrame pFrame;
	protected JFileChooser jfc;
	
	public AbstractLoadAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeFrame pFrame) {
		super ();
		this.setTextPropertyFile (this.getClass().getPackage().getName()+".labels");
		this.setup (keyStroke, actionPrefix);
		this.pFrame = pFrame;
	}
	
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		jfc = new JFileChooser (new File ("."));
		
		final Preferences prefs = Preferences.userNodeForPackage (this.getClass());
		final String lastDir = prefs.get (actionPrefix+"_DIR", "");
		if (lastDir.length() > 0) {
			jfc.setCurrentDirectory (new File (lastDir));
		}
		
		final String lastFile = prefs.get (actionPrefix+"_LAST_FILE", "");
		if (lastFile.length() > 0) {
			jfc.setSelectedFile (new File (lastFile));
		}
				
		jfc.setDialogTitle (Messages.getString (textPropertyFile, actionPrefix + "DialogTitle"));
		jfc.setApproveButtonText (Messages.getString (textPropertyFile, actionPrefix + "OpenText"));
		jfc.setApproveButtonToolTipText (Messages.getString (textPropertyFile, actionPrefix + "OpenTooltip"));

		final PropertyChangeListener genPropListener = new PropertyChangeListener () {
			@Override
			public void propertyChange (final PropertyChangeEvent evt) {
				LOGGER.debug ("Current Dir: "+jfc.getCurrentDirectory());
				prefs.put (actionPrefix+"_DIR", jfc.getCurrentDirectory().getAbsolutePath());
				if (jfc.getSelectedFile() != null) {
					prefs.put (actionPrefix+"_LAST_FILE", jfc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		jfc.addPropertyChangeListener ("JFileChooserDialogIsClosingProperty", genPropListener);
	}

}
