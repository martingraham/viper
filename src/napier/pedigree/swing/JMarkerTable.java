package napier.pedigree.swing;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.SNPMarker;

import util.Messages;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.AbstractErrorModelTableModel;
import napier.pedigree.model.ChangeOnNextRecalcStore;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrixEvent;
import napier.pedigree.model.filter.MarkerFilter;
import napier.pedigree.model.filter.impl.MarkerByMarkerFilter;
import napier.pedigree.model.filter.impl.MaskMarkerOperator;



public class JMarkerTable extends AbstractErrorModelTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1963618302662125906L;
	
	static private final Logger LOGGER = Logger.getLogger (JMarkerTable.class);
	
	

	public JMarkerTable () {
		super ();
		this.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
		this.setColumnSelectionAllowed (true);
	}
	
	@Override
	public void setErrorModel (final ErrorCollator newErrorModel) {
		super.setErrorModel (newErrorModel);
		setModel (new ErrorMarkerTableModel (newErrorModel));
		
		// 'Cos SNPMarker toString() doesn't output the marker's name
		final TableRowSorter<TableModel> trs = new TableRowSorter<TableModel> (getModel());
		trs.setComparator(0, 
			new Comparator<SNPMarker> () {
				@Override
				public int compare (final SNPMarker marker1, final SNPMarker marker2) {
					if (marker1 == null) {
						return (marker2 == null ? 0 : -1);
					}
					if (marker2 == null) {
						return 1;
					}
					return marker1.getName().compareTo (marker2.getName());
				}
			}
		);
		//trs.setSortsOnUpdates (true);
		setRowSorter (trs);
		
	}
	
	public SNPMarker getMarker (final int viewRow) {
		final int modelRow = this.convertRowIndexToModel (viewRow);
		final ErrorMarkerTableModel errorTableModel = (ErrorMarkerTableModel)getModel();
		return errorTableModel.markers.get(modelRow);
	}
	
	
	
	public void setMarkerFilter (final MarkerByMarkerFilter filter) {
		if (getModel() != null) {
			final ErrorMarkerTableModel errorTableModel = (ErrorMarkerTableModel)getModel();
			errorTableModel.setMarkerFilter (filter);
		} else {
			LOGGER.error ("Model is not set yet. Cannot set marker by marker filter till then.");
		}
	}
	
	public MarkerByMarkerFilter getMarkerFilter () {
		final ErrorMarkerTableModel errorTableModel = (ErrorMarkerTableModel)getModel();
		return errorTableModel.getMarkerFilter ();
	}
	
	
	public void setOverallMarkerFilter (final MarkerFilter filter) {
		if (getModel() != null) {
			final ErrorMarkerTableModel errorTableModel = (ErrorMarkerTableModel)getModel();
			errorTableModel.setOverallMarkerFilter (filter);
		} else {
			LOGGER.error ("Model is not set yet. Cannot set overall filter till then.");
		}
	}
	
	public MarkerFilter getOverallMarkerFilter () {
		final ErrorMarkerTableModel errorTableModel = (ErrorMarkerTableModel)getModel();
		return errorTableModel.getOverallMarkerFilter ();
	}
	
	
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (ErrorCollator.FILTER.equals (evt.getPropertyName()) && evt.getSource() instanceof ErrorCollator) {
			repaint ();
		}
		else if (ErrorCollator.RECALCULATED.equals (evt.getPropertyName())) {
			/*
			final int oldMarkers = getModel().getRowCount();
			((ErrorMarkerTableModel)getModel()).expandMarkerList();
			if (oldMarkers != getModel().getRowCount()) {
				((AbstractTableModel)getModel()).fireTableDataChanged();
			}
			*/
			repaint ();
		}
		else if (ErrorCollator.REPAINT_NEEDED.equals (evt.getPropertyName())) {
			repaint ();
		}
	}
}




