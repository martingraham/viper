package napier.pedigree.swing.app;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import napier.pedigree.swing.JGeneration;
import napier.pedigree.swing.ui.GenerationTableUI;
import swingPlus.shared.MyFrame;

public class DetailFrame extends MyFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6376385671509330486L;
	
	protected JGeneration detailTable;
	
	public DetailFrame () {
		
		super ();
		this.setAlwaysOnTop (true);
		this.setTitle (AppUtils.getInstance().getLabelString ("detailTableWindowTitle"));
		
		detailTable = new JGeneration ();
		((GenerationTableUI)detailTable.getUI()).setRowAnimationEnabled (false);
				
		final JScrollPane detailScroll = new JScrollPane (detailTable);
		detailScroll.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		detailTable.setFillsViewportHeight (true);
		
		this.getContentPane().add (detailScroll, BorderLayout.CENTER);
		final Dimension defaultSize = new Dimension (320, 160);
		AppUtils.getInstance().populateDimension ("detailFrame", defaultSize);
		this.setSize (defaultSize);
		this.setAlwaysOnTop (true);
	}
	
	public JGeneration getDetailTable () {
		return detailTable;
	}
}
