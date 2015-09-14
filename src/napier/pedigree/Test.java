package napier.pedigree;

import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;

import napier.pedigree.swing.app.PedigreeFrame;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.Messages;
import util.ui.NewMetalTheme;


public class Test {
	
	static final private Logger LOGGER = Logger.getLogger (Test.class);


	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		initialiseLogging ();
		System.setProperty ("awt.useSystemAAFontSettings","on"); 

		new Test ();
	}
	
	public Test () {	
		//PopupFactory.setSharedInstance (new TranslucentPopupFactory());
		//ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false); 
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					final PedigreeFrame pedigreeFrame = new PedigreeFrame ();
					pedigreeFrame.launch ();
					//pedigreeFrame.setModel (parser);
				}
			}
		);
	}
	
	
	static void initialiseLogging () {
		PropertyConfigurator.configure (Messages.makeProperties ("napier.pedigree.log4j"));
		LOGGER.info ("Logger activated");
	}
}
