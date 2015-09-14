package napier.pedigree.io;

import io.DataPrep;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.resspecies.datasourceaware.FallBack;
import org.resspecies.datasourceaware.FallBackIndividual;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;
import org.resspecies.parsing.GenotypeFileParser;

import util.Messages;
import util.XMLConstants2;

public class MaskedFilesGenerator {

	private static final Logger LOGGER = Logger.getLogger (MaskedFilesGenerator.class);
	public final static Properties LABELS = Messages.makeProperties ("labels", MaskedFilesGenerator.class, true);	
	public final static String NEWLINE = System.getProperty ("line.separator");
	public final static String COMMENT_PREFIX = "# ";
	public final static String NULL_PARENT = "0";
	
	protected GenotypeFileParser parser;
	protected File baseFolder;
	

	
	final SimpleDateFormat filenameTimestampFormat;
	final SimpleDateFormat textTimestampFormat;
	
	final Date date;
	
	
	public MaskedFilesGenerator (final File folder, final GenotypeFileParser parser) {
		baseFolder = folder;
		this.parser = parser;
		filenameTimestampFormat = new SimpleDateFormat (getLabelString ("FilenameTimestampFormat"));
		textTimestampFormat = new SimpleDateFormat (getLabelString ("TextTimestampFormat"));
		date = new Date ();
	}
	
	/**
	 * Output a masked genotype file
	 */
	public void makeMaskedGenotypeFile () {
		final String maskedGenotypeName = makeMaskedGenotypeFilename ();
		final File maskedGenotypeFile = new File (baseFolder, maskedGenotypeName);
		
		try {
			final PrintWriter maskedGenotypeWriter = DataPrep.getInstance().makeBufferedPrintWriter (
					maskedGenotypeFile, XMLConstants2.UTF8, false);
			final HeritablePopulation hPop = parser.getGenotypedPopulation();
			outputUnmaskedGenotypes (maskedGenotypeWriter, hPop, "");
			//outputMaskedGenotypes (maskedGenotypeWriter, hPop, commentOutPrefix);
			//outputMaskedMarkers (maskedGenotypeWriter, hPop, commentOutPrefix);
			//outputMaskedIndividuals (maskedGenotypeWriter, hPop, commentOutPrefix);
			
			maskedGenotypeWriter.flush();
			maskedGenotypeWriter.close();
		} catch (final IOException e) {
			LOGGER.error (ErrorStrings.getInstance().getString("maskedGenotypeWriteError"), e);
		}
	}
	
	// make a suitable filename from the loaded genotype file and the date object
	public String makeMaskedGenotypeFilename () {
		final String genotypeName = parser.getGenotypeFile().getName();
		return makeTimestamp(filenameTimestampFormat)+"_Masked_"+genotypeName;
	}
	
