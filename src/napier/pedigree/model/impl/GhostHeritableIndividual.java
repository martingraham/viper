package napier.pedigree.model.impl;

import org.resspecies.inheritance.model.HeritableIndividual;
import org.resspecies.inheritance.model.SNPMarker;

public class GhostHeritableIndividual extends HeritableIndividual {

	@Override
    public String getShortGenotypeString (final SNPMarker marker) {
        return "?/?";
    }
}
