package napier.pedigree.undo.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import util.Messages;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.undo.Memento;


public class DefaultMaskStateMemento implements Memento<HeritablePopulation> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1580620508095564674L;
	
	protected Collection<String> maskedMarkers;
	protected Collection<String> maskedIndividuals;
	protected Map<String, List<String>> maskedGenotypesItoM;
	protected Collection<String> nullSireIndividuals;
	protected Collection<String> nullDamIndividuals;
	
	protected int maskedGenotypeCount;
	protected MessageFormat mFormat = new MessageFormat (
			Messages.getString (PropertyConstants.TEXTPROPS, "HistoryNodeTemplate1"));
	
	@Override
	public void make (final HeritablePopulation hPop) {
		maskedGenotypeCount = 0;
		
		final Set<SNPMarker> maskedSNPMarkers = hPop.getMaskedMarkers();
		maskedMarkers = new ArrayList<String> ();
		for (SNPMarker snpMarker : maskedSNPMarkers) {
			maskedMarkers.add (snpMarker.getName());
		}
		
		final Set<HeritableIndividual> maskedObjIndividuals = hPop.getMaskedIndividuals();
		maskedIndividuals = new ArrayList<String> ();
		for (Individual ind : maskedObjIndividuals) {
			maskedIndividuals.add (ind.getName());
		}
		
		final Map<String, List<String>> curMaskedGenotypes = hPop.getMaskedGenotypesItoM();
		maskedGenotypesItoM = new HashMap<String, List<String>> ();
		for (Entry<String, List<String>> genEntry : curMaskedGenotypes.entrySet()) {
			final List<String> markerNames = genEntry.getValue();
			maskedGenotypeCount += (markerNames == null ? 0 : markerNames.size());
			maskedGenotypesItoM.put (genEntry.getKey(), markerNames == null ? new ArrayList<String> () : new ArrayList<String> (markerNames));
		}
		
		final Set<HeritableIndividual> haveMaskedSires = hPop.getMaskedPaternityRelationships().keySet();
		nullSireIndividuals = new ArrayList<String> ();
		for (Individual ind : haveMaskedSires) {
			nullSireIndividuals.add (ind.getName());
		}
		
		final Set<HeritableIndividual> haveMaskedDams = hPop.getMaskedMaternityRelationships().keySet();
		nullDamIndividuals = new ArrayList<String> ();
		for (Individual ind : haveMaskedDams) {
			nullDamIndividuals.add (ind.getName());
		}
	}
	
	
	@Override
	public void restore (final HeritablePopulation hPop) {
		hPop.unmaskAllGenotypes();
		hPop.unmaskAllIndividuals();
		//hPop.unmaskAllMarkers(); // slow, as turns set of masked markers into list
		hPop.unmaskMarkers (hPop.getMaskedMarkers()); // quicker,as uses sets
		hPop.resetAllToOriginalGenotypes();


		final Collection<SNPMarker> maskedObjMarkers = new HashSet<SNPMarker> ();
		for (String markerName : maskedMarkers) {
			maskedObjMarkers.add (hPop.getMarkerByName (markerName));
		}

		hPop.maskMarkers (maskedObjMarkers); // <- this takes the most time as it loops through all individuals per marker
		//System.err.println ("masked markers: "+hPop.getMaskedMarkers().size());
		
		for (String indName : maskedIndividuals) {
			hPop.maskIndividual (indName);
		}
		
		for (Entry<String, List<String>> genEntry : maskedGenotypesItoM.entrySet()) {
			for (String markerName : genEntry.getValue()) {
				hPop.maskGenotype (genEntry.getKey(), markerName);
			}
		}
		
		for (Individual ind : hPop.getIndividuals()) {
			final HeritableIndividual hInd = (HeritableIndividual)ind;
			final String indName = ind.getName();
			if (hInd.isFatherMasked() && !nullSireIndividuals.contains(indName)) {
				hInd.unmaskFather();
			} else if (!hInd.isFatherMasked() && nullSireIndividuals.contains(indName)) {
				hInd.maskFather ();
			}
			
			if (hInd.isMotherMasked() && !nullDamIndividuals.contains(indName)) {
				hInd.unmaskMother();
			} else if (!hInd.isMotherMasked() && nullDamIndividuals.contains(indName)) {
				hInd.maskMother ();
			}
		}
	}
	
	
	@Override
	public BitSet compare (final HeritablePopulation hPop) {
		// Compare parent states
		final BitSet differences = new BitSet ();
		
		compareNullParentStates (hPop.getMaskedPaternityRelationships().keySet(), nullSireIndividuals, differences, 0);
		compareNullParentStates (hPop.getMaskedMaternityRelationships().keySet(), nullDamIndividuals, differences, 1);
		
		return differences;
	}
	
	
	
	public void compareNullParentStates (final Set<HeritableIndividual> currentNullParentIndividuals,
			final Collection<String> oldNullParentNames, final BitSet differences, final int bitIndex) {
		
		if (currentNullParentIndividuals.size() != oldNullParentNames.size()) {
			differences.set (bitIndex);
		} else {
			final Set<String> currentNullParentNames = new HashSet<String> ();
			for (HeritableIndividual ind : currentNullParentIndividuals) {
				currentNullParentNames.add (ind.getName());
			}
			
			for (String nullParentName : oldNullParentNames) {
				if (! currentNullParentNames.contains (nullParentName)) {
					differences.set (bitIndex);
					break;
				}
			}
		}
	}
	
	
	public String toString () {	
		final StringBuilder strBuilder = new StringBuilder ();
		final Object[] obj = {
			Integer.valueOf (maskedMarkers.size()),
			Integer.valueOf (maskedIndividuals.size()),
			Integer.valueOf (maskedGenotypeCount),
			Integer.valueOf (nullSireIndividuals.size()),
			Integer.valueOf (nullDamIndividuals.size())
		};
		strBuilder.append (mFormat.format (obj));
		return strBuilder.toString();
	}
}
