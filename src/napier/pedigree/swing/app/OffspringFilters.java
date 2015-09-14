package napier.pedigree.swing.app;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import napier.pedigree.model.ModelRowConstants;
import napier.pedigree.model.PedigreeGenerationModel;


public class OffspringFilters {

	public static final RowFilter<? extends TableModel, ? extends Integer> SHOW_MULTIPLE_OFFSPRING_ROWS = new ShowMultipleChildRowsFilter ();
	public static final RowFilter<? extends TableModel, ? extends Integer> SHOW_POP_MULTIPLE_OFFSPRING_ROWS = new ShowPopulatedMultipleChildRowsFilter ();
	public static final RowFilter<? extends TableModel, ? extends Integer> SHOW_SINGLE_OFFSPRING_ROW = new ShowSingleChildRowFilter ();
}


	class ShowMultipleChildRowsFilter extends RowFilter<TableModel, Integer> {

		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int row = entry.getIdentifier().intValue();
			final PedigreeGenerationModel pgm = (PedigreeGenerationModel)entry.getModel();
			final int maxCategoryRow = pgm.getCategoriser().getRange();
			return (row != ModelRowConstants.OFFSPRING && row < ModelRowConstants.OFFSPRING_SPLIT_START + maxCategoryRow);
		}
	}
	
	
	class ShowPopulatedMultipleChildRowsFilter extends RowFilter<TableModel, Integer> {

		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int row = entry.getIdentifier().intValue();
			final PedigreeGenerationModel pgm = (PedigreeGenerationModel)entry.getModel();
			return (row != ModelRowConstants.OFFSPRING && 
					(row < ModelRowConstants.OFFSPRING_SPLIT_START || pgm.areSplitOffspringPresent (row - ModelRowConstants.OFFSPRING_SPLIT_START)));
		}
	}
	
	class ShowSingleChildRowFilter extends RowFilter<TableModel, Integer> {

		@Override
		public boolean include (final Entry<? extends TableModel, ? extends Integer> entry) {
			final int row = entry.getIdentifier().intValue();
			return (row < ModelRowConstants.OFFSPRING_SPLIT_START);
		}
	}