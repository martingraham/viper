package napier.pedigree.model;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

/**
 * Abstract class for an ErrorMatrix that allow registration of listeners for error adding events
 * We don't use the ErrorMatrixEvent / ErrorMatrixListener stuff now though, but it's not hurting anyone being here
 * @author cs22
 *
 */
public abstract class AbstractErrorMatrix implements ErrorMatrix {

	private static final Logger LOGGER = Logger.getLogger (AbstractErrorMatrix.class);
	
	
	
	/** List of listeners */
    protected EventListenerList listenerList = new EventListenerList();
	

	@Override
	public void addError (final SNPMarker marker, final Individual ind) {
		fireErrorMatrixChanged (new ErrorMatrixEvent (this));
	}


	
	
	// Listener Methods
	
    /**
     * Adds a listener to the list that is notified each time a change
     * to the data model occurs.
     *
     * @param	emml		the ErrorMatrixModelListener
     */
    public void addErrorMatrixModelListener (final ErrorMatrixListener emml) {
    	listenerList.add (ErrorMatrixListener.class, emml);
    }

    /**
     * Removes a listener from the list that is notified each time a
     * change to the data model occurs.
     *
     * @param	emml		the ErrorMatrixModelListener
     */
    public void removeErrorMatrixModelListener (final ErrorMatrixListener emml) {
    	listenerList.remove (ErrorMatrixListener.class, emml);
    }

       
    public ErrorMatrixListener[] getErrorMatrixModelListeners () {
        return (ErrorMatrixListener[])listenerList.getListeners (
        		ErrorMatrixListener.class);
    }
    
    
    public void fireErrorMatrixChanged (final ErrorMatrixEvent emme) {
    	// Guaranteed to return a non-null array
    	final Object[] listeners = listenerList.getListenerList();
    	// Process the listeners last to first, notifying
    	// those that are interested in this event
    	for (int i = listeners.length-2; i>=0; i-=2) {
    	    if (listeners[i] == ErrorMatrixListener.class) {
    		((ErrorMatrixListener)listeners[i+1]).errorMatrixChanged (emme);
    	    }
    	}
    }
    
    public <T extends EventListener> T[] getListeners (final Class<T> listenerType) { 
    	return listenerList.getListeners(listenerType); 
    }
}
