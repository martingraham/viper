package napier.pedigree.model.impl;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.model.categoriser.Categoriser;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritableFamily;
import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.model.Individual;

import util.Messages;



public class DefaultPedigreeGenerationModel extends AbstractTableModel implements PedigreeGenerationModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5568722003777430631L;
	private static final Logger LOGGER = Logger.getLogger (DefaultPedigreeGenerationModel.class);
	
	protected static final String PROPERTYFILE = PropertyConstants.TEXTPROPS;
	public static final String UNKNOWNSIRE = Messages.getString (PROPERTYFILE, "UnknownSire");
	public static final String UNKNOWNDAM = Messages.getString (PROPERTYFILE, "UnknownDam");
	
	protected int maxRows = 12; // Chosen for convenience, allows split children with 7 categories
	// Mostly introduced because DELETEing and INSERTing rows after each new category choice was
	// causing havoc when mixed in with a RowSorter and RowFilter i.e. mucho bugs
	// Now we just keep the row count the same and filter out what we don't want to see.
	
	protected Collection<HeritableFamily> familyCache;
	protected int generationIndex;
	
	protected List<Individual> males;
	protected List<Individual> females;

	protected MultiKeyMap offspringMappedListsCache;
	protected List<MultiKeyMap> splitOffspringMappedListsCaches;
	protected BitSet splitOffspringPresent;	// Tells us if anything is in one of the splitOffspringMappedListsCaches
	
	private boolean femalePolygamy, malePolygamy;
	private Object lastRowPlaceholder, firstRowPlaceholder;
	
	protected Categoriser<HeritableIndividual> categoriser;
	
	

	public DefaultPedigreeGenerationModel (final Collection<HeritableFamily> families, final int generationIndex) {
		super ();
		this.generationIndex = generationIndex;
		make (families);
	}

	
	@Override
	public int getGenerationIndex() {
		return generationIndex;
	}
	
	
	@Override
	public Collection<Individual> getSires() { return males; }
	
	
	@Override
	public Collection<Individual> getDams() { return females; }
	
	
	@Override
	public void make (final Collection<HeritableFamily> families) {
		males = new ArrayList<Individual> ();
		females = new ArrayList<Individual> ();
		offspringMappedListsCache = new MultiKeyMap ();
		splitOffspringMappedListsCaches = new ArrayList<MultiKeyMap> ();
		splitOffspringPresent = new BitSet ();
		lastRowPlaceholder = new SplitLinkNodeObject (SplitLinkNodeObject.UP);
		firstRowPlaceholder = new SplitLinkNodeObject (SplitLinkNodeObject.DOWN);
		familyCache = families;
		
		LOGGER.debug ("generation: "+getGenerationIndex ());
		final StringBuilder debugSB = new StringBuilder ();
		
		// Calculate this generation as a collection of offspring lists
		for (HeritableFamily family : families) {
			
			if (LOGGER.isDebugEnabled()) {
				debugSB.setLength (0);
				debugSB.append ("family: "+family.toString());
			}
			
			final Individual sire = family.getSire ();
			final Individual dam = family.getDam ();
			final Collection<HeritableIndividual> offspringColl = family.getOffspring();
			
			// Dealing with unknown parents (can have a one-parent family but may not be the same unknown parent)
			if (sire == null) {
				final Collection<Individual> unknownSires = makeUnknownParents ("M", offspringColl);
				if (unknownSires != null) {
					for (Individual unknownSire : unknownSires) {
						offspringMappedListsCache.put (unknownSire, dam, new ArrayList<Individual> (unknownSire.getOffspring()));
						
						if (LOGGER.isDebugEnabled()) {
							debugSB.append (",\tSplit Fam: "+unknownSire+"x"+dam+"\t"+unknownSire.getOffspring());
						}
					}
				}
			}
			if (dam == null) {
				final Collection<Individual> unknownDams = makeUnknownParents ("F", offspringColl);
				if (unknownDams != null) {
					for (Individual unknownDam : unknownDams) {
						offspringMappedListsCache.put (sire, unknownDam, new ArrayList<Individual> (unknownDam.getOffspring()));
						
						if (LOGGER.isDebugEnabled()) {
							debugSB.append ("\t"+sire+"x"+unknownDam+"\t"+unknownDam.getOffspring());
						}
					}
				}
			}
			
			// Normal test for two-parent family to add offspring to multikey map
			if (sire != null && dam != null && getOffspring (sire, dam) == null) {
				offspringMappedListsCache.put (sire, dam, new ArrayList<Individual> (offspringColl));
				if (LOGGER.isDebugEnabled()) {
					debugSB.append ("\t"+sire+"x"+dam+"\t"+offspringColl);
				}
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug (debugSB.toString());
			}

		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug (offspringMappedListsCache.toString());
			LOGGER.debug ("columnCount: "+getColumnCount());
		}
		
		// The offspring lists are the central row in this table and from this we calculate
		// parent row combinations etc
		final Set<Map.Entry> entries = offspringMappedListsCache.entrySet();
		for (Map.Entry entry : entries) {
			final MultiKey multiKey = (MultiKey)entry.getKey();
			final Individual sire = (Individual)multiKey.getKey (0);
			final Individual dam = (Individual)multiKey.getKey (1);
			males.add (sire);
			females.add (dam);
		}
		
		//setCategoriser (categoriser == null ? new OffspringCategoriser () : categoriser);
		
		//MapUtils.verbosePrint (System.out, null, offspringMappedListsCache);

		// Calculate polygamy states
		malePolygamy = calcGenderPolygamy (males);
		femalePolygamy = calcGenderPolygamy (females);
	}
	
	
	@Override
	public void remake () {
		make (familyCache);
	}
	
	
	
	@Override
	public void setCategoriser (final Categoriser<HeritableIndividual> newCategoriser) {
		if (categoriser == null || (! categoriser.equals (newCategoriser))) {
			final int oldRows = (categoriser == null ? 0 : categoriser.getRange()) + ModelRowConstants.OFFSPRING_SPLIT_START - 1;
			final int newRows = (newCategoriser == null ? 0 : newCategoriser.getRange()) + ModelRowConstants.OFFSPRING_SPLIT_START - 1;
				
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug ("old rows: "+oldRows+", new rows: "+newRows);
				
				final TableModelListener[] tmls = this.getListeners (TableModelListener.class);
				for (TableModelListener tml : tmls) {
					LOGGER.debug ("tml: "+tml.toString());
				}
				LOGGER.debug("----");
			}
			
			// If necessary, fire a table row deletion event so the associated JTable removes rows from its row height size cache
			//if (oldRows > newRows) {
			//	this.fireTableRowsDeleted (newRows + 1, oldRows);
			//}


			categoriser = newCategoriser;			
			splitOffspring (categoriser);	
			
			// Again if necc, fire a table insertion event so the associated JTable adds a requisite number of rows to its row height size cache
			//if (newRows > oldRows) {
			//	this.fireTableRowsInserted (oldRows + 1, newRows);
			//}
			
			//if (newRows == oldRows) {
			//	this.fireTableRowsUpdated (ModelRowConstants.OFFSPRING_SPLIT_START, newRows);
			//}
			
			// The deletion and insertion are done respectively before and after the change in the categoriser object, as it's this object
			// the tablemodel bases it's row count on. Doing it the other way, the deletion will try to delete rows the model's new rowcount 
			// doesn't go up to (= ERROR) or the insertion would try to add rows the model's old rowcount doesn't go up to either (again, = ERROR).

			// Alternatively the whole thing can be done with a tablemodel update event, which relies on the called JGeneration caching and restoring
			// table row heights properly. Except NO IT CAN'T, because then the restore selection goes fubar if we've accidentally selected a row in the rowheader
			// that has a higher index than the new set of rows.
			
			//if (newRows != oldRows) {
				//this.fireTableRowsUpdated (ModelRowConstants.OFFSPRING_SPLIT_START, Integer.MAX_VALUE);
				//this.fireTableRowsUpdated (ModelRowConstants.OFFSPRING_SPLIT_START, Integer.MAX_VALUE);
				//this.fireTableDataChanged();
			//}
		}
	}
	
	
	@Override
	public Categoriser<HeritableIndividual> getCategoriser () {
		return categoriser;
	}
	
	@Override
	public void splitOffspring () {
		splitOffspring (getCategoriser());
	}
	
	public void splitOffspring (final Categoriser<HeritableIndividual> categoriser) {
		splitOffspring (offspringMappedListsCache, categoriser);
		//this.fireTableDataChanged();
		
	}
	
	
	/**
	 * Split the MultiKeyMap of Sire/Dam/Offspring into a number of submaps dependent on a categorisation
	 * The number of lists depends on the category operator to be applied
	 *
	 *											Sire/Dam combo1   					Sire/Dam combo2 etc etc
	 * MultiKeyMap for category value 0:	  <sire, dam, list<children>>		<sire, dam, list<children>>
	 * MultiKeyMap for category value 1:										<sire, dam, list<children>>
	 * MultiKeyMap for category value 2:	  <sire, dam, list<children>>
	 * etc
	 */
	protected void splitOffspring (final MultiKeyMap offspringMappedLists, final Categoriser<HeritableIndividual> categoriser) {
			
		splitOffspringMappedListsCaches.clear ();
		splitOffspringPresent.clear ();
			
		if (categoriser != null) {
			final int size = categoriser.getRange();
			
			for (int listCount = size; --listCount >= 0;) {
				splitOffspringMappedListsCaches.add (new MultiKeyMap ());
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug ("Categoriser: "+categoriser+", range: "+size);
				LOGGER.debug ("Extra rows: "+splitOffspringMappedListsCaches.size());
			}
			// Run through the entries in the main offspring map of lists and
			// process them into the split lists.
			final Set<Map.Entry<MultiKey, List<HeritableIndividual>>> entries = offspringMappedLists.entrySet();
			for (Map.Entry<MultiKey, List<HeritableIndividual>> entry : entries) {
				final MultiKey key = entry.getKey();
				final List<HeritableIndividual> offspring = entry.getValue();
				final Individual sire = (Individual) key.getKey (0);
				final Individual dam = (Individual) key.getKey (1);
				splitOffspring (sire, dam, offspring, categoriser);
			}
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug ("BS: "+splitOffspringPresent);
		}
		//MapUtils.verbosePrint (System.out, null, offspringMappedLists);
	}
	
	
	protected void splitOffspring (final Individual sire, final Individual dam, final Collection<HeritableIndividual> offspring, 
			final Categoriser<HeritableIndividual> categoriser) {

		final int size = categoriser.getRange();
		final List<List<HeritableIndividual>> splitLists = new ArrayList<List<HeritableIndividual>> ();
		for (int listCount = size; --listCount >= 0;) {
			splitLists.add (new ArrayList<HeritableIndividual> ());
		}
		
		for (HeritableIndividual child : offspring) {
			final int category = categoriser.categorise (child);
			splitLists.get(category).add (child);
		}
		
		for (int listCount = size; --listCount >= 0;) {
			final List<HeritableIndividual> splitOffspringList = splitLists.get(listCount);
			splitOffspringMappedListsCaches.get(listCount).put (sire, dam, splitOffspringList);
			if (!splitOffspringList.isEmpty()) {
				splitOffspringPresent.set (listCount);
			}
		}
	}
	
	
	
	protected Collection<Individual> makeUnknownParents (final String gender, final Collection<HeritableIndividual> offspring) {
		
		final Collection<Individual> unknownParents = new ArrayList<Individual> ();
		if (offspring != null && !offspring.isEmpty()) {
			for (HeritableIndividual child : offspring) {
				final HeritableIndividual hInd = new GhostHeritableIndividual (); 
				hInd.setName ("M".equals(gender) ? UNKNOWNSIRE : UNKNOWNDAM);
				hInd.setGender (gender);
				hInd.setPopulation (child.getPopulation());
				// Make a unique accession (used for hashcode, if left to default unknown parents overwrite each other in collections)
				hInd.setAccession (hInd.getName()+child.getName());
				
				hInd.getOffspring().add(child);
				//hInd.addOffspring (child);	// Why does this not work?
				unknownParents.add (hInd);
				//System.err.println ("hind: "+hInd+", off: "+hInd.getOffspring());
			}
		}
		
		return unknownParents;
	}
	
	
	@Override
	public boolean areSplitOffspringPresent (final int categoryValue) {
		return splitOffspringPresent.get (categoryValue);
	}
	
	
	
	
	protected String familyToString (final HeritableFamily hFamily) {
		return hFamily.toString()+"\tsire: "+hFamily.getSire()+"\tdam: "+hFamily.getDam();
	}
	


	
	protected List<Individual> getOffspring (final Individual male, final Individual female) {
		return (List<Individual>) offspringMappedListsCache.get (male, female);
	}
	
	

	@Override
	public Integer getRowPriority (final int modelRow) {
		return (modelRow == ModelRowConstants.DAM || modelRow == ModelRowConstants.DAM_SPLITS ? Integer.valueOf (0) : Integer.valueOf (1));
	}
	
	
	// overridden TableModel interface methods
	
	@Override
	public int getRowCount() {
		return maxRows;
	}

	@Override
	public int getColumnCount() {
		return offspringMappedListsCache.size();
	}
	

	@Override
	public Object getValueAt (final int rowIndex, final int columnIndex) {
		
		if (columnIndex >= 0) {
			final Individual male = males.get (columnIndex);
			final Individual female = females.get (columnIndex);
			switch (rowIndex) {
				case ModelRowConstants.SIRE_SPLITS:
					return firstRowPlaceholder;
					
				case ModelRowConstants.SIRE:
					return male;
					
				case ModelRowConstants.OFFSPRING:
					return getOffspring (male, female);
					
				case ModelRowConstants.DAM:
					return female;
					
				case ModelRowConstants.DAM_SPLITS:
					return lastRowPlaceholder;
					
				default:
					//System.err.println ("column size: "+this.getColumnCount());
					//System.err.println ("row size: "+this.getRowCount());
					//System.err.println ("Extra rows: "+splitOffspringMappedListsCaches.size());
					//System.err.println ("index: "+(rowIndex - ModelRowConstants.OFFSPRING_SPLIT_START));
					final int splitIndex = rowIndex - ModelRowConstants.OFFSPRING_SPLIT_START;
					if (splitIndex >= splitOffspringMappedListsCaches.size()) {
						return null;
					}
					return (List<Individual>) splitOffspringMappedListsCaches.get(splitIndex).get (male, female);
			}
		}
		return null;
	}
	
	
	// PolygamyStateModel interface methods
	
	@Override
	public boolean isMalePolygamous () {
		return malePolygamy;
	}
	
	@Override
	public boolean isFemalePolygamous () {
		return femalePolygamy;
	}
	
	@Override
	public boolean isPolygamous () {
		return isFemalePolygamous() || isMalePolygamous ();
	}
	
	@Override
	public boolean isDualPolygamous () {
		return isFemalePolygamous() && isMalePolygamous ();
	}
	
	
	public boolean calcGenderPolygamy (final Iterable<Individual> siresOrDams) {
		
		boolean polygamous = false;
		final Collection<Individual> inPairingsSoFar = new HashSet<Individual> ();
		final Iterator<Individual> iter = siresOrDams.iterator();
		
		while (iter.hasNext() && !polygamous) {
			final Individual parent = iter.next();
			polygamous = inPairingsSoFar.contains (parent);
			inPairingsSoFar.add (parent);
		}
		
		return polygamous;
	}
}
