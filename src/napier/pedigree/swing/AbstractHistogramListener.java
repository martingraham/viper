package napier.pedigree.swing;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class AbstractHistogramListener implements ChangeListener {

	protected int lastBottomVal = -1;
	protected int lastTopVal = -1;
	
	protected boolean blocked = false; // used to stop snapping back refiring the listener


	@Override
	public void stateChanged (final ChangeEvent cEvent) {
		//System.err.println (cEvent.getSource().hashCode()+" isblocked: "+isBlocked());
		if (!isBlocked() && cEvent.getSource() instanceof AbstractErrorHistogram) {
			final AbstractErrorHistogram histo = (AbstractErrorHistogram)cEvent.getSource();
			//System.err.println ("isadjusting: "+histo.getModel().getValueIsAdjusting());
			if (!histo.getModel().getValueIsAdjusting() || histo.isContinuousUpdate()) {
				final int bottomVal = histo.getFloorBin (histo.getValue() - 1);
				final int topVal = histo.getFloorBin (histo.getValue() + histo.getExtent() - 1);
	
				if (bottomVal != lastBottomVal || topVal != lastTopVal) {
					doStuff (histo, bottomVal, topVal);
				}
				
				lastBottomVal = bottomVal;
				lastTopVal = topVal;
				
				if (histo.isSnapback()) {
					SwingUtilities.invokeLater (
						new Runnable () {
							@Override
							public void run() {
								AbstractHistogramListener.this.setBlocked (true);	// wrap in enabled switch to stop refiring
								histo.setValue (histo.getMinimum()); // reset to minimum
								AbstractHistogramListener.this.setBlocked (false);
							}
						}
					);

				}
			}
		}
	}
	
	public final boolean isBlocked () {
		return blocked;
	}

	public final void setBlocked (final boolean blocked) {
		this.blocked = blocked;
	}

	abstract public void doStuff (final AbstractErrorHistogram histo, final int curBottomValue, final int curTopValue);
}
