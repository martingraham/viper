package napier.pedigree.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import util.GraphicsUtil;

public class JCollapsiblePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2486116116443072247L;
	
	protected final static Icon COLLAPSE_ICON = UIManager.getIcon ("Tree.collapsedIcon");
	protected final static Icon EXPAND_ICON = UIManager.getIcon ("Tree.expandedIcon");
	
	protected JComponent contents, header;
	protected JButton collapseButton;
	protected JPanel headerPanel;
	protected ComponentAdapter visListener;

	
	public JCollapsiblePanel (final JComponent newHeader, final JComponent newContents) {
		this ();
		setContents (newContents);
		setHeader (newHeader);
	}
	
	
	public JCollapsiblePanel () {
		super ();
		
		setLayout (new BorderLayout ());
		collapseButton = new JButton (EXPAND_ICON);
		collapseButton.setMargin (GraphicsUtil.ZEROINSETS);
		collapseButton.setPreferredSize (new Dimension (16, 16));
		collapseButton.setFont (collapseButton.getFont().deriveFont (Font.BOLD));
		collapseButton.setHorizontalAlignment (SwingConstants.CENTER);
		collapseButton.setHorizontalTextPosition (SwingConstants.LEFT);
		collapseButton.setVerticalTextPosition (SwingConstants.TOP);
		
		collapseButton.addActionListener (
			new ActionListener () {
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					if (contents != null) {
						final boolean contentsVisible = contents.isVisible();
						contents.setVisible (!contentsVisible);	
					}
				}
			}
		);
		
		visListener = new VisibilityAdapter ();
		
		headerPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		headerPanel.add (collapseButton);
		
		this.add (headerPanel, BorderLayout.NORTH);
	}


	public JComponent getContents () {
		return contents;
	}


	public void setContents (final JComponent newContents) {
		if (contents != null) {
			contents.removeComponentListener (visListener);
			this.remove (contents);
		}
		final boolean visible = (contents == null ? true : contents.isVisible ());
		
		this.contents = newContents;
		
		if (contents != null) {
			contents.addComponentListener (visListener);
			contents.setVisible (visible);
			this.add (contents, BorderLayout.CENTER);
		}
	}


	public JComponent getHeader() {
		return header;
	}


	public void setHeader (final JComponent newHeader) {
		if (header != null) {
			headerPanel.remove (header);
		}
		
		this.header = newHeader;
		
		if (header != null) {
			headerPanel.add (header);
		}
	}
	
	
	
	class VisibilityAdapter extends ComponentAdapter {
	    /**
	     * Invoked when the component has been made visible.
	     */
	    public void componentShown (final ComponentEvent cEvent) {
	    	collapseButton.setIcon (EXPAND_ICON);
	    	//collapseButton.setSize (collapseButton.getIcon().getIconWidth(), collapseButton.getIcon().getIconHeight());
	    }
	    
	    /**
	     * Invoked when the component has been made invisible.
	     */
	    public void componentHidden (final ComponentEvent cEvent) {
	    	collapseButton.setIcon (COLLAPSE_ICON);
	    	//collapseButton.setSize (collapseButton.getIcon().getIconWidth(), collapseButton.getIcon().getIconHeight());
	    }
	}
}
