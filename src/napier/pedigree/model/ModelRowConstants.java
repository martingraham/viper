package napier.pedigree.model;

/**
 * Class of constants for stating which row is which in the PedigreeGenerationModel data
 * @author cs22
 *
 */
public final class ModelRowConstants {
	private ModelRowConstants () {}
	
	public final static int SIRE_SPLITS = 0;
	public final static int SIRE = 1;
	public final static int OFFSPRING = 2;
	public final static int DAM = 3;
	public final static int DAM_SPLITS = 4;
	public final static int OFFSPRING_SPLIT_START = 5;
}
