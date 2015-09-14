package napier.pedigree.swing.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import util.Messages;
import util.colour.ColorUtilities;

import napier.pedigree.util.PedigreeIconCache;


public class IndentTreeUI extends BasicTreeUI {

	private static final Icon OPEN_BRANCH_ICON = PedigreeIconCache.makeIcon ("treeOpenIcon");
	private static final Icon CLOSED_BRANCH_ICON = PedigreeIconCache.makeIcon ("treeClosedIcon");
    private static final Color TREE_HASH = Color.decode (Messages.getString ("napier.pedigree.graphics", "treeHashColour"));
 
    private static final Stroke OTHER_STROKE = new BasicStroke (2.0f);
    private static final Stroke STANDARD_STROKE = new BasicStroke (2.0f);

	private final Map<Integer, Rectangle> boundCache = new HashMap<Integer, Rectangle> ();
	private boolean pLines;
    
	
    public static ComponentUI createUI (final JComponent comp) {
    	return new IndentTreeUI();
    }
	

    @Override
	protected void installDefaults () {
    	super.installDefaults ();
    	
    	setCollapsedIcon (OPEN_BRANCH_ICON);
	    setExpandedIcon (CLOSED_BRANCH_ICON);
	    setHashColor (TREE_HASH); 
		pLines = UIManager.getBoolean ("Tree.paintLines");	
    }
	
    
	@Override
	public void paint (final Graphics graphics, final JComponent comp) {		
		
		//provokeResize();

		final Graphics2D g2d = (Graphics2D) graphics;
		g2d.setStroke (OTHER_STROKE);
		
		paintBackground (graphics, comp);
		super.paint (graphics, comp);
	}	
	
	
	@Override
	protected void updateSize () {
		super.updateSize ();
		boundCache.clear();
		if (treeState != null) {
			treeState.invalidateSizes();
		}
	}
	
	
    //
    // Painting routines.
    //

    public void paintBackground (final Graphics graphics, final JComponent comp) {
		if (tree != comp) {
		    throw new InternalError("incorrect component");
		}
	
		// Should never happen if installed for a UI
		if(treeState == null) {
		    return;
		}
	
		final Rectangle        paintBounds = graphics.getClipBounds();
		final Insets           insets = tree.getInsets();
		final TreePath         initialPath = getClosestPathForLocation (tree, 0, paintBounds.y);
		final Enumeration<TreePath>	paintingEnumerator = treeState.getVisiblePathsFrom (initialPath);
		int              row = treeState.getRowForPath(initialPath);
		final int              endY = paintBounds.y + paintBounds.height;
	
		final Paint defaultPaint = ((Graphics2D)graphics).getPaint();
		drawingCache.clear();
	
		if(initialPath != null && paintingEnumerator != null) {
		    TreePath   parentPath = initialPath;
	
		    // Draw the lines, knobs, and rows
	
		    // Find each parent and have them draw a line to their last child
		    parentPath = parentPath.getParentPath();
		    while(parentPath != null) {
				paintVerticalPartOfLeg(graphics, paintBounds, insets, parentPath);
				drawingCache.put(parentPath, Boolean.TRUE);
				parentPath = parentPath.getParentPath();
		    }
	
		    boolean         done = false;
		    // Information for the node being rendered.
		    final Rectangle       boundsBuffer = new Rectangle();
		    final Rectangle		visRectBuffer = new Rectangle ();
		    Rectangle       bounds; 
		    TreePath        path;
	
		    while(!done && paintingEnumerator.hasMoreElements()) {
				path = paintingEnumerator.nextElement();
				Color c2 = null;
				int depth = 0;
				
				if (path != null) {
		            bounds = getPathBounds (path, insets, boundsBuffer);
		            depth = path.getPathCount();
		            //System.out.println ("d: "+depth);
		            
				    if (bounds == null) {
						// This will only happen if the model changes out
						// from under us (usually in another thread).
						// Swing isn't multithreaded, but I'll put this
						// check in anyway.
						return;
				    }
				    comp.computeVisibleRect (visRectBuffer);
		            //bounds.x = visRectBuffer.x;
				    //bounds.width = visRectBuffer.width;
		            //bounds.width = visRectBuffer.width - (bounds.x - visRectBuffer.x);
		            
		            if (depth > 0) {
		            	final int offset = 6; //depth * 2;
		            	c2 = ColorUtilities.darkenSlightly (tree.getBackground(), 0.05f * depth);
		            	graphics.setColor (c2);
		            	graphics.fillRect ((int)bounds.getMaxX(), bounds.y + offset, offset, bounds.height - offset);
		            	graphics.fillRect (bounds.x + offset, (int)bounds.getMaxY(), bounds.width, offset);
		            }
	
				    if((bounds.y + bounds.height) >= endY) {
				    	done = true;
				    }
				}
				else {
				    done = true;
				}

			row++;
		    }
		}
	    ((Graphics2D)graphics).setPaint (defaultPaint);
    }
	
    
    private Rectangle getPathBounds(final TreePath path, final Insets insets, Rectangle bounds) {
		bounds = treeState.getBounds(path, bounds);
		if (bounds != null) {
			final boolean leftToRight = tree.getComponentOrientation().isLeftToRight();
			if (leftToRight) {
				bounds.x += insets.left;
			} else {
				bounds.x = tree.getWidth() - (bounds.x + bounds.width) -
				insets.right;
			}
			bounds.y += insets.top;
		}
		return bounds;
	}
    

	
	   /**
     * Paints the vertical part of the leg. The receiver should
     * NOT modify <code>clipBounds</code>, <code>insets</code>.<p>
     */
	
