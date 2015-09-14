package napier.pedigree.swing.renderers.base;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.*;

import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

import java.io.Serializable;


/**
 * The standard class for rendering (displaying) individual cells
 * in a <code>JTable</code>.
 * <p>
 *
 * <strong><a name="override">Implementation Note:</a></strong>
 * This class inherits from <code>JLabel</code>, a standard component class. 
 * However <code>JTable</code> employs a unique mechanism for rendering
 * its cells and therefore requires some slightly modified behavior
 * from its cell renderer.  
 * The table class defines a single cell renderer and uses it as a 
 * as a rubber-stamp for rendering all cells in the table; 
 * it renders the first cell,
 * changes the contents of that cell renderer, 
 * shifts the origin to the new location, re-draws it, and so on.
 * The standard <code>JLabel</code> component was not
 * designed to be used this way and we want to avoid 
 * triggering a <code>revalidate</code> each time the
 * cell is drawn. This would greatly decrease performance because the
 * <code>revalidate</code> message would be
 * passed up the hierarchy of the container to determine whether any other
 * components would be affected.  
 * As the renderer is only parented for the lifetime of a painting operation
 * we similarly want to avoid the overhead associated with walking the
 * hierarchy for painting operations.
 * So this class
 * overrides the <code>validate</code>, <code>invalidate</code>,
 * <code>revalidate</code>, <code>repaint</code>, and
 * <code>firePropertyChange</code> methods to be 
 * no-ops and override the <code>isOpaque</code> method solely to improve
 * performance.  If you write your own renderer,
 * please keep this performance consideration in mind.
 * <p>
 *
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @version %I% %G%
 * @author Philip Milne 
 * @see JTable
 */
