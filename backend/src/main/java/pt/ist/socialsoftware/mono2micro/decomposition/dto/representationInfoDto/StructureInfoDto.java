package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;

public class StructureInfoDto extends RepresentationInfoDto {
    public StructureInfoDto(RepresentationInformation representationInformation) {
        super(representationInformation.getType());
    }
}
