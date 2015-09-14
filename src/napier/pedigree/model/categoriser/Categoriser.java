package napier.pedigree.model.categoriser;

/**
 * 
 * @author cs22
 *
 * Interface that takes in an object and outputs a numerical value in response.
 * The aim is to use this interface to segregate objects into
 * a small range of numerical values for various purposes.
 *
 * @param <T>
 */
public interface Categoriser<T> {

	
	/**
	 * @return minimum value permitted by this Categoriser
	 */
	public int getMinValue ();
	
	
	/**
	 * @return maximum value permitted by this Categoriser
	 */
	public int getMaxValue ();
	
	
	/**
	 * @return number of distinct values permitted by this Categoriser
	 */
	public int getRange ();
	
	
	/**
	 * @param obj
	 * @return int value representing how this Categoriser categorises the parameterised Object obj
	 */
	public int categorise (T obj);
	
	
	/**
	 * @param value
	 * @return String descriptor of the category represented by the int value
	 */
	public String getDescription (int value);
}
