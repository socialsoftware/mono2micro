package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.ClassVectorizationInfo.CLASS_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.EntityVectorizationInfo.ENTITY_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationByCallGraphInfo.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationBySequenceOfAccessesInfo.FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;

public class RepresentationInfoDtoFactory {
    public static List<RepresentationInfoDto> getRepresentationInfoDtos(List<RepresentationInfo> representationInfos) {
        List<RepresentationInfoDto> representationInfoDtos = new ArrayList<>();
        for (RepresentationInfo representationInfo : representationInfos)
            representationInfoDtos.add(getRepresentationInfoDto(representationInfo));
        return representationInfoDtos;
    }

    public  static RepresentationInfoDto getRepresentationInfoDto(RepresentationInfo representationInfo) {
        switch (representationInfo.getType()) {
            case ACCESSES_INFO:
                return new AccessesInfoDto(representationInfo);
            case REPOSITORY_INFO:
                return new RepositoryInfoDto(representationInfo);
            case CLASS_VECTORIZATION_INFO:
                return new ClassVectorizationInfoDto(representationInfo);
            case ENTITY_VECTORIZATION_INFO:
                return new EntityVectorizationInfoDto(representationInfo);
            case FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO:
                return new FunctionalityVectorizationByCallGraphInfoDto(representationInfo);
            case FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO:
                return new FunctionalityVectorizationBySequenceOfAccessesInfoDto(representationInfo);
            default:
                throw new RuntimeException("No known representation info type: " + representationInfo.getType());
        }
    }
}
