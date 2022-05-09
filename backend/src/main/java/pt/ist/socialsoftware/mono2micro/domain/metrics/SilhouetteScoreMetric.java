package pt.ist.socialsoftware.mono2micro.domain.metrics;

import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RECOMMEND_FOLDER;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

public class SilhouetteScoreMetric extends Metric<Float> {
    public String getType() {
        return Metric.MetricType.SILHOUETTE_SCORE;
    }

    public void calculateMetric(Decomposition decomposition) throws Exception {

        switch (decomposition.getStrategyType()) {
            case ACCESSES_SCIPY:
                if (((AccessesSciPyDecomposition)decomposition).isExpert()) {
                    this.value = 0F;
                    return;
                }

                JSONObject clustersJSON;

                if (decomposition.getStrategyName().startsWith(RECOMMENDATION_ACCESSES_SCIPY))
                    clustersJSON = CodebaseManager.getInstance().getClusters(decomposition.getCodebaseName(), RECOMMEND_FOLDER, decomposition.getStrategyName(), decomposition.getName());
                else clustersJSON = CodebaseManager.getInstance().getClusters(decomposition.getCodebaseName(), STRATEGIES_FOLDER, decomposition.getStrategyName(), decomposition.getName());
                this.value = (float) clustersJSON.getDouble("silhouetteScore");
                break;
            default:
                throw new RuntimeException("Decomposition strategy '" + decomposition.getStrategyType() + "' not known.");
        }
    }

    public void calculateMetric(Decomposition decomposition, Functionality functionality) {}

    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {}
}