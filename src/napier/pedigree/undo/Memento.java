package napier.pedigree.undo;

import java.io.Serializable;
import java.util.BitSet;


public interface Memento<T extends Object> extends Serializable {

	public void make (final T obj);

	public void restore (final T obj);
	
	public BitSet compare (final T obj);
}
