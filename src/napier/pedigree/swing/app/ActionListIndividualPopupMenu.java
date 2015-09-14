package napier.pedigree.swing.app;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.resspecies.model.Individual;

import napier.pedigree.swing.JGeneration;
import napier.pedigree.swing.JIndividualTable;
import napier.pedigree.swing.app.actions.AbstractFamilyCentricAction;
import napier.pedigree.swing.renderers.base.MultipleItemsRenderer;

import util.collections.SingleItemSet;



public class ActionListIndividualPopupMenu extends ActionListPopupMenu implements PedigreeSelectionSource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5676495999699383318L;

	private static final Logger LOGGER = Logger.getLogger (ActionListIndividualPopupMenu.class);
	
	private JGeneration selectedGeneration;
	private Collection<Individual> selectedGroup;
	private SingleItemSet<Individual> selectedIndividual;	// when we just want to show a tooltip for an individual

    
    public ActionListIndividualPopupMenu (final int activeButton) {
  		
        super (activeButton);
		
		selectedIndividual = new SingleItemSet<Individual> ();
	}

	
	
	protected boolean setDetails (final Component comp, final Point mouseCoord) {
		if (comp instanceof JGeneration) {
			final JGeneration jgen = (JGeneration)comp;
			final int row = jgen.rowAtPoint (mouseCoord);
			final int column = jgen.columnAtPoint (mouseCoord);
			//final int modelRow = jgen.convertRowIndexToModel (row);
			
			if (column >= 0 /* && (modelRow == ModelRowConstants.OFFSPRING || modelRow >= ModelRowConstants.OFFSPRING_SPLIT_START )*/) {
				final Object value = jgen.getValueAt (row, column);
				LOGGER.debug ("cell value at popup: "+value);
				final TableCellRenderer cellRenderer = jgen.getDefaultRenderer (value.getClass());
				
				selectedGroup = null;
				selectedIndividual.clear ();
				
		    	if (value instanceof Individual) {
		    		selectedIndividual.add ((Individual)value);
		    		selectedGroup = selectedIndividual;
		    	}
		    	else if (value instanceof Collection && !((Collection)value).isEmpty()) {
		    		selectedGroup = ((Collection<Individual>)value);
			    	if (selectedGroup.size() > 1 && cellRenderer instanceof MultipleItemsRenderer) {
				    	selectedIndividual = (SingleItemSet<Individual>)isolateSubCellIntoValue (jgen, row, column, selectedGroup, (MultipleItemsRenderer)cellRenderer);
			    	}
			    	else if (selectedGroup.size() == 1) {
			    		selectedIndividual.addAll (selectedGroup);
			    	} //else {
			    	//	selectedIndividual.clear ();
			    	//}
		    	}
		    	

		    	
		    	if (LOGGER.isInfoEnabled()) {
		    		LOGGER.info ("selected group: "+selectedGroup);
		    		final Iterator<Individual> iter = selectedIndividual.iterator();
		    		final Individual ind = (iter.hasNext() ? iter.next() : null);
		    		LOGGER.info ("selected individual: "+ind == null ? "empty" : ind);
		    		if (ind != null) {
		    			LOGGER.info ("ind father: "+ind.getSire());
		    		}
		    	}
		    	
				if (selectedGroup != null) {
					selectedGeneration = jgen;
			    	// Update the popup's menu item labelling given context of current selection
			    	updateMenuItemActions ();
			    	title.setText (makeTitle());
					return true;
				}
			}
		}
		
		else if (comp instanceof JIndividualTable) {
			final JIndividualTable jIndTable = (JIndividualTable)comp;
			final int row = jIndTable.rowAtPoint (mouseCoord);
			//final int column = jIndTable.columnAtPoint (mouseCoord);
			
			final Individual ind = jIndTable.getIndividual (row);
			if (ind != null) {
				selectedGroup = null;
				selectedIndividual.clear ();
				selectedIndividual.add (ind);
				selectedGroup = selectedIndividual;
				selectedGeneration = null;
		    	// Update the popup's menu item labelling given context of current selection
		    	updateMenuItemActions ();
		    	title.setText (makeTitle());
				return true;
			}
			
		}
		return false;
	}
	
	
	protected String makeTitle () {
		if (selectedGroup != null && !selectedGroup.isEmpty()) {
			final Individual firstIndividual = selectedGroup.iterator().next();
			
			if (selectedIndividual != null && selectedIndividual.size() == 1) {
				return selectedIndividual.iterator().next().getName();
			}
			
			if (selectedGroup.size() == 1) {
				return firstIndividual.getName();
			}
			
			if (selectedGroup.size() == firstIndividual.getFamily().getMembers().size() - 2) {
				return firstIndividual.getFamily().getName();
			}
		
			return selectedGroup.size()+" Individuals";
		}
		
		return "???";
	}
	
	/**
	 * Iterates through the popup's components, looking for menu items that hold
	 * FamilyCentricActions. Then it calls the FamilyCentricActions's updateAction.
	 * This is to update the labelling mainly.
	 * e.g. a mask action will present a label knowing whether the individual concerned is
	 * currently masked or not.
	 * The similar isWorthwhileAction calls decide whether or not each action is plausible/
	 * possible/necessary etc given the current selection. If not, we usually just hide these.
	 */
	protected void updateMenuItemActions () {
    	final Component[] menuList = this.getComponents();
    	
    	for (Component menuComp : menuList) {
    		if (menuComp instanceof JMenuItem) {
    			final JMenuItem jmi = (JMenuItem)menuComp;
    			final Action action = jmi.getAction();
    			
    			if (action instanceof AbstractFamilyCentricAction) {
    				final AbstractFamilyCentricAction familyAction = (AbstractFamilyCentricAction)action;
    				familyAction.updateAction (this);
    			}
    		}
    	}
    	
    	for (Component menuComp : menuList) {
    		if (menuComp instanceof JMenuItem) {
    			final JMenuItem jmi = (JMenuItem)menuComp;
    			final Action action = jmi.getAction();
    			
    			if (action instanceof AbstractFamilyCentricAction) {
    				jmi.setVisible (((AbstractFamilyCentricAction)action).isWorthwhileAction (this));
    			}
    		}
    	}
	}
	
	
	
    /**
     * If tooltip is over a table cell drawn with a renderer that displays a set of objects,
     * then make the tooltip render the individual object that the mouse pointer is currently over.
     * This needs the table to be drawn with a GenerationTableUI to work successfully.
     * @param table	- JTable
     * @param row	- row of cell
     * @param column	- column of cell
     * @param spaceFiller	- the set renderer of the table cell the tooltip is to provide info for
     */
    protected Collection<Individual> isolateSubCellIntoValue (final JTable table, final int row, final int column, 
    		final Collection<Individual> group, final MultipleItemsRenderer spaceFiller) {
		
		// Convert table mouse point to cell point
		final Point rendererMouseOffset = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen (rendererMouseOffset, table);
		final Rectangle cellBounds = table.getCellRect (row, column, false);
		rendererMouseOffset.translate (-cellBounds.x, -cellBounds.y);
		
		selectedIndividual.clear ();
		
		// Isolate the object in the set the mouse is currently over
		final int objectIndex = spaceFiller.getObjectIndexAt (rendererMouseOffset, cellBounds.width,
				table, row, column);

		// Push that object into a single item set instance and make that the focus of the tooltip
		if (objectIndex >= 0 && objectIndex < group.size()) {	
			final Iterator<Individual> iter = group.iterator();
			Object obj = null;
			int index = 0;
			while (iter.hasNext() && index <= objectIndex) {
				index++;
				obj = iter.next();
			}
    		selectedIndividual.add ((Individual)obj);
		}

		return selectedIndividual;
    }


	// FamilySelectionSource interface
	// i.e. this class is a source of user selections on the pedigree
	@Override
	public Individual getSelectedIndividual() {
		return (selectedIndividual == null || selectedIndividual.isEmpty() ? null : selectedIndividual.iterator().next());
	}
	
	
	
	@Override
	public Collection<Individual> getSelectedGroup() {
		return selectedGroup;
	}


	@Override
	public JGeneration getSelectedJGeneration() {
		return selectedGeneration;
	}
}