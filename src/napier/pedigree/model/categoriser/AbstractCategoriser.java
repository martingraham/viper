package napier.pedigree.model.categoriser;

import java.util.Properties;

import util.Messages;


public abstract class AbstractCategoriser<T> implements Categoriser<T> {

	/**
	 *  Descriptors for specific implementations of this class stored in categoriserNames.properties file, used by toString() method
	 */
	protected final static Properties LABELS = Messages.makeProperties ("categoriserNames", AbstractCategoriser.class, true);
	
	/**
	 *  To be filled in by constructor of specific implementations of this class
	 */
	protected String[] valueDescriptors;
	
	
	public AbstractCategoriser (final String simpleClassName) {
		valueDescriptors = new String [getRange() + 1];
		for (int val = getMinValue(); val <= getMaxValue(); val++) {
			final String descriptor = LABELS.getProperty (simpleClassName + Integer.toString (val));
			valueDescriptors [val - getMinValue()] = descriptor;
		}
	}
	
	
	public final int getRange () {
		return getMaxValue() - getMinValue() + 1;
	}
	
	/**
	 * Make value within range helper function
	 * @param unrestrictedValue
	 * @return a valid value for the categoriser
	 */
	protected int restrict (final int unrestrictedValue) {
		final int min = getMinValue();
		final int max = getMaxValue();
		return ((unrestrictedValue < min) ? min
				: (unrestrictedValue > max) ? max : unrestrictedValue);
	}
	
	/**
	 * Test if unrestrictedValue is within range helper function
	 * @param unrestrictedValue
	 * @return true if within range, false if not
	 */
	protected boolean valid (final int unrestrictedValue) {
		return unrestrictedValue >= getMinValue() && unrestrictedValue <= getMaxValue();
	}
	
	
	public String getDescription (final int value) {
		return valid (value) ? valueDescriptors [value] : null;
	}
	
	public String toString () {
		return LABELS.getProperty (getClass().getSimpleName());
	}
}
