package napier.pedigree.undo.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritablePopulation;

import napier.pedigree.io.ErrorStrings;
import napier.pedigree.undo.Memento;



public class FileBasedMaskStateMemento implements Memento<HeritablePopulation> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 464053863115314820L;
	private static final Logger LOGGER = Logger.getLogger (FileBasedMaskStateMemento.class);
	private static final SimpleDateFormat SDF = new SimpleDateFormat ("yyMMdd_HHmmss");
	
	protected File historyFile;
	
	
	
	@Override
	public void make (final HeritablePopulation hPop) {
		final Memento<HeritablePopulation> histObj = new DefaultMaskStateMemento ();
		histObj.make (hPop);
		serialize (histObj);
	}
	
	@Override
	public void restore (final HeritablePopulation hPop) {
		final Memento<HeritablePopulation> histObj = reflate ();
		if (histObj != null) {
			histObj.restore (hPop);
		}
	}
	
	@Override
	public BitSet compare (final HeritablePopulation hPop) {
		final Memento<HeritablePopulation> histObj = reflate ();
		return histObj.compare (hPop);
	}

	
	public Memento<HeritablePopulation> reflate () {
		Memento<HeritablePopulation> hms = null;
		try {								
			final FileInputStream fis = new FileInputStream (historyFile);
			final ObjectInputStream oin = new ObjectInputStream (fis);
			hms = (Memento<HeritablePopulation>)oin.readObject();	
			oin.close();
		} catch (final FileNotFoundException fnfe) {
			final Object obj[] = {historyFile.getName()};
			LOGGER.error (ErrorStrings.getInstance().getString ("HistoryFileNotFound", obj), fnfe);
		} catch (final IOException ioe) {
			LOGGER.error (ErrorStrings.getInstance().getString ("IOReadClassError"), ioe);
		} catch (final ClassNotFoundException e) {
			LOGGER.error (ErrorStrings.getInstance().getString ("HistoryClassNotFound"), e);
		}
		return hms;
	}
	
	public void serialize (final Memento<HeritablePopulation> inMemHistoryState) {
		historyFile = null;
		try {					
			historyFile = File.createTempFile ("Hist"+SDF.format (new Date ()), ".obj");
			final ObjectOutput out = new ObjectOutputStream (new FileOutputStream (historyFile));
	        out.writeObject (inMemHistoryState);
	        out.close();
		} catch (final IOException ioe) {
			final Object[] obj = {inMemHistoryState.getClass()};
			LOGGER.error (ErrorStrings.getInstance().getString ("HistoryFileWriteError", obj), ioe);
		}
	}
}
