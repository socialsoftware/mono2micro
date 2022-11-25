package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

public class FunctionalityVectorizationByCallGraphInfoDto extends RepresentationInfoDto {
    public FunctionalityVectorizationByCallGraphInfoDto(RepresentationInfo representationInfo) {
        super(representationInfo.getType());
    }
}
