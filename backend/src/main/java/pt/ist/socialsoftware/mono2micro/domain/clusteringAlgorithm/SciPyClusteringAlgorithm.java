package pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.decomposition.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.metrics.Metric;
import pt.ist.socialsoftware.mono2micro.domain.source.AccessesSource;
import pt.ist.socialsoftware.mono2micro.domain.strategy.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.AccessesSciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.RequestDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.RECOMMENDATION_ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

public class SciPyClusteringAlgorithm implements ClusteringAlgorithm {

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

    public SciPyClusteringAlgorithm() {}

    @Override
    public void createDendrogram(Strategy strategy) {
        switch (strategy.getType()) {
            case ACCESSES_SCIPY:
                //Creates dendrogram image
                WebClient.create(SCRIPTS_ADDRESS)
                        .get()
                        .uri("/scipy/{codebaseName}/{strategyName}/createDendrogram", strategy.getCodebaseName(), strategy.getName())
                        .exchange()
                        .doOnSuccess(clientResponse -> {
                            if (clientResponse.statusCode() != HttpStatus.OK)
                                throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                        }).block();
                break;
            case RECOMMENDATION_ACCESSES_SCIPY:
                // no dendrogram
                break;
            default:
                throw new RuntimeException("No strategy type provided. Cannot determine dendrogram.");
        }
    }

    @Override
    public void createDecomposition(Strategy strategy, RequestDto requestDto) throws Exception {
        switch (strategy.getType()) {
            case Strategy.StrategyType.ACCESSES_SCIPY:
                AccessesSciPyRequestDto accessesSciPyCutDto = (AccessesSciPyRequestDto) requestDto;
                AccessesSciPyStrategy accessesSciPyStrategy = (AccessesSciPyStrategy) strategy;
                AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(accessesSciPyStrategy.getCodebaseName(), ACCESSES);

                AccessesSciPyDecomposition decomposition = cut(accessesSciPyStrategy, accessesSciPyCutDto);

                decomposition.setupFunctionalities(
                        source.getInputFilePath(),
                        source.getProfile(accessesSciPyStrategy.getProfile()),
                        accessesSciPyStrategy.getTracesMaxLimit(),
                        accessesSciPyStrategy.getTraceType(),
                        true);

                // Calculate decomposition's metrics
                decomposition.calculateMetrics();

                codebaseManager.writeStrategyDecomposition(accessesSciPyStrategy.getCodebaseName(), STRATEGIES_FOLDER, accessesSciPyStrategy.getName(), decomposition);
                accessesSciPyStrategy.addDecompositionName(decomposition.getName());
                codebaseManager.writeCodebaseStrategy(accessesSciPyStrategy.getCodebaseName(), STRATEGIES_FOLDER, accessesSciPyStrategy);
                break;

            case RECOMMENDATION_ACCESSES_SCIPY:
                generateMultipleDecompositions((RecommendAccessesSciPyStrategy) strategy);
                break;
            default:
                throw new RuntimeException("Unknown strategy type when creating a decomposition: " + strategy.getType());
        }
    }

    private AccessesSciPyDecomposition cut(AccessesSciPyStrategy strategy, AccessesSciPyRequestDto cutDto)
            throws Exception
    {
        AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
        // If these values are needed, these next lines could be added to the decomposition,
        // but since the name of the decomposition is composed of these values, they are ignored
        //decomposition.setCutValue(cutDto.getCutValue());
        //decomposition.setCutType(cutDto.getCutValue());
        decomposition.setCodebaseName(strategy.getCodebaseName());
        decomposition.setStrategyName(strategy.getName());

        JSONObject clustersJSON;
        if (cutDto.getExpertName() == null) { // Decomposition produced by a cut in the dendrogram
            decomposition.setName(getDecompositionName(strategy, cutDto));
            codebaseManager.createDecompositionDirectory(decomposition.getCodebaseName(), STRATEGIES_FOLDER, decomposition.getStrategyName(), decomposition.getName());

            invokePythonCut(strategy.getCodebaseName(), strategy.getName(), STRATEGIES_FOLDER, decomposition.getName(), "similarityMatrix", cutDto);

            clustersJSON = codebaseManager.getClusters(strategy.getCodebaseName(), STRATEGIES_FOLDER, strategy.getName(), decomposition.getName());
        }
        else { // When in an expert decomposition
            if (strategy.getDecompositionsNames().contains(cutDto.getExpertName()))
                throw new KeyAlreadyExistsException();
            decomposition.setName(cutDto.getExpertName());
            decomposition.setExpert(true);

            codebaseManager.createDecompositionDirectory(decomposition.getCodebaseName(), STRATEGIES_FOLDER, decomposition.getStrategyName(), decomposition.getName());

            if (cutDto.getExpertFile().isPresent()) { // Expert decomposition with file
                InputStream is = new BufferedInputStream(cutDto.getExpertFile().get().getInputStream());
                clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
                is.close();
            }
            else {
                createGenericDecomposition(strategy, decomposition);
                return decomposition;
            }
        }

        addClustersAndEntities(decomposition, clustersJSON);

        return decomposition;
    }

