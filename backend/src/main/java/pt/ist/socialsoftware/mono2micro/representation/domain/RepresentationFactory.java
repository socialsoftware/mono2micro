package pt.ist.socialsoftware.mono2micro.representation.domain;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;

public class RepresentationFactory {
    public static Representation getRepresentation(String representationType) {
        switch (representationType) {
            case ACCESSES:
                return new AccessesRepresentation();
            case ID_TO_ENTITY:
                return new IDToEntityRepresentation();
            case AUTHOR:
                return new AuthorRepresentation();
            case COMMIT:
                return new CommitRepresentation();
            default:
                throw new RuntimeException("The type \"" + representationType + "\" is not a valid representation type.");
        }
    }
}