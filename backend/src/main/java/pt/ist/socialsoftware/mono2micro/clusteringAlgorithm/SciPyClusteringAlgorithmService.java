package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.*;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.SciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.element.DomainEntity;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.log.domain.PositionLog;
import pt.ist.socialsoftware.mono2micro.log.repository.LogRepository;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionService.MetricService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendForSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendationService;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.service.RepresentationService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityForSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
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

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition.LOG_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

@Service
public class SciPyClusteringAlgorithmService {
    static final int MIN_CLUSTERS = 3, CLUSTER_STEP = 1;

    @Autowired
    RepresentationService representationService;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    RecommendationService recommendationService;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    MetricService metricService;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    LogRepository logRepository;

    @Autowired
    GridFsService gridFsService;

    //####################################################################################################
    //                                  NEW ADDITIONS IN THE FOLLOWING METHODS
    //####################################################################################################

    public Map<Short, String> getIDToEntity(Similarity similarity) throws JSONException, IOException {
        List<String> representations = similarity.getStrategy().getRepresentationTypes();
        if (representations.contains(ID_TO_ENTITY)) {
            IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
            return getIDToEntityFromRepresentation(idToEntityRepresentation);
        }
        // ADD OTHER METHODS TO FILL IDTOENTITY HERE
        else throw new RuntimeException("Could not fill idToEntity");
    }

