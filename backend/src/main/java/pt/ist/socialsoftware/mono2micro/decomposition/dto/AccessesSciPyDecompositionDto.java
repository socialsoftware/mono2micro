package pt.ist.socialsoftware.mono2micro.decomposition.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

public class AccessesSciPyDecompositionDto extends DecompositionDto {
    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;
    private Map<String, Cluster> clusters = new HashMap<>();
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<String, Short> entityIDToClusterName = new HashMap<>();

    public AccessesSciPyDecompositionDto() {this.setStrategyType(ACCESSES_SCIPY);}

    public AccessesSciPyDecompositionDto(AccessesSciPyDecomposition decomposition) {
        this.setCodebaseName(decomposition.getStrategy().getCodebase().getName());
        this.setStrategyName(decomposition.getStrategy().getName());
        this.setName(decomposition.getName());
        this.setStrategyType(ACCESSES_SCIPY);
        this.setMetrics(decomposition.getMetrics());
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    public Map<String, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, Cluster> clusters) {
        this.clusters = clusters;
    }

    public Map<String, Functionality> getFunctionalities() {
        return functionalities;
    }

    public void setFunctionalities(Map<String, Functionality> functionalities) {
        this.functionalities = functionalities;
    }

    public Map<String, Short> getEntityIDToClusterName() {
        return entityIDToClusterName;
    }

    public void setEntityIDToClusterName(Map<String, Short> entityIDToClusterName) {
        this.entityIDToClusterName = entityIDToClusterName;
    }
}
