package napier.pedigree.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box.Filler;
import javax.swing.Icon;
import javax.swing.JCheckBox;

import napier.pedigree.io.PropertyConstants;

import util.Messages;



public class HiderButton extends JCheckBox implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1124964263117581900L;
	
	protected final static Icon HIDE_ICON = PedigreeIconCache.makeIcon ("hideOptionsIcon");
	protected final static Icon SHOW_ICON = PedigreeIconCache.makeIcon ("showOptionsIcon");
	
	protected final static String HIDE_OPTIONS_TEXT = Messages.getString (PropertyConstants.TEXTPROPS, "HideOptionsText");
	protected final static String SHOW_OPTIONS_TEXT = Messages.getString (PropertyConstants.TEXTPROPS, "ShowOptionsText");

	public HiderButton() {
		super ();
		setIcon (HIDE_ICON);
		setSelectedIcon (SHOW_ICON);
		setText (this.isSelected());
		setBorderPainted (true);
		addActionListener (this);
	}

	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		final Container parent = this.getParent();
		
		if (parent != null) {
			final boolean isSelected = getModel().isSelected();
			
			int index = -1;
			for (int n = 0; n < parent.getComponentCount() && index < 0; n++) {
				if (parent.getComponent(n) == this) {
					index = n;
				}
			}
			
			for (int n = index + 1; n < parent.getComponentCount(); n++) {
				final Component comp = parent.getComponent(n);
				if (!(comp instanceof Filler)) {
					parent.getComponent(n).setVisible (!isSelected);
				}
			}
			
			setText (isSelected);
		}
	}	
	
	
	public void setText (final boolean isSelected) {
		setText (isSelected ? SHOW_OPTIONS_TEXT : HIDE_OPTIONS_TEXT);
	}
}
