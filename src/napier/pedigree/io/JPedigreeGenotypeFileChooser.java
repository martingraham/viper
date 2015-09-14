package napier.pedigree.io;

import java.io.File;
import java.util.Properties;


import org.apache.log4j.Logger;

import util.Messages;


public class JPedigreeGenotypeFileChooser extends JMultiFileChooser {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1837450742183789595L;
	static final private Logger LOGGER = Logger.getLogger (JPedigreeGenotypeFileChooser.class);
	static final private int PEDINDEX = 0, GENINDEX = 1;

	
	public JPedigreeGenotypeFileChooser () {
		this (null);
	}
	
	public JPedigreeGenotypeFileChooser (final File file) {
		this (file, new String[] {"PedigreeFileLabel", "GenotypeFileLabel"});
	}
	
	public JPedigreeGenotypeFileChooser (final File file, final String[] newLabels) {
		super (file, 2, newLabels, null);
	}
	
	@Override
	public void setOverriddenData () {
		final Properties fileChooserProps = Messages.makeProperties ("labels", this.getClass(), true);
	
		//this.labelKeys = new String[] {"PedigreeFileLabel", "GenotypeFileLabel"};
		labelProperties = new Properties ();
		for (String labelKey : labelKeys) {
			labelProperties.put (labelKey, fileChooserProps.getProperty (labelKey));	
		}
		setToolTipTemplate (fileChooserProps.getProperty ("TooltipLabel"));
		this.setDialogTitle (fileChooserProps.getProperty ("LoadPedigreeGenotypeDialogTitle"));
		this.setApproveButtonText (fileChooserProps.getProperty ("LoadPedigreeGenotypeOpenText"));
		this.setApproveButtonToolTipText (fileChooserProps.getProperty ("LoadPedigreeGenotypeOpenTooltip"));
		//this.setC
	}
	
	public File getPedigreeFile() {
		return getChosenFile (PEDINDEX);
	}

	public void setPedigreeFile (final File pedigreeFile) {
		setChosenFile (pedigreeFile, PEDINDEX);
	}

	public File getGenotypeFile() {
		return getChosenFile (GENINDEX);
	}

	public void setGenotypeFile (final File genotypeFile) {
		setChosenFile (genotypeFile, GENINDEX);
	}
}
