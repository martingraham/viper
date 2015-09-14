package napier.pedigree.model;

import java.util.EventObject;


public class ErrorMatrixEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5894060564136654631L;

	
	public ErrorMatrixEvent (final ErrorMatrix source) {
		super (source);
	}
}
