package napier.pedigree.model.filter;

/**
 * Filter interface that acts on an marker's error count
 * In situations where we already have this count, or don't have a handle to the Marker anymore (i.e. a histogram)
 * we can use the boolean include (int value) method
 * @author cs22
 *
 */

public interface HistogramValueFilter {
	public boolean include (final int value);
}
