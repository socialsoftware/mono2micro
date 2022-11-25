package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

public class FunctionalityVectorizationBySequenceOfAccessesInfoDto extends RepresentationInfoDto {
    public FunctionalityVectorizationBySequenceOfAccessesInfoDto(RepresentationInfo representationInfo) {
        super(representationInfo.getType());
    }
}
