package pt.ist.socialsoftware.mono2micro.recommendation.domain.algorithm;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RecommendationForSciPy {
    Strategy getStrategy();
    String getDecompositionType();
    String getLinkageType();
    Set<String> getSimilarityMatricesNames();
    Map<Short, String> getIDToEntityName(GridFsService gridFsService) throws Exception;
    void getDecompositionPropertiesForRecommendation(Decomposition decomposition) throws Exception;
    String getRecommendationResultName();
    void setCompleted(boolean completed);
    List<Weights> getWeightsList();
    default List<String> getWeightsNames() {
        List<String> weightsNames = new ArrayList<>();
        for (Weights weights : getWeightsList())
            weightsNames.addAll(weights.getWeightsNames());
        return weightsNames;
    }
}
