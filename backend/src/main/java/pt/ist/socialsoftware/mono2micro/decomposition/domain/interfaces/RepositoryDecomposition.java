package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;

public interface RepositoryDecomposition {
    String REPOSITORY_DECOMPOSITION = "REPOSITORY_DECOMPOSITION";
    Strategy getStrategy();
    Similarity getSimilarity();
    Map<Short, String> getEntityIDToClusterName();
    Map<Short, Map<Short, Integer>> getCommitsInCommon();
    Map<Short, Integer> getTotalCommits();
    Map<Short, ArrayList<String>> getAuthors();
    void setAuthors(Map<Short, ArrayList<String>> authors);
    Integer getTotalAuthors();
    void setTotalAuthors(Integer totalAuthors);
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

    default void setupAuthorsAndCommits() throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
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

    default String getEdgeWeightsFromRepository(GridFsService gridFsService) throws JSONException, IOException {
        Dendrogram similarity = (Dendrogram) getSimilarity();
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(similarity.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entities = new ArrayList<>(getEntityIDToClusterName().keySet());

        JSONArray edgesJSON = new JSONArray();
        int k = 0;
        for (int i = 0; i < entities.size(); i++) {
            short e1ID = entities.get(i);
            Map<Short, Integer> commitsInCommon = getCommitsInCommon().get(e1ID);
            if (commitsInCommon == null)
                continue;

            for (int j = i + 1; j < entities.size(); j++) {
                short e2ID = entities.get(j);
                Integer numberOfCommits = commitsInCommon.get(e2ID);
                if (numberOfCommits == null)
                    continue;

                JSONObject edgeJSON = new JSONObject();
                if (e1ID < e2ID) {
                    edgeJSON.put("e1ID", e1ID); edgeJSON.put("e2ID", e2ID);
                }
                else {
                    edgeJSON.put("e1ID", e2ID); edgeJSON.put("e1ID", e2ID);
                }
                edgeJSON.put("dist", copheneticDistances.getDouble(k));

                // Since the frontend counts the length of the array, numbers are added to count as independent commits
                JSONArray commitJSON = new JSONArray();
                for (int l = 0; l < numberOfCommits; l++)
                    commitJSON.put(l);
                edgeJSON.put("commits", commitJSON);

                edgesJSON.put(edgeJSON);
                k++;
            }
        }
        return edgesJSON.toString();
    }
}
