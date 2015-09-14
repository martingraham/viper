package napier.pedigree.util;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;

import util.colour.ColorUtilities;

public class HTMLLabel extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4446909205948484375L;

	public final static String HTML_START = "<HTML><BODY>";
	public final static String HTML_END = "</BODY></HTML>";
	
	public HTMLLabel (final String text, final Color colour) {
		super ();
		this.setText (makeHTMLText (text, colour));
	}

	@Override
	// Because labels with HTML in them return Integer.MAX_VALUE as their max width / height
	public Dimension getMaximumSize () {
		return getPreferredSize();
	}
	
	
	
	static public String makeHTMLText (final String text, final Color colour) {
		final StringBuilder strBuilder = new StringBuilder ();
		strBuilder.append (HTML_START).append("<B>");

		if (colour != null) {
			strBuilder.append ("<font color=\"").append(ColorUtilities.toHTMLHexString(colour)).append("\">");
		}
		strBuilder.append (text);
		if (colour != null) {
			strBuilder.append ("</font>");
		}
		strBuilder.append ("</B>").append(HTML_END);
		
		return strBuilder.toString();
	}
}
