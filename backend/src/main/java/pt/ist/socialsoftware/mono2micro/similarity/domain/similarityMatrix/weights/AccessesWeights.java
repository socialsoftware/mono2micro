package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyAccessesAndRepository;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;

public class AccessesWeights extends Weights {
    public static final String ACCESSES_WEIGHTS = "ACCESSES_WEIGHTS";
    private float accessMetricWeight;
    private float writeMetricWeight;
    private float readMetricWeight;
    private float sequenceMetricWeight;

    public AccessesWeights() {}

    public AccessesWeights(float accessMetricWeight, float writeMetricWeight, float readMetricWeight, float sequenceMetricWeight) {
        this.accessMetricWeight = accessMetricWeight;
        this.writeMetricWeight = writeMetricWeight;
        this.readMetricWeight = readMetricWeight;
        this.sequenceMetricWeight = sequenceMetricWeight;
    }

    @Override
    public String getType() {
        return ACCESSES_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 4;
    }

    @Override
    public float[] getWeights() {
        return new float[]{accessMetricWeight, writeMetricWeight, readMetricWeight, sequenceMetricWeight};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<>(Arrays.asList("accessMetricWeight", "writeMetricWeight", "readMetricWeight", "sequenceMetricWeight"));
    }

    @Override
    public String getName() {
        StringBuilder result = new StringBuilder("ws(");
        result.append("Ac")
                .append(Math.round(getWeights()[0]))
                .append(",")
                .append("Wr")
                .append(Math.round(getWeights()[1]))
                .append(",")
                .append("Re")
                .append(Math.round(getWeights()[2]))
                .append(",")
                .append("Se")
                .append(Math.round(getWeights()[3]))
                .append(")");
        return result.toString();
    }

    @Override
    public void setWeightsFromArray(float[] weightsArray) {
        this.accessMetricWeight = weightsArray[0];
        this.writeMetricWeight = weightsArray[1];
        this.readMetricWeight = weightsArray[2];
        this.sequenceMetricWeight = weightsArray[3];
    }

    public float getAccessMetricWeight() {
        return accessMetricWeight;
    }
    public void setAccessMetricWeight(float accessMetricWeight) {
        this.accessMetricWeight = accessMetricWeight;
    }
    public float getWriteMetricWeight() {
        return writeMetricWeight;
    }
    public void setWriteMetricWeight(float writeMetricWeight) {
        this.writeMetricWeight = writeMetricWeight;
    }
    public float getReadMetricWeight() {
        return readMetricWeight;
    }
    public void setReadMetricWeight(float readMetricWeight) {
        this.readMetricWeight = readMetricWeight;
    }
    public float getSequenceMetricWeight() {
        return sequenceMetricWeight;
    }
    public void setSequenceMetricWeight(float sequenceMetricWeight) {
        this.sequenceMetricWeight = sequenceMetricWeight;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccessesWeights))
            return false;
        AccessesWeights accessesWeights = (AccessesWeights) object;
        return this.accessMetricWeight == accessesWeights.getAccessMetricWeight() &&
                this.writeMetricWeight == accessesWeights.getWriteMetricWeight() &&
                this.readMetricWeight == accessesWeights.getReadMetricWeight() &&
                this.sequenceMetricWeight == accessesWeights.getSequenceMetricWeight();
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        SimilarityScipyAccessesAndRepository s = (SimilarityScipyAccessesAndRepository) similarity;
        AccessesRepresentation accesses = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);
        fillRawMatrixFromAccesses(rawMatrix, fillFromIndex, gridFsService.getFile(accesses.getName()), accesses.getProfile(s.getProfile()), s.getTraceType(), s.getTracesMaxLimit());
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        RecommendMatrixSciPy r = (RecommendMatrixSciPy) recommendation;
        AccessesRepresentation accesses = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);
        fillRawMatrixFromAccesses(rawMatrix, fillFromIndex, gridFsService.getFile(accesses.getName()), accesses.getProfile(r.getProfile()), r.getTraceType(), r.getTracesMaxLimit());
    }

    public static void fillRawMatrixFromAccesses(
            float[][][] rawMatrix,
            int fillFromIndex,
            InputStream accessesFile,
            Set<String> profileFunctionalities,
            Constants.TraceType traceType,
            int tracesMaxLimit
    ) throws JSONException, IOException {
        Set<Short> entities = new TreeSet<>();
        Map<String, Integer> e1e2PairCount = new HashMap<>();
        Map<Short, List<Pair<String, Byte>>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>
        fillDataStructures(entities, e1e2PairCount, entityFunctionalities, new FunctionalityTracesIterator(accessesFile, tracesMaxLimit), profileFunctionalities, traceType);
        fillRawMatrix(rawMatrix, entities, e1e2PairCount, entityFunctionalities, fillFromIndex);
    }

    public static void fillDataStructures(
            Set<Short> entities,
            Map<String, Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities,
            FunctionalityTracesIterator iter,
            Set<String> profileFunctionalities,
            Constants.TraceType traceType
    )
            throws JSONException {
        System.out.println("Creating similarity matrix...");

        TraceDto t;

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

    private static void fillEntityDataStructures(
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

    public static void fillRawMatrix(
            float[][][] rawMatrix,
            Set<Short> entities,
            Map<String, Integer> e1e2PairCount,
            Map<Short, List<Pair<String, Byte>>> entityFunctionalities,
            int fillFromIndex
    ) {
        int maxNumberOfPairs = getMaxNumberOfPairs(e1e2PairCount);

        int i = 0;
        for (short e1ID : entities) {
            int j = 0;

            for (short e2ID : entities) {
                if (e1ID == e2ID) {
                    for (int k = fillFromIndex; k < fillFromIndex + 4; k++)
                        rawMatrix[i][j][k] = 1;
                    j++;
                    continue;
                }

                float[] weights = calculateSimilarityMatrixWeights(e1ID, e2ID, maxNumberOfPairs, e1e2PairCount, entityFunctionalities);

                for (int k = fillFromIndex, l = 0; k < fillFromIndex + 4; k++, l++)
                    rawMatrix[i][j][k] = weights[l];
                j++;
            }
            i++;
        }
    }

    private static int getMaxNumberOfPairs(Map<String,Integer> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    private static float[] calculateSimilarityMatrixWeights(
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
}
