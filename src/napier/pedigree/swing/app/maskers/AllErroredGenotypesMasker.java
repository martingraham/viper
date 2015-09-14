package napier.pedigree.swing.app.maskers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.resspecies.inheritance.model.HeritablePopulation;
import org.resspecies.inheritance.model.SNPMarker;
import org.resspecies.model.Individual;

import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrix;

public class AllErroredGenotypesMasker {

	protected ErrorCollator errorModel;
	
	public AllErroredGenotypesMasker (final ErrorCollator errorModel) {
		this.errorModel = errorModel;
	}
	
	public void mask () {
		final HeritablePopulation hPop = errorModel.getPopCheckerContext().getPopulation();	
		final ErrorMatrix errorMatrix = errorModel.getCurrentAllErrorMap();
		final Map<SNPMarker, List<Individual>> errorMap = errorMatrix.getMarkerMap();
		
		/*
		 * All this commented-out timing was to discover that looking for a Marker in a TreeSet (O(log N) access) of
		 * umpteen thousand markers is a lot slower than looking for the non-presence of a Marker in a Set (O(1) access)
		 * of very few markers. Who'd have thought it eh?
		 */
		//int tot = 0;
		//long tmp, na1 = 0, na2 = 0, na3 = 0;
		
		for (Entry<SNPMarker, List<Individual>> errorMapEntry : errorMap.entrySet()) {
			final SNPMarker marker = errorMapEntry.getKey();
			//tmp = System.nanoTime();
			
			//if (hPop.getActiveMarkers().contains (marker)) {
			if (! hPop.getMaskedMarkers().contains(marker)) {
				//na1 += (System.nanoTime() - tmp);
				final List<Individual> individuals = errorMapEntry.getValue();
				for (Individual ind : individuals) {
					
					//tmp = System.nanoTime();
					if (!hPop.getMaskedIndividuals().contains (ind)) {
						//na2 += (System.nanoTime() - tmp);
						
						//tmp = System.nanoTime();
						hPop.maskGenotype (ind.getName(), marker.getName());
						//na3 += (System.nanoTime() - tmp);
						
						//tot++;
						//if (tot % 20 == 0) {
						//	System.err.println ("Masked "+tot+" genotypes");
						//	System.err.println ("activeMarkersContains "+(na1 / tot)/1E6+" ms");
						//	System.err.println ("maskedIndividualsContains "+(na2 / tot)/1E6+" ms");
						//	System.err.println ("maskGenotype "+(na3 / tot)/1E6+" ms");
						//}
					}
				}
			}
		}
	}
}
