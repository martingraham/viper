package napier.pedigree.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import model.shared.MultiComparator;
import napier.pedigree.model.PedigreeGenerationModel;
import napier.pedigree.swing.ui.GenerationTableUI;

import org.apache.log4j.Logger;
import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.model.Individual;

import swingPlus.shared.tooltip.AbstractRendererToolTip;


public final class JGenerationUtils {

	private static final Logger LOGGER = Logger.getLogger (JGenerationUtils.class);

	
	private static final JGenerationUtils JGENERATIONUTILS_INSTANCE = new JGenerationUtils ();
	
	private JGenerationUtils () {}

	public static JGenerationUtils getInstance() { return JGENERATIONUTILS_INSTANCE; }
	
	
	public void setOffspringSort (final Collection<JGeneration> generations, final MultiComparator<Individual> offspringSort) {
		for (JGeneration generation : generations) {
			((AbstractRendererToolTip)(generation.createToolTip())).setSorter (offspringSort);
		}
	}
	
	
	
	public void setColumnWidthBounds (final JGeneration generation) {
		for (int i = 0; i < generation.getColumnCount(); i++) {
		    final TableColumn column = generation.getColumnModel().getColumn(i);
		    column.setMinWidth (1); 
		    column.setMaxWidth (1000); 
		    column.setPreferredWidth (generation.getRowHeight()); 
		}
	}
	
	
	
	public void revalidateSuperScrollPane (final JTable jTable) {
		
		// Calculate new scroll pane size of JTable
		final Dimension curPrefSize = jTable.getPreferredSize();
		final Dimension curPrefScrollSize = jTable.getPreferredScrollableViewportSize();
		if (curPrefScrollSize != null) {
			jTable.setPreferredScrollableViewportSize (new Dimension (curPrefScrollSize.width, curPrefSize.height));
		}
		
		
		// Tell the JGenerationStack and it's ScrollPane to resize
		Container jcomp = (Container)jTable;
		while (jcomp != null && !(jcomp instanceof JGenerationStack)) {
			jcomp = jcomp.getParent();
		}
		
		if (jcomp != null) {
			final Component comp2 = jcomp.getParent().getParent();
			
			if (comp2 instanceof JScrollPane) {
				final JScrollPane jsp = (JScrollPane)comp2;
				jsp.revalidate();
			}
		}
	}
	
	
	
	/**
	 * Recalculates the model for a generation based on the families for that generation in the HeritablePopulation object.
	 * Used after sires/dams have been masked or restored.
	 * @param generation - JGeneration to remodel and relayout
	 * @param hPop - HeritablePopulation to get family data from
	 */
	public void redoGenerationFamilies (final JGeneration generation, final HeritablePopulation hPop) {		
		((GenerationTableUI)generation.getUI()).setColumnAnimationEnabled (false);
		
		final PedigreeGenerationModel pgm = (PedigreeGenerationModel)generation.getModel();
		pgm.make (hPop.getFamiliesByGeneration (pgm.getGenerationIndex()));
		pgm.splitOffspring();
		
		generation.createDefaultColumnsFromModel();	// cos we may have a different number of columns (if the number of families is now different)
		generation.refilter();	// redo row ordering
		generation.resortColumns (); // redo column ordering
		setColumnWidthBounds (generation); // rein in each generation's new bunch of columns. Stops new minimum col sizes overspilling the superscrollpane
		
		((GenerationTableUI)generation.getUI()).setColumnAnimationEnabled (true);
	}
	
	
	
	public void redoPedigreeFamilies (final Collection<JGeneration> generations, final HeritablePopulation hPop) {
		for (JGeneration generation : generations) {
			redoGenerationFamilies (generation, hPop);
		}
		
		//for (JGeneration generation : generations) {
		//	revalidateSuperScrollPane (generation);
		//}
	}
}
