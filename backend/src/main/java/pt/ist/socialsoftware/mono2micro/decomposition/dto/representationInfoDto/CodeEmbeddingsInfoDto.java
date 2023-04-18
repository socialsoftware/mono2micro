package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;

public class CodeEmbeddingsInfoDto extends RepresentationInfoDto {
    public CodeEmbeddingsInfoDto(RepresentationInformation representationInformation) {
        super(representationInformation.getType());
    }
}
