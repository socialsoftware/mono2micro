package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

public class AccessesInfoDto extends RepresentationInfoDto {
    public AccessesInfoDto(RepresentationInfo representationInfo) {
        super(representationInfo.getType());
    }
}
