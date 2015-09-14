package napier.pedigree.io;

import io.DataPrep;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import napier.pedigree.util.PedigreeIconCache;

import org.apache.log4j.Logger;

import util.Messages;
import util.XMLConstants2;
import util.swing.MyButton;
import util.swing.MyDialog;



public class ParsingErrorDialog {

	static final private Logger LOGGER = Logger.getLogger (ParsingErrorDialog.class);
	static final Icon TICKICON = PedigreeIconCache.makeIcon ("tickIcon");
	static final String NEWLINE = System.getProperty ("line.separator");
	
	public void showTheseErrors (final JFrame jframe, final Collection<String> parsingErrors, final int maxErrorDisplay) {
		final StringBuilder sBuilder = makeErrorStringBuilder (parsingErrors, maxErrorDisplay);
		
		final int remainingErrors = parsingErrors.size() - maxErrorDisplay;
		if (remainingErrors > 0) {
			final MessageFormat form = new MessageFormat (getLabelString ("parsingErrorDialogChoice"));
			final Number[] testArgs = {Long.valueOf (remainingErrors)};
			final String formatted = form.format (testArgs);
			
			sBuilder.append (formatted);
			LOGGER.info (remainingErrors+" further errors");
		}
		
		final MyDialog dialog = new ErrorDialog (jframe, true, sBuilder.toString(), parsingErrors);
		dialog.setTitle (getLabelString ("parsingErrorDialogTitle"));
		dialog.makeVisible (null);
		//JOptionPane.showMessageDialog (jframe, sBuilder.toString(),
		//		getLabelString ("parsingErrorDialogTitle"), 
		//		JOptionPane.ERROR_MESSAGE);
	}
	
	public String getLabelString (final String propertyKey) {
		final String properties = this.getClass().getPackage().getName().toString() + ".labels";
		return Messages.getString (properties, propertyKey);
	}
	
	
	public StringBuilder makeErrorStringBuilder (final Collection<String> parsingErrors, final int maxErrors) {
		final StringBuilder sBuilder = new StringBuilder (getLabelString ("parsingErrorDialogContent") + NEWLINE);
		final Iterator<String> iter = parsingErrors.iterator();
		int errorCount = 0;
		while (iter.hasNext() && errorCount < maxErrors) {
			errorCount++;
			final String parsingError = iter.next();
			sBuilder.append(parsingError).append(NEWLINE);
			LOGGER.info ("parsing error: "+parsingError);
		}
		return sBuilder;
	}
	
	class ErrorDialog extends MyDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = -316953716136396060L;

		public ErrorDialog (final Frame frame, final boolean modal, final String truncatedErrorStr, 
				final Collection<String> parsingErrors) {
			super (frame, modal);
			this.setUndecorated (false);
			final JTextArea errorTextArea = new JTextArea (truncatedErrorStr);
			errorTextArea.setLineWrap (true);
			errorTextArea.setWrapStyleWord (true);
			getUserPanel().add (new JScrollPane (errorTextArea));
			
			getOptionBox().setLayout (new BoxLayout (this.getOptionBox(), BoxLayout.X_AXIS));
			getOptionBox().add (Box.createHorizontalGlue());
			getOptionBox().add (Box.createHorizontalStrut (12));
			

			final JButton saveErrorButton = new MyButton (getLabelString ("SaveParseErrors"));
			getOptionBox().add (saveErrorButton);
			
			saveErrorButton.addActionListener (
				new ActionListener () {
					@Override
					public void actionPerformed (final ActionEvent aEvent) {
						final JFileChooser jfc = new JFileChooser ();
						final Preferences prefs = Preferences.userNodeForPackage (this.getClass());
						final String lastDir = prefs.get ("LAST_DIR", "");
						if (lastDir.length() > 0) {
							jfc.setCurrentDirectory (new File (lastDir));
						}
						final int retValue = jfc.showSaveDialog (frame);
						if (retValue == JFileChooser.APPROVE_OPTION) {
							final File errorFile = jfc.getSelectedFile ();
							if (errorFile != null) {
								try {
									final StringBuilder allErrorBuilder = makeErrorStringBuilder (parsingErrors, Integer.MAX_VALUE);
									final PrintWriter errorWriter = DataPrep.getInstance().makeBufferedPrintWriter (
											errorFile, XMLConstants2.UTF8, false);
									errorWriter.println (allErrorBuilder.toString());
									errorWriter.flush();
									errorWriter.close();
									saveErrorButton.setText (getLabelString ("ParseErrorsSaved"));
									saveErrorButton.setIcon (TICKICON);
								} catch (final IOException ioe) {
									LOGGER.error (ErrorStrings.getInstance().getString("ParsingErrorFileWriteError"), ioe);
								}
							}
						}
					}
				}
			);
		}
	}
}