    @Override
	protected void paintVerticalPartOfLeg(final Graphics graphics, final Rectangle clipBounds,
					  final Insets insets, final TreePath path) {
		if (!pLines) {
		    return;
		}
		final boolean leftToRight = tree.getComponentOrientation().isLeftToRight();

        final int depth = path.getPathCount() - 1;
		if (depth == 0 && !getShowsRootHandles() && !isRootVisible()) {
		    return;
        }
		final int row = this.getRowForPath (tree, path);
		final Rectangle rect = boundCache.get (row);
		int lineX = (rect == null ? getRowX(-1, depth + 1) : rect.x + rect.width);
		if (leftToRight) {
	            lineX = lineX - getRightChildIndent() + insets.left;
		}
		else {
	            lineX = tree.getWidth() - lineX - insets.right +
	                    getRightChildIndent();
		}
		final int clipLeft = clipBounds.x;
		final int clipRight = clipBounds.x + (clipBounds.width - 1);

		if (lineX >= clipLeft && lineX <= clipRight) {
		    final int clipTop = clipBounds.y;
		    final int clipBottom = clipBounds.y + clipBounds.height;
		    Rectangle parentBounds = getPathBounds(tree, path);
		    final Rectangle firstChildBounds = getPathBounds (tree, getFirstChildPath (path));
		    final Rectangle lastChildBounds = getPathBounds (tree, getLastChildPath (path));
	
		    if (lastChildBounds == null || firstChildBounds == null) {
				// This shouldn't happen, but if the model is modified
				// in another thread it is possible for this to happen.
				// Swing isn't multithreaded, but I'll add this check in
				// anyway.
				return;
		    }
	
		    int top;
	
		    if (parentBounds == null) {
		    	top = Math.max (insets.top + getVerticalLegBuffer(), clipTop);
		    }
		    else {
		    	//top = Math.max ((int)parentBounds.getMaxX() +
				//       getVerticalLegBuffer() + getRightChildIndent(), clipTop);
		    	top = Math.max ((int)firstChildBounds.getCenterY(), clipTop);
			}
		    if(depth == 0 && !isRootVisible()) {
				final TreeModel      model = getModel();
		
				if(model != null) {
				    final Object        root = model.getRoot();
				    path.getLastPathComponent();
		
				    if(model.getChildCount (root) > 0) {
				    	parentBounds = getPathBounds (tree, path.
						pathByAddingChild (model.getChild (root, 0)));
				    	if(parentBounds != null) {
				    		top = Math.max(insets.top + getVerticalLegBuffer(), (int)parentBounds.getCenterY());
				    	}
					}
				}
		    }
	
		    final int bottom = Math.min ((int)lastChildBounds.getCenterY(), clipBottom);
	
	            if (top <= bottom) {
	                graphics.setColor (getHashColor());
	                final Path2D.Double p2d = new Path2D.Double ();
	                p2d.moveTo (lineX, bottom);
	                p2d.lineTo (lineX, top);
	                p2d.lineTo (lineX, firstChildBounds.y + getRightChildIndent());
	                p2d.lineTo (lineX + (depth > 0 ? (getRightChildIndent() * (leftToRight ? 1 : -1)) : 0), firstChildBounds.y);
	                ((Graphics2D)graphics).draw (p2d);
	            }
		}
	 }
	
    
    /**
     * Returns a path to the last child of <code>parent</code>.
     */
    protected TreePath getFirstChildPath (final TreePath parent) {
		if(treeModel != null) {
		    final int         childCount = treeModel.getChildCount
			(parent.getLastPathComponent());
		    
		    if(childCount > 0) {
		    	return parent.pathByAddingChild(treeModel.getChild
				   (parent.getLastPathComponent(), 0));
		    }
		}
		return null;
    }
    
