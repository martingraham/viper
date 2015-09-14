package napier.pedigree.swing;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.filter.HistogramValueFilter;
import swingPlus.histogram.JHistogram;

public abstract class AbstractErrorHistogram extends JHistogram implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -934863140464410579L;
	static final private Logger LOGGER = Logger.getLogger (AbstractErrorHistogram.class);

	protected MessageFormat tooltipTemplateSingle, tooltipTemplateMultiple;
	protected HistogramValueFilter primitiveFilter;
	protected boolean continuousRecalc;
	protected boolean useFilteredData;
	protected boolean snapback;

	public AbstractErrorHistogram () {
		this (SwingConstants.HORIZONTAL);
		setUseFilteredData (false);
		setContinuousUpdate (false);
		setSnapback (false);
	}

	public AbstractErrorHistogram (final int orientation) {
		this (orientation, new int[] {0, 120}, 50);
	}

	public AbstractErrorHistogram (final int orientation, final int[] data, final int value) {
		super (orientation, data, value);
		
		setMajorTickSpacing (10);
		setPaintTicks (true);
		setPaintLabels (true);
		setPreferredSize (new Dimension (getPreferredSize().width, 96));
	}
	
	public AbstractErrorHistogram (final ErrorCollator errorModel) {
		this (SwingConstants.HORIZONTAL);
		setData (errorModel);
	}
	
	
	public void setData (final ErrorCollator errorModel) {
		if (errorModel != null) {
			blockAbstractHistogramListeners (isSnapback());
			setData (getErrorCount (errorModel));
			blockAbstractHistogramListeners (false);
		}
	}
	
	public void setHistogramValueFilter (final HistogramValueFilter histValFilter) {
		primitiveFilter = histValFilter;
	}
	
	public HistogramValueFilter getHistogramValueFilter () {
		return primitiveFilter;
	}
	
	
	public final boolean isContinuousUpdate () {
		return continuousRecalc;
	}

	public final void setContinuousUpdate (final boolean continuousRecalc) {
		this.continuousRecalc = continuousRecalc;
	}
	
	
	public final boolean isUseFilteredData() {
		return useFilteredData;
	}

	public final void setUseFilteredData (final boolean useFilteredData) {
		this.useFilteredData = useFilteredData;
	}
	
	public final boolean isSnapback() {
		return snapback;
	}

	public final void setSnapback (final boolean snapback) {
		this.snapback = snapback;
		if (snapback) {
			setContinuousUpdate (false);
		}
	}
	
	
	public String getToolTipText (final MouseEvent mEvent) {
		final Dimension errorValue = this.getValueFromPos (mEvent);
		final Dimension binRange = this.narrowRangeToBins (errorValue);
		final double errorCount = this.getBinCount (binRange);
		final Object[] args = {Integer.valueOf ((int)Math.round (errorCount)), Integer.valueOf (binRange.width), Integer.valueOf (binRange.height)};
		return (errorCount == 0.0 ? "" : 
			(binRange.width == binRange.height) ? tooltipTemplateSingle.format (args) : tooltipTemplateMultiple.format (args));
	}
	
	
	@Override
	public void propertyChange (final PropertyChangeEvent evt) {
		if (isUseFilteredData() && "filter".equals (evt.getPropertyName()) 
				&& evt.getSource() instanceof ErrorCollator) {
			this.setData ((ErrorCollator)evt.getSource());
		}
	}
	
	
	public void blockAbstractHistogramListeners (final boolean block) {
		final ChangeListener[] listeners = this.getChangeListeners ();
		for (ChangeListener listener : listeners) {
			if (listener instanceof AbstractHistogramListener) {
				((AbstractHistogramListener)listener).setBlocked (block);
			}
			LOGGER.debug ("listener: "+listener+", block: "+block);
		}
	}


	//Abstract methods
	
	abstract public int[] getErrorCount (final ErrorCollator errorModel);
}
