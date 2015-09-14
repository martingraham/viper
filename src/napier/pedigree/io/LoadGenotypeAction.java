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
public class LoadGenotypeAction extends AbstractLoadAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2307338046060738537L;
	static final private Logger LOGGER = Logger.getLogger (LoadGenotypeAction.class);
	
	
	public LoadGenotypeAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeFrame pFrame) {
		super (keyStroke, actionPrefix, pFrame);
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		super.actionPerformed (aEvent);
		
		final int retValue = jfc.showOpenDialog (pFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			pFrame.setModel (new GenotypeFileParser (
					jfc.getSelectedFile(),
					pFrame.getModel().getPedigreeFile()
				)
			);
		}
	}
}