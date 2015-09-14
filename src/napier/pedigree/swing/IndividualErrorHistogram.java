package napier.pedigree.swing;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import napier.pedigree.io.PropertyConstants;
import napier.pedigree.model.ErrorCollator;
import napier.pedigree.model.ErrorMatrix;

import org.resspecies.inheritance.model.SNPMarker;

import util.Messages;


public class IndividualErrorHistogram extends AbstractErrorHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7957024616234574782L;


	
	public IndividualErrorHistogram () {
		this (null);
	}
	
	public IndividualErrorHistogram (final ErrorCollator errorModel) {
		super (errorModel);
		
		tooltipTemplateSingle = new MessageFormat (Messages.getString (PropertyConstants.TEXTPROPS, "IndividualHistogramTooltipText"));
		tooltipTemplateMultiple = new MessageFormat (Messages.getString (PropertyConstants.TEXTPROPS, "IndividualHistogramMultiTooltipText"));
	}
	
	
	public int[] getErrorCount (final ErrorCollator errorModel) {
		final ErrorMatrix overallErrorMap = (isUseFilteredData()
				? errorModel.getCurrentAllErrorMap () : errorModel.getInitialAllErrorMap());
		if (overallErrorMap == null) {
			return new int[] {0, 100};
		}
		
		final Collection<List<SNPMarker>> errors = overallErrorMap.getIndividualMap().values();
		final int[] errorCountsPrim = new int [errorModel.getInitialIndividualsSize()];
		
		// Fill the array with error counts of wrong individuals per marker
		final Iterator<List<SNPMarker>> iter = errors.iterator();
		for (int n = 0; n < errors.size(); n++) {
			final List<SNPMarker> errorList = iter.next();
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
