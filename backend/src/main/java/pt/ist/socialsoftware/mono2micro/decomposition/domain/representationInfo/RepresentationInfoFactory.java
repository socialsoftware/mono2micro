package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.ClassVectorizationInfo.CLASS_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.EntityVectorizationInfo.ENTITY_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationByCallGraphInfo.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationBySequenceOfAccessesInfo.FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;

public class RepresentationInfoFactory {
    public static List<RepresentationInfo> getRepresentationInfosFromType(Strategy strategy) {
        List<RepresentationInfo> representationInfos = new ArrayList<>();
        List<String> representationFiles = strategy.getCodebase()
                                                    .getRepresentations()
                                                    .stream()
                                                    .map(Representation::getType)
                                                    .collect(Collectors.toList());

        Map<String, List<String>> representationInfoTypeToFiles = RepresentationInfoType.representationInfoTypeToFiles;

        for (Map.Entry<String, List<String>> entry : representationInfoTypeToFiles.entrySet()) {
            if (representationFiles.containsAll(entry.getValue())) {
                representationInfos.add(getRepresentationInfoFromType(entry.getKey()));
            }
        }

        return representationInfos;
    }

    public static RepresentationInfo getRepresentationInfoFromType(String type) {
        switch (type) {
            case ACCESSES_INFO:
                return new AccessesInfo();
            case REPOSITORY_INFO:
                return new RepositoryInfo();
            case CLASS_VECTORIZATION_INFO:
                return new ClassVectorizationInfo();
            case ENTITY_VECTORIZATION_INFO:
                return new EntityVectorizationInfo();
            case FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO:
                return new FunctionalityVectorizationByCallGraphInfo();
            case FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO:
                return new FunctionalityVectorizationBySequenceOfAccessesInfo();
            default:
                throw new RuntimeException("Unknown Representation Info type: " + type);
        }
    }
}
