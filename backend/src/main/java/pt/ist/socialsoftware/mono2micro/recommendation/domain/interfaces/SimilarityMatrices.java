package pt.ist.socialsoftware.mono2micro.recommendation.domain.interfaces;

import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface SimilarityMatrices {
    String SIMILARITY_MATRICES = "SIMILARITY_MATRICES";
    Strategy getStrategy();
    String getName();
    List<Weights> getWeightsList();
    Set<String> getSimilarityMatricesNames();
    default void addSimilarityMatrixName(String similarityMatrixName) {
        getSimilarityMatricesNames().add(similarityMatrixName);
    }

    default List<String> getWeightsNames() {
        List<String> weightsNames = new ArrayList<>();
        for (Weights weights : getWeightsList())
            weightsNames.addAll(weights.getWeightsNames());
        return weightsNames;
    }

    default int getTotalNumberOfWeights() {
        return getWeightsList().stream().reduce(0, (totalNumberOfWeights, weights) -> totalNumberOfWeights + weights.getNumberOfWeights(), Integer::sum);
    }

    default List<String> getAllWeightsTypes() {
        return getWeightsList().stream().map(Weights::getType).collect(Collectors.toList());
    }
}
