package napier.pedigree.swing.renderers.base;

import java.awt.Point;

import javax.swing.JTable;

public interface MultipleItemsRenderer {
	
	int calcItemEdgeLength (final int width, final int height, final int collectionSize);

	void setGlobalItemEdgeLength (final int length);
	
	int getGlobalItemEdgeLength ();
	
	int getObjectIndexAt (final Point mousePos, final int cellWidth,
			final JTable table, final int vrow, final int vcolumn);
}