    /**
     * Paints the expand (toggle) part of a row. The receiver should
     * NOT modify <code>clipBounds</code>, or <code>insets</code>.
     */
    @Override
	protected void paintExpandControl(final Graphics graphics,
				      final Rectangle clipBounds, final Insets insets,
				      final Rectangle bounds, final TreePath path,
				      final int row, final boolean isExpanded,
				      final boolean hasBeenExpanded,
				      final boolean isLeaf) {
	final Object       value = path.getLastPathComponent();

	// Draw icons if not a leaf and either hasn't been loaded,
	// or the model child count is > 0.
	if (!isLeaf && (!hasBeenExpanded ||
			treeModel.getChildCount(value) > 0)) {
            int middleXOfKnob;
            if (!tree.getComponentOrientation().isLeftToRight()) {
                middleXOfKnob = bounds.x - getRightChildIndent() + 1;
            } else {
                middleXOfKnob = (int)bounds.getMaxX() + getRightChildIndent() - 1;
            }
  
            //System.err.println ("row: "+row+", path bounds: "+bounds+", midx: "+middleXOfKnob+", treeArea: "+tree.getSize()+", path: "+path);
            final Icon icon = isExpanded ? getExpandedIcon() : getCollapsedIcon(); 
                    
            if (icon != null) {
            	((Graphics2D)graphics).setColor (getHashColor());
                final Path2D.Double p2d = new Path2D.Double ();
                p2d.moveTo (bounds.getMaxX(), bounds.getCenterY());
                p2d.lineTo (middleXOfKnob, bounds.getCenterY());
                if (isExpanded) {
                	final int hgap = getRightChildIndent() - 1;
                	final int vgap = bounds.height / 2;
                	if (hgap > vgap) {
                		p2d.lineTo (bounds.x + vgap, bounds.getCenterY());
                	}
                	else {
                		p2d.lineTo (middleXOfKnob, bounds.getMaxY() - hgap);
                	}
                	p2d.lineTo (bounds.getMaxX(), bounds.getMaxY());
                 }
                ((Graphics2D)graphics).draw (p2d);
            	
            	final int middleYOfKnob = (int)bounds.getCenterY() + 2;     
            	//int middleYOfKnob = (int)bounds.getMaxY() - (i.getIconHeight() / 2);
            	((Graphics2D)graphics).setStroke (STANDARD_STROKE);
            	drawCentered (tree, graphics, icon, middleXOfKnob, middleYOfKnob);
            	((Graphics2D)graphics).setStroke (OTHER_STROKE);
            }
	    }
    }
    
    
    
