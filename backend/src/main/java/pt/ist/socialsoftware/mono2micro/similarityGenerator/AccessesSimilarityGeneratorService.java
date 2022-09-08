package pt.ist.socialsoftware.mono2micro.similarityGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.dendrogram.service.AccessesSciPyDendrogramService;
import pt.ist.socialsoftware.mono2micro.recommendation.service.RecommendAccessesSciPyService;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

@Service
public class AccessesSimilarityGeneratorService {

    @Autowired
    SourceService sourceService;

    @Autowired
    AccessesSciPyDendrogramService accessesSciPyDendrogramService;

    @Autowired
    RecommendAccessesSciPyService recommendAccessesSciPyService;

    //#############################################
    // ACCESSES SCIPY
    //#############################################

    public void createAccessesSciPyDendrogram(AccessesSciPyDendrogram dendrogram) {
        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{dendrogramName}/{similarityMatrixName}/createDendrogram", dendrogram.getName(), dendrogram.getSimilarityMatrixName())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();
        try {
            JSONObject jsonObject = new JSONObject(response);
            dendrogram.setImageName(jsonObject.getString("imageName"));
            dendrogram.setCopheneticDistanceName(jsonObject.getString("copheneticDistanceName"));
        } catch(Exception e) { throw new RuntimeException("Could not produce or extract elements from JSON Object"); }
    }

    public void createSimilarityMatrixForSciPy(AccessesSciPyDendrogram dendrogram) throws Exception {
        Set<Short> entities = new TreeSet<>();
        Map<String, Integer> e1e2PairCount = new HashMap<>();
        Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>
        AccessesSource source = (AccessesSource) dendrogram.getStrategy().getCodebase().getSourceByType(ACCESSES);
        fillMatrix(
                entities,
                e1e2PairCount,
                entityFunctionalities,
                source,
                dendrogram.getProfile(),
                dendrogram.getTracesMaxLimit(),
                dendrogram.getTraceType());

        JSONObject matrixJSON = getSciPyMatrixAsJSONObject(
                entities,
                getRawMatrix(entities, e1e2PairCount, entityFunctionalities),
                dendrogram.getAccessMetricWeight(),
                dendrogram.getWriteMetricWeight(),
                dendrogram.getReadMetricWeight(),
                dendrogram.getSequenceMetricWeight(),
                dendrogram.getLinkageType());

        dendrogram.setSimilarityMatrixName(dendrogram.getName() + "_similarityMatrix");
        accessesSciPyDendrogramService.saveSimilarityMatrix(new ByteArrayInputStream(matrixJSON.toString().getBytes()), dendrogram.getSimilarityMatrixName());
    }

    private void fillMatrix(
            Set<Short> entities,
            Map<String, Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities,
            AccessesSource source,
            String profile,
            int tracesMaxLimit,
            Constants.TraceType traceType
    )
            throws IOException, JSONException {
        System.out.println("Creating similarity matrix...");

        FunctionalityTracesIterator iter = new FunctionalityTracesIterator(
                sourceService.getSourceFileAsInputStream(source.getName()),
                tracesMaxLimit
        );

        TraceDto t;
        Set<String> profileFunctionalities = source.getProfile(profile);

        for (String functionalityName : profileFunctionalities) {
            iter.getFunctionalityWithName(functionalityName);

            switch (traceType) {
                case LONGEST:
                    t = iter.getLongestTrace();

                    if (t != null)
                        fillEntityDataStructures(e1e2PairCount, entityFunctionalities, t.expand(2), functionalityName);

                    break;
                case WITH_MORE_DIFFERENT_ACCESSES:
                    t = iter.getTraceWithMoreDifferentAccesses();

                    if (t != null)
                        fillEntityDataStructures(e1e2PairCount, entityFunctionalities, t.expand(2), functionalityName);

                    break;
                default:
                    List<TraceDto> traceDtos = iter.getAllTraces();
                    for (TraceDto traceDto : traceDtos)
                        fillEntityDataStructures(e1e2PairCount, entityFunctionalities, traceDto.expand(2), functionalityName);
            }
        }

        entities.addAll(entityFunctionalities.keySet());
    }

