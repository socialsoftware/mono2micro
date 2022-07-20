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
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.log.domain.AccessesSciPyLog;
import pt.ist.socialsoftware.mono2micro.log.repository.LogRepository;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionService.AccessesSciPyMetricService;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;
import pt.ist.socialsoftware.mono2micro.strategy.service.RecommendAccessesSciPyStrategyService;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

@Service
public class SciPyClusteringAlgorithmService {

    @Autowired
    SourceService sourceService;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    RecommendAccessesSciPyStrategyService recommendStrategyService;

    @Autowired
    AccessesSciPyMetricService metricService;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    LogRepository logRepository;

    public void createAccessesSciPyDendrogram(AccessesSciPyStrategy strategy) {
        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{strategyName}/{similarityMatrixName}/createDendrogram", strategy.getName(), strategy.getSimilarityMatrixName())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();
        try {
            JSONObject jsonObject = new JSONObject(response);
            strategy.setImageName(jsonObject.getString("imageName"));
            strategy.setCopheneticDistanceName(jsonObject.getString("copheneticDistanceName"));
        } catch(Exception e) { throw new RuntimeException("Could not produce or extract elements from JSON Object"); }
    }

    public void createDecomposition(AccessesSciPyStrategy strategy, String cutType, float cutValue) throws Exception {
        AccessesSource source = (AccessesSource) strategy.getCodebase().getSourceByType(ACCESSES);

        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        decomposition.setName(getDecompositionName(strategy, cutType, cutValue));
        decomposition.setStrategy(strategy);

        JSONObject clustersJSON = invokePythonCut(decomposition, strategy.getSimilarityMatrixName(), cutType, cutValue);
        addClustersAndEntities(decomposition, clustersJSON);

        setupFunctionalitiesAndMetrics(strategy.getProfile(), strategy.getTracesMaxLimit(), strategy.getTraceType(), source, decomposition);

        strategy.addDecomposition(decomposition);

        //Add decomposition log to save operations during the usage of the view
        AccessesSciPyLog decompositionLog = new AccessesSciPyLog(decomposition);
        decomposition.setLog(decompositionLog);
        logRepository.save(decompositionLog);

        decompositionRepository.save(decomposition);
        strategyRepository.save(strategy);
    }

    public void createExpertDecomposition(AccessesSciPyStrategy strategy, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        AccessesSource source = (AccessesSource) strategy.getCodebase().getSourceByType(ACCESSES);

        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        decomposition.setStrategy(strategy);
        List<String> decompositionNames = strategy.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(expertName))
            throw new KeyAlreadyExistsException();
        decomposition.setName(strategy.getName() + " " + expertName);
        decomposition.setExpert(true);

