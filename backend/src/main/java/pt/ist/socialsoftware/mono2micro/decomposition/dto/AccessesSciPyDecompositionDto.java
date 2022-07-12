package pt.ist.socialsoftware.mono2micro.decomposition.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Functionality;

import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

public class AccessesSciPyDecompositionDto extends DecompositionDto {
    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;
    private Map<Short, Cluster> clusters = new HashMap<>();
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, Short> entityIDToClusterID = new HashMap<>();

    public AccessesSciPyDecompositionDto() {this.setStrategyType(ACCESSES_SCIPY);}

    public AccessesSciPyDecompositionDto(AccessesSciPyDecomposition decomposition) {
        this.setName(decomposition.getName());
        this.setStrategyType(ACCESSES_SCIPY);
        this.setMetrics(decomposition.getMetrics());
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

    public Map<Short, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Map<Short, Cluster> clusters) {
        this.clusters = clusters;
    }

    public Map<String, Functionality> getFunctionalities() {
        return functionalities;
    }

    public void setFunctionalities(Map<String, Functionality> functionalities) {
        this.functionalities = functionalities;
    }

    public Map<Short, Short> getEntityIDToClusterID() {
        return entityIDToClusterID;
    }

    public void setEntityIDToClusterID(Map<Short, Short> entityIDToClusterID) {
        this.entityIDToClusterID = entityIDToClusterID;
    }
}
