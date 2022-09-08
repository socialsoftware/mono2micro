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
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendAccessesSciPyRepository;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.dendrogram.repository.DendrogramRepository;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendAccessesSciPyService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;
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
    DendrogramRepository dendrogramRepository;

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

    public void createDecomposition(AccessesSciPyStrategy strategy, AccessesSciPyDendrogram dendrogram, String cutType, float cutValue) throws Exception {
        AccessesSource source = (AccessesSource) dendrogram.getStrategy().getCodebase().getSourceByType(ACCESSES);

        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        decomposition.setName(getDecompositionName(dendrogram, cutType, cutValue));
        decomposition.setDendrogram(dendrogram);

        JSONObject clustersJSON = invokePythonCut(decomposition, dendrogram.getSimilarityMatrixName(), cutType, cutValue);
        addClustersAndEntities(decomposition, clustersJSON);

        setupAndSaveDendrogram(strategy, dendrogram, source, decomposition);
    }

    private void setupAndSaveDendrogram(AccessesSciPyStrategy strategy, AccessesSciPyDendrogram dendrogram, AccessesSource source, AccessesSciPyDecomposition decomposition) throws Exception {
        setupFunctionalitiesAndMetrics(dendrogram.getProfile(), dendrogram.getTracesMaxLimit(), dendrogram.getTraceType(), source, decomposition);

        dendrogram.addDecomposition(decomposition);
        strategy.addDecomposition(decomposition);
        decomposition.setStrategy(strategy);

        //Add decomposition log to save operations during the usage of the view
        AccessesSciPyLog decompositionLog = new AccessesSciPyLog(decomposition);
        decomposition.setLog(decompositionLog);
        logRepository.save(decompositionLog);

        decompositionRepository.save(decomposition);
        dendrogramRepository.save(dendrogram);
        strategyRepository.save(strategy);
    }

    public void createExpertDecomposition(AccessesSciPyStrategy strategy, AccessesSciPyDendrogram dendrogram, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        AccessesSource source = (AccessesSource) dendrogram.getStrategy().getCodebase().getSourceByType(ACCESSES);

        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        decomposition.setDendrogram(dendrogram);
        List<String> decompositionNames = dendrogram.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(expertName))
            throw new KeyAlreadyExistsException();
        decomposition.setName(dendrogram.getName() + " " + expertName);
        decomposition.setExpert(true);

        if (expertFile.isPresent()) { // Expert decomposition with file
            InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
            JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8)).getJSONObject("clusters");
            addClustersAndEntities(decomposition, clustersJSON);
            is.close();
        }
        else createGenericDecomposition(dendrogram, decomposition);

        setupAndSaveDendrogram(strategy, dendrogram, source, decomposition);
    }

    private void createGenericDecomposition(AccessesSciPyDendrogram dendrogram, AccessesSciPyDecomposition decomposition) throws Exception {
        Cluster cluster = new Cluster("Generic");

        JSONObject similarityMatrixData = new JSONObject(dendrogram.getSimilarityMatrixName());

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

    public String getDecompositionName(AccessesSciPyDendrogram dendrogram, String cutType, float cutValue) {
        String cutValueString = Float.valueOf(cutValue).toString().replaceAll("\\.?0*$", "");
        List<String> decompositionNames = dendrogram.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(dendrogram.getName() + " " + cutType + cutValueString)) {
            int i = 2;
            while (decompositionNames.contains(dendrogram.getName() + " " + cutType + cutValueString + "(" + i + ")"))
                i++;
            return dendrogram.getName() + " " + cutType + cutValueString + "(" + i + ")";

        } else return dendrogram.getName() + " " + cutType + cutValueString;
    }

    private void addClustersAndEntities(AccessesSciPyDecomposition decomposition, JSONObject clustersJSON) throws JSONException {
        Iterator<String> clusters = clustersJSON.sortedKeys();
        ArrayList<String> clusterNames = new ArrayList<>();

        while(clusters.hasNext())
            clusterNames.add(clusters.next());

        Collections.sort(clusterNames);

        for (String name : clusterNames) {
            JSONArray entities = clustersJSON.getJSONArray(name);
            Cluster cluster;
            if (decomposition.isExpert())
                cluster = new Cluster(name);
            else cluster = new Cluster("Cluster" + name);

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

    public void generateMultipleDecompositions(RecommendAccessesSciPy recommendation) throws Exception {
        AccessesSource source = (AccessesSource) recommendation.getStrategy().getCodebase().getSourceByType(ACCESSES);
        byte[] sourceBytes = IOUtils.toByteArray(sourceService.getSourceFileAsInputStream(source.getName()));

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

                    addClustersAndEntities(decomposition, clustersJSON);

                    // create functionalities and calculate their metrics
                    functionalityService.setupFunctionalities(
                            decomposition,
                            new ByteArrayInputStream(sourceBytes),
                            source.getProfile(recommendation.getProfile()),
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
