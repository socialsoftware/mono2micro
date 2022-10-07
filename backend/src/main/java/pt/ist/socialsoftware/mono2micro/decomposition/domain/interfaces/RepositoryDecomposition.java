package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RepositoryDecomposition {
    String REPOSITORY_DECOMPOSITION = "REPOSITORY_DECOMPOSITION";
    String getName();
    String getStrategyType();
    Strategy getStrategy();
    Similarity getSimilarity();
    Map<String, Cluster> getClusters();
    Map<Short, String> getEntityIDToClusterName();
    Map<Short, Map<Short, Integer>> getCommitsInCommon();
    Map<Short, Integer> getTotalCommits();
    Map<Short, ArrayList<String>> getAuthors();
    void setAuthors(Map<Short, ArrayList<String>> authors);
    Integer getTotalAuthors();
    void setTotalAuthors(Integer totalAuthors);
    void setMetrics(Map<String, Object> metrics);
    void addMetric(String metricType, Object metricValue);
    default List<String> getAuthorsFromId(Short id) {
        return getAuthors().get(id);
    }
    default void addCommitInCommon(Short entityId, Short commonEntityId, Integer totalCommits) {
        Map<Short, Map<Short, Integer>> commitsInCommon = getCommitsInCommon();
        Map<Short, Integer> commits = commitsInCommon.getOrDefault(entityId, new HashMap<>());
        commits.put(commonEntityId, totalCommits);
        commitsInCommon.put(entityId, commits);
    }
    default void addTotalCommit(Short entityId, Integer totalCommits) {
        getTotalCommits().put(entityId, totalCommits);
    }
}
