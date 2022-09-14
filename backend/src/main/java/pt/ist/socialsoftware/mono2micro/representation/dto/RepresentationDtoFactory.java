package pt.ist.socialsoftware.mono2micro.representation.dto;

import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.util.ArrayList;
import java.util.List;

public class RepresentationDtoFactory {
    private static RepresentationDtoFactory factory = null;

    public static RepresentationDtoFactory getFactory() {
        if (factory == null)
            factory = new RepresentationDtoFactory();
        return factory;
    }

    public RepresentationDto getRepresentationDto(Representation representation) {
        if (representation == null)
            return null;
        switch (representation.getType()) {
            case AccessesRepresentation.ACCESSES:
                return new AccessesRepresentationDto((AccessesRepresentation) representation);
            case IDToEntityRepresentation.ID_TO_ENTITY:
                return new IDToEntityRepresentationDto((IDToEntityRepresentation) representation);
            default:
                throw new RuntimeException("The type \"" + representation.getType() + "\" is not a valid representation type.");
        }
    }

    public List<RepresentationDto> getRepresentationDtos(List<Representation> representations) {
        if (representations == null)
            return null;
        List<RepresentationDto> representationDtos = new ArrayList<>();
        for (Representation representation : representations)
            representationDtos.add(getRepresentationDto(representation));
        return representationDtos;
    }
}
