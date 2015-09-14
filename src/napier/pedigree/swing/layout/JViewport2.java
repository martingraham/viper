package napier.pedigree.swing.layout;

import java.awt.Dimension;

import javax.swing.JViewport;

public class JViewport2 extends JViewport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -951837299548456211L;

	/**
	 * Fixes the JViewport preferred height to be never bigger than the component it holds
	 */
	@Override
    public Dimension getPreferredSize() {
		final Dimension dim = super.getPreferredSize();
		final Dimension viewPref = this.getView().getPreferredSize();
        dim.height = viewPref.height;
        return dim;
    }
	
	@Override
    public Dimension getMaximumSize() {
		final Dimension dim = super.getMaximumSize();
		final Dimension viewMax = this.getView().getMaximumSize();
        dim.height = viewMax.height;
        return dim;
    }
}
