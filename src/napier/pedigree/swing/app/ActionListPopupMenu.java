package napier.pedigree.swing.app;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import util.Messages;



public abstract class ActionListPopupMenu extends JPopupMenu implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5940027438601301319L;
	private static final Logger LOGGER = Logger.getLogger (ActionListPopupMenu.class);
	public static final Border INDENT_BORDER = BorderFactory.createEmptyBorder (1, 12, 1 ,2);
	//public static final Border INDENT_BORDER = BorderFactory.createEmptyBorder (1, 12, 1 ,2);

	protected int activeButton;
	
    protected JLabel title;
    protected final static Font TYPEFACE = Font.decode (Messages.getString ("napier.pedigree.graphics", "popupFont"));

    
    public ActionListPopupMenu (final int activeButton) {
  		
        //setForeground (Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, cl, "text")));

        //this.setLayout(new BoxLayout (this, BoxLayout.Y_AXIS));
        super ();
    	
        this.activeButton = activeButton;
    	
        this.addFocusListener(
        		new FocusAdapter () {
        			@Override
					public void focusLost (final FocusEvent focusEvent) {
        				ActionListPopupMenu.this.setVisible(false);
        			}
        		}
        );        
        
        title = new JLabel ("title");  
        title.setBorder (INDENT_BORDER);
        add (title);

        
		for (int i = 0; i < getComponents().length; i++) {
			getComponent(i).setFont (TYPEFACE);
		}
	}

	
    public void setPopupPosition (final JComponent jComp, final Point point) {      
	       this.show (jComp, point.x, point.y);
    }
    
    
    
    // MouseListener interface
    
	public void mouseClicked (final MouseEvent mEvent) {
		if (mEvent.getButton() == activeButton && setDetails (mEvent.getComponent(), mEvent.getPoint())) {
			setPopupPosition ((JComponent)mEvent.getComponent(), mEvent.getPoint());
		}
	}
	
	
	@Override
	public void mousePressed (final MouseEvent mEvent) {
		// EMPTY	
	}


	@Override
	public void mouseReleased (final MouseEvent mEvent) {
		// EMPTY	
	}


	@Override
	public void mouseEntered (final MouseEvent mEvent) {
		// EMPTY	
	}


	@Override
	public void mouseExited (final MouseEvent mEvent) {
		// EMPTY	
	}
	
	
	abstract protected boolean setDetails (final Component comp, final Point mouseCoord);
	
	
	abstract protected String makeTitle ();
	
	/**
	 * Iterates through the popup's components, looking for menu items that hold
	 * FamilyCentricActions. Then it calls the FamilyCentricActions's updateAction.
	 * This is to update the labelling mainly.
	 * e.g. a mask action will present a label knowing whether the individual concerned is
	 * currently masked or not.
	 * The similar isWorthwhileAction calls decide whether or not each action is plausible/
	 * possible/necessary etc given the current selection. If not, we usually just hide these.
	 */
	abstract protected void updateMenuItemActions ();
}