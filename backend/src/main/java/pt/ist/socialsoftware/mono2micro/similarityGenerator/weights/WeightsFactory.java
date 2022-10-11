package pt.ist.socialsoftware.mono2micro.similarityGenerator.weights;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.AccessesWeights.ACCESSES_WEIGHTS;
import static pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.RepositoryWeights.REPOSITORY_WEIGHTS;

public class WeightsFactory {
    public static Weights getWeights(String type) {
        switch (type) {
            case ACCESSES_WEIGHTS:
                return new AccessesWeights();
            case REPOSITORY_WEIGHTS:
                return new RepositoryWeights();
            default:
                throw new RuntimeException("The type \"" + type + "\" is not a valid weight type.");
        }
    }

    public static List<Weights> getWeightsList(List<String> types) {
        List<Weights> weightsList = new ArrayList<>();
        for (String weightsType : types)
            weightsList.add(getWeights(weightsType));
        return weightsList;
    }
}
