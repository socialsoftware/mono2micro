package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation;

import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.*;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy.*;


public class RepresentationInformationFactory {
    public static List<RepresentationInformation> getStrategyRepresentationInformations(Strategy strategy) {
        Set<String> representationTypes = new HashSet<>();

        for (String strategyType: strategy.getStrategyTypes()) {
            representationTypes.addAll(strategiesToRepresentationTypes.get(strategyType));
        }

        return representationTypes.stream()
                .map(representationType -> getRepresentationInformationFromType(representationType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static RepresentationInformation getRepresentationInformationFromType(String type) {
        switch (type) {
            case ACCESSES_TYPE:
                return new AccessesInformation();
            case REPOSITORY_TYPE:
                return new RepositoryInformation();
            case CODE_EMBEDDINGS_TYPE:
                return null;
            default:
                throw new RuntimeException("Unknown Representation type: " + type);
        }
    }
}
