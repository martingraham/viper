package napier.pedigree.swing.app.actions;

import java.util.HashSet;
import java.util.Set;

import javax.swing.KeyStroke;

import org.resspecies.inheritance.model.SNPMarker;

import napier.pedigree.model.ChangeOnNextRecalcStore;
import napier.pedigree.model.ErrorCollator;

abstract public class AbstractMaskingAction extends AbstractFamilyCentricAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1964490395854365073L;
	
	protected ErrorCollator errorModel;
	
	public AbstractMaskingAction (final KeyStroke keyStroke, final String actionPrefix, final ErrorCollator errorModel) {
		super (keyStroke, actionPrefix);
		this.errorModel = errorModel;
	}

	protected void tellErrorModel () {
		if (actionAffectsAllMarkers ()) {	// if ind getting affected across all markers
			errorModel.setRecalculationNeeded (true, ChangeOnNextRecalcStore.IND_CHANGE);
		} else {
			final Set<SNPMarker> singleMarkerSet = new HashSet<SNPMarker> ();
			singleMarkerSet.add (errorModel.getPopCheckerContext().getFocusMarker());
			errorModel.setRecalculationNeeded (true, ChangeOnNextRecalcStore.MARKER_CHANGE, singleMarkerSet);
		}
	}
	
	protected boolean actionAffectsAllMarkers () {
		return errorModel.getPopCheckerContext().getFocusMarker() == null;
	}
}