    private static int getMaxNumberOfPairs(Map<String,Integer> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    private float[] calculateSimilarityMatrixWeights(
            short e1ID,
            short e2ID,
            int maxNumberOfPairs,
            Map<String, Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities
    ) {

        float inCommon = 0;
        float inCommonW = 0;
        float inCommonR = 0;
        float e1FunctionalitiesW = 0;
        float e1FunctionalitiesR = 0;

        for (Pair<String, Byte> e1Functionalities : entityFunctionalities.get(e1ID)) {
            for (Pair<String, Byte> e2Functionalities : entityFunctionalities.get(e2ID)) {
                if (e1Functionalities.getFirst().equals(e2Functionalities.getFirst())) {
                    inCommon++;
                    // != 1 == contains("W") -> "W" or "RW"
                    if (e1Functionalities.getSecond() != 1 && e2Functionalities.getSecond() != 1)
                        inCommonW++;

                    // != 2 == contains("R") -> "R" or "RW"
                    if (e1Functionalities.getSecond() != 2 && e2Functionalities.getSecond() != 2)
                        inCommonR++;
                }
            }

            // != 1 == contains("W") -> "W" or "RW"
            if (e1Functionalities.getSecond() != 1)
                e1FunctionalitiesW++;

            // != 2 == contains("R") -> "R" or "RW"
            if (e1Functionalities.getSecond() != 2)
                e1FunctionalitiesR++;
        }

        float accessWeight = inCommon / entityFunctionalities.get(e1ID).size();
        float writeWeight = e1FunctionalitiesW == 0 ? 0 : inCommonW / e1FunctionalitiesW;
        float readWeight = e1FunctionalitiesR == 0 ? 0 : inCommonR / e1FunctionalitiesR;

        String e1e2 = e1ID + "->" + e2ID;
        float e1e2Count = e1e2PairCount.getOrDefault(e1e2, 0);

        float sequenceWeight;

        if (maxNumberOfPairs != 0)
            sequenceWeight = e1e2Count / maxNumberOfPairs;
        else // nao ha controladores a aceder a mais do que uma entidade
            sequenceWeight = 0;

        return new float[] {
                accessWeight,
                writeWeight,
                readWeight,
                sequenceWeight
        };
    }

    private void fillEntityDataStructures(
            Map<String, Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities,
            List<AccessDto> accessesList,
            String functionalityName
    ) {

        for (int i = 0; i < accessesList.size(); i++) {
            AccessDto access = accessesList.get(i);
            short entityID = access.getEntityID();
            byte mode = access.getMode();

            if (entityFunctionalities.containsKey(entityID)) {
                boolean containsFunctionality = false;

                for (Pair<String, Byte> functionalityPair : entityFunctionalities.get(entityID)) {
                    if (functionalityPair.getFirst().equals(functionalityName)) {
                        containsFunctionality = true;

                        if (functionalityPair.getSecond() != 3 && functionalityPair.getSecond() != mode)
                            functionalityPair.setSecond((byte) 3); // "RW" -> 3

                        break;
                    }
                }

                if (!containsFunctionality) {
                    entityFunctionalities.get(entityID).add(new Pair<>(functionalityName, mode));
                }

            } else {
                List<Pair<String, Byte>> functionalitiesPairs = new ArrayList<>();
                functionalitiesPairs.add(new Pair<>(functionalityName, mode));

                entityFunctionalities.put(entityID, functionalitiesPairs);
            }

            if (i < accessesList.size() - 1) {
                AccessDto nextAccess = accessesList.get(i + 1);
                short nextEntityID = nextAccess.getEntityID();

                if (entityID != nextEntityID) {
                    String e1e2 = entityID + "->" + nextEntityID;
                    String e2e1 = nextEntityID + "->" + entityID;

                    int count = e1e2PairCount.getOrDefault(e1e2, 0);
                    e1e2PairCount.put(e1e2, count + 1);

                    count = e1e2PairCount.getOrDefault(e2e1, 0);
                    e1e2PairCount.put(e2e1, count + 1);
                }
            }
        }
    }

    public float[][][] getRawMatrix(
            Set<Short> entities,
            Map<String, Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities
    ) {
        int maxNumberOfPairs = getMaxNumberOfPairs(e1e2PairCount);

        float[][][] rawMatrix = new float[entities.size()][entities.size()][4];

        int i = 0;
        for (short e1ID : entities) {
            int j = 0;

            for (short e2ID : entities) {
                if (e1ID == e2ID) {
                    rawMatrix[i][j][0] = rawMatrix[i][j][1] = rawMatrix[i][j][2] = rawMatrix[i][j][3] = 1;
                    j++;
                    continue;
                }

                float[] weights = calculateSimilarityMatrixWeights(e1ID, e2ID, maxNumberOfPairs, e1e2PairCount, entityFunctionalities);

                rawMatrix[i][j][0] = weights[0];
                rawMatrix[i][j][1] = weights[1];
                rawMatrix[i][j][2] = weights[2];
                rawMatrix[i][j][3] = weights[3];
                j++;
            }
            i++;
        }

        return rawMatrix;
    }

    private JSONObject getSciPyMatrixAsJSONObject(
            Set<Short> entities,
            float[][][] matrix,
            float accessMetricWeight,
            float writeMetricWeight,
            float readMetricWeight,
            float sequenceMetricWeight,
            String linkageType
    ) throws Exception {
        JSONObject matrixData = new JSONObject();
        JSONArray similarityMatrixJSON = new JSONArray();

        int i = 0;
        for (short e1ID : entities) {
            JSONArray matrixRow = new JSONArray();
            int j = 0;

            for (short e2ID : entities) {
                if (e1ID == e2ID) {
                    matrixRow.put(1);
                    j++;
                    continue;
                }

                float metric = matrix[i][j][0] * accessMetricWeight / 100 +
                        matrix[i][j][1] * writeMetricWeight / 100 +
                        matrix[i][j][2] * readMetricWeight / 100 +
                        matrix[i][j][3] * sequenceMetricWeight / 100;

                matrixRow.put(metric);
                j++;
            }
            similarityMatrixJSON.put(matrixRow);
            i++;
        }

        matrixData.put("linkageType", linkageType);
        matrixData.put("matrix", similarityMatrixJSON);
        matrixData.put("entities", entities);

        return matrixData;
    }

    //#############################################
    // RECOMMEND ACCESSES SCIPY
    //#############################################

    public void createSimilarityMatricesForSciPy(AccessesSource source, RecommendAccessesSciPy recommendation) throws Exception {
        int INTERVAL = 100, STEP = 10;
        Set<Short> entities = new TreeSet<>();
        Map<String, Integer> e1e2PairCount = new HashMap<>();
        Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>

        for (TraceType traceType : recommendation.getTraceTypes()) {
            for (String linkageType : recommendation.getLinkageTypes()) {
                if (recommendation.containsCombination(traceType, linkageType))
                    continue;
                fillMatrix(entities, e1e2PairCount, entityFunctionalities, source, recommendation.getProfile(), recommendation.getTracesMaxLimit(), traceType);

                // needed later during clustering algorithm
                recommendation.setNumberOfEntities(entities.size());

                float[][][] rawMatrix = getRawMatrix(entities, e1e2PairCount, entityFunctionalities);

                for (int a = INTERVAL, remainder; a >= 0; a -= STEP) {
                    remainder = INTERVAL - a;
                    if (remainder == 0)
                        createAndWriteSimilarityMatrix(recommendation, entities, traceType, linkageType, rawMatrix, a, 0, 0, 0);
                    else {
                        for (int w = remainder, remainder2; w >= 0; w -= STEP) {
                            remainder2 = remainder - w;
                            if (remainder2 == 0)
                                createAndWriteSimilarityMatrix(recommendation, entities, traceType, linkageType, rawMatrix, a, w, 0, 0);
                            else {
                                for (int r = remainder2, remainder3; r >= 0; r -= STEP) {
                                    remainder3 = remainder2 - r;
                                    createAndWriteSimilarityMatrix(recommendation, entities, traceType, linkageType, rawMatrix, a, w, r, remainder3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void createAndWriteSimilarityMatrix(
            RecommendAccessesSciPy recommendation,
            Set<Short> entities,
            TraceType traceType,
            String linkageType,
            float[][][] rawMatrix,
            float accessMetricWeight,
            float writeMetricWeight,
            float readMetricWeight,
            float sequenceMetricWeight
    ) throws Exception {
        JSONObject matrixJSON = getSciPyMatrixAsJSONObject(
                entities,
                rawMatrix,
                accessMetricWeight,
                writeMetricWeight,
                readMetricWeight,
                sequenceMetricWeight,
                linkageType);

        String similarityMatrixName =
                recommendation.getName() + "," +
                getWeightAsString(accessMetricWeight) + "," +
                getWeightAsString(writeMetricWeight) + "," +
                getWeightAsString(readMetricWeight) + "," +
                getWeightAsString(sequenceMetricWeight) + "," +
                traceType + "," +
                linkageType;

        recommendation.addSimilarityMatrixName(similarityMatrixName);
        recommendAccessesSciPyService.saveSimilarityMatrix(new ByteArrayInputStream(matrixJSON.toString().getBytes()), similarityMatrixName);
    }

    private String getWeightAsString(float weight) {
        return Float.toString(weight).replaceAll("\\.?0*$", "");
    }
}