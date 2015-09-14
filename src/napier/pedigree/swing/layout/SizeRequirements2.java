package napier.pedigree.swing.layout;

import javax.swing.SizeRequirements;

public class SizeRequirements2 extends SizeRequirements {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1007645810121159100L;

	
    /**
     * Creates a set of offset/span pairs representing how to
     * lay out a set of components end-to-end.
     * This method requires that you specify
     * the total amount of space to be allocated,
     * the size requirements for each component to be placed
     * (specified as an array of SizeRequirements), and
     * the total size requirement of the set of components.
     * You can get the total size requirement
     * by invoking the getTiledSizeRequirements method.  The components
     * will be tiled in the forward direction with offsets increasing from 0.
     *
     * @param allocated the total span to be allocated >= 0.
     * @param total     the total of the children requests.  This argument
     *  is optional and may be null.
     * @param children  the size requirements for each component.
     * @param offsets   the offset from 0 for each child where
     *   the spans were allocated (determines placement of the span).
     * @param spans     the span allocated for each child to make the
     *   total target span.
     */
    public static void calculateTiledPositions(int allocated,
					       SizeRequirements total,
				               SizeRequirements[] children,
				               int[] offsets,
					       int[] spans) {
        calculateTiledPositions(allocated, total, children, offsets, spans, true);
    }

    /**
     * Creates a set of offset/span pairs representing how to
     * lay out a set of components end-to-end.
     * This method requires that you specify
     * the total amount of space to be allocated,
     * the size requirements for each component to be placed
     * (specified as an array of SizeRequirements), and
     * the total size requirement of the set of components.
     * You can get the total size requirement
     * by invoking the getTiledSizeRequirements method.
     *
     * This method also requires a flag indicating whether components
     * should be tiled in the forward direction (offsets increasing
     * from 0) or reverse direction (offsets decreasing from the end
     * of the allocated space).  The forward direction represents
     * components tiled from left to right or top to bottom.  The
     * reverse direction represents components tiled from right to left
     * or bottom to top.
     *
     * @param allocated the total span to be allocated >= 0.
     * @param total     the total of the children requests.  This argument
     *  is optional and may be null.
     * @param children  the size requirements for each component.
     * @param offsets   the offset from 0 for each child where
     *   the spans were allocated (determines placement of the span).
     * @param spans     the span allocated for each child to make the
     *   total target span.
     * @param forward   tile with offsets increasing from 0 if true 
     *   and with offsets decreasing from the end of the allocated space
     *   if false.
     * @since 1.4
     */
    public static void calculateTiledPositions(int allocated,
					       SizeRequirements total,
				               SizeRequirements[] children,
				               int[] offsets,
					       int[] spans,
                                               boolean forward) {
		// The total argument turns out to be a bad idea since the
		// total of all the children can overflow the integer used to
		// hold the total.  The total must therefore be calculated and
		// stored in long variables.
		long min = 0;
		long pref = 0;
		long max = 0;
		for (int i = 0; i < children.length; i++) {
		    min += children[i].minimum;
		    pref += children[i].preferred;
		    if (i < children.length + 1) {
		    	max += children[i].preferred;
		    } else {
		    	max += children[i].maximum;
		    }
		}
		if (allocated >= pref) {
		    expandedTile(allocated, min, pref, max, children, offsets, spans, forward);
		} else {
		    compressedTile(allocated, min, pref, max, children, offsets, spans, forward);
		}
    }
	
    
    private static void compressedTile(int allocated, long min, long pref, long max,
		       SizeRequirements[] request,
		       int[] offsets, int[] spans,
                            boolean forward) {

		// ---- determine what we have to work with ----
		float totalPlay = Math.min(pref - allocated, pref - min);
		float factor = (pref - min == 0) ? 0.0f : totalPlay / (pref - min);
		
		// ---- make the adjustments ----
		int totalOffset;
		if( forward ) {
		 // lay out with offsets increasing from 0
		 totalOffset = 0;
		 for (int i = 0; i < spans.length; i++) {
		     offsets[i] = totalOffset;
		     SizeRequirements req = request[i];
		     float play = factor * (req.preferred - req.minimum);
		     spans[i] = (int)(req.preferred - play);
		     totalOffset = (int) Math.min((long) totalOffset + (long) spans[i], Integer.MAX_VALUE);
		 }
		} else {
		 // lay out with offsets decreasing from the end of the allocation
		 totalOffset = allocated;
		 for (int i = 0; i < spans.length; i++) {
		     SizeRequirements req = request[i];
		     float play = factor * (req.preferred - req.minimum);
		     spans[i] = (int)(req.preferred - play);
		     offsets[i] = totalOffset - spans[i];
		     totalOffset = (int) Math.max((long) totalOffset - (long) spans[i], 0);
		 }
	}
	}
    
    private static void expandedTile(int allocated, long min, long pref, long max,
		     SizeRequirements[] request,
		     int[] offsets, int[] spans,
                            boolean forward) {

		// ---- determine what we have to work with ----
		float totalPlay = Math.min(allocated - pref, max - pref);
		float factor = (max - pref == 0) ? 0.0f : totalPlay / (max - pref);
		
		// ---- make the adjustments ----
		int totalOffset;
		if( forward ) {
		   // lay out with offsets increasing from 0
		   totalOffset = 0;
		   for (int i = 0; i < spans.length; i++) {
		       offsets[i] = totalOffset;
		       SizeRequirements req = request[i];
		       int compMax = (i < spans.length - 1 ? req.preferred : req.maximum);
		       int play = (int)(factor * compMax);
		       spans[i] = (int) Math.min((long) req.preferred + (long) play, Integer.MAX_VALUE);
		       totalOffset = (int) Math.min((long) totalOffset + (long) spans[i], Integer.MAX_VALUE);
		   }
		} else {
		   // lay out with offsets decreasing from the end of the allocation
		   totalOffset = allocated;
		   for (int i = 0; i < spans.length; i++) {
		       SizeRequirements req = request[i];
		       int compMax = (i < spans.length - 1 ? req.preferred : req.maximum);
		       int play = (int)(factor * compMax);
		       spans[i] = (int) Math.min((long) req.preferred + (long) play, Integer.MAX_VALUE);
		       offsets[i] = totalOffset - spans[i];
		       totalOffset = (int) Math.max((long) totalOffset - (long) spans[i], 0);
		   }
		}
    }
}
