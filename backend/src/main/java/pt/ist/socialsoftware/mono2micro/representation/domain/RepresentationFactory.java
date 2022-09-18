package pt.ist.socialsoftware.mono2micro.representation.domain;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

public class RepresentationFactory {
    private static RepresentationFactory factory = null;

    public static RepresentationFactory getFactory() {
        if (factory == null)
            factory = new RepresentationFactory();
        return factory;
    }

    public Representation getRepresentation(String representationType) {
        switch (representationType) {
            case ACCESSES:
                return new AccessesRepresentation();
            case ID_TO_ENTITY:
                return new IDToEntityRepresentation();
            default:
                throw new RuntimeException("The type \"" + representationType + "\" is not a valid representation type.");
        }
    }
}