    public void fillDecompositionAndSave(SimilarityForSciPy similarity, SciPyDecomposition decomposition) throws Exception {
        Strategy strategy = strategyRepository.findByName(similarity.getStrategy().getName());

        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION)) {
            AccessesRepresentation accesses = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
            setupFunctionalities(accesses, similarity.getProfile(), similarity.getTraceType(), similarity.getTracesMaxLimit(), (AccessesDecomposition) decomposition);
        }
        if (decomposition.containsImplementation(REPOSITORY_DECOMPOSITION)) {
            setupAuthorsAndCommits((RepositoryDecomposition) decomposition, strategy);
        }
        if (decomposition.containsImplementation(LOG_DECOMPOSITION)) {
            createLogForDecomposition((LogDecomposition) decomposition);
        }
        // FILL DECOMPOSITION WITH SPECIFIC PROPERTIES HERE

        metricService.calculateMetrics((Decomposition) decomposition);

        similarity.addDecomposition((Decomposition) decomposition);
        strategy.addDecomposition((Decomposition) decomposition);
        decomposition.setSimilarity(similarity);
        decomposition.setStrategy(strategy);

        decompositionRepository.save((Decomposition) decomposition);
        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }

    public void getDecompositionPropertiesForRecommendation(RecommendForSciPy recommendation, SciPyDecomposition decomposition) throws Exception {
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION)) {
            AccessesRepresentation representation = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(ACCESSES);
            byte[] representationBytes = recommendation.getRepresentationBytes();
            if (representationBytes == null) {
                representationBytes = IOUtils.toByteArray(representationService.getRepresentationFileAsInputStream(representation.getName()));
                recommendation.setRepresentationBytes(representationBytes);
            }
            functionalityService.setupFunctionalities((AccessesDecomposition) decomposition,
                    new ByteArrayInputStream(representationBytes), representation.getProfile(recommendation.getProfile()),
                    recommendation.getTracesMaxLimit(), recommendation.getTraceType(), true);
        }
        if (decomposition.containsImplementation(REPOSITORY_DECOMPOSITION)) {
            AuthorRepresentation authorRepresentation = (AuthorRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(AUTHOR);
            Map<Short, ArrayList<String>> authors = getAuthors(authorRepresentation.getName());
            ((RepositoryDecomposition) decomposition).setAuthors(authors);
            extractNumberOfAuthors(authors, (RepositoryDecomposition) decomposition);
        }
        // FILL DECOMPOSITION WITH SPECIFIC PROPERTIES HERE

        metricService.calculateMetrics((Decomposition) decomposition);
    }

    public Map<Short, String> getIDToEntity(Recommendation recommendation) throws JSONException, IOException {
        List<String> representations = recommendation.getStrategy().getRepresentationTypes();
        if (representations.contains(ID_TO_ENTITY)) {
            IDToEntityRepresentation idToEntityRepresentation = (IDToEntityRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(ID_TO_ENTITY);
            return getIDToEntityFromRepresentation(idToEntityRepresentation);
        }
        // ADD OTHER METHODS TO FILL IDTOENTITY HERE
        else throw new RuntimeException("Could not fill idToEntity");
    }

    //####################################################################################################
    //                                  NEW ADDITIONS END HERE
    //####################################################################################################

    public void createDendrogramImage(Dendrogram similarity) {
        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{similarityName}/{similarityMatrixName}/{linkageType}/createDendrogram", similarity.getName(), similarity.getSimilarityMatrixName(), similarity.getLinkageType())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();
        try {
            JSONObject jsonObject = new JSONObject(response);
            similarity.setDendrogramName(jsonObject.getString("dendrogramName"));
            similarity.setCopheneticDistanceName(jsonObject.getString("copheneticDistanceName"));
        } catch(Exception e) { throw new RuntimeException("Could not produce or extract elements from JSON Object"); }
    }

    public void createDecomposition(SciPyRequestDto dto) throws Exception {
        SimilarityForSciPy similarity = (SimilarityForSciPy) similarityRepository.findByName(dto.getSimilarityName());
        Map<Short, String> idToEntity;

        SciPyDecomposition decomposition = (SciPyDecomposition) DecompositionFactory.getFactory().getDecomposition(dto.getType());
        decomposition.setName(getDecompositionName(similarity, dto.getCutType(), dto.getCutValue()));

        JSONObject clustersJSON = invokePythonCut(decomposition, similarity.getSimilarityMatrixName(), similarity.getLinkageType(), dto.getCutType(), dto.getCutValue());

        idToEntity = getIDToEntity(similarity);
        addClustersAndEntities(decomposition, clustersJSON, idToEntity);

        fillDecompositionAndSave(similarity, decomposition);
    }

    public void createExpertDecomposition(SimilarityForSciPy similarity, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        Map<Short, String> idToEntity;

        SciPyDecomposition decomposition = (SciPyDecomposition) DecompositionFactory.getFactory().getDecomposition(similarity.getType());
        List<String> decompositionNames = similarity.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(expertName))
            throw new KeyAlreadyExistsException();
        decomposition.setName(similarity.getName() + " " + expertName);
        decomposition.setExpert(true);

        idToEntity = getIDToEntity(similarity);

        if (expertFile.isPresent()) { // Expert decomposition with file
            InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
            JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8)).getJSONObject("clusters");
            addClustersAndEntities(decomposition, clustersJSON, idToEntity);
            is.close();
        }
        else createGenericDecomposition(similarity.getSimilarityMatrixName(), decomposition.getClusters(), decomposition.getEntityIDToClusterName(), idToEntity);

        fillDecompositionAndSave(similarity, decomposition);
    }

    private void createGenericDecomposition(
            String similarityMatrixName,
            Map<String, Cluster> clusters,
            Map<Short, String> entityIDToClusterName,
            Map<Short, String> idToEntity
    ) throws Exception {
        SciPyCluster cluster = new SciPyCluster("Generic");

        JSONObject similarityMatrixData = new JSONObject(gridFsService.getFile(similarityMatrixName));

        JSONArray entities = similarityMatrixData.getJSONArray("entities");

        for (int i = 0; i < entities.length(); i++) {
            short entityID = (short) entities.getInt(i);

            cluster.addElement(new DomainEntity(entityID, idToEntity.get(entityID)));
            entityIDToClusterName.put(entityID, cluster.getName());
        }

        clusters.putIfAbsent(cluster.getName(), cluster);
    }

    private void setupAuthorsAndCommits(RepositoryDecomposition repositoryDecomposition, Strategy strategy) throws IOException {
        AuthorRepresentation authorRepresentation = (AuthorRepresentation) strategy.getCodebase().getRepresentationByType(AUTHOR);
        CommitRepresentation commitRepresentation = (CommitRepresentation) strategy.getCodebase().getRepresentationByType(COMMIT);
        Map<Short, ArrayList<String>> authors = getAuthors(authorRepresentation.getName());
        Map<String, Map<String, Integer>> commits = getCommits(commitRepresentation.getName());
        repositoryDecomposition.setAuthors(authors);
        extractNumberOfAuthors(authors, repositoryDecomposition);
        extractCommitsInCommon(commits, repositoryDecomposition);
    }

    private void createLogForDecomposition(LogDecomposition decomposition) {
        PositionLog decompositionLog = new PositionLog(decomposition);
        decomposition.setLog(decompositionLog);
        logRepository.save(decompositionLog);
    }

    public Map<Short, String> getIDToEntityFromRepresentation(IDToEntityRepresentation representation) throws IOException, JSONException {
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

    private void setupFunctionalities(AccessesRepresentation accesses, String profile, Constants.TraceType traceType, int tracesMaxLimit, AccessesDecomposition accessesDecomposition) throws Exception {
        functionalityService.setupFunctionalities(
                accessesDecomposition, representationService.getRepresentationFileAsInputStream(accesses.getName()),
                accesses.getProfile(profile), tracesMaxLimit, traceType, false);
    }

    private JSONObject invokePythonCut(SciPyDecomposition decomposition, String similarityMatrixName, String linkageType, String cutType, float cutValue) {
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
            decomposition.setSilhouetteScore(jsonObject.getDouble("silhouetteScore"));
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

    private void addClustersAndEntities(
            SciPyDecomposition decomposition,
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
            SciPyCluster cluster;
            if (decomposition.isExpert())
                cluster = new SciPyCluster(name);
            else cluster = new SciPyCluster("Cluster" + name);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addElement(new DomainEntity(entityID, idToEntity.get(entityID)));
                decomposition.getEntityIDToClusterName().put(entityID, cluster.getName());
            }

            decomposition.addCluster(cluster);
        }
    }

    // This prevents bug where, during the generateMultipleDecompositions, SCRIPTS_ADDRESS is not loaded
    // this might happen since this operation is called in another thread and might not load SCRIPTS_ADDRESS because of it
    public void prepareAutowire() {
        System.out.println("Preparing to contact " + SCRIPTS_ADDRESS);
    }

    public void generateMultipleDecompositions(RecommendForSciPy recommendation) throws Exception {
        Map<Short, String> idToEntity;
        idToEntity = getIDToEntity(recommendation);

        List<String> similarityMatricesNames = new ArrayList<>(recommendation.getSimilarityMatricesNames());
        JSONArray recommendationJSON;
        if (recommendation.getRecommendationResultName() == null) {
            recommendationJSON = new JSONArray();
            recommendation.setRecommendationResultName(recommendation.getName() + "_recommendationResult");
            recommendationRepository.save(recommendation);
        }
        else recommendationJSON = new JSONArray(recommendationService.getRecommendationResult(recommendation));

        int maxClusters = getMaxClusters(idToEntity.size());
        int numberOfTotalSteps = similarityMatricesNames.size() * (1 + maxClusters - MIN_CLUSTERS)/CLUSTER_STEP;

        System.out.println("Number of decompositions to be made: " + numberOfTotalSteps);

        similarityMatricesNames.parallelStream().forEach(similarityMatrixName -> {

            for (int numberOfClusters = MIN_CLUSTERS; numberOfClusters <= maxClusters; numberOfClusters += CLUSTER_STEP) {
                try {
                    SciPyDecomposition decomposition = (SciPyDecomposition) DecompositionFactory.getFactory().getDecomposition(recommendation.getType());

                    decomposition.setName(similarityMatrixName + "," + numberOfClusters);

                    JSONObject clustersJSON = invokePythonCut(decomposition, similarityMatrixName, recommendation.getLinkageType(), "N", numberOfClusters);

                    addClustersAndEntities(decomposition, clustersJSON, idToEntity);

                    getDecompositionPropertiesForRecommendation(recommendation, decomposition);

                    // Add decomposition's relevant information to the file
                    JSONObject decompositionJSON = new JSONObject();
                    String[] weights = decomposition.getName().split(",");
                    decompositionJSON.put("name", decomposition.getName());
                    decompositionJSON.put("traceType", recommendation.getTraceType());
                    decompositionJSON.put("linkageType", recommendation.getLinkageType());
                    int i = 0;
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
        recommendationService.replaceRecommendationResult(recommendationResult.toString(), recommendationResultName);
    }

    private synchronized void addRecommendationToJSON(JSONArray arrayJSON, JSONObject decompositionJSON) {
        arrayJSON.put(decompositionJSON);
    }

    private void extractNumberOfAuthors(Map<Short, ArrayList<String>> filesAndAuthors, RepositoryDecomposition decomposition) {
        Set<String> allAuthors = new HashSet<>();
        for (ArrayList<String> authors : filesAndAuthors.values())
            allAuthors.addAll(authors);
        decomposition.setTotalAuthors(allAuthors.size());
    }

    private void extractCommitsInCommon(Map<String, Map<String, Integer>> commits, RepositoryDecomposition decomposition) {
        Set<Short> entityIds = decomposition.getEntityIDToClusterName().keySet();
        for (Short entityId : entityIds) {
            Map<String, Integer> commonEntities = commits.get(entityId.toString());
            decomposition.addTotalCommit(entityId, commonEntities.get("total_commits"));

            for (Map.Entry<String, Integer> commonEntity : commonEntities.entrySet()) {
                if (!commonEntity.getKey().equals("total_commits")) {
                    Short commonEntityId = Short.parseShort(commonEntity.getKey());
                    if (entityIds.contains(commonEntityId))
                        decomposition.addCommitInCommon(entityId, commonEntityId, commonEntity.getValue());
                }
            }
        }
    }

    private Map<Short, ArrayList<String>> getAuthors(String authorFileName) throws IOException {
        return new ObjectMapper().readValue(
            representationService.getRepresentationFileAsString(authorFileName),
            new TypeReference<Map<Short, ArrayList<String>>>() {});
    }

    private Map<String, Map<String, Integer>> getCommits(String commitFileName) throws IOException {
        return new ObjectMapper().readValue(
            representationService.getRepresentationFileAsString(commitFileName),
            new TypeReference<Map<String, Map<String, Integer>>>() {});
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