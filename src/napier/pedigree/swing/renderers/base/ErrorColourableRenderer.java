package napier.pedigree.swing.renderers.base;

import java.awt.Color;

public interface ErrorColourableRenderer {
	
	void setErrorColourScale (final Color[] newScale);
	
	void setIncompleteColourScale (final Color[] newScale);
	
	Color[] makeColourScale (final Color baseColour, final int graduations);
}
