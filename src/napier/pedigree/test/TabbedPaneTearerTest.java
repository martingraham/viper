package napier.pedigree.test;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;

import util.swing.TabbedMoveHandler;

public class TabbedPaneTearerTest extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2635490005741056648L;
	protected JPanel panel;
	
	static public void main (final String[] args) {
		new TabbedPaneTearerTest ();
	}

	public TabbedPaneTearerTest () {
		super ();
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false); 
		//ToolTipManager.sharedInstance().setReshowDelay(0); 
		
		setTitle (this.getClass().toString());
		setSize (1024, 768);
		
		this.getContentPane().setLayout (new BorderLayout());
		
		final JTabbedPane jtp = new JTabbedPane ();
		final JTabbedPane jtp2 = new JTabbedPane ();
		
		final JPanel[] panels = new JPanel [5];
		for (int n = 0; n < panels.length; n++) {
			panels[n] = new JPanel ();
			panels[n].add (new JLabel ("I'm tab"+n+"'s panel"));
		}
		
		for (int n = 0; n < panels.length - 1; n++) {
			jtp.addTab ("I'm tab "+n, panels[n]);
		}
		
		final int lastPanel = panels.length - 1;
		jtp2.addTab ("I'm tab "+lastPanel, panels[lastPanel]);
		
		this.add(jtp, "Center");
		this.add(jtp2, "South");
		
		
		final TabbedMoveHandler dragHandler = new TabbedMoveHandler (this);
		dragHandler.setAllowPaletteDialogs (true);
		final JTabbedPane[] tabArray = {jtp, jtp2};
		for (JTabbedPane tabbedPane : tabArray) {
			dragHandler.addToTabbedPane (tabbedPane);
		}
			
		this.setVisible (true);
	}
}