	// output all masked genotypes to a writer object
	void outputMaskedGenotypes (final PrintWriter pWriter, final HeritablePopulation hPop, final String prefix) {
		final Map<String, List<String>> maskedGenotypesByIndividual = hPop.getMaskedGenotypesItoM();
		
		for (Entry<String, List<String>> maskedEntry : maskedGenotypesByIndividual.entrySet()) {
			final String indName = maskedEntry.getKey();
			final HeritableIndividual hInd = hPop.getIndividualForName (indName);
			
			for (String markerName : maskedEntry.getValue()) {
				
				final SNPMarker marker = hPop.getMarkerByName (markerName);
				final int originalGenotype = hPop.getOriginalGenotype (marker, hInd);
				final String originalGenotypeStr = marker.getShortGenotypeString (originalGenotype);
				pWriter.println (prefix + makeGenotypeEntry (indName, markerName, originalGenotypeStr));
			}
		}
	}
	
	
	// output all masked markers, by individual genotypes, to a writer object
	void outputMaskedMarkers (final PrintWriter pWriter, final HeritablePopulation hPop, final String prefix) {
		final Set<SNPMarker> maskedMarkers = hPop.getMaskedMarkers();
		
		for (Individual ind : hPop.getIndividuals()) {
			final String indName = ind.getName();
			final HeritableIndividual hInd = (HeritableIndividual)ind;

			for (SNPMarker marker : maskedMarkers) {		
				final int originalGenotype = hPop.getOriginalGenotype (marker, hInd);
				final String originalGenotypeStr = marker.getShortGenotypeString (originalGenotype);
				pWriter.println (prefix + makeGenotypeEntry (indName, marker.getName(), originalGenotypeStr));
			}
		}
	}
	
	
	// output all masked individuals, by marker genotypes, to a writer object
	void outputMaskedIndividuals (final PrintWriter pWriter, final HeritablePopulation hPop, final String prefix) {
		final Set<HeritableIndividual> maskedInds = hPop.getMaskedIndividuals();
		// Active markers of masked individuals because all inactive markers 
		// are covered in outputMaskedMarkers
		final Set<SNPMarker> activeMarkers = hPop.getActiveMarkers();
		
		for (HeritableIndividual hInd : maskedInds) {
			final String indName = hInd.getName();

			for (SNPMarker marker : activeMarkers) {		
				final int originalGenotype = hPop.getOriginalGenotype (marker, hInd);
				final String originalGenotypeStr = marker.getShortGenotypeString (originalGenotype);
				pWriter.println (prefix + makeGenotypeEntry (indName, marker.getName(), originalGenotypeStr));
			}
		}
	}
	
	
	// output all unmasked (valid) genotypes to a writer object
	void outputUnmaskedGenotypes (final PrintWriter pWriter, final HeritablePopulation hPop, final String prefix) {
		final Set<HeritableIndividual> maskedInds = hPop.getMaskedIndividuals();
		final Collection<Individual> activeInds = hPop.getIndividuals();
		activeInds.removeAll (maskedInds);
		
		final Set<SNPMarker> activeMarkers = hPop.getActiveMarkers();
		
		final Map<String, List<String>> maskedGenotypesByIndividual = hPop.getMaskedGenotypesItoM();
		
		for (Individual ind : activeInds) {
			final String indName = ind.getName();
			final HeritableIndividual hInd = (HeritableIndividual)ind;
			final List<String> maskedGenotypes = maskedGenotypesByIndividual.get (indName);

			for (SNPMarker marker : activeMarkers) {	
				if (maskedGenotypes == null || ! maskedGenotypes.contains (marker.getName())) {
					// Use original genotypes when outputting again
					final int originalGenotype = hInd.getOriginalGenotype (marker);
					if (originalGenotype != marker.getUnknownHomozygote()) {
						final String originalGenotypeStr = marker.getShortGenotypeString(originalGenotype).replace("/", "\t");
						pWriter.println (prefix + makeGenotypeEntry (indName, marker.getName(), originalGenotypeStr));
					}
				}
			}
		}
	}
	
	
	// makes a genotype entry string, used by all the preceding methods
	String makeGenotypeEntry (final String indName, final String markerName, final String genotype) {
		return indName+"\t"+markerName+"\t"+genotype;
	}
	
	
	
	/**
	 * Output a masked pedigree file
	 */
	public void makeMaskedPedigreeFile () {
		final String maskedPedigreeName = makeMaskedPedigreeFilename ();
		final File maskedPedigreeFile = new File (baseFolder, maskedPedigreeName);
		try {
			final PrintWriter maskedPedigreeWriter = DataPrep.getInstance().makeBufferedPrintWriter (
					maskedPedigreeFile, XMLConstants2.UTF8, false);
			final HeritablePopulation hPop = parser.getGenotypedPopulation();
			outputPedigreeRelationships (maskedPedigreeWriter, hPop, "");
			maskedPedigreeWriter.flush();
			maskedPedigreeWriter.close();
		} catch (final IOException e) {
			LOGGER.error (ErrorStrings.getInstance().getString("maskedPedigreeWriteError") , e);
		}
	}
	
