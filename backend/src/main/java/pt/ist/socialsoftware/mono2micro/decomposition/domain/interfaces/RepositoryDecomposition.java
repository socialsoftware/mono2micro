package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;

public interface RepositoryDecomposition {
    String REPOSITORY_DECOMPOSITION = "REPOSITORY_DECOMPOSITION";
    String getName();
    String getType();
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

    default void setupAuthorsAndCommits(GridFsService gridFsService) throws IOException {
        AuthorRepresentation authorRepresentation = (AuthorRepresentation) getStrategy().getCodebase().getRepresentationByType(AUTHOR);
        CommitRepresentation commitRepresentation = (CommitRepresentation) getStrategy().getCodebase().getRepresentationByType(COMMIT);
        Map<Short, ArrayList<String>> authors = new ObjectMapper().readValue(gridFsService.getFileAsString(authorRepresentation.getName()), new TypeReference<Map<Short, ArrayList<String>>>() {});
        Map<String, Map<String, Integer>> commits = new ObjectMapper().readValue(gridFsService.getFileAsString(commitRepresentation.getName()), new TypeReference<Map<String, Map<String, Integer>>>() {});

        setAuthors(authors);
        extractNumberOfAuthors(authors);
        extractCommitsInCommon(commits);
    }

    default void extractNumberOfAuthors(Map<Short, ArrayList<String>> filesAndAuthors) {
        Set<String> allAuthors = new HashSet<>();
        for (ArrayList<String> authors : filesAndAuthors.values())
            allAuthors.addAll(authors);
        setTotalAuthors(allAuthors.size());
    }

    default void extractCommitsInCommon(Map<String, Map<String, Integer>> commits) {
        Set<Short> entityIds = getEntityIDToClusterName().keySet();
        for (Short entityId : entityIds) {
            Map<String, Integer> commonEntities = commits.get(entityId.toString());
            addTotalCommit(entityId, commonEntities.get("total_commits"));

            for (Map.Entry<String, Integer> commonEntity : commonEntities.entrySet()) {
                if (!commonEntity.getKey().equals("total_commits")) {
                    Short commonEntityId = Short.parseShort(commonEntity.getKey());
                    if (entityIds.contains(commonEntityId))
                        addCommitInCommon(entityId, commonEntityId, commonEntity.getValue());
                }
            }
        }
    }
}
