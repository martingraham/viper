package napier.pedigree.io;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import napier.pedigree.swing.app.PedigreeFrame;
import napier.pedigree.swing.app.actions.PropertyPrefixBasedAction;

import org.apache.log4j.Logger;
import org.resspecies.parsing.GenotypeFileParser;


/**
 * Action that launches file dialog and loads in chosen data sets
 * @author cs22
 *
 */
public class LoadDualFileAction extends PropertyPrefixBasedAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1810641433484072309L;
	static final private Logger LOGGER = Logger.getLogger (LoadDualFileAction.class);

	protected PedigreeFrame pFrame;
	
	public LoadDualFileAction (final KeyStroke keyStroke, final String actionPrefix, final PedigreeFrame pFrame) {
		super ();
		this.setTextPropertyFile (this.getClass().getPackage().getName()+".labels");
		this.setup (keyStroke, actionPrefix);
		this.pFrame = pFrame;
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		final JPedigreeGenotypeFileChooser jdfc = new JPedigreeGenotypeFileChooser (new File ("."));
		jdfc.setup ();
		final int retValue = jdfc.showOpenDialog (pFrame);
		if (retValue == JFileChooser.APPROVE_OPTION) {
			LOGGER.debug ("ped file: "+jdfc.getPedigreeFile());
			LOGGER.debug ("geno file: "+jdfc.getGenotypeFile());
			pFrame.setModel (new GenotypeFileParser (
				jdfc.getGenotypeFile(), 
				jdfc.getPedigreeFile()
			));
		}
	}
}