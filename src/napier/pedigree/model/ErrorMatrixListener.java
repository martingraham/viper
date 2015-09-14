package napier.pedigree.model;

import java.util.EventListener;

/**
 * This doesn't get used at the moment
 * @author cs22
 */

public interface ErrorMatrixListener extends EventListener {

    /**
     * When an error is added to an ErrorMatrix, registered listeners
     * are notified via this method
     */
    void errorMatrixChanged (ErrorMatrixEvent emme);
}
