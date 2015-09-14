package napier.pedigree.test;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;


public class JTooltipFlickerTest extends JFrame {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5449306103775448458L;
	protected JPanel panel;
	
	static public void main (final String[] args) {
		new JTooltipFlickerTest ();
	}
	
	public JTooltipFlickerTest () {
		super ();
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false); 
		//ToolTipManager.sharedInstance().setReshowDelay(0); 
		
		setTitle (this.getClass().toString());
		setSize (1024, 768);
		
		this.getContentPane().setLayout (new BorderLayout());
		
		SwingUtilities.invokeLater (
			new Runnable () {

				@Override
				public void run() {
					panel = new JPanel () {
						/**
						 * 
						 */
						private static final long serialVersionUID = 7698866781805842124L;

						@Override
						public Point getToolTipLocation (final MouseEvent mEvent) {
							return mEvent.getPoint();
						}
					};
						
					final MouseAdapter mAdapter = new MouseAdapter () {
						
						public void mouseMoved (final MouseEvent mEvent) { 
							panel.setToolTipText ("hello");
							//panel.setToolTipText ("x: "+e.getX()+", y: "+e.getY());
						}
					};	
					panel.addMouseMotionListener(mAdapter);
					
					//stack.setDoubleBuffered(true);
					//stack.createToolTip().setDoubleBuffered(true);

					JTooltipFlickerTest.this.getContentPane().add (panel, "Center");				
					JTooltipFlickerTest.this.setVisible (true);
				}
			}
		);
	}
}