public class DefaultTableCellRenderer2 extends JLabel
    implements TableCellRenderer, Serializable
{

   /**
	 * 
	 */
	private static final long serialVersionUID = -7212620238408510469L;
	
	/**
    * An empty <code>Border</code>. This field might not be used. To change the
    * <code>Border</code> used by this renderer override the 
    * <code>getTableCellRendererComponent</code> method and set the border
    * of the returned component directly.
    */
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    // We need a place to store the color the JLabel should be returned 
    // to after its foreground and background colors have been set 
    // to the selection background color. 
    // These ivars will be made protected when their names are finalized. 
    private Color unselectedForeground; 
    private Color unselectedBackground; 

    /**
     * Creates a default table cell renderer.
     */
    public DefaultTableCellRenderer2() {
    	super();
    	setOpaque(true);
        setBorder(getNoFocusBorder());
        setName("Table.cellRenderer");
    }

    private Border getNoFocusBorder() {
    	final Border border =  (Border) UIManager.get ("Table.cellNoFocusBorder", this.getLocale());
        if (System.getSecurityManager() != null) {
            if (border != null) {
            	return border;
            }
            return SAFE_NO_FOCUS_BORDER;
        } else if (border != null) {
            if (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER) {
                return border;
            }
        }
        return noFocusBorder;
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     * 
     * @param fColor set the foreground color to this value
     */
    public void setForeground (final Color fColor) {
        super.setForeground (fColor); 
        unselectedForeground = fColor; 
    }
    
    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param bColor set the background color to this value
     */
    public void setBackground (final Color bColor) {
        super.setBackground(bColor); 
        unselectedBackground = bColor; 
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed.
     * Replaces the current UI object with the latest version from the 
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        super.updateUI(); 
        setForeground(null);
        setBackground(null);
    }
    
    // implements javax.swing.table.TableCellRenderer
    /**
     *
     * Returns the default table cell renderer.
     * <p>
     * During a printing operation, this method will be called with
     * <code>isSelected</code> and <code>hasFocus</code> values of
     * <code>false</code> to prevent selection and focus from appearing
     * in the printed output. To do other customization based on whether
     * or not the table is being printed, check the return value from
     * {@link javax.swing.JComponent#isPaintingForPrint()}.
     *
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     *			<code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     * @see javax.swing.JComponent#isPaintingForPrint()
     */
    public Component getTableCellRendererComponent(final JTable table, final Object value,
    		boolean isSelected, final boolean hasFocus, final int row, final int column) {

        Color fg = null;
        Color bg = null;

        final JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row
                && dropLocation.getColumn() == column) {

            fg = (Color)UIManager.get ("Table.dropCellForeground", this.getLocale());
            bg = (Color)UIManager.get ("Table.dropCellBackground", this.getLocale());

            isSelected = true;
        }

        if (isSelected) {
            super.setForeground(fg == null ? table.getSelectionForeground()
                                           : fg);
            super.setBackground(bg == null ? table.getSelectionBackground()
                                           : bg);
	} else {
            Color background = unselectedBackground != null
                                    ? unselectedBackground
                                    : table.getBackground();
            if (background == null || background instanceof javax.swing.plaf.UIResource) {
            	final Color alternateColor = (Color)UIManager.get ("Table.alternateRowColor", this.getLocale());
                if (alternateColor != null && row % 2 == 0)
                    background = alternateColor;
            }
            super.setForeground(unselectedForeground != null
                                    ? unselectedForeground
                                    : table.getForeground());
            super.setBackground(background);
	}

	setFont(table.getFont());

	if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = (Border)UIManager.get ("Table.focusSelectedCellHighlightBorder", this.getLocale());
            }
            if (border == null) {
                border = (Border)UIManager.get ("Table.focusCellHighlightBorder", this.getLocale());
            }
            setBorder(border);

	    if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = (Color)UIManager.get ("Table.focusCellForeground", this.getLocale());
                if (col != null) {
                    super.setForeground(col);
                }
                col =  (Color)UIManager.get ("Table.focusCellBackground", this.getLocale());
                if (col != null) {
                    super.setBackground(col);
                }
	    }
	} else {
            setBorder(getNoFocusBorder());
	}

        setValue(value); 

	return this;
    }
    
    /*
     * The following methods are overridden as a performance measure to 
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and 
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public boolean isOpaque() { 
    	final Color back = getBackground();
    	Component parentComp = getParent(); 
		if (parentComp != null) { 
		    parentComp = parentComp.getParent(); 
		}
	        
		// parentComp should now be the JTable. 
		final boolean colorMatch = (back != null) && (parentComp != null) && 
		    back.equals(parentComp.getBackground()) && 
				parentComp.isOpaque();
		return !colorMatch && super.isOpaque(); 
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     */
    public void invalidate() {
    	if (this.getComponentCount() > 0) {
    		super.invalidate();
    	}
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void validate() {
    	if (this.getComponentCount() > 0) {
    		super.validate();
    	}
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void revalidate() {
    	if (this.getComponentCount() > 0) {
    		super.revalidate();
    	}
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint (final long tm, final int x, final int y, final int width, final int height) {
    	// EMPTY
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint (final Rectangle rect) {
    	// EMPTY
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     */
    public void repaint() {
    	// EMPTY
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    protected void firePropertyChange (final String propertyName, final Object oldValue, final Object newValue) {	
	// Strings get interned...
	if (propertyName=="text"
                || propertyName == "labelFor"
                || propertyName == "displayedMnemonic"
                || ((propertyName == "font" || propertyName == "foreground")
                    && oldValue != newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void firePropertyChange (final String propertyName, final boolean oldValue, final boolean newValue) {
    	// EMPTY
    }


    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     * 
     * @param value  the string value for this cell; if value is
     *		<code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     * 
     */
    protected void setValue (final Object value) {
    	setText((value == null) ? "" : value.toString());
    }
	

    /**
     * A subclass of <code>DefaultTableCellRenderer</code> that
     * implements <code>UIResource</code>.
     * <code>DefaultTableCellRenderer</code> doesn't implement
     * <code>UIResource</code>
     * directly so that applications can safely override the
     * <code>cellRenderer</code> property with
     * <code>DefaultTableCellRenderer</code> subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends DefaultTableCellRenderer2 
        implements javax.swing.plaf.UIResource
    {}
}