	// Make a masked pedigree filename from the old pedigree file and a timestamp
	public String makeMaskedPedigreeFilename () {
		final String pedigreeName = parser.getPedigreeFile().getName();
		return makeTimestamp(filenameTimestampFormat)+"_Masked_"+pedigreeName;
	}
	
	
	// output pedigree relationships to a writer object
	void outputPedigreeRelationships (final PrintWriter pWriter, final HeritablePopulation hPop, final String prefix) {
		for (Individual ind : hPop.getIndividuals()) {
			final HeritableIndividual hInd = (HeritableIndividual)ind;
			pWriter.println (makePedigreeEntry (false, ind.getName(), getFatherName(hInd), getMotherName(hInd), ind.getGender(), ind.getLitter()));
		}
	}
	
	// Get the sire name of an individual, accounting for maskings etc
	protected String getFatherName (final HeritableIndividual hInd) {
		final boolean notKnown = (hInd.isFatherMasked() || hInd.getSire() == null || hInd.getSire() instanceof FallBackIndividual);
		return (notKnown ? NULL_PARENT : hInd.getSire().getName());
	}
	
	// Get the dam name of an individual, accounting for maskings etc
	protected String getMotherName (final HeritableIndividual hInd) {
		final boolean notKnown = (hInd.isMotherMasked() || hInd.getDam() == null || hInd.getDam() instanceof FallBackIndividual);
		return (notKnown ? NULL_PARENT : hInd.getDam().getName());
	}
	
	
	// output all masked individuals to a writer object
	void outputMaskedRelationships (final PrintWriter pWriter, final HeritablePopulation hPop, final String prefix) {
		final Set<HeritableIndividual> maskedInds = hPop.getMaskedIndividuals();
		// Active markers of masked individuals because all inactive markers 
		// are covered in outputMaskedMarkers
		final Set<SNPMarker> activeMarkers = hPop.getActiveMarkers();
		
		//final TreeMap<HeritableIndividual, HeritableIndividual> maskedRels = hPop.getMaskedMaternityRelationships();
		
		for (HeritableIndividual hInd : maskedInds) {
			final String indName = hInd.getName();

			for (SNPMarker marker : activeMarkers) {		
				final int originalGenotype = hPop.getOriginalGenotype (marker, hInd);
				final String originalGenotypeStr = marker.getShortGenotypeString (originalGenotype);
				pWriter.println (prefix + makeGenotypeEntry (indName, marker.getName(), originalGenotypeStr));
			}
		}
	}
	
	
	
	// Make a pedigree entry string, used by the preceding methods
	protected String makePedigreeEntry (final boolean comment, final String indName, final String sireName,
			 final String damName, final String gender, final String litter) {		
		final boolean emptyLitter = (litter == null || litter.equals ("") || litter.equals (FallBack.SAFE_LITTER));
		return (comment ? COMMENT_PREFIX : "")+indName+"\t"+sireName+"\t"+damName+"\t"+gender+(emptyLitter ? "" : "\t"+litter);
	}
	
	
	
	
	