        if (expertFile.isPresent()) { // Expert decomposition with file
            InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
            JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8)).getJSONObject("clusters");
            addClustersAndEntities(decomposition, clustersJSON);
            is.close();
        }
        else createGenericDecomposition(strategy, decomposition);

        setupFunctionalitiesAndMetrics(strategy.getProfile(), strategy.getTracesMaxLimit(), strategy.getTraceType(), source, decomposition);

        strategy.addDecomposition(decomposition);

        //Add decomposition log to save operations during the usage of the view
        AccessesSciPyLog decompositionLog = new AccessesSciPyLog(decomposition);
        decomposition.setLog(decompositionLog);
        logRepository.save(decompositionLog);

        decompositionRepository.save(decomposition);
        strategyRepository.save(strategy);
    }

    private void createGenericDecomposition(AccessesSciPyStrategy strategy, AccessesSciPyDecomposition decomposition) throws Exception {
        Cluster cluster = new Cluster("Generic");

        JSONObject similarityMatrixData = new JSONObject(strategy.getSimilarityMatrixName());

        JSONArray entities = similarityMatrixData.getJSONArray("entities");

        for (int i = 0; i < entities.length(); i++) {
            short entityID = (short) entities.getInt(i);

            cluster.addEntity(entityID);
            decomposition.putEntity(entityID, cluster.getName());
        }

        decomposition.addCluster(cluster);
    }

    private void setupFunctionalitiesAndMetrics(
            String profile,
            int tracesMaxLimit,
            Constants.TraceType traceType,
            AccessesSource source,
            AccessesSciPyDecomposition decomposition
    ) throws Exception {
        functionalityService.setupFunctionalities(
                decomposition,
                sourceService.getSourceFileAsInputStream(source.getName()),
                source.getProfile(profile),
                tracesMaxLimit,
                traceType,
                true);

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

    public String getDecompositionName(AccessesSciPyStrategy strategy, String cutType, float cutValue) {
        String cutValueString = Float.valueOf(cutValue).toString().replaceAll("\\.?0*$", "");
        List<String> decompositionNames = strategy.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(strategy.getName() + " " + cutType + cutValueString)) {
            int i = 2;
            while (decompositionNames.contains(strategy.getName() + " " + cutType + cutValueString + "(" + i + ")"))
                i++;
            return strategy.getName() + " " + cutType + cutValueString + "(" + i + ")";

        } else return strategy.getName() + " " + cutType + cutValueString;
    }

    private void addClustersAndEntities(AccessesSciPyDecomposition decomposition, JSONObject clustersJSON) throws JSONException {
        Iterator<String> clusters = clustersJSON.sortedKeys();
        ArrayList<String> clusterNames = new ArrayList<>();

        while(clusters.hasNext())
            clusterNames.add(clusters.next());

        Collections.sort(clusterNames);

        for (String name : clusterNames) {
            JSONArray entities = clustersJSON.getJSONArray(name);
            Cluster cluster = new Cluster("Cluster " + name);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addEntity(entityID);
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

    public void generateMultipleDecompositions(RecommendAccessesSciPyStrategy strategy) throws Exception {
        AccessesSource source = (AccessesSource) strategy.getCodebase().getSourceByType(ACCESSES);
        byte[] sourceBytes = IOUtils.toByteArray(sourceService.getSourceFileAsInputStream(source.getName()));

        int MIN_CLUSTERS = 3, CLUSTER_STEP = 1;

        int maxClusters, totalNumberOfEntities = strategy.getNumberOfEntities();

        if (totalNumberOfEntities >= 20)
            maxClusters = 10;
        else if (totalNumberOfEntities >= 10)
            maxClusters = 5;
        else if (totalNumberOfEntities >= 4)
            maxClusters = 3;
        else throw new RuntimeException("Number of entities is too small (less than 4)");

        List<String> similarityMatricesNames = new ArrayList<>(strategy.getSimilarityMatricesNames());
        JSONArray recommendationJSON;
        if (strategy.getRecommendationResultName() == null) {
            recommendationJSON = new JSONArray();
            strategy.setRecommendationResultName(strategy.getName() + "_recommendationResult");
            strategyRepository.save(strategy);
        }
        else {
            recommendationJSON = new JSONArray(recommendStrategyService.getRecommendationResult(strategy));
        }
        int numberOfTotalSteps = similarityMatricesNames.size() * (1 + maxClusters - MIN_CLUSTERS)/CLUSTER_STEP;

        System.out.println("Number of decompositions to be made: " + numberOfTotalSteps);

        similarityMatricesNames.parallelStream().forEach(similarityMatrixName -> {

            //Name captured from the similarity matrix used to produce the cut
            String[] properties = similarityMatrixName.split(",");
            Constants.TraceType traceType = Constants.TraceType.valueOf(properties[5]);
            String linkageType = properties[6];
            if (strategy.containsCombination(traceType, linkageType))
                return;

            for (int numberOfClusters = MIN_CLUSTERS; numberOfClusters <= maxClusters; numberOfClusters += CLUSTER_STEP) {
                try {
                    AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
                    decomposition.setStrategy(strategy);

                    decomposition.setName(similarityMatrixName + "," + numberOfClusters);

                    JSONObject clustersJSON = invokePythonCut(
                            decomposition,
                            similarityMatrixName,
                            "N",
                            numberOfClusters);

                    addClustersAndEntities(decomposition, clustersJSON);

                    // create functionalities and calculate their metrics
                    functionalityService.setupFunctionalities(
                            decomposition,
                            new ByteArrayInputStream(sourceBytes),
                            source.getProfile(strategy.getProfile()),
                            strategy.getTracesMaxLimit(),
                            traceType,
                            false);

                    // Calculate decomposition's metrics
                    metricService.calculateMetrics(decomposition);

                    decompositionRepository.save(decomposition);
                    strategy.addDecomposition(decomposition);

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
                        setRecommendationResult(recommendationJSON, strategy.getRecommendationResultName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        setRecommendationResult(recommendationJSON, strategy.getRecommendationResultName());
        strategy.setCompleted(true);
    }

    // This function was created to be sure that no writes are made at the same time
    private synchronized void setRecommendationResult(JSONArray recommendationResult, String recommendationResultName) {
        recommendStrategyService.replaceRecommendationResult(recommendationResult.toString(), recommendationResultName);
    }

    private synchronized void addRecommendationToJSON(JSONArray arrayJSON, JSONObject decompositionJSON) {
        arrayJSON.put(decompositionJSON);
    }
}