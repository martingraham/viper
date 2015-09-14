package napier.pedigree.io;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import napier.pedigree.swing.app.PedigreeFrame;

import org.apache.log4j.Logger;
import org.resspecies.parsing.GenotypeFileParser;


/**
 * Action that launches file dialog and loads in chosen data sets
 * @author cs22
 *
 */
public class LoadPedigreeAction extends AbstractLoadAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1810641433484072309L;
	static final private Logger LOGGER = Logger.getLogger (LoadPedigreeAction.class);
	
	
	public LoadPedigreeAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeFrame pFrame) {
		super (keyStroke, actionPrefix, pFrame);
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		super.actionPerformed (aEvent);
		
		final int retValue = jfc.showOpenDialog (pFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			pFrame.setModel (new GenotypeFileParser (
					PedigreeIOUtils.getInstance().makeFakeMarkerFile (jfc.getSelectedFile()), 
					jfc.getSelectedFile()
				)
			);
		}
	}
}