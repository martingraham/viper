package napier.pedigree.io;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import napier.pedigree.util.SpringUtilities;

import org.apache.log4j.Logger;



public class JMultiFileChooser extends JFileChooser {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1011460982330672167L;

	static final private Logger LOGGER = Logger.getLogger (JMultiFileChooser.class);
	
	public final static String ALL_SLOTS_FILLED = "AllSlotsFilled";
	
	private Preferences prefs;
	protected int fileCount;
	protected File[] chosenFiles;
	protected JTextField[] textFields;
	protected String[] labelKeys;
	protected String toolTipTemplate;
	protected Properties labelProperties;

	
	public JMultiFileChooser (final int fileCount, final String[] newLabelKeys, final Properties labelProps) {
		this (null, fileCount, newLabelKeys, labelProps);
	}
	
	public JMultiFileChooser (final File file, final int fileCount, final String[] newLabelKeys, final Properties labelProps) {
		super (file);
		
		this.fileCount = fileCount;
		chosenFiles = new File [fileCount];
		this.labelKeys = newLabelKeys;
		this.labelProperties = labelProps;
		
		prefs = Preferences.userNodeForPackage (this.getClass());
		final String lastDir = prefs.get ("LAST_DIR", "");
		if (lastDir.length() > 0) {
			this.setCurrentDirectory (new File (lastDir));
		}
		
		final String lastFile = prefs.get ("LAST_FILE", "");
		if (lastFile.length() > 0) {
			this.setSelectedFile (null);
			this.setSelectedFile (new File (lastFile));
		}
	}
	
	public void setup () {
		
		setupAccessory ();

		for (int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
			final String chosenFile = prefs.get (labelKeys [fileIndex], null);
			if (chosenFile != null && chosenFile.length() > 0) {
				setChosenFile (new File (chosenFile), fileIndex);
			}
		}
	}
	

	public void setToolTipTemplate (final String newToolTipTemplate) {
		toolTipTemplate = newToolTipTemplate;
	}
	
	
	public void setupAccessory () {
		setOverriddenData ();
		
		final JPanel accessoryPanel = new JPanel (new BorderLayout ());
		final JPanel buttonPanel = new JPanel (new SpringLayout ());
		final JButton[] arrowButtons = new JButton [fileCount];
		textFields = new JTextField [fileCount];
		final MessageFormat tooltipFormat = new MessageFormat (toolTipTemplate);
		final Object[] tooltipArgs = new Object [1];
		
		for (int i = 0; i < fileCount; i++) {
			final JButton button = new BasicArrowButton (SwingConstants.EAST);
			button.setBorder (BorderFactory.createEtchedBorder());
			final String labelText = labelProperties.getProperty (labelKeys [i]);
			tooltipArgs [0] = labelText;
			button.setToolTipText (tooltipFormat.format(tooltipArgs));
			arrowButtons [i] = button;
			
		    final JTextField textField = new JTextField (25); 
		    textField.setEditable (false);
		    textFields [i] = textField;

		    final JLabel label = new JLabel (labelText, JLabel.TRAILING);
		    label.setLabelFor (textField);
		    
		    final JPanel boxPanel = new JPanel ();
		    boxPanel.setLayout (new BoxLayout (boxPanel, BoxLayout.Y_AXIS));
		    final JPanel textFieldPanel = new JPanel (new BorderLayout ());
		    textFieldPanel.add (button, BorderLayout.WEST);
		    textFieldPanel.add (textField, BorderLayout.CENTER);
		    boxPanel.add (label);
		    boxPanel.add (textFieldPanel);
		    
		    buttonPanel.add (boxPanel);
		    
		    final int loopCount = i;
		    
		    final ActionListener buttonListener = new ActionListener () {

				@Override
				public void actionPerformed (final ActionEvent event) {
					File file = JMultiFileChooser.this.getSelectedFile();
					if (file != null) {
						setChosenFile (file, loopCount);
					}
				}	    	
		    };
		    
		    button.addActionListener (buttonListener);
		}
	


		SpringUtilities.makeCompactGrid (buttonPanel,
				fileCount, 1, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
		
		accessoryPanel.add (buttonPanel, BorderLayout.NORTH);
		
		final PropertyChangeListener buttonEnabler = new PropertyChangeListener () {
			@Override
			public void propertyChange (final PropertyChangeEvent evt) {
				for (JButton button : arrowButtons) {
					button.setEnabled (evt != null && evt.getNewValue() != null);
				}
			}
		};
		addPropertyChangeListener (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, buttonEnabler);

		
		final PropertyChangeListener genPropListener = new PropertyChangeListener () {
			@Override
			public void propertyChange (final PropertyChangeEvent evt) {
				LOGGER.debug ("Current Dir: "+JMultiFileChooser.this.getCurrentDirectory());
				prefs.put ("LAST_DIR", JMultiFileChooser.this.getCurrentDirectory().getAbsolutePath());
				if (JMultiFileChooser.this.getSelectedFile() != null) {
					prefs.put ("LAST_FILE", JMultiFileChooser.this.getSelectedFile().getAbsolutePath());
				}

				for (int fileIndex = 0; fileIndex < fileCount; fileIndex++) {
					final File chosenFile = getChosenFile (fileIndex);
					if (chosenFile != null) {
						prefs.put (labelKeys [fileIndex], chosenFile.getAbsolutePath());		
					}
				}
			}
		};
		addPropertyChangeListener ("JFileChooserDialogIsClosingProperty", genPropListener);
		
		
		final PropertyChangeListener enableOpenButtonListener = new PropertyChangeListener () {
			@Override
			public void propertyChange (final PropertyChangeEvent evt) {
				
				//JFileChooser.this.setApproveButtonText(approveButtonText)
			}
		};
		addPropertyChangeListener (ALL_SLOTS_FILLED, enableOpenButtonListener);
		
		this.setAccessory (accessoryPanel);
		
		buttonEnabler.propertyChange (new PropertyChangeEvent (this, JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, null, this.getSelectedFile()));
	}
	
	
	public void setOverriddenData () {
		/* EMPTY */
	}
	
	
	public Properties getLabelProperties () {
		return labelProperties;
	}
	
	public File getChosenFile (final int index) {
		return (index >= 0 && index < chosenFiles.length) ? chosenFiles [index] : null;
	}
	
	public void setChosenFile (final File chosenFile, final int index) {
		final boolean oldAllSlotsFull = testAllSlotsFull ();
		chosenFiles [index] = chosenFile;
		textFields[index].setText (chosenFile == null ? "" : chosenFile.getAbsolutePath());
		final boolean newAllSlotsFull = testAllSlotsFull ();

		this.firePropertyChange (ALL_SLOTS_FILLED, oldAllSlotsFull, newAllSlotsFull);
	}
	
	public boolean testAllSlotsFull () {
		boolean allFull = true;
		for (int i = 0; i < chosenFiles.length; i++) {
			if (getChosenFile (i) == null) {
				allFull = false;
				break;
			}
		}
		return allFull;
	}
}
