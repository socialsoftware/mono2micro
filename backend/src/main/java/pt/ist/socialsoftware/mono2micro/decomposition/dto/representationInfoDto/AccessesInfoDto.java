package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;

public class AccessesInfoDto extends RepresentationInfoDto {
    public AccessesInfoDto(RepresentationInformation representationInformation) {
        super(representationInformation.getType());
    }
}
