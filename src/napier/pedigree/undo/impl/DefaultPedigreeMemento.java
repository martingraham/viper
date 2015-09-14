package napier.pedigree.undo.impl;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

import org.resspecies.inheritance.model.HeritablePopulation;

import napier.pedigree.swing.AbstractErrorHistogram;
import napier.pedigree.swing.JMarkerTable;
import napier.pedigree.swing.app.PedigreeFrame;
import napier.pedigree.undo.Memento;


public class DefaultPedigreeMemento implements Memento<PedigreeFrame> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5413459610833576680L;
	
	protected Memento<HeritablePopulation> maskMemento;
	protected Memento<Collection<AbstractErrorHistogram>> histogramsMemento;
	protected Memento<JMarkerTable> markerByMarkerFilterMemento;
	protected int errorTotal;
	
	
	@Override
	public void make (final PedigreeFrame pFrame) {
		maskMemento = new DefaultMaskStateMemento ();
		maskMemento.make (pFrame.getModel().getGenotypedPopulation());
		
		histogramsMemento = new DefaultHistogramsMemento ();
		histogramsMemento.make (Arrays.asList (pFrame.getHistograms()));
		
		markerByMarkerFilterMemento = new DefaultMarkerByMarkerFilterMemento ();
		markerByMarkerFilterMemento.make (pFrame.getMarkerTable ());
		
		errorTotal = pFrame.getErrorCollator().getCurrentAllErrorTotal();
	}

	@Override
	public void restore (final PedigreeFrame pFrame) {
		maskMemento.restore (pFrame.getModel().getGenotypedPopulation());
		histogramsMemento.restore (Arrays.asList (pFrame.getHistograms()));
		markerByMarkerFilterMemento.restore (pFrame.getMarkerTable());
	}

	@Override
	public BitSet compare (final PedigreeFrame pFrame) {
		final BitSet bitSet = maskMemento.compare (pFrame.getModel().getGenotypedPopulation());
		bitSet.or (histogramsMemento.compare (Arrays.asList (pFrame.getHistograms())));
		bitSet.or (markerByMarkerFilterMemento.compare (pFrame.getMarkerTable()));
		return bitSet;
		
	}
	
	public String toString () {
		final StringBuilder strBuilder = new StringBuilder ("<HTML>");		
		strBuilder.append (errorTotal).append(" total errors");
		strBuilder.append ("<br>");
		strBuilder.append (maskMemento.toString());
		strBuilder.append ("</HTML>");
		return strBuilder.toString();
	}
}
