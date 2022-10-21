package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;

public class RepositoryInfo extends RepresentationInformation {
    public static final String REPOSITORY_INFO = "REPOSITORY_INFO";
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();
    private Integer totalAuthors;

    public RepositoryInfo() {}

    public RepositoryInfo(Decomposition decomposition) throws IOException {
        this.decompositionName = decomposition.getName();
        decomposition.addRepresentationInformation(this);
        setupAuthorsAndCommits(decomposition);
    }

    public RepositoryInfo(Decomposition snapshotDecomposition, Decomposition decomposition) {
        this.decompositionName = snapshotDecomposition.getName();
        snapshotDecomposition.addRepresentationInformation(this);
        RepositoryInfo repositoryInfo = (RepositoryInfo) decomposition.getRepresentationInformationByType(REPOSITORY_INFO);
        this.authors = repositoryInfo.getAuthors();
        this.commitsInCommon = repositoryInfo.getCommitsInCommon();
        this.totalCommits = repositoryInfo.getTotalCommits();
        this.totalAuthors = repositoryInfo.getTotalAuthors();
    }

    @Override
    public String getType() {
        return REPOSITORY_INFO;
    }

    @Override
    public void update(Decomposition decomposition) throws Exception {}

    @Override
    public void deleteProperties() {}


    public Map<Short, ArrayList<String>> getAuthors() {
        return authors;
    }

    public void setAuthors(Map<Short, ArrayList<String>> authors) {
        this.authors = authors;
    }

    public Map<Short, Map<Short, Integer>> getCommitsInCommon() {
        return commitsInCommon;
    }

    public void setCommitsInCommon(Map<Short, Map<Short, Integer>> commitsInCommon) {
        this.commitsInCommon = commitsInCommon;
    }

    public Map<Short, Integer> getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(Map<Short, Integer> totalCommits) {
        this.totalCommits = totalCommits;
    }

    public Integer getTotalAuthors() {
        return totalAuthors;
    }

    public void setTotalAuthors(Integer totalAuthors) {
        this.totalAuthors = totalAuthors;
    }

    public List<String> getAuthorsFromId(Short id) {
        return getAuthors().get(id);
    }
    public void addCommitInCommon(Short entityId, Short commonEntityId, Integer totalCommits) {
        Map<Short, Map<Short, Integer>> commitsInCommon = getCommitsInCommon();
        Map<Short, Integer> commits = commitsInCommon.getOrDefault(entityId, new HashMap<>());
        commits.put(commonEntityId, totalCommits);
        commitsInCommon.put(entityId, commits);
    }
    public void addTotalCommit(Short entityId, Integer totalCommits) {
        getTotalCommits().put(entityId, totalCommits);
    }

    private void setupAuthorsAndCommits(Decomposition decomposition) throws IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        AuthorRepresentation authorRepresentation = (AuthorRepresentation) decomposition.getStrategy().getCodebase().getRepresentationByType(AUTHOR);
        CommitRepresentation commitRepresentation = (CommitRepresentation) decomposition.getStrategy().getCodebase().getRepresentationByType(COMMIT);
        Map<Short, ArrayList<String>> authors = new ObjectMapper().readValue(gridFsService.getFileAsString(authorRepresentation.getName()), new TypeReference<Map<Short, ArrayList<String>>>() {});
        Map<String, Map<String, Integer>> commits = new ObjectMapper().readValue(gridFsService.getFileAsString(commitRepresentation.getName()), new TypeReference<Map<String, Map<String, Integer>>>() {});

        setAuthors(authors);
        extractNumberOfAuthors(authors);
        extractCommitsInCommon(decomposition, commits);
    }

    private void extractNumberOfAuthors(Map<Short, ArrayList<String>> filesAndAuthors) {
        Set<String> allAuthors = new HashSet<>();
        for (ArrayList<String> authors : filesAndAuthors.values())
            allAuthors.addAll(authors);
        setTotalAuthors(allAuthors.size());
    }

    private void extractCommitsInCommon(Decomposition decomposition, Map<String, Map<String, Integer>> commits) {
        Set<Short> entityIds = decomposition.getEntityIDToClusterName().keySet();
        for (Short entityId : entityIds) {
            Map<String, Integer> commonEntities = commits.get(entityId.toString());
            if (commonEntities == null) {
                addTotalCommit(entityId, 1);
                Map<Short, Map<Short, Integer>> commitsInCommon = getCommitsInCommon();
                commitsInCommon.put(entityId, new HashMap<>());
                continue;
            }
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

    @Override
    public String getEdgeWeights(Decomposition decomposition) throws JSONException, IOException {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        Dendrogram dendrogram = ((SimilarityMatrixSciPy) decomposition.getSimilarity()).getDendrogram();
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(dendrogram.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entities = new ArrayList<>(decomposition.getEntityIDToClusterName().keySet());

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

    @Override
    public String getSearchItems(Decomposition decomposition) throws JSONException {
        JSONArray searchItems = new JSONArray();

        for (Cluster cluster : decomposition.getClusters().values()) {
            JSONObject clusterItem = new JSONObject();
            clusterItem.put("name", cluster.getName());
            clusterItem.put("type", "Cluster");
            clusterItem.put("id", cluster.getName());
            clusterItem.put("entities", Integer.toString(cluster.getElements().size()));
            searchItems.put(clusterItem);

            for (Element element : cluster.getElements()) {
                JSONObject entityItem = new JSONObject();
                entityItem.put("name", element.getName());
                entityItem.put("type", "Entity");
                entityItem.put("id", String.valueOf(element.getId()));
                entityItem.put("cluster", cluster.getName());
                searchItems.put(entityItem);
            }
        }

        return searchItems.toString();
    }
}
