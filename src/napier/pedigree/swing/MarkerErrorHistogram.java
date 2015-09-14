package napier.pedigree.swing;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrix;

import org.resspecies.model.Individual;

import util.Messages;


public class MarkerErrorHistogram extends AbstractErrorHistogram {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 519830990455865352L;
	
	
	public MarkerErrorHistogram () {
		this (null);
	}
	
	public MarkerErrorHistogram (final ErrorCollator errorModel) {
		super (errorModel);
		
		tooltipTemplateSingle = new MessageFormat (Messages.getString (PropertyConstants.TEXTPROPS, "MarkerHistogramTooltipText"));
		tooltipTemplateMultiple = new MessageFormat (Messages.getString (PropertyConstants.TEXTPROPS, "MarkerHistogramMultiTooltipText"));
	}
	
	
	public int[] getErrorCount (final ErrorCollator errorModel) {
		final ErrorMatrix overallErrorMap = (isUseFilteredData()
				? errorModel.getCurrentAllErrorMap () : errorModel.getInitialAllErrorMap());
		if (overallErrorMap == null) {
			return new int[] {0, 100};
		}
		final Collection<List<Individual>> errors = overallErrorMap.getMarkerMap().values();
		/*
		final int[] errorCountsPrim = new int [errors.size()];
		final Iterator<List<Individual>> iter = errors.iterator();
		for (int n = 0; n < errorCountsPrim.length; n++) {
			final List<Individual> errorList = iter.next();
			errorCountsPrim [n] = errorList.size();
		}
		*/

		final int[] errorCountsPrim = new int [errorModel.getInitialMarkerSize()];
		
		// Fill the array with error counts of wrong individuals per marker
		final Iterator<List<Individual>> iter = errors.iterator();
		for (int n = 0; n < errors.size(); n++) {
			final List<Individual> errorList = iter.next();
			errorCountsPrim [n] = errorList.size();
		}
		
		// Fill the remainder of the array to the marker total with zeroes
		// to represent markers with no errors
		Arrays.fill (errorCountsPrim, errors.size(), errorCountsPrim.length, 0);
		//for (int n = errors.size(); n < errorModel.getMarkerSize(); n++) {
		//	errorCountsPrim [n] = 0;
		//}
		return errorCountsPrim;
	}
}
