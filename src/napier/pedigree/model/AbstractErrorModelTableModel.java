package napier.pedigree.model;

import javax.swing.table.AbstractTableModel;




public abstract class AbstractErrorModelTableModel extends AbstractTableModel implements ErrorMatrixListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4828062545053434972L;
	
	protected ErrorCollator errorModel;
	
	
	public AbstractErrorModelTableModel () {
		this (null);
	}
	
	protected AbstractErrorModelTableModel (final ErrorCollator errorModel) {
		super ();
		this.errorModel = errorModel;
	}
	
	public ErrorCollator getErrorModel () { return errorModel; }
}
