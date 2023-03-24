package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.*;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.SciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.element.DomainEntity;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipy;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.AccessesInfo.ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.ClassVectorizationInfo.CLASS_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.EntityVectorizationInfo.ENTITY_VECTORIZATION_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationByCallGraphInfo.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.FunctionalityVectorizationBySequenceOfAccessesInfo.FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo.REPOSITORY_INFO;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

public class SciPyClustering extends Clustering {
    public static final String SCIPY = "SciPy Clustering";
    static final int MIN_CLUSTERS = 3, CLUSTER_STEP = 1;

    private final GridFsService gridFsService;

    public SciPyClustering() {
        this.gridFsService = ContextManager.get().getBean(GridFsService.class);
    }

    @Override
    public String getType() {
        return SCIPY;
    }

    @Override
    public List<String> getSupportedRepresentationInfoTypes() {
        return new ArrayList<>(Arrays.asList(
                ACCESSES_INFO,
                REPOSITORY_INFO,
                CLASS_VECTORIZATION_INFO,
                ENTITY_VECTORIZATION_INFO,
                FUNCTIONALITY_VECTORIZATION_CALLGRAPH_INFO,
                FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO));
    }

    @Override
    public Decomposition generateDecomposition(Similarity s, DecompositionRequest request) throws Exception {
        SciPyRequestDto dto = (SciPyRequestDto) request;
        SimilarityScipy similarity = (SimilarityScipy) s;

        PartitionsDecomposition decomposition = new PartitionsDecomposition();
        decomposition.setSimilarity(similarity);
        similarity.addDecomposition(decomposition);
        decomposition.setStrategy(similarity.getStrategy());
        similarity.getStrategy().addDecomposition(decomposition);
        decomposition.setName(getDecompositionName(similarity, dto.getCutType(), dto.getCutValue()));

        JSONObject clustersJSON = invokePythonCut(decomposition, similarity.getSimilarityMatrix().getName(), similarity.getLinkageType(), dto.getCutType(), dto.getCutValue());

        Map<Short, String> idToEntity = similarity.getIDToEntityName();
        addClustersAndEntities(decomposition, clustersJSON, idToEntity);

        return decomposition;
    }

