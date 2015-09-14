package napier.pedigree.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import napier.pedigree.model.impl.SplitLinkNodeObject;
import napier.pedigree.swing.renderers.tooltip.SplitLinkTooltipInfoRenderer;

import org.apache.log4j.Logger;
import org.resspecies.model.Individual;

import swingPlus.shared.tooltip.AbstractRendererToolTip;


/**
 * @author Martin Graham
 * @version 1.01 03/03/09
 *
 * A tooltip class that uses a JTable and its renderers to produce variable tooltips
 * depending on the Object is it passed as an argument
 */
public class PedigreeRendererToolTip extends AbstractRendererToolTip {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1462349250142881631L;
	
	private final static Logger LOGGER = Logger.getLogger (PedigreeRendererToolTip.class);
	private final static Double GOLDEN = 1.618;
	protected final static SplitLinkTooltipInfoRenderer TOOLTIPSPLITINFORENDERER = new SplitLinkTooltipInfoRenderer ();
	
	protected final static Color BACKGROUND_SHADE = new Color (160, 160, 255, 192);
	
	protected JTable table;
	protected int row = -1, column = -1;
	protected Map<Class<?>, TableCellRenderer> renderMap = new HashMap <Class<?>, TableCellRenderer> ();

	
	public PedigreeRendererToolTip (final JTable table) {
		super (table);
		this.table = table;
		setSorter (new IndividualComparator ());
		//remove (pane);
		//pane = new CellRendererPane ();
		//add (pane);
		this.setDoubleBuffered (true);
		addRenderer (SplitLinkNodeObject.class, TOOLTIPSPLITINFORENDERER);
	}
	
	@Override
	protected void setTitle () {
		
		if (this.getBorder() instanceof TitledBorder) {
			final TitledBorder tBorder = (TitledBorder)this.getBorder();
			//final TableModel tModel = (TableModel)table.getModel();
			final Object val = table.getValueAt (row, column);
			if (val instanceof Collection) {
				tBorder.setTitle (((Collection)val).size() < 2 ? val.toString() 
						: ((Collection)val).size() + " Offspring");
			} else {
				tBorder.setTitle (val == null ? "Null" : val.toString());
			}
		}
	}
	
	
	@Override
	public void setToolTipObject (final Object object, final int row, final int column) {
		this.row = row;
		this.column = column;
		setToolTipObject (object);
	}
	
	
	public void setToolTipObject (final Object object) {
		LOGGER.debug ("object: "+object);
		setVisible (object != null);
		
		if (obj != object) {
			obj = object;	
			curSize.setSize (getObjectPreferredSize (obj));
			setTitle ();

			borderInsets = this.getInsets (borderInsets);
			curSize.width += borderInsets.left + borderInsets.right;
			curSize.height += borderInsets.top + borderInsets.bottom;
			setPreferredSize (curSize);
		}
	}
	
	
	@Override
	public Dimension getObjectPreferredSize (final Object obj) {
		if (obj == null) {
			return oneCellSize;
		}
		final Object object = obj;
		final TableCellRenderer tcr = getRenderer (object.getClass());
		if (tcr != null) {
			tcr.getTableCellRendererComponent (table, obj, true, false, row, column);
		}
		return (tcr instanceof JComponent) ?
				((JComponent)tcr).getPreferredSize() : oneCellSize;
	}
	
	
	public void addRenderer (final Class<?> klass, final TableCellRenderer tcr) {
		renderMap.put (klass, tcr);
	}
	
	public TableCellRenderer getRenderer (final Class<?> klass) {
		TableCellRenderer tcr = renderMap.get (klass);
		//System.err.println ("klass: "+klass+", renderer: "+tcr);
		if (tcr == null) {
			final TableCellRenderer tcrTable = table.getDefaultRenderer (klass);
			//System.err.println ("klass: "+klass);
			if (tcrTable != null) {
				//try {
					//tcr = tcrTable.getClass().newInstance();
					//addRenderer (klass, tcr);
					addRenderer (klass, tcrTable);
					tcr = tcrTable;
				//} catch (InstantiationException e) {
				//	LOGGER.error (e.toString(), e);
				//} catch (IllegalAccessException e) {
				//	LOGGER.error (e.toString(), e);
				//}
			}
		}
		
		return tcr;
	}
	
	
	@Override
	public void paintComponent (final Graphics graphics) {
		
		fillBackground (graphics, BACKGROUND_SHADE);
		//super.paintComponent (graphics);
		renderBounds.setLocation (borderInsets.left, borderInsets.top);
		renderBounds.setSize (getObjectPreferredSize (obj));
		paintRenderer (graphics, obj, renderBounds);
	}
	
	
	@Override
	public void paintRenderer (final Graphics graphics, final Object obj, final Rectangle bounds) {
		final Object obj2 = obj;

		final TableCellRenderer tcr = (obj2 == null ? null : getRenderer (obj2.getClass()));
		if (tcr != null) {
			final Component comp = tcr.getTableCellRendererComponent (table, obj, true, false, row, column);
			LOGGER.debug ("painting tooltip");
			if (comp instanceof JComponent) {
				((JComponent)comp).setBorder(null);
			}
			pane.paintComponent (graphics, comp, this, bounds);
		}
	}
	
	public void fillBackground (final Graphics graphics, final Color backColour) {
		graphics.setColor (backColour);
		graphics.fillRect (0, 0, this.getWidth(), this.getHeight());
	}
	
	
	static class IndividualComparator implements Comparator<Individual>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2486736203298038220L;

		public int compare (final Individual ind1, final Individual ind2) {
			return ind1.getGender().compareTo (ind2.getGender());	
		}
	}
};