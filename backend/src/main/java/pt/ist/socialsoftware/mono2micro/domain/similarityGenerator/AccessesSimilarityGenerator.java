package pt.ist.socialsoftware.mono2micro.domain.similarityGenerator;

import org.json.JSONArray;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.domain.source.AccessesSource;
import pt.ist.socialsoftware.mono2micro.domain.strategy.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.domain.source.Source.SourceType.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;

public class AccessesSimilarityGenerator implements SimilarityGenerator {

    private final Set<Short> entities = new TreeSet<>();
    private final Map<String, Integer> e1e2PairCount = new HashMap<>();
    private final Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>


    public AccessesSimilarityGenerator() {}

    @Override
    public void createSimilarityMatrix(Strategy strategy) throws Exception {
        switch (strategy.getType()) { // Needs to know the specific output format
            case ACCESSES_SCIPY:
                createSimilarityMatrixForSciPy((AccessesSciPyStrategy) strategy);
                break;
            default:
                throw new RuntimeException("No clustering algorithm type provided. Cannot infer the matrix format.");
        }

    }


    private void createSimilarityMatrixForSciPy(AccessesSciPyStrategy strategy) throws Exception {
        CodebaseManager codebaseManager = CodebaseManager.getInstance();

        fillMatrix(strategy);
        JSONObject matrixJSON = getSciPyMatrixAsJSONObject(strategy);
        codebaseManager.writeSimilarityMatrix(strategy.getCodebaseName(), strategy.getName(), matrixJSON);
    }

    private void fillMatrix(AccessesSciPyStrategy strategy)
        throws IOException
    {
        System.out.println("Creating similarity matrix...");

        CodebaseManager codebaseManager = CodebaseManager.getInstance();
        AccessesSource source = (AccessesSource) codebaseManager.getCodebaseSource(strategy.getCodebaseName(), ACCESSES);

        FunctionalityTracesIterator iter = new FunctionalityTracesIterator(
                source.getInputFilePath(),
                strategy.getTracesMaxLimit()
        );

        TraceDto t;
        Set<String> profileFunctionalities = source.getProfile(strategy.getProfile());

        for (String functionalityName : profileFunctionalities) {
            iter.nextFunctionalityWithName(functionalityName);

            switch (strategy.getTraceType()) {
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

    private JSONObject getSciPyMatrixAsJSONObject(AccessesSciPyStrategy strategy)
            throws Exception
    {
        JSONObject matrixData = new JSONObject();
        JSONArray similarityMatrixJSON = new JSONArray();

        int maxNumberOfPairs = getMaxNumberOfPairs(e1e2PairCount);

        for (short e1ID : entities) {
            JSONArray matrixRow = new JSONArray();

            for (short e2ID : entities) {
                if (e1ID == e2ID) {
                    matrixRow.put(1);
                    continue;
                }

                float[] weights = calculateSimilarityMatrixWeights(
                        e1ID,
                        e2ID,
                        maxNumberOfPairs
                );

                float metric = weights[0] * strategy.getAccessMetricWeight() / 100 +
                        weights[1] * strategy.getWriteMetricWeight() / 100 +
                        weights[2] * strategy.getReadMetricWeight() / 100 +
                        weights[3] * strategy.getSequenceMetricWeight() / 100;

                matrixRow.put(metric);
            }
            similarityMatrixJSON.put(matrixRow);
        }

        matrixData.put("linkageType", strategy.getLinkageType());
        matrixData.put("matrix", similarityMatrixJSON);
        matrixData.put("entities", entities);

        return matrixData;
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
}