    /**
     * Returns true if <code>mouseX</code> and <code>mouseY</code> fall
     * in the area of row that is used to expand/collapse the node and
     * the node at <code>row</code> does not represent a leaf.
     */
    @Override
	protected boolean isLocationInExpandControl(final TreePath path, 
						final int mouseX, final int mouseY) {
	if(path != null && !treeModel.isLeaf(path.getLastPathComponent())){
	    int boxWidth = (getExpandedIcon() == null) ? 8 : getExpandedIcon().getIconWidth();
	    
	    final Rectangle bounds = getPathBounds (tree, path);
            int boxLeftX = 0;
            //int boxLeft = getRowX(tree.getRowForPath(path), path.getPathCount() - 1);

            if (!tree.getComponentOrientation().isLeftToRight()) {
                boxLeftX = bounds.x - getRightChildIndent() + 1;
            } else {
                boxLeftX = bounds.x + bounds.width + getRightChildIndent() - 1;
            }
            
            /*
            if (!tree.getComponentOrientation().isLeftToRight()) {
                boxLeftX = boxLeftX + i.left - getRightChildIndent() + 1;
            } else {
                boxLeftX = tree.getWidth() - boxLeftX - i.right + getRightChildIndent() - 1;
            }
			*/
            
            boxLeftX = findCenteredX (boxLeftX, boxWidth);

            return (mouseX >= boxLeftX && mouseX < (boxLeftX + boxWidth));
	}
	return false;
    }

    
    private int findCenteredX(final int x, final int iconWidth) {
        return !tree.getComponentOrientation().isLeftToRight()
               ? x - (int)Math.ceil(iconWidth / 2.0)
               : x - (int)Math.floor(iconWidth / 2.0);
    }
    
	   /**
     * Class responsible for getting size of node, method is forwarded
     * to BasicTreeUI method. X location does not include insets, that is
     * handled in getPathBounds.
     */
    // This returns locations that don't include any Insets.
    public class NodeDimensionsHandler2 extends
	         NodeDimensionsHandler {
	/**
	 * Responsible for getting the size of a particular node.
	 */
	@Override
	public Rectangle getNodeDimensions(final Object value, final int row,
					   final int depth, final boolean expanded,
					   final Rectangle size) {
		
		final Rectangle rect = super.getNodeDimensions (value, row, depth, expanded, size);
		if (rect != null) {
			boundCache.put (row, new Rectangle (rect));
		}
		return rect;
	}

	/**
	 * @return amount to indent the given row.
	 */
	@Override
	protected int getRowX(final int row, final int depth) {
          return IndentTreeUI.this.getRowX(row, depth);
	}

    } // End of class BasicTreeUI.NodeDimensionsHandler

    
    @Override
	protected int getRowX (final int row, final int depth) {
    	
    	//System.out.println ("row: "+row);
    	int x = totalChildIndent;
    	final Rectangle rect = boundCache.get (row);
    	if (rect == null) {
    		final TreePath tPath = treeState.getPathForRow (row);
    		
    		if (tPath != null) {
    			final TreePath parentPath = tPath.getParentPath ();
    			
    			if (parentPath != null) {
    				final int pRow = treeState.getRowForPath (parentPath);
    				Rectangle parentBounds = boundCache.get (pRow);
    				
    				if (parentBounds == null) {
    					parentBounds = new Rectangle ();
    					parentBounds = treeState.getBounds (parentPath, parentBounds);
    					//System.out.println ("parentBounds calcs: "+parentBounds);
    				}
    				
    				x = parentBounds.x + parentBounds.width;
    				//System.out.println ("x from parent: "+x);
    			} 
    		} 
    	} else {
    		//System.out.println ("r: "+r+", row: "+row);
    		x = rect.x;
    	}
    	//System.out.println ("row: "+row+", x: "+x+", r: "+r);
    	return x;
    }
    

    
    @Override
	protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
    	return new NodeDimensionsHandler2();
    }
    
    
    protected void updateCachedPreferredSize() {
		if (treeState != null) {
			super.updateCachedPreferredSize();
			preferredSize.width += 32; // Make space for expand / collapse node to right of longest path   
		}
    }
}
