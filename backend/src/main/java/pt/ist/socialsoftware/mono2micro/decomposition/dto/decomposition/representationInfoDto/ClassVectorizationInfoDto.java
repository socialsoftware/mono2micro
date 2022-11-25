package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

public class ClassVectorizationInfoDto extends RepresentationInfoDto {
    public ClassVectorizationInfoDto(RepresentationInfo representationInfo) {
        super(representationInfo.getType());
    }
}
