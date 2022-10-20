package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;

import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoDecomposition.ACC_AND_REPO_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesDecompositionDto.class, name = ACCESSES_DECOMPOSITION),
        @JsonSubTypes.Type(value = RepositoryDecompositionDto.class, name = REPOSITORY_DECOMPOSITION),
        @JsonSubTypes.Type(value = AccAndRepoDecompositionDto.class, name = ACC_AND_REPO_DECOMPOSITION),
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
