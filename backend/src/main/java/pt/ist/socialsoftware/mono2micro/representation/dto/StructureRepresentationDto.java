package pt.ist.socialsoftware.mono2micro.representation.dto;

import pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation;

import java.util.Map;
import java.util.Set;

public class StructureRepresentationDto extends RepresentationDto {
    private Map<String, Set<String>> profiles;

    public StructureRepresentationDto(StructureRepresentation representation) {
        super(representation);
        this.profiles = representation.getProfiles();
    }

    public Map<String, Set<String>> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, Set<String>> profiles) {
        this.profiles = profiles;
    }
}
