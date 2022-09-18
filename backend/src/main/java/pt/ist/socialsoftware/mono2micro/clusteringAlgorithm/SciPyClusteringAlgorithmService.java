package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.cluster.AccessesSciPyCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.element.DomainEntity;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.log.domain.AccessesSciPyLog;
import pt.ist.socialsoftware.mono2micro.log.repository.LogRepository;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionService.AccessesSciPyMetricService;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendAccessesSciPyRepository;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.service.RepresentationService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.AccessesSciPySimilarity;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendAccessesSciPyService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

@Service
public class SciPyClusteringAlgorithmService {

    @Autowired
    RepresentationService representationService;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    RecommendAccessesSciPyService recommendAccessesSciPyService;

    @Autowired
    RecommendAccessesSciPyRepository recommendAccessesSciPyRepository;

    @Autowired
    AccessesSciPyMetricService metricService;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    LogRepository logRepository;

    public void createDecomposition(AccessesSciPyStrategy strategy, AccessesSciPySimilarity similarity, String cutType, float cutValue) throws Exception {
        AccessesRepresentation representation = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
        Map<Short, String> idToEntity = getIDToEntity(idToEntityRepresentation);

        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        decomposition.setName(getDecompositionName(similarity, cutType, cutValue));
        decomposition.setSimilarity(similarity);

        JSONObject clustersJSON = invokePythonCut(decomposition, similarity.getSimilarityMatrixName(), cutType, cutValue);
        addClustersAndEntities(decomposition, clustersJSON, idToEntity);

        setupAndSaveSimilarity(strategy, similarity, representation, decomposition);
    }

    public Map<Short, String> getIDToEntity(IDToEntityRepresentation representation) throws IOException, JSONException {
        Map<Short, String> idToEntity = new HashMap<>();
        String file = representationService.getRepresentationFileAsString(representation.getName());
        JSONObject idToEntityJSON = new JSONObject(file);
        Iterator<String> entityIDs = idToEntityJSON.keys();

        while (entityIDs.hasNext()) {
            String entityID = entityIDs.next();
            idToEntity.put(Short.valueOf(entityID), idToEntityJSON.getString(entityID));
        }
        return idToEntity;
    }

    private void setupAndSaveSimilarity(AccessesSciPyStrategy strategy, AccessesSciPySimilarity similarity, AccessesRepresentation representation, AccessesSciPyDecomposition decomposition) throws Exception {
        setupFunctionalitiesAndMetrics(similarity.getProfile(), similarity.getTracesMaxLimit(), similarity.getTraceType(), representation, decomposition);

        similarity.addDecomposition(decomposition);
        strategy.addDecomposition(decomposition);
        decomposition.setStrategy(strategy);

        //Add decomposition log to save operations during the usage of the view
        AccessesSciPyLog decompositionLog = new AccessesSciPyLog(decomposition);
        decomposition.setLog(decompositionLog);
        logRepository.save(decompositionLog);

        decompositionRepository.save(decomposition);
        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }

    public void createExpertDecomposition(AccessesSciPyStrategy strategy, AccessesSciPySimilarity similarity, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        AccessesRepresentation representation = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
        Map<Short, String> idToEntity = getIDToEntity(idToEntityRepresentation);

        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        decomposition.setSimilarity(similarity);
        List<String> decompositionNames = similarity.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(expertName))
            throw new KeyAlreadyExistsException();
        decomposition.setName(similarity.getName() + " " + expertName);
        decomposition.setExpert(true);

