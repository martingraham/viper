package napier.pedigree.io;

import java.text.MessageFormat;
import java.util.Properties;

import napier.pedigree.swing.app.AppUtils;

import util.Messages;


public final class ErrorStrings {
	
	public final static Properties LABELS = Messages.makeProperties ("errorStrings", AppUtils.class, true);	
	
	
	private static final ErrorStrings ERRORSTRINGS_INSTANCE = new ErrorStrings ();
	
	private ErrorStrings () {}

	public static ErrorStrings getInstance() { return ERRORSTRINGS_INSTANCE; }
	
	public String getString (final String key) {
		return LABELS.getProperty (key, "Error Message missing for key: "+key);
	}
	
	public String getString (final String key, final Object[] values) {
		final MessageFormat mFormat = new MessageFormat (
				LABELS.getProperty (key, "Error Message missing for key: "+key));
		return mFormat.format (values);
	}
}
