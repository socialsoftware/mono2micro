package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityGenerator;

import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;
import java.util.Set;

public interface SimilarityMatrix {
    Strategy getStrategy();
    String getName();
    List<Weights> getWeightsList();
    String getSimilarityMatrixName();
    void setSimilarityMatrixName(String similarityMatrixName);

    Set<Short> fillElements(GridFsService gridFsService) throws Exception; // Returns a Set of the entityIDs

    default int getTotalNumberOfWeights() {
        return getWeightsList().stream().reduce(0, (totalNumberOfWeights, weights) -> totalNumberOfWeights + weights.getNumberOfWeights(), Integer::sum);
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
