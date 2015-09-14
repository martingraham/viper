package napier.pedigree.model.filter;

import org.resspecies.inheritance.model.SNPMarker;

public interface MarkerFilter {

	/**
	 * Simple interface that decides whether or not to include
	 * a marker in operations.
	 * @param marker - Marker object
	 * @return boolean - true to include
	 */
	public boolean include (final SNPMarker marker);
}