        if (expertFile.isPresent()) { // Expert decomposition with file
            InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
            JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8)).getJSONObject("clusters");
            addClustersAndEntities(decomposition, clustersJSON, idToEntity);
            is.close();
        }
        else createGenericDecomposition(similarity, decomposition, idToEntity);

        setupAndSaveSimilarity(strategy, similarity, representation, decomposition);
    }

    private void createGenericDecomposition(AccessesSciPySimilarity similarity, AccessesSciPyDecomposition decomposition, Map<Short, String> idToEntity) throws Exception {
        AccessesSciPyCluster cluster = new AccessesSciPyCluster("Generic");

        JSONObject similarityMatrixData = new JSONObject(similarity.getSimilarityMatrixName());

        JSONArray entities = similarityMatrixData.getJSONArray("entities");

        for (int i = 0; i < entities.length(); i++) {
            short entityID = (short) entities.getInt(i);

            cluster.addElement(new DomainEntity(entityID, idToEntity.get(entityID)));
            decomposition.putEntity(entityID, cluster.getName());
        }

        decomposition.addCluster(cluster);
    }

    private void setupFunctionalitiesAndMetrics(
            String profile,
            int tracesMaxLimit,
            Constants.TraceType traceType,
            AccessesRepresentation representation,
            AccessesSciPyDecomposition decomposition
    ) throws Exception {
        functionalityService.setupFunctionalities(
                decomposition,
                representationService.getRepresentationFileAsInputStream(representation.getName()),
                representation.getProfile(profile),
                tracesMaxLimit,
                traceType,
                false);

        // Calculate decomposition's metrics
        metricService.calculateMetrics(decomposition);
    }

    private JSONObject invokePythonCut(AccessesSciPyDecomposition decomposition, String similarityMatrixName, String cutType, float cutValue) {

        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{similarityMatrixName}/{cutType}/{cutValue}/createDecomposition",
                        similarityMatrixName, cutType, Float.toString(cutValue))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();

        try {
            JSONObject jsonObject = new JSONObject(response);
            decomposition.setSilhouetteScore(jsonObject.getDouble("silhouetteScore"));
            return new JSONObject(jsonObject.getString("clusters"));
        } catch(Exception e) { throw new RuntimeException(e.getMessage()); }
    }

    public String getDecompositionName(AccessesSciPySimilarity similarity, String cutType, float cutValue) {
        String cutValueString = Float.valueOf(cutValue).toString().replaceAll("\\.?0*$", "");
        List<String> decompositionNames = similarity.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(similarity.getName() + " " + cutType + cutValueString)) {
            int i = 2;
            while (decompositionNames.contains(similarity.getName() + " " + cutType + cutValueString + "(" + i + ")"))
                i++;
            return similarity.getName() + " " + cutType + cutValueString + "(" + i + ")";

        } else return similarity.getName() + " " + cutType + cutValueString;
    }

    private void addClustersAndEntities(AccessesSciPyDecomposition decomposition, JSONObject clustersJSON, Map<Short, String> idToEntity) throws JSONException {
        Iterator<String> clusters = clustersJSON.sortedKeys();
        ArrayList<String> clusterNames = new ArrayList<>();

        while(clusters.hasNext())
            clusterNames.add(clusters.next());

        Collections.sort(clusterNames);

        for (String name : clusterNames) {
            JSONArray entities = clustersJSON.getJSONArray(name);
            AccessesSciPyCluster cluster;
            if (decomposition.isExpert())
                cluster = new AccessesSciPyCluster(name);
            else cluster = new AccessesSciPyCluster("Cluster" + name);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addElement(new DomainEntity(entityID, idToEntity.get(entityID)));
                decomposition.putEntity(entityID, cluster.getName());
            }

            decomposition.addCluster(cluster);
        }
    }

    // This prevents bug where, during the generateMultipleDecompositions, SCRIPTS_ADDRESS is not loaded
    // this might happen since this operation is called in another thread and might not load SCRIPTS_ADDRESS because of it
    public void prepareAutowire() {
        System.out.println("Preparing to contact " + SCRIPTS_ADDRESS);
    }

    public void generateMultipleDecompositions(RecommendAccessesSciPy recommendation) throws Exception {
        AccessesRepresentation representation = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        byte[] representationBytes = IOUtils.toByteArray(representationService.getRepresentationFileAsInputStream(representation.getName()));
        IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
        Map<Short, String> idToEntity = getIDToEntity(idToEntityRepresentation);

        int MIN_CLUSTERS = 3, CLUSTER_STEP = 1;

        int maxClusters, totalNumberOfEntities = recommendation.getNumberOfEntities();

        if (totalNumberOfEntities >= 20)
            maxClusters = 10;
        else if (totalNumberOfEntities >= 10)
            maxClusters = 5;
        else if (totalNumberOfEntities >= 4)
            maxClusters = 3;
        else throw new RuntimeException("Number of entities is too small (less than 4)");

        List<String> similarityMatricesNames = new ArrayList<>(recommendation.getSimilarityMatricesNames());
        JSONArray recommendationJSON;
        if (recommendation.getRecommendationResultName() == null) {
            recommendationJSON = new JSONArray();
            recommendation.setRecommendationResultName(recommendation.getName() + "_recommendationResult");
            recommendAccessesSciPyRepository.save(recommendation);
        }
        else {
            recommendationJSON = new JSONArray(recommendAccessesSciPyService.getRecommendationResult(recommendation));
        }
        int numberOfTotalSteps = similarityMatricesNames.size() * (1 + maxClusters - MIN_CLUSTERS)/CLUSTER_STEP;

        System.out.println("Number of decompositions to be made: " + numberOfTotalSteps);

        similarityMatricesNames.parallelStream().forEach(similarityMatrixName -> {

            //Name captured from the similarity matrix used to produce the cut
            String[] properties = similarityMatrixName.split(",");
            Constants.TraceType traceType = Constants.TraceType.valueOf(properties[5]);
            String linkageType = properties[6];
            if (recommendation.containsCombination(traceType, linkageType))
                return;

            for (int numberOfClusters = MIN_CLUSTERS; numberOfClusters <= maxClusters; numberOfClusters += CLUSTER_STEP) {
                try {
                    AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();

                    decomposition.setName(similarityMatrixName + "," + numberOfClusters);

                    JSONObject clustersJSON = invokePythonCut(
                            decomposition,
                            similarityMatrixName,
                            "N",
                            numberOfClusters);

                    addClustersAndEntities(decomposition, clustersJSON, idToEntity);

                    // create functionalities and calculate their metrics
                    functionalityService.setupFunctionalities(
                            decomposition,
                            new ByteArrayInputStream(representationBytes),
                            representation.getProfile(recommendation.getProfile()),
                            recommendation.getTracesMaxLimit(),
                            traceType,
                            true);

                    // Calculate decomposition's metrics
                    metricService.calculateMetrics(decomposition);

                    // Add decomposition's relevant information to the file
                    JSONObject decompositionJSON = new JSONObject();
                    String[] weights = decomposition.getName().split(",");
                    decompositionJSON.put("name", decomposition.getName());
                    decompositionJSON.put("traceType", traceType);
                    decompositionJSON.put("linkageType", linkageType);
                    decompositionJSON.put("accessMetricWeight", weights[1]);
                    decompositionJSON.put("writeMetricWeight", weights[2]);
                    decompositionJSON.put("readMetricWeight", weights[3]);
                    decompositionJSON.put("sequenceMetricWeight", weights[4]);
                    decompositionJSON.put("numberOfClusters", weights[7]);

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
        recommendAccessesSciPyService.replaceRecommendationResult(recommendationResult.toString(), recommendationResultName);
    }

    private synchronized void addRecommendationToJSON(JSONArray arrayJSON, JSONObject decompositionJSON) {
        arrayJSON.put(decompositionJSON);
    }
}