	/**
	 * Generate a log file, detailing all the removed data from the masked (cleaned)
	 * pedigree and genotype files.
	 */
	public void makeLogFile () {
		final String pedigreeName = parser.getPedigreeFile().getName();
		final String genotypeName = parser.getGenotypeFile().getName();
		final String maskedPedigreeFilename = makeMaskedPedigreeFilename ();
		final String maskedGenotypeFilename = makeMaskedGenotypeFilename ();
		final String logName = makeTimestamp(filenameTimestampFormat)+"_Log_"+genotypeName;
		
		final MessageFormat genoFormat = new MessageFormat (getLabelString ("GenotypeLogInfo"));
		final MessageFormat pedFormat = new MessageFormat (getLabelString ("PedigreeLogInfo"));
		
		final File logFile = new File (baseFolder, logName);
		try {
			final PrintWriter logFileWriter = DataPrep.getInstance().makeBufferedPrintWriter (
					logFile, XMLConstants2.UTF8, false);
			final HeritablePopulation hPop = parser.getGenotypedPopulation();
			logFileWriter.println (getLabelString ("LogHeader"));
			logFileWriter.println ();
			
			final Object[] pedArgs = {pedigreeName, maskedPedigreeFilename};
			logFileWriter.println (pedFormat.format (pedArgs));
			logFileWriter.println ();
			
			final Object[] genoArgs = {genotypeName, maskedGenotypeFilename};
			logFileWriter.println (genoFormat.format (genoArgs));
			logFileWriter.println ();
			
			logFileWriter.println (makeTimestamp (textTimestampFormat));
			
			logFileWriter.println (NEWLINE);
			logFileWriter.println (getLabelString ("LogMaskedIndividuals"));
			logMaskedIndividuals (logFileWriter, hPop);
			
			logFileWriter.println (NEWLINE);
			logFileWriter.println (getLabelString ("LogMaskedMarkers"));
			logMaskedMarkers (logFileWriter, hPop);
			
			logFileWriter.println (NEWLINE);
			logFileWriter.println (getLabelString ("LogMaskedGenotypes"));
			logMaskedGenotypes (logFileWriter, hPop);
			
			logFileWriter.println (NEWLINE);
			logFileWriter.println (getLabelString ("LogMaskedParents"));
			logMaskedParents (logFileWriter, hPop);
			
			logFileWriter.flush();
			logFileWriter.close();
		} catch (final IOException ioe) {
			LOGGER.error (ErrorStrings.getInstance().getString("maskedPedigreeWriteError") , ioe);
		}
	}
	
	
	// log all masked individuals to a writer object
	void logMaskedIndividuals (final PrintWriter pWriter, final HeritablePopulation hPop) {
		final Set<HeritableIndividual> maskedInds = hPop.getMaskedIndividuals();
		final String prefix = getLabelString ("LogIndividualPrefix");
		
		for (HeritableIndividual hInd : maskedInds) {
			pWriter.println (prefix + hInd.getName());
		}
	}
	
	
	// log all masked markers to a writer object
	void logMaskedMarkers (final PrintWriter pWriter, final HeritablePopulation hPop) {
		final Set<SNPMarker> maskedMarkers = hPop.getMaskedMarkers();
		final String prefix = getLabelString ("LogMarkerPrefix");
		
		for (SNPMarker marker : maskedMarkers) {		
			pWriter.println (prefix + marker.getName());
		}
	}
	
	
	// log all masked individual/marker combinations to a writer object
	void logMaskedGenotypes (final PrintWriter pWriter, final HeritablePopulation hPop) {
		final Map<String, List<String>> maskedGenotypesByIndividual = hPop.getMaskedGenotypesItoM();
		final String indPrefix = getLabelString ("LogIndividualPrefix");
		final String markerPrefix = getLabelString ("LogMarkerPrefix");
		
		for (Entry<String, List<String>> maskedEntry : maskedGenotypesByIndividual.entrySet()) {
			final String indName = maskedEntry.getKey();
			
			for (String markerName : maskedEntry.getValue()) {
				pWriter.println (indPrefix + indName + markerPrefix + markerName);
			}
		}
	}
	
	
	// log all masked parents to a writer object
	void logMaskedParents (final PrintWriter pWriter, final HeritablePopulation hPop) {
		final Map<HeritableIndividual, HeritableIndividual> maskedSireMap = hPop.getMaskedPaternityRelationships();
		final String sirePrefix = getLabelString ("LogMaskedSirePrefix");
		
		for (Entry<HeritableIndividual, HeritableIndividual> maskedSireRel : maskedSireMap.entrySet()) {
			pWriter.println (sirePrefix + maskedSireRel.getKey() + "\t-->\t" + maskedSireRel.getValue());
		}
		
		final Map<HeritableIndividual, HeritableIndividual> maskedDamMap = hPop.getMaskedMaternityRelationships();
		final String damPrefix = getLabelString ("LogMaskedDamPrefix");
		
		for (Entry<HeritableIndividual, HeritableIndividual> maskedDamRel : maskedDamMap.entrySet()) {
			pWriter.println (damPrefix + maskedDamRel.getKey() + "\t-->\t" + maskedDamRel.getValue());
		}
	}
	
	
	// Get a string from the labels.properties file in this package
	public String getLabelString (final String propertyKey) {
		return LABELS.getProperty (propertyKey);
	}
	

	// Make a timestamp string with a given formatter
	final String makeTimestamp (final SimpleDateFormat sdf) {
	    return sdf.format (date);
	}
}
