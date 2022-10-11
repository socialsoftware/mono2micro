package pt.ist.socialsoftware.mono2micro.recommendation.domain.similarityGenerator;

import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface SimilarityMatrices {
    String SIMILARITY_MATRICES = "SIMILARITY_MATRICES";
    Strategy getStrategy();
    String getName();
    List<Weights> getWeightsList();
    Set<String> getSimilarityMatricesNames();

    Set<Short> fillElements(GridFsService gridFsService) throws Exception; // Returns a Set of the entityIDs

    default void addSimilarityMatrixName(String similarityMatrixName) {
        getSimilarityMatricesNames().add(similarityMatrixName);
    }

    default int getTotalNumberOfWeights() {
        return getWeightsList().stream().reduce(0, (totalNumberOfWeights, weights) -> totalNumberOfWeights + weights.getNumberOfWeights(), Integer::sum);
    }

    default List<String> getAllWeightsTypes() {
        return getWeightsList().stream().map(Weights::getType).collect(Collectors.toList());
    }
}
