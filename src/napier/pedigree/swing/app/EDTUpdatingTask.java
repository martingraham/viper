package napier.pedigree.swing.app;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import util.swing.BlockingDialog;

/**
 * Helper class that takes in a SwingWorker, JFrame and a bunch of Strings
 * The JFrame's user input is blocked via the glasspane while the task runs.
 * Periodically, and this should be done in the SwingWorker, the "progress" property
 * is fired with an integer value corresponding to the text string array to show what
 * stage the task is at to the user.
 * 
 * @author cs22
 *
 */
public class EDTUpdatingTask {

	protected SwingWorker<?, ?> sWorker;
	protected Frame frame;
	protected String[] phaseText;
	protected Color glassPaneTint = new Color (255, 255, 0, 32);
	
	public EDTUpdatingTask (final Frame frame, final SwingWorker<?, ?> sWorker, final String[] phaseText) {
		this.sWorker = sWorker;
		this.frame = frame;
		this.phaseText = phaseText;
	}
	
	
	public EDTUpdatingTask setGlassPaneColour (final Color newColor) {
		glassPaneTint = newColor;
		return this;
	}
	
	public void doIt () {
		
		final BlockingDialog blockingDialog = new BlockingDialog (frame, glassPaneTint);
	
		
		/**
		 * Run the SwingWorker which loads the data and updates the interface as above
		 */
		sWorker.addPropertyChangeListener(
			new PropertyChangeListener () {
			    public void propertyChange (final PropertyChangeEvent evt) {
			        if ("progress".equals (evt.getPropertyName())) {
			        	final int progress = (Integer) evt.getNewValue();
			            blockingDialog.setProgressText (phaseText [progress]);
			        } 
			    }
			}
		);
		
		frame.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
		final javax.swing.Timer visTimer = new javax.swing.Timer (250, blockingDialog.getCentreDialogActionListener());
		visTimer.start();
		sWorker.addPropertyChangeListener (blockingDialog.getDisposeDialogListener());
		//System.err.println ("launch background executing sworker on edt: "+SwingUtilities.isEventDispatchThread());
		sWorker.execute();
		//the dialog will be visible until the SwingWorker is done
		//System.err.println ("\n***\nBLOCKING DIALOG "+blockingDialog.hashCode()+" ACTIVE\n***\n");
		blockingDialog.setVisible (true); // cannot use gui till swingworker finished
		//attachModelsToInterface (newGenotypeParser);
		//System.err.println ("\n***\nBLOCKING DIALOG "+blockingDialog.hashCode()+" REMOVED\n***\n");
		visTimer.stop();
		frame.setCursor (null);
	}
}
