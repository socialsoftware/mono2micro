package pt.ist.socialsoftware.mono2micro.similarity.domain.generator;

import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;
import java.util.stream.Collectors;

public interface SimilarityMatrix {
    String SIMILARITY_MATRIX = "SIMILARITY_MATRIX";
    Strategy getStrategy();
    String getName();
    List<Weights> getWeightsList();
    void setWeightsList(List<Weights> weightsList);
    String getSimilarityMatrixName();
    void setSimilarityMatrixName(String similarityMatrixName);

    default int getTotalNumberOfWeights() {
        return getWeightsList().stream().reduce(0, (totalNumberOfWeights, weights) -> totalNumberOfWeights + weights.getNumberOfWeights(), Integer::sum);
    }

    default List<String> getAllWeightsTypes() {
        return getWeightsList().stream().map(Weights::getType).collect(Collectors.toList());
    }

    default Weights getWeightsByType(String type) {
        return getWeightsList().stream().filter(weight -> weight.getType().equals(type)).findFirst().orElse(null);
    }

    default boolean hasSameWeights(List<Weights> weightsList) {
        return weightsList.stream().allMatch(weights -> {
            Weights w = getWeightsByType(weights.getType());
            if (w == null)
                return false;
            return weights.equals(w);
        });
    }

    default float[] getWeightsAsArray() {
        float[] allWeightsAsArray = new float[getTotalNumberOfWeights()];
        int i = 0;

        for (Weights weights : getWeightsList()) {
            float[] weightsAsArray = weights.getWeights();
            for (float weight : weightsAsArray) allWeightsAsArray[i++] = weight;
        }
        return allWeightsAsArray;
    }
}