    private JSONObject invokePythonCut(Decomposition decomposition, String similarityMatrixName, String linkageType, String cutType, float cutValue) {
        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{similarityMatrixName}/{linkageType}/{cutType}/{cutValue}/createDecomposition",
                        similarityMatrixName, linkageType, cutType, Float.toString(cutValue))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();
        try {
            JSONObject jsonObject = new JSONObject(response);
            decomposition.addMetric("Silhouette Score", jsonObject.getDouble("silhouetteScore"));
            return new JSONObject(jsonObject.getString("clusters"));
        } catch(Exception e) { throw new RuntimeException(e.getMessage()); }
    }


    public String getDecompositionName(Similarity similarity, String cutType, float cutValue) {
        String cutValueString = Float.valueOf(cutValue).toString().replaceAll("\\.?0*$", "");
        List<String> decompositionNames = similarity.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(similarity.getName() + " " + cutType + cutValueString)) {
            int i = 2;
            while (decompositionNames.contains(similarity.getName() + " " + cutType + cutValueString + "(" + i + ")"))
                i++;
            return similarity.getName() + " " + cutType + cutValueString + "(" + i + ")";

        } else return similarity.getName() + " " + cutType + cutValueString;
    }

    public static void addClustersAndEntities(
            Decomposition decomposition,
            JSONObject clustersJSON,
            Map<Short, String> idToEntity
    ) throws JSONException {
        Iterator<String> clusters = clustersJSON.sortedKeys();
        ArrayList<String> clusterNames = new ArrayList<>();

        while(clusters.hasNext())
            clusterNames.add(clusters.next());

        Collections.sort(clusterNames);

        for (String name : clusterNames) {
            JSONArray entities = clustersJSON.getJSONArray(name);
            Partition cluster;
            if (decomposition.isExpert())
                cluster = new Partition(name);
            else cluster = new Partition("Cluster" + name);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addElement(new DomainEntity(entityID, idToEntity.get(entityID)));
            }

            decomposition.addCluster(cluster);
        }
    }

    // This prevents bug where, during the generateMultipleDecompositions, SCRIPTS_ADDRESS is not loaded
    public void prepareAutowire() {
        System.out.println("Preparing to contact " + SCRIPTS_ADDRESS);
    }

    public void generateMultipleDecompositions(RecommendMatrixSciPy recommendation) throws Exception {
        Map<Short, String> idToEntity = recommendation.getIDToEntityName(gridFsService);

        JSONArray recommendationJSON = new JSONArray();

        List<String> similarityMatricesNames = new ArrayList<>(recommendation.getSimilarityMatricesNames());

        int maxClusters = getMaxClusters(idToEntity.size());
        int numberOfTotalSteps = similarityMatricesNames.size() * (1 + maxClusters - MIN_CLUSTERS)/CLUSTER_STEP;

        System.out.println("Number of decompositions to be made: " + numberOfTotalSteps);

        similarityMatricesNames.parallelStream().forEach(similarityMatrixName -> {

            for (int numberOfClusters = MIN_CLUSTERS; numberOfClusters <= maxClusters; numberOfClusters += CLUSTER_STEP) {
                try {
                    Decomposition decomposition = new PartitionsDecomposition();
                    decomposition.setStrategy(recommendation.getStrategy());

                    decomposition.setName(similarityMatrixName + "," + numberOfClusters);

                    JSONObject clustersJSON = invokePythonCut(decomposition, similarityMatrixName, recommendation.getLinkageType(), "N", numberOfClusters);

                    addClustersAndEntities(decomposition, clustersJSON, idToEntity);

                    recommendation.getDecompositionPropertiesForRecommendation(decomposition);

                    // Add decomposition's relevant information to the file
                    JSONObject decompositionJSON = new JSONObject();
                    String[] weights = decomposition.getName().split(",");
                    decompositionJSON.put("name", decomposition.getName());
                    int i = 1;
                    for (String weightName : recommendation.getWeightsNames())
                        decompositionJSON.put(weightName, weights[i++]);
                    decompositionJSON.put("numberOfClusters", weights[i]);

                    decompositionJSON.put("maxClusterSize", decomposition.maxClusterSize());

                    for (Map.Entry<String, Object> metric : decomposition.getMetrics().entrySet())
                        decompositionJSON.put(metric.getKey(), metric.getValue());

                    addRecommendationToJSON(recommendationJSON, decompositionJSON);

                    System.out.println("Decomposition " + recommendationJSON.length() + "/" + numberOfTotalSteps);

                    // Every 10 decompositions, updates the recommendation results file
                    if (recommendationJSON.length() % 20 == 0)
                        setRecommendationResult(recommendationJSON, recommendation.getRecommendationResultName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        setRecommendationResult(recommendationJSON, recommendation.getRecommendationResultName());
        recommendation.setCompleted(true);
    }

    // This function was created to be sure that no writes are made at the same time
    private synchronized void setRecommendationResult(JSONArray recommendationResult, String recommendationResultName) {
        gridFsService.replaceFile(new ByteArrayInputStream(recommendationResult.toString().getBytes()), recommendationResultName);
    }

    private synchronized void addRecommendationToJSON(JSONArray arrayJSON, JSONObject decompositionJSON) {
        arrayJSON.put(decompositionJSON);
    }

    private static int getMaxClusters(int totalNumberOfEntities) {
        if (totalNumberOfEntities >= 20)
            return 10;
        else if (totalNumberOfEntities >= 10)
            return 5;
        else if (totalNumberOfEntities >= 4)
            return 3;
        else throw new RuntimeException("Number of entities is too small (less than 4)");
    }
}