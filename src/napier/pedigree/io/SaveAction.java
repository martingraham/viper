package napier.pedigree.io;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicFileChooserUI;

import napier.pedigree.swing.app.EDTUpdatingTask;
import napier.pedigree.swing.app.PedigreeFrame;
import napier.pedigree.swing.app.actions.PropertyPrefixBasedAction;

import org.apache.log4j.Logger;

import util.Messages;


/**
 * Action that launches file dialog and saves adjusted data sets
 * @author cs22
 *
 */
public class SaveAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1810641433484072309L;
	static final private Logger LOGGER = Logger.getLogger (SaveAction.class);

	protected PedigreeFrame pFrame;
	
	public SaveAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeFrame pFrame) {
		super ();
		this.setTextPropertyFile (this.getClass().getPackage().getName()+".labels");
		this.setup (keyStroke, actionPrefix);
		this.pFrame = pFrame;
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		final JMultiFileChooser jdfc = new JMultiFileChooser (new File ("."), 0, 
				null,
				Messages.makeProperties ("labels", this.getClass(), true)) {
			
			/**
			* 
			*/
			private static final long serialVersionUID = 2995074157525670535L;

			public void approveSelection() {
		    	boolean isDir = getSelectedFile() != null && getSelectedFile().isDirectory();
		    	if (isDir) {
		    		super.approveSelection ();
		    	}
		    }
		};
		jdfc.setToolTipTemplate (jdfc.getLabelProperties().getProperty ("TooltipLabel"));
		jdfc.setDialogTitle (jdfc.getLabelProperties().getProperty ("SaveDialogTitle"));
		jdfc.setApproveButtonToolTipText (jdfc.getLabelProperties().getProperty ("SaveButtonTooltip"));
		jdfc.setup ();

		jdfc.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
		jdfc.addPropertyChangeListener (
			new PropertyChangeListener () {
				@Override
				public void propertyChange (final PropertyChangeEvent evt) {
					if ("SelectedFileChangedProperty".equals (evt.getPropertyName())) {
						final boolean isDir = jdfc.getSelectedFile() != null && jdfc.getSelectedFile().isDirectory();
						final BasicFileChooserUI bcfui = ((BasicFileChooserUI)jdfc.getUI());
						bcfui.getApproveSelectionAction().setEnabled (isDir);
						setApproveButtonEnabledState (jdfc, isDir);
					}
					else if ("ApproveButtonTextChangedProperty".equals (evt.getPropertyName())) {
						setApproveButtonEnabledState (jdfc, false);
					}
				}
			}
		);

		final int retValue = jdfc.showDialog (pFrame, jdfc.getLabelProperties().getProperty ("SaveButtonText"));
		if (retValue == JFileChooser.APPROVE_OPTION) {
			final SwingWorker<Void, Void> swork = new SwingWorker<Void, Void> () {
				@Override
				public Void doInBackground() {
					setProgress (1);
					final MaskedFilesGenerator maskFileGen = new MaskedFilesGenerator (jdfc.getSelectedFile(), pFrame.getModel());
					maskFileGen.makeMaskedPedigreeFile();
					setProgress (2);
					maskFileGen.makeMaskedGenotypeFile();
					setProgress (3);
					maskFileGen.makeLogFile();
					return null;
				}
				
		        @Override
		        public void done() {
		        	//EMPTY
		        }
			};

			final String properties = this.getClass().getPackage().getName().toString() + ".labels";
			final String[] updateMessages = Messages.getString (properties, "SaveStages").split("\\|");
			new EDTUpdatingTask (pFrame, swork, updateMessages).doIt();
		}
	}
	
	
	
	
	void setApproveButtonEnabledState (final JFileChooser jfc, final boolean enabled) {
		recurseComps (jfc, jfc, enabled);
	}
	
	final void recurseComps (final JFileChooser jfc, final Container con, final boolean enabled) {
		final Component[] comps = con.getComponents();
		for (Component comp : comps) {
			if (comp instanceof Container) {
				recurseComps (jfc, (Container)comp, enabled);
			} 
			if (comp instanceof AbstractButton) {
				final AbstractButton aButton = ((AbstractButton)comp);
				if (aButton.getText() != null && aButton.getText().equals (jfc.getApproveButtonText())) {
					aButton.setEnabled (enabled);
				}
			}
		}
	}
}