    private void createGenericDecomposition(AccessesSciPyStrategy strategy, AccessesSciPyDecomposition decomposition) throws Exception {
        Cluster cluster = new Cluster((short) 0, "Generic");

        JSONObject similarityMatrixData = codebaseManager.getSimilarityMatrix(
                strategy.getCodebaseName(),
                strategy.getName(),
                "similarityMatrix.json"
        );

        JSONArray entities = similarityMatrixData.getJSONArray("entities");

        for (int i = 0; i < entities.length(); i++) {
            short entityID = (short) entities.getInt(i);

            cluster.addEntity(entityID);
            decomposition.putEntity(entityID, cluster.getID());
        }

        decomposition.addCluster(cluster);
    }

    private void invokePythonCut(String codebaseName, String strategyName, String strategyFolder, String decompositionName, String matrixFile, AccessesSciPyRequestDto cutDto) {

        WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{codebaseName}{strategyFolder}{strategyName}/{graphName}/{matrixFile}/{cutType}/{cutValue}/createDecomposition",
                        codebaseName, strategyFolder, strategyName, decompositionName, matrixFile, cutDto.getCutType(), Float.toString(cutDto.getCutValue()))
                .exchange()
                .doOnSuccess(clientResponse -> {
                    if (clientResponse.statusCode() != HttpStatus.OK)
                        throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                }).block();
    }

    private String getDecompositionName(AccessesSciPyStrategy strategy, AccessesSciPyRequestDto cutDto) {
        String cutValueString = Float.valueOf(cutDto.getCutValue()).toString().replaceAll("\\.?0*$", "");

        if (strategy.getDecompositionsNames().contains(cutDto.getCutType() + cutValueString)) {
            int i = 2;
            while (strategy.getDecompositionsNames().contains(cutDto.getCutType() + cutValueString + "(" + i + ")"))
                i++;
            return cutDto.getCutType() + cutValueString + "(" + i + ")";

        } else return cutDto.getCutType() + cutValueString;
    }

    private void addClustersAndEntities(AccessesSciPyDecomposition decomposition, JSONObject clustersJSON) throws JSONException {
        Iterator<String> clusters = clustersJSON.getJSONObject("clusters").sortedKeys();
        ArrayList<String> clusterNames = new ArrayList<>();

        while(clusters.hasNext())
            clusterNames.add(clusters.next());

        Collections.sort(clusterNames);

        short clusterID = 0;
        for (String name : clusterNames) {
            JSONArray entities = clustersJSON.getJSONObject("clusters").getJSONArray(name);
            Cluster cluster = new Cluster(clusterID, name);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addEntity(entityID);
                decomposition.putEntity(entityID, clusterID);
            }
            clusterID++;

            decomposition.addCluster(cluster);
        }
    }

    private void generateMultipleDecompositions(RecommendAccessesSciPyStrategy strategy) throws Exception {
        AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(strategy.getCodebaseName(), ACCESSES);

        int MIN_CLUSTERS = 3, CLUSTER_STEP = 1;

        int maxClusters, totalNumberOfEntities = strategy.getNumberOfEntities();

        if (totalNumberOfEntities >= 20)
            maxClusters = 10;
        else if (totalNumberOfEntities >= 10)
            maxClusters = 5;
        else if (totalNumberOfEntities >= 4)
            maxClusters = 3;
        else throw new RuntimeException("Number of entities is too small (less than 4)");

        List<String> similarityMatricesNames = codebaseManager.getSimilarityMatricesNames(strategy.getCodebaseName(), RECOMMEND_FOLDER, strategy.getName());
        JSONArray recommendationJSON = codebaseManager.getRecommendationResultAsJSON(strategy.getCodebaseName(), strategy.getName());
        int numberOfTotalSteps = similarityMatricesNames.size() * (1 + maxClusters - MIN_CLUSTERS)/CLUSTER_STEP;

        System.out.println("Number of decompositions to be made: " + numberOfTotalSteps);

        similarityMatricesNames.parallelStream().forEach(similarityMatrixFile -> {

            //Name captured from the similarity matrix used to produce the cut
            String similarityMatrixName = similarityMatrixFile.substring(0, similarityMatrixFile.lastIndexOf('.'));
            String[] properties = similarityMatrixName.split(",");
            TraceType traceType = TraceType.valueOf(properties[4]);
            String linkageType = properties[5];
            if (strategy.containsCombination(traceType, linkageType))
                return;

            for (int numberOfClusters = MIN_CLUSTERS; numberOfClusters <= maxClusters; numberOfClusters += CLUSTER_STEP) {
                try {
                    AccessesSciPyDecomposition decomposition = new AccessesSciPyDecomposition();
                    decomposition.setCodebaseName(strategy.getCodebaseName());
                    decomposition.setStrategyName(strategy.getName());

                    //Properties of the cut
                    AccessesSciPyRequestDto cutDto = new AccessesSciPyRequestDto("N", numberOfClusters);

                    decomposition.setName(similarityMatrixName + "," + numberOfClusters);
                    codebaseManager.createDecompositionDirectory(decomposition.getCodebaseName(), RECOMMEND_FOLDER, strategy.getName(), decomposition.getName());

                    invokePythonCut(
                            strategy.getCodebaseName(),
                            strategy.getName(),
                            RECOMMEND_FOLDER,
                            decomposition.getName(),
                            similarityMatrixName,
                            cutDto);

                    JSONObject clustersJSON = codebaseManager.getClusters(strategy.getCodebaseName(), RECOMMEND_FOLDER, strategy.getName(), decomposition.getName());

                    addClustersAndEntities(decomposition, clustersJSON);

                    // create functionalities and calculate their metrics
                    decomposition.setupFunctionalities(
                            source.getInputFilePath(),
                            source.getProfile(strategy.getProfile()),
                            strategy.getTracesMaxLimit(),
                            traceType,
                            false);

                    decomposition.calculateMetrics();
                    codebaseManager.writeStrategyDecomposition(strategy.getCodebaseName(), RECOMMEND_FOLDER, strategy.getName(), decomposition);
                    strategy.addDecompositionName(decomposition.getName());

                    // Add decomposition's relevant information to the file
                    JSONObject decompositionJSON = new JSONObject();
                    String[] weights = decomposition.getName().split(",");
                    decompositionJSON.put("name", decomposition.getName());
                    decompositionJSON.put("traceType", traceType);
                    decompositionJSON.put("linkageType", linkageType);
                    decompositionJSON.put("accessMetricWeight", weights[0]);
                    decompositionJSON.put("writeMetricWeight", weights[1]);
                    decompositionJSON.put("readMetricWeight", weights[2]);
                    decompositionJSON.put("sequenceMetricWeight", weights[3]);
                    decompositionJSON.put("numberOfClusters", weights[6]);

                    decompositionJSON.put("maxClusterSize", decomposition.maxClusterSize());

                    for (Metric metric : decomposition.getMetrics())
                        decompositionJSON.put(metric.getType(), metric.getValue());

                    addRecommendationToJSON(recommendationJSON, decompositionJSON);

                    System.out.println("Decomposition " + recommendationJSON.length() + "/" + numberOfTotalSteps);

                    // Every 10 decompositions, updates the recommendation results file
                    if (recommendationJSON.length() % 20 == 0)
                        codebaseManager.writeRecommendationResults(strategy.getCodebaseName(), strategy.getName(), recommendationJSON);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        codebaseManager.writeRecommendationResults(strategy.getCodebaseName(), strategy.getName(), recommendationJSON);
        strategy.setCompleted(true);
    }

    private synchronized void addRecommendationToJSON(JSONArray arrayJSON, JSONObject decompositionJSON) {
        arrayJSON.put(decompositionJSON);
    }
}