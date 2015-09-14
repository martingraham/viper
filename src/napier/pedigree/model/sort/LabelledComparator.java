package napier.pedigree.model.sort;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Properties;

import util.Messages;

/**
 * Get nice toString from cached properties file using simple class name as key
 * @author Martin
 *
 * @param <T>
 */
public abstract class LabelledComparator<T> implements Comparator<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7862126994965012796L;
	protected static final Properties LABELPROPERTIES = Messages.makeProperties ("sortLabels", LabelledComparator.class, true);
	
	@Override
	public String toString () {
		return LABELPROPERTIES.getProperty (getClass().getSimpleName());
		//return Messages.getString ("napier.pedigree.model.sort.sortlabels", getClass().getSimpleName());
	}
}
