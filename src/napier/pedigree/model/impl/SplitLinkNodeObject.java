package napier.pedigree.model.impl;

import javax.swing.JTable;

import napier.pedigree.io.PropertyConstants;

import util.Messages;
import util.collections.ArrayListUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SplitLinkNodeObject {

	public final static int UP = 1, DOWN = 2;
	protected int val;
	protected final static String PROPERTIESFILE = PropertyConstants.TEXTPROPS;
	protected MessageFormat toStringFormat = new MessageFormat 
			(Messages.getString (PROPERTIESFILE, "SplitLinkNodeObject.toString"));
	protected final static String SIRES = Messages.getString (PROPERTIESFILE, "Sires");
	protected final static String DAMS = Messages.getString (PROPERTIESFILE, "Dams");
	
	
	public SplitLinkNodeObject (final int val) {
		this.val = val;
	}
	
	public int getVal () { return val; }
	
	
	/**
	 * 	Make a map of Objects to (possibly multiple) column positions for a row;
	 * @param jtable
	 * @param row
	 * @return Map of Object to Column view positions
	 */
	public Map<Object, List<Integer>> getObjectColumnPositions (final JTable jtable, final int row) {
		final Map<Object, List<Integer>> objectColumnMap = new HashMap<Object, List<Integer>> ();

		for (int column = 0; column < jtable.getColumnCount(); column++) {
			final Object obj = jtable.getValueAt (row, column);
			List<Integer> columns = objectColumnMap.get (obj);
			if (columns == null) {
				columns = new ArrayList<Integer> ();
				objectColumnMap.put (obj, columns);
			}
			columns.add (Integer.valueOf (column));
		}
		
		return objectColumnMap;
	}
	
	
	/**
	 * Remove objects from map that only occur in one column
	 * @param objectColumnMap
	 */
	public void removeSingleOccurences (final Map<Object, List<Integer>> objectColumnMap) {
		final Set<Map.Entry<Object, List<Integer>>> entries = objectColumnMap.entrySet ();
		final Iterator<Map.Entry<Object, List<Integer>>> iter = entries.iterator();
		while (iter.hasNext()) {
			final Map.Entry<Object, List<Integer>> entry = iter.next();
			if (entry.getValue().size() == 1) {
				iter.remove();
			}
		}
	}
	
	
	/**
	 * Removes objects from map that occur in consecutive columns only
	 * @param objectColumnMap
	 */
	public void removeEntireContiguousOccurences (final Map<Object, List<Integer>> objectColumnMap) {
		final Set<Map.Entry<Object, List<Integer>>> entries = objectColumnMap.entrySet ();
		final Iterator<Map.Entry<Object, List<Integer>>> iter = entries.iterator();
		
		while (iter.hasNext()) {
			final Map.Entry<Object, List<Integer>> entry = iter.next();
			final List<Integer> columns = entry.getValue();
			
			boolean contiguous = true;
			int lastValue = -1;
			for (int listIndex = 0; listIndex < columns.size() && contiguous; listIndex++) {
				final Integer colObj = columns.get (listIndex);
				final int columnValue = colObj.intValue();
				contiguous = (lastValue == -1 || columnValue == lastValue + 1);
				lastValue = columnValue;
			}
			
			if (contiguous) {
				iter.remove();
			}
		}
	}
	
	
	
	/**
	 * Replaces contiguous occurrences of objects in columns with one representative column
	 * usually the middle one if it can
	 * @param objectColumnMap
	 */
	public void representiseOtherContiguousOccurences (final Map<Object, List<Integer>> objectColumnMap) {
		final Set<Map.Entry<Object, List<Integer>>> entries = objectColumnMap.entrySet ();
		final Iterator<Map.Entry<Object, List<Integer>>> iter = entries.iterator();
		
		while (iter.hasNext()) {
			final Map.Entry<Object, List<Integer>> entry = iter.next();
			final List<Integer> columns = entry.getValue();
			
			boolean actionTaken = false;
			int lastValue = -1;
			for (int listIndex = 0; listIndex < columns.size(); listIndex++) {
				final Integer colObj = columns.get (listIndex);
				final int columnValue = colObj.intValue();
				if (lastValue != -1 && columnValue == lastValue + 1) {
					columns.set (listIndex, null);
					actionTaken = true;
				}
				lastValue = columnValue;
			}
			
			if (actionTaken) {
				ArrayListUtil.removeNulls (columns);
			}
		}
	}
	
	@Override
	public String toString () {
		final Object[] args = new Object [] {(val == UP ? DAMS : SIRES)};
		return toStringFormat.format (args);
	}
}
