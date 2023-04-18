package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.*;

public class RepresentationInfoDtoFactory {
    public static List<RepresentationInfoDto> getRepresentationInfoDtos(List<RepresentationInformation> representationInformations) {
        List<RepresentationInfoDto> representationInfoDtos = new ArrayList<>();
        for (RepresentationInformation representationInformation : representationInformations)
            representationInfoDtos.add(getRepresentationInfoDto(representationInformation));
        return representationInfoDtos;
    }

    public  static RepresentationInfoDto getRepresentationInfoDto(RepresentationInformation representationInformation) {
        switch (representationInformation.getType()) {
            case ACCESSES_TYPE:
                return new AccessesInfoDto(representationInformation);
            case REPOSITORY_TYPE:
                return new RepositoryInfoDto(representationInformation);
            case CODE_EMBEDDINGS_TYPE:
                return new CodeEmbeddingsInfoDto(representationInformation);
            default:
                throw new RuntimeException("No known representation type: " + representationInformation.getType());
        }
    }
}
