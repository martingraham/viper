package napier.pedigree.io;

import io.DataPrep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import napier.pedigree.swing.app.PedigreeFrame;

import org.apache.log4j.Logger;

import util.XMLConstants2;


public final class PedigreeIOUtils {
	
	
	static final private Logger LOGGER = Logger.getLogger (PedigreeIOUtils.class);
	
	
	private static final PedigreeIOUtils PEDIGREEIOUTILS_INSTANCE = new PedigreeIOUtils ();
	
	private PedigreeIOUtils () {}

	public static PedigreeIOUtils getInstance() { return PEDIGREEIOUTILS_INSTANCE; }

	
	
	public File makeFakeMarkerFile (final File pedigreeFile) {
		File tempFile = null;
		final String repIndividual = grabFirstIndividual (pedigreeFile);
		
		if (repIndividual != null) {
		
			try {	
				tempFile = File.createTempFile ("fakeMarker", ".txt");
				final PrintWriter pWriter = 
						DataPrep.getInstance().makeBufferedPrintWriter (tempFile, XMLConstants2.UTF8, false);
				pWriter.println (repIndividual+"\tDummy_Marker\tA\tG");
				pWriter.flush();
				pWriter.close();
				tempFile.deleteOnExit();
			} catch (IOException e) {
				LOGGER.error ("Fake Marker File IO Error", e);
			}
		}
		
		return tempFile;
	}
	
	
	public String grabFirstIndividual (final File pedigreeFile) {

        String line;
        String individualName = null;
		
		try {
			final BufferedReader reader = new BufferedReader (new FileReader (pedigreeFile));
			
	        //loop through all the lines till we return null at the end
	        while ((line = reader.readLine()) != null && individualName == null) {

	            //skip over empty lines
	            if (line.isEmpty() || line.startsWith("--")) {
	                continue;
	            }

	            final StringTokenizer tokens = new StringTokenizer (line);
	            individualName = tokens.nextToken();
	        }
	        
	        reader.close();
		} catch (FileNotFoundException e) {
			LOGGER.error ("Pedigree File Error", e);
		} catch (IOException e) {
			LOGGER.error ("Pedigree File IO Error", e);
		}

		return individualName;
	}
}
