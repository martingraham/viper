package napier.pedigree.model;

/**
 * 
 * @author cs22
 *
 * Interface that returns data on the implementing model
 * Basically, for a generation are the males/females/both polygamous or not.
 */
public interface PolygamyState {

	public boolean isMalePolygamous ();
	
	public boolean isFemalePolygamous ();
	
	/**
	 * @return true if either males or females are polygamous
	 */
	public boolean isPolygamous ();
	
	/**
	 * @return true if both males and females are polygamous
	 */
	public boolean isDualPolygamous ();
}
