package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

public class EntityVectorizationInfoDto extends RepresentationInfoDto {
    public EntityVectorizationInfoDto(RepresentationInfo representationInfo) {
        super(representationInfo.getType());
    }
}
