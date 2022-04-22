package pt.ist.socialsoftware.mono2micro.domain.metrics;

import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

public class SilhouetteScoreMetric extends Metric<Float> {
    public String getType() {
        return Metric.MetricType.SILHOUETTE_SCORE;
    }

    public void calculateMetric(Decomposition decomposition) throws Exception {

        System.out.println("Accessing silhouette score...");

        switch (decomposition.getStrategyType()) {
            case Strategy.StrategyType.ACCESSES_SCIPY:
                if (((AccessesSciPyDecomposition)decomposition).isExpert()) {
                    this.value = 0F;
                    return;
                }

                JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(decomposition.getCodebaseName(), decomposition.getStrategyName(), decomposition.getName());
                this.value = (float) clustersJSON.getDouble("silhouetteScore");
                break;
            default:
                throw new RuntimeException("Decomposition strategy '" + decomposition.getStrategyType() + "' not known.");
        }
    }

    public void calculateMetric(Decomposition decomposition, Functionality functionality) {}

    public void calculateMetric(Decomposition decomposition, Functionality functionality, FunctionalityRedesign functionalityRedesign) {}
}