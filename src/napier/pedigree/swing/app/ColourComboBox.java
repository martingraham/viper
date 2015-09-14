package napier.pedigree.swing.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import org.apache.log4j.Logger;

import util.GraphicsUtil;
import util.Messages;

import napier.pedigree.io.ErrorStrings;
import napier.pedigree.io.PropertyConstants;
import napier.pedigree.swing.GraphFrame;
import napier.pedigree.swing.renderers.base.ErrorColourableRenderer;

public class ColourComboBox extends JComboBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4715956693025185692L;
	private final static Logger LOGGER = Logger.getLogger (GraphFrame.class);
	protected Collection<ErrorColourableRenderer> renderers;
	
	protected Color[] colourChoices = new Color[] {Color.red, Color.orange, Color.green, Color.cyan, Color.blue, GraphicsUtil.NULLCOLOUR};
	protected String[] colourDescriptors = Messages.getString(PropertyConstants.TEXTPROPS, "ErrorColourComboBoxLabels").split("\\|");
	protected Map<Color, String> descriptionMap = new HashMap <Color, String> ();

	
	public ColourComboBox () {
		super ();
		
		setColourModel (colourChoices, colourDescriptors);
		
		setSelectedIndex (0);
		setAlignmentX (0.0f);
		setMaximumSize (new Dimension (200, 24));
		
		setRenderer (new ColourComboCellRenderer ());
	}
	
	public Collection<ErrorColourableRenderer> getColourableRenderers () { 
		return renderers;
	}
	
	public void setColourableRenderers (final Collection<ErrorColourableRenderer> renderers) {
		this.renderers = renderers;
	}
	
	public void addColourableRenderer (final ErrorColourableRenderer renderer) {
		this.renderers.add (renderer);
	}
	
	public void setColourModel (final Color[] newColourChoices, final String[] newColourDescriptors) {
		if (newColourChoices.length != newColourDescriptors.length) {
			final String errorString = ErrorStrings.getInstance().getString ("matchingArrayLengthError");
			try {
				throw new IndexOutOfBoundsException (errorString);
			} catch (final Exception excep) {
				LOGGER.error (errorString, excep);
			}
			return;
		}
		colourChoices = newColourChoices;
		colourDescriptors = newColourDescriptors;
		
		setDescriptionMap ();
		
		setModel (new DefaultComboBoxModel (colourChoices));
	}

	
	public void setDescriptionMap () {
		descriptionMap.clear ();
		for (int n = 0; n < colourChoices.length; n++) {
			descriptionMap.put (colourChoices [n], colourDescriptors [n]);
		}
	}

	
	class ColourComboCellRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 51565056027622841L;

		public Component getListCellRendererComponent (
				final JList list, final Object value, final int index,
		        final boolean isSelected, final boolean cellHasFocus) {
			super.getListCellRendererComponent (list, value, index, isSelected, cellHasFocus);
				
			/**
			 * Found a bug/side-effect here. 
			 * Swing tries to calculate the necessary max width of a combo box through
			 * getDisplaySize() in BasicComboBoxUI which iterates over all the items in a ComboBoxModel and
			 * sends them to this renderer to calculate the resultant size. 
			 * However getDisplaySize() always calls this function with a index value of -1.
			 * This meant the code below always set the text value as the selected value,
			 * and thus the display size was always calculated using the selected item, regardless of
			 * whether that was the widest item, leading to obvious issues with sizing.
			 * Now it's fixed to use a Map that uses the value Object to reference the correct text rather than
			 * using the index values.
			 */
			
			this.setText (descriptionMap.get (value));
			
			if (index >= 0) {
				this.setBackground ((Color)value);
				//this.setText (colourDescriptors [index]);
			} //else {
				//this.setText (colourDescriptors [getSelectedIndex()]);
			//}
			return this;
	    }
	}
	
	
	abstract class WindowColourUpdateListener implements ActionListener {
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			final JComboBox comboBox = (JComboBox)aEvent.getSource();
			final Color chosenColour = (Color)comboBox.getSelectedItem();

			updateRenderers (chosenColour);
			
			final Window[] windows = Window.getWindows();
			for (Window window : windows) {
				window.repaint();
			}
		}
		
		abstract public void updateRenderers (final Color chosenColour);
	};
}
