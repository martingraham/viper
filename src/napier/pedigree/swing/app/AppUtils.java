package napier.pedigree.swing.app;


import java.awt.Color;
import java.awt.Dimension;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;


import napier.pedigree.util.HTMLLabel;
import napier.pedigree.util.PedigreeIconCache;

import org.apache.log4j.Logger;

import util.Messages;

public final class AppUtils {

	public final static Properties LABELS = Messages.makeProperties ("frameLabel", AppUtils.class, true);	
	// static key name used to store preferences keyname in a JComponent
	// JComponent.putClientProperty (PREF_KEY, prefKey), 
	// Object prefKey = prefs.get (JComponent.getClientProperty (PREF_KEY))
	// prefs.put (prefKey, somePrefValue)
	private final static String PREF_KEY = "prefKey"; 
	
	private final static Logger LOGGER = Logger.getLogger (AppUtils.class);
	private final static Pattern SIZE_PATTERN = Pattern.compile ("\\d+");
	
	
	private static final AppUtils APPUTILS_INSTANCE = new AppUtils ();
	
	private AppUtils () {}

	public static AppUtils getInstance() { return APPUTILS_INSTANCE; }
	
	
	
	public void decorateButton (final AbstractButton button, final String propertyPrefix, final boolean showText) {
		button.setText (showText ? getLabelString (propertyPrefix+"Label") : "");
		final String tooltip = getLabelString (propertyPrefix+"Tooltip");
		button.setToolTipText (tooltip == null ? button.getText() : tooltip);
		final Icon icon = PedigreeIconCache.makeIcon (propertyPrefix+"Icon");
		if (icon != null) {
			button.setIcon (icon);
		}
	}
	
	
	public void decorateButton (final AbstractButton button, final String propertyPrefix) {
		decorateButton (button, propertyPrefix, true);
	}
	

	
	
	public HTMLLabel makeHTMLLabel (final String propertyPrefix, final Color textColour, final boolean showText) {
		return makeHTMLLabel (propertyPrefix, textColour, "", showText);
	}
	
	public HTMLLabel makeHTMLLabel (final String propertyPrefix, final Color textColour, final String extraText, final boolean showText) {
		final HTMLLabel label = new HTMLLabel (showText ? getLabelString (propertyPrefix+"Label") + extraText : "", textColour);
		final String tooltip = getLabelString (propertyPrefix+"Tooltip");
		label.setToolTipText (tooltip == null ? label.getText() : tooltip);
		final Icon icon = PedigreeIconCache.makeIcon (propertyPrefix+"Icon");
		if (icon != null) {
			label.setIcon (icon);
		}
		return label;
	}
	
	
	public JLabel makeLabel (final String propertyPrefix, final Color textColour, final boolean showText) {
		return makeLabel (propertyPrefix, textColour, "", showText);
	}
	
	public JLabel makeLabel (final String propertyPrefix, final Color textColour, final String extraText, final boolean showText) {
		final JLabel label = new JLabel (showText ? getLabelString (propertyPrefix+"Label") + extraText : "");
		label.setForeground (textColour);
		final String tooltip = getLabelString (propertyPrefix+"Tooltip");
		label.setToolTipText (tooltip == null ? label.getText() : tooltip);
		final Icon icon = PedigreeIconCache.makeIcon (propertyPrefix+"Icon");
		if (icon != null) {
			label.setIcon (icon);
		}
		return label;
	}
	
	
	public void populateDimension (final String propertyPrefix, final Dimension dim) {
		if (dim != null) {
			final String sizeString = getLabelString (propertyPrefix+"Size");	
			populateDimension2 (sizeString, dim);
		}
	}
	
	
	public void populateDimension2 (final String sizeString, final Dimension dim) {
    	if (sizeString != null) {
        	final Matcher matcher = SIZE_PATTERN.matcher (sizeString);
        	
        	if (matcher.find()) {
	        	final int width = Integer.parseInt (matcher.group());
	        	
	        	if (matcher.find ()) {
		        	final int height = Integer.parseInt (matcher.group());
		        	LOGGER.debug ("x: "+width+", y: "+height);
		        	dim.setSize (width, height);
	        	}
        	}
	    }
	}
	
	
	public String getLabelString (final String propertyKey) {
		return LABELS.getProperty (propertyKey);
		//return Messages.getString (LABEL_PROPS, propertyKey);
	}
	
	
	public double getDividerLocationAsRatio (final JSplitPane jsp) {
		final int divLoc = jsp.getDividerLocation ();
		final int alongAxis = (jsp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? jsp.getWidth() : jsp.getHeight());
		return (double)divLoc / (double)alongAxis;
	}
	
	
	public String getPrefKeyString (final JComponent jcomp) {
		return getPrefKeyString (jcomp, "");
	}
	
	public String getPrefKeyString (final JComponent jcomp, final String suffix) {
		return jcomp.getClientProperty(PREF_KEY).toString() + suffix;
	}
	
	public void setPrefKeyString (final JComponent jcomp, final String prefKey) {
		jcomp.putClientProperty (PREF_KEY, prefKey);
	}
}
