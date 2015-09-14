package napier.pedigree.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import util.Messages;

import napier.pedigree.swing.app.actions.PropertyPrefixBasedAction;
import napier.pedigree.undo.HistoryModel;


/**
* Describes the layout and actions of a control panel for selecting global actions and
* modes upon the relevant forest model.
* @author Martin Graham
* @version 1.1
*/

public class HistoryPanel extends JPanel  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7887449594405759738L;
	private static final Logger LOGGER = Logger.getLogger (HistoryPanel.class);
	
	

    //private static final Icon navigationIcon = IconCache.makeIcon (cl, "NavigationIcon");
    private final JScrollPane sideScroller;
    private final HistoryTree historyJTree;
    private final MessageFormat cellTemplate = new MessageFormat (Messages.getString ("cellTemplate"));
    private final Object[] messageArgs = new Object [10];
    private final Map<Integer, Color> colorMap = new HashMap<Integer, Color> ();
    
	private final Action save, load, email;

    protected HistoryModel historyModel;
    
    
    
	public HistoryPanel () {
		
		super ();
		
		setLayout (new BorderLayout ());
		
		save = new HistorySaveAction (KeyStroke.getKeyStroke 
				(KeyEvent.VK_S, KeyEvent.CTRL_MASK), "histSave");
		load = new HistoryLoadAction (KeyStroke.getKeyStroke 
				(KeyEvent.VK_L, KeyEvent.CTRL_MASK), "histLoad");
		email = new HistoryEMailAction (KeyStroke.getKeyStroke 
				(KeyEvent.VK_E, KeyEvent.CTRL_MASK), "email");
		//final Action back = SharedActions.getInstance().getBackAction();
		final JButton saveButton = new JButton (save);
		final JButton loadButton = new JButton (load);
		final JButton emailButton = new JButton (email);
		//final JButton backButton = new JButton (back);
		saveButton.setHideActionText (true);
		loadButton.setHideActionText (true);
		emailButton.setHideActionText (true);
		//backButton.setHideActionText (true);
		
		//saveButton.setBorder (ROUNDBORDER);
		
		final JToolBar jtb = new JToolBar ();
		//jtb.add (backButton);
		jtb.add (Box.createRigidArea (new Dimension (10, 10)));
		jtb.add (loadButton);
		jtb.add (saveButton);
		jtb.add (emailButton);
		jtb.add (Box.createRigidArea (new Dimension (10, 10)));
		//jtb.add (text);
		
						
		historyJTree = new HistoryTree ();
		ToolTipManager.sharedInstance().registerComponent (historyJTree);
		historyJTree.setRowHeight(-1); // make each row height query the cell renderer for that row

	
	    sideScroller = new JScrollPane ();//historyJTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		sideScroller.setViewportView (historyJTree);
	    sideScroller.getViewport().putClientProperty ("EnableWindowBlit", Boolean.TRUE);
		sideScroller.setPreferredSize (new Dimension (sideScroller.getPreferredSize().width, 128));
		
        add (sideScroller, "Center");
        
        //this.addComponentListener (
        //	new ToolBarPositionAdapter (HistoryPanel.this, jtb, true)
       //);

        setVisible (true);
	}
	
	
	

	class HistorySaveAction extends PropertyPrefixBasedAction {
		  
		/**
		 * 
		 */
		private static final long serialVersionUID = 7486411541079646025L;

		HistorySaveAction (final KeyStroke keyStroke, final String actionPrefix) {
			super (keyStroke, actionPrefix);
		}
    	
		public void actionPerformed (final ActionEvent aEvent) {		
			//outputTreeModel (assocModel.getHistoryModel().getHistory());
			//SerFileChooser.saveFile (assocModel);
		}
    };
    
    
	class HistoryLoadAction extends PropertyPrefixBasedAction {
		  
		/**
		 * 
		 */
		private static final long serialVersionUID = 7486411541079646025L;

		HistoryLoadAction (final KeyStroke keyStroke, final String actionPrefix) {
			super (keyStroke, actionPrefix);
		}
    	
		public void actionPerformed (final ActionEvent aEvent) {
			//SerFileChooser.chooseFile (assocModel);		
		}
    };
    
	class HistoryEMailAction extends PropertyPrefixBasedAction {
		  
		/**
		 * 
		 */
		private static final long serialVersionUID = -4910975921759321254L;

		HistoryEMailAction (final KeyStroke keyStroke, final String actionPrefix) {
			super (keyStroke, actionPrefix);
		}
    	
		public void actionPerformed (final ActionEvent aEvent) {
	        //SerFileChooser.mailFile (assocModel.getHistoryModel());
			//EMailer.send (assocModel);
		}
    };
    
   

    public void setHistoryModel (final HistoryModel newHistoryModel) {
    	historyJTree.setModel (newHistoryModel);
    	actionAbler ();
		//outputTreeModel (historyJTree.getModel());
	}

    public HistoryModel getHistoryModel () { return historyModel; }

    

	protected void actionAbler () {
    	final boolean emptyModel = historyModel == null;
    	save.setEnabled (!emptyModel);
    	email.setEnabled (!emptyModel);
	}
}