class ErrorMarkerTableModel extends AbstractErrorModelTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5034460154047285943L;
	private final static String NAME_COLUMN_LABEL = Messages.getString (PropertyConstants.TEXTPROPS, "NameLabel");
	private final static String SELECTION_COLUMN_LABEL = Messages.getString (PropertyConstants.TEXTPROPS, "FilterOutMarkerLabel");
	protected int MARKER_COLUMN = 0, ERROR_COL_START = 1, SELECTION_COLUMN = ErrorCollator.ERROR_RANGE + 1;
	
	protected MarkerByMarkerFilter markerFilter;
	protected MarkerFilter overallMarkerFilter;
	protected List<SNPMarker> markers;
	
	public ErrorMarkerTableModel (final ErrorCollator errorModel) {
		super (errorModel);
		initialiseMarkerList ();
	}
	
	
	@Override
	public int getRowCount() {
		return (markers == null ? 0 : markers.size());
	}

	@Override
	public int getColumnCount() {
		return ErrorCollator.ERROR_RANGE + 2; // 1 for name column, 1 for single marker deselection column
	}

	@Override
	public Object getValueAt (final int rowIndex, final int columnIndex) {
		Object obj = null;
		if (rowIndex >= 0 && rowIndex < markers.size()) {
			final SNPMarker marker = markers.get (rowIndex);
			
			if (marker != null) {
				if (columnIndex == MARKER_COLUMN) {
					obj = marker;
				} else if (columnIndex >= ERROR_COL_START && columnIndex < ErrorCollator.ERROR_RANGE + 1) {
                    //for the column counting incomplete genotypes we could interrogate the
                    //population directly and avoid having the inferred error map
                    obj = Integer.valueOf (errorModel.getCurrentMarkerErrorCount (columnIndex - 1, marker));
				}
				else if (columnIndex == SELECTION_COLUMN) {
					obj = (getMarkerFilter() == null ? false : ! getMarkerFilter().include (marker));
				}
			}
		}
		
		return obj;
	}

	
	@Override
	public String getColumnName (final int columnIndex) {
		if (columnIndex == MARKER_COLUMN) {
			return NAME_COLUMN_LABEL;
		}
		else if (columnIndex >= ERROR_COL_START && columnIndex < ErrorCollator.ERROR_RANGE + 1) {
			return ErrorCollator.ERROR_NAMES [columnIndex - 1];
		}
		else if (columnIndex == SELECTION_COLUMN) {
			return SELECTION_COLUMN_LABEL;
		}
		return "";
	}
	
	
	@Override
    public Class<?> getColumnClass (final int columnIndex) {
    	if (columnIndex == MARKER_COLUMN) {
    		return SNPMarker.class;
    	} else if (columnIndex >= ERROR_COL_START && columnIndex < ErrorCollator.ERROR_RANGE + 1) {
    		return Integer.class;
    	} else if (columnIndex == SELECTION_COLUMN) {
    		return Boolean.class;
    	}
    	return Object.class;
    }

	
    @Override 
    public boolean isCellEditable (final int row, final int column) { 
    	return column == SELECTION_COLUMN;
    } 
    
    
    @Override
    public void setValueAt (final Object aValue, final int rowIndex, final int columnIndex) {
    	if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex == SELECTION_COLUMN) {
    		if (aValue instanceof Boolean) {
    			final boolean curVal = ((Boolean)aValue).booleanValue();
    			final SNPMarker marker = (SNPMarker) getValueAt (rowIndex, MARKER_COLUMN);
    			boolean changed = false;
	    		if (getMarkerFilter() != null) {
	    			if (curVal) {
	    				changed = markerFilter.add (marker);
	    			} else {
	    				changed = markerFilter.remove (marker);
	    			}
	    		}

    			
    			if (changed) {
    				final Set<SNPMarker> testMarkerSet = new HashSet<SNPMarker> ();
    				testMarkerSet.add (marker);
    				final MaskMarkerOperator mmo = new MaskMarkerOperator ();
    				mmo.mask (errorModel, overallMarkerFilter, testMarkerSet);
    				
    				//errorModel.filter();
    				errorModel.setRecalculationNeeded (true, ChangeOnNextRecalcStore.MARKER_CHANGE, testMarkerSet);	
    				//this.repaint();
    			}
    		}
    	}
    }
    
    
    /**
     * First go at building marker list from error data
     */
	protected void initialiseMarkerList () {
		if (errorModel != null) {
			//final Set<SNPMarker> allMarkers = new HashSet<SNPMarker> (errorModel.getPopCheckerContext().getPopulation().getActiveMarkers());
			//allMarkers.addAll (errorModel.getPopCheckerContext().getPopulation().getMaskedMarkers());
			//markers = new ArrayList<SNPMarker> (allMarkers);
			
			markers = new ArrayList<SNPMarker> (errorModel.getPopCheckerContext().getAllMarkers());
			//markers = new ArrayList<SNPMarker> (errorModel.getCurrentAllErrorMap().getMarkerMap().keySet());
		}
	}
	
	
	/**
	 * Subsequent changes to the list we want to append to this initial list
	 * i.e. we want to add markers that suddenly gain errors, but we don't want to remove markers
	 * who have had their errors removed (so we can see which ones we've cleaned)
	 * 
	 * Now not needed as we show all markers at start, not just errored ones
	 */
	protected void expandMarkerList () {
		if (errorModel != null) {
			//final Set<SNPMarker> currentErroredMarkers = new HashSet<SNPMarker> (errorModel.getCurrentAllErrorMap().getMarkerMap().keySet());
			//currentErroredMarkers.removeAll (markers);
			//markers.addAll (currentErroredMarkers);
		}
	}


	
	public void setMarkerFilter (final MarkerByMarkerFilter filter) {
		markerFilter = filter;
	}
	
	public MarkerByMarkerFilter getMarkerFilter () {
		return markerFilter;
	}
	
	public void setOverallMarkerFilter (final MarkerFilter filter) {
		overallMarkerFilter = filter;
	}
	
	public MarkerFilter getOverallMarkerFilter () {
		return overallMarkerFilter;
	}
	
	@Override
	public void errorMatrixChanged (final ErrorMatrixEvent emme) {
		initialiseMarkerList ();
	}
}