package pt.ist.socialsoftware.mono2micro.domain.similarityGenerator;

import org.json.JSONArray;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.domain.source.AccessesSource;
import pt.ist.socialsoftware.mono2micro.domain.strategy.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.*;

public class AccessesSimilarityGenerator implements SimilarityGenerator {

    private final Set<Short> entities = new TreeSet<>();
    private final Map<String, Integer> e1e2PairCount = new HashMap<>();
    private final Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>
    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();


    public AccessesSimilarityGenerator() {}

    @Override
    public void createSimilarityMatrix(Strategy strategy) throws Exception {
        switch (strategy.getType()) { // Needs to know the specific output format
            case ACCESSES_SCIPY:
                createSimilarityMatrixForSciPy((AccessesSciPyStrategy) strategy);
                break;
            case RECOMMENDATION_ACCESSES_SCIPY:
                createSimilarityMatricesForSciPy((RecommendAccessesSciPyStrategy) strategy);
                break;
            default:
                throw new RuntimeException("No strategy type provided. Cannot infer the matrix format.");
        }

    }

    //#############################################
    // ACCESSES_SCIPY
    //#############################################

    private void createSimilarityMatrixForSciPy(AccessesSciPyStrategy strategy) throws Exception {
        fillMatrix(strategy.getCodebaseName(), strategy.getProfile(), strategy.getTracesMaxLimit(), strategy.getTraceType());
        JSONObject matrixJSON = getSciPyMatrixAsJSONObject(
                getRawMatrix(),
                strategy.getAccessMetricWeight(),
                strategy.getWriteMetricWeight(),
                strategy.getReadMetricWeight(),
                strategy.getSequenceMetricWeight(),
                strategy.getLinkageType());
        codebaseManager.writeSimilarityMatrix(strategy.getCodebaseName(), STRATEGIES_FOLDER, strategy.getName(), "similarityMatrix.json", matrixJSON);
    }

    private void fillMatrix(String codebaseName, String profile, int tracesMaxLimit, Constants.TraceType traceType)
        throws IOException
    {
        System.out.println("Creating similarity matrix...");

        AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(codebaseName, ACCESSES);

        FunctionalityTracesIterator iter = new FunctionalityTracesIterator(
                source.getInputFilePath(),
                tracesMaxLimit
        );

        TraceDto t;
        Set<String> profileFunctionalities = source.getProfile(profile);

        for (String functionalityName : profileFunctionalities) {
            iter.nextFunctionalityWithName(functionalityName);
            iter.getFirstTrace();

            switch (traceType) {
                case LONGEST:
                    t = iter.getLongestTrace();

                    if (t != null)
                        fillEntityDataStructures(t.expand(2), functionalityName);

                    break;
                case WITH_MORE_DIFFERENT_ACCESSES:
                    t = iter.getTraceWithMoreDifferentAccesses();

                    if (t != null)
                        fillEntityDataStructures(t.expand(2), functionalityName);

                    break;
                default:
                    while (iter.hasMoreTraces()) {
                        t = iter.nextTrace();

                        fillEntityDataStructures(t.expand(2), functionalityName);
                    }
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
            int maxNumberOfPairs
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

    public float[][][] getRawMatrix() {
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

                float[] weights = calculateSimilarityMatrixWeights(e1ID, e2ID, maxNumberOfPairs);

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
    // RECOMMENDATION_ACCESSES_SCIPY
    //#############################################

    private void createSimilarityMatricesForSciPy(RecommendAccessesSciPyStrategy strategy) throws Exception {
        int INTERVAL = 100, STEP = 10;

        for (TraceType traceType : strategy.getTraceTypes()) {
            for (String linkageType : strategy.getLinkageTypes()) {
                if (strategy.containsCombination(traceType, linkageType))
                    continue;
                fillMatrix(strategy.getCodebaseName(), strategy.getProfile(), strategy.getTracesMaxLimit(), traceType);

                // needed later during clustering algorithm
                strategy.setNumberOfEntities(entities.size());

                float[][][] rawMatrix = getRawMatrix();

                for (int a = INTERVAL, remainder; a >= 0; a -= STEP) {
                    remainder = INTERVAL - a;
                    if (remainder == 0)
                        createAndWriteSimilarityMatrix(strategy, traceType, linkageType, rawMatrix, a, 0, 0, 0);
                    else {
                        for (int w = remainder, remainder2; w >= 0; w -= STEP) {
                            remainder2 = remainder - w;
                            if (remainder2 == 0)
                                createAndWriteSimilarityMatrix(strategy, traceType, linkageType, rawMatrix, a, w, 0, 0);
                            else {
                                for (int r = remainder2, remainder3; r >= 0; r -= STEP) {
                                    remainder3 = remainder2 - r;
                                    createAndWriteSimilarityMatrix(strategy, traceType, linkageType, rawMatrix, a, w, r, remainder3);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void createAndWriteSimilarityMatrix(
            RecommendAccessesSciPyStrategy strategy,
            TraceType traceType,
            String linkageType,
            float[][][] rawMatrix,
            float accessMetricWeight,
            float writeMetricWeight,
            float readMetricWeight,
            float sequenceMetricWeight
    ) throws Exception {
        JSONObject matrixJSON = getSciPyMatrixAsJSONObject(
                rawMatrix,
                accessMetricWeight,
                writeMetricWeight,
                readMetricWeight,
                sequenceMetricWeight,
                linkageType);

        codebaseManager.writeSimilarityMatrix(
                strategy.getCodebaseName(),
                RECOMMEND_FOLDER,
                strategy.getName(),
                getWeightAsString(accessMetricWeight) + "," +
                        getWeightAsString(writeMetricWeight) + "," +
                        getWeightAsString(readMetricWeight) + "," +
                        getWeightAsString(sequenceMetricWeight) + "," +
                        traceType + "," +
                        linkageType +
                        ".json",
                matrixJSON);
    }

    private String getWeightAsString(float weight) {
        return Float.toString(weight).replaceAll("\\.?0*$", "");
    }
}