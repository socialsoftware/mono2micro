package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;

import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesSciPyDecompositionDto.class, name = ACCESSES_SCIPY),
        @JsonSubTypes.Type(value = RepositorySciPyDecompositionDto.class, name = REPOSITORY_SCIPY),
        @JsonSubTypes.Type(value = AccAndRepoSciPyDecompositionDto.class, name = ACC_AND_REPO_SCIPY),
})
public abstract class DecompositionDto {
    String name;
    String codebaseName;
    String strategyName;
    String type;
    Map<String, Object> metrics;
    Map<String, Cluster> clusters = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
    public Map<String, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, Cluster> clusters) {
        this.clusters = clusters;
    }

}
