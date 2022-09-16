package pt.ist.socialsoftware.mono2micro.decomposition.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

public class AccessesSciPyDecompositionDto extends DecompositionDto {
    // For decomposition initialization
    private String similarityName;
    private String cutType;
    private float cutValue;

    // For usage
    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<String, Short> entityIDToClusterName = new HashMap<>();

    public AccessesSciPyDecompositionDto() {this.type = ACCESSES_SCIPY;}

    public AccessesSciPyDecompositionDto(AccessesSciPyDecomposition decomposition) {
        this.setCodebaseName(decomposition.getSimilarity().getStrategy().getCodebase().getName());
        this.setStrategyName(decomposition.getStrategy().getName());
        this.setName(decomposition.getName());
        this.type = ACCESSES_SCIPY;
        this.setMetrics(decomposition.getMetrics());
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
    }

    public String getSimilarityName() {
        return similarityName;
    }

    public void setSimilarityName(String similarityName) {
        this.similarityName = similarityName;
    }

    public String getCutType() {
        return cutType;
    }

    public void setCutType(String cutType) {
        this.cutType = cutType;
    }

    public float getCutValue() {
        return cutValue;
    }

    public void setCutValue(float cutValue) {
        this.cutValue = cutValue;
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
