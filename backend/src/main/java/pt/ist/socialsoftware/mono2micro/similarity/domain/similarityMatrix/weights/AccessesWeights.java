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
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityGraphTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;
import pt.ist.socialsoftware.mono2micro.utils.TracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesGraphRepresentation.ACCESSES_GRAPH;

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
        AccessesRepresentation accesses = Utils.getCodebaseAccessRepresentation(similarity.getStrategy().getCodebase());
        fillRawMatrixFromAccesses(rawMatrix, fillFromIndex, gridFsService.getFile(accesses.getName()), accesses.getProfile(s.getProfile()), s.getTraceType(), s.getTracesMaxLimit(), accesses.getType());
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        System.out.println("fill matrix");
        RecommendMatrixSciPy r = (RecommendMatrixSciPy) recommendation;
        System.out.println("get representation");
        AccessesRepresentation accesses = Utils.getCodebaseAccessRepresentation(recommendation.getStrategy().getCodebase());
        System.out.println("fill raw matrix from acc");
        fillRawMatrixFromAccesses(rawMatrix, fillFromIndex, gridFsService.getFile(accesses.getName()), accesses.getProfile(r.getProfile()), r.getTraceType(), r.getTracesMaxLimit(), accesses.getType());
        System.out.println("weights out");
    }

    public static TracesIterator getTraceIterator(String representationType, InputStream accessesFile, int tracesMaxLimit) throws JSONException, IOException {
        switch (representationType) {
            case ACCESSES:
                return new FunctionalityTracesIterator(accessesFile, tracesMaxLimit);
        
            case ACCESSES_GRAPH:
                return new FunctionalityGraphTracesIterator(accessesFile);

            default:
                break;
        }

        return null;
    }

    public static void fillRawMatrixFromAccesses(
            float[][][] rawMatrix,
            int fillFromIndex,
            InputStream accessesFile,
            Set<String> profileFunctionalities,
            Constants.TraceType traceType,
            int tracesMaxLimit,
            String representationType
    ) throws JSONException, IOException {
        Set<Short> entities = new TreeSet<>();
        Map<String, Float> e1e2PairCount = new HashMap<>();
        Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities = new HashMap<>(); // Map<entityID, List<Pair<functionalityName, accessMode>>>
        System.out.println("fill data structures");
        fillDataStructures(entities, e1e2PairCount, entityFunctionalities, getTraceIterator(representationType, accessesFile, tracesMaxLimit), profileFunctionalities, traceType);
        System.out.println("fill raw matrix");
        fillRawMatrix(rawMatrix, entities, e1e2PairCount, entityFunctionalities, fillFromIndex);
    }

    public static void fillDataStructures(
            Set<Short> entities,
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities,
            TracesIterator iter,
            Set<String> profileFunctionalities,
            Constants.TraceType traceType
    )
            throws JSONException {

        TraceDto t;

        for (String functionalityName : profileFunctionalities) {
            iter.getFunctionalityWithName(functionalityName);
            System.out.println(functionalityName);
            switch (traceType) {
                case LONGEST:
                case MOST_PROBABLE:
                case WITH_MORE_DIFFERENT_ACCESSES:
                    t = iter.getTracesByType(traceType).get(0);

                    if (t != null)
                        fillEntityDataStructures(e1e2PairCount, entityFunctionalities, t.expand(2), functionalityName);

                    break;
                default:
                    if (iter instanceof FunctionalityGraphTracesIterator) {
                        System.out.println("graph trace iterator");
                        ((FunctionalityGraphTracesIterator)iter).fillEntityDataStructures(e1e2PairCount, entityFunctionalities);
                    } else {
                        System.out.println("trace iterator");
                        List<TraceDto> traceDtos = iter.getAllTraces();
                        for (TraceDto traceDto : traceDtos)
                            fillEntityDataStructures(e1e2PairCount, entityFunctionalities, traceDto.expand(2), functionalityName);
                    }
                    System.out.println("finished");
            }
            String filepath = System.getProperty("user.home") + File.separator + "output" + File.separator;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            String fileName = dtf.format(now) + " - " + functionalityName;
            fileName = fileName.replaceAll("\\s|/","_");
            storeJsonFile(filepath, fileName, entityFunctionalities);
        }
        System.out.println("add all");
        entities.addAll(entityFunctionalities.keySet());
    }

    private static void storeJsonFile(String filepath, String fileName, Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities) {
        try {
            File filePath = new File(filepath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream(filepath+fileName + ".json"), entityFunctionalities);

            System.out.println("File '" + fileName + "' created at: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fillEntityDataStructures(
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities,
            List<AccessDto> accessesList,
            String functionalityName
    ) {
        System.out.println("fill data structures");
        float runningProbability = 1.0f;
        for (int i = 0; i < accessesList.size(); i++) {
            AccessDto access = accessesList.get(i);
            short entityID = access.getEntityID();
            byte mode = access.getMode();

            runningProbability *= access.getProbability();

            if (entityFunctionalities.containsKey(entityID)) {
                Pair<String, Byte> thisFunctionalityPair = null;
                float currentAccessingEntityProb = -1;

                for (Pair<String, Byte> functionalityPair : entityFunctionalities.get(entityID).keySet()) {
                    if (functionalityPair.getFirst().equals(functionalityName)) {
                        thisFunctionalityPair = functionalityPair;
                        currentAccessingEntityProb = entityFunctionalities.get(entityID).get(functionalityPair);

                        if (functionalityPair.getSecond() != 3 && functionalityPair.getSecond() != mode)
                            functionalityPair.setSecond((byte) 3); // "RW" -> 3

                        break;
                    }
                }

                if (thisFunctionalityPair == null || currentAccessingEntityProb == -1 || runningProbability > currentAccessingEntityProb) {
                    if (thisFunctionalityPair == null) {
                        thisFunctionalityPair = new Pair<>(functionalityName, mode);
                    }
    
                    entityFunctionalities.get(entityID).put(thisFunctionalityPair, runningProbability);
                }

            } else {
                Map<Pair<String, Byte>, Float> functionalitiesPairMap = new HashMap<>();
                functionalitiesPairMap.put(new Pair<>(functionalityName, mode), runningProbability);

                entityFunctionalities.put(entityID, functionalitiesPairMap);
            }

            if (i < accessesList.size() - 1) {
                AccessDto nextAccess = accessesList.get(i + 1);
                short nextEntityID = nextAccess.getEntityID();

                if (entityID != nextEntityID) {
                    String e1e2 = entityID + "->" + nextEntityID;
                    String e2e1 = nextEntityID + "->" + entityID;

                    float count = e1e2PairCount.getOrDefault(e1e2, 0f);
                    e1e2PairCount.put(e1e2, count + runningProbability * nextAccess.getProbability());

                    count = e1e2PairCount.getOrDefault(e2e1, 0f);
                    e1e2PairCount.put(e2e1, count + runningProbability * nextAccess.getProbability());
                }
            }
        }
    }

    public static void fillRawMatrix(
            float[][][] rawMatrix,
            Set<Short> entities,
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities,
            int fillFromIndex
    ) {
        float maxNumberOfPairs = getMaxNumberOfPairs(e1e2PairCount);

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

    private static float getMaxNumberOfPairs(Map<String,Float> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    private static float[] calculateSimilarityMatrixWeights(
            short e1ID,
            short e2ID,
            float maxNumberOfPairs,
            Map<String, Float> e1e2PairCount,
            Map<Short, Map<Pair<String, Byte>, Float>> entityFunctionalities
    ) {

        float inCommon = 0;
        float inCommonW = 0;
        float inCommonR = 0;
        float e1FunctionalitiesW = 0;
        float e1FunctionalitiesR = 0;

        for (Pair<String, Byte> e1Functionalities : entityFunctionalities.get(e1ID).keySet()) {
            Float e1Probability = entityFunctionalities.get(e1ID).get(e1Functionalities);
            for (Pair<String, Byte> e2Functionalities : entityFunctionalities.get(e2ID).keySet()) {
                Float e2Probability = entityFunctionalities.get(e2ID).get(e2Functionalities);
                Float e1AndE2Probability = e1Probability * e2Probability;
                if (e1Functionalities.getFirst().equals(e2Functionalities.getFirst())) {
                    inCommon += e1AndE2Probability;
                    // != 1 == contains("W") -> "W" or "RW"
                    if (e1Functionalities.getSecond() != 1 && e2Functionalities.getSecond() != 1)
                        inCommonW += e1AndE2Probability;

                    // != 2 == contains("R") -> "R" or "RW"
                    if (e1Functionalities.getSecond() != 2 && e2Functionalities.getSecond() != 2)
                        inCommonR += e1AndE2Probability;
                }
            }

            // != 1 == contains("W") -> "W" or "RW"
            if (e1Functionalities.getSecond() != 1)
                e1FunctionalitiesW += e1Probability;

            // != 2 == contains("R") -> "R" or "RW"
            if (e1Functionalities.getSecond() != 2)
                e1FunctionalitiesR += e1Probability;
        }

        float accessWeight = inCommon / entityFunctionalities.get(e1ID).size();
        float writeWeight = e1FunctionalitiesW == 0 ? 0 : inCommonW / e1FunctionalitiesW;
        float readWeight = e1FunctionalitiesR == 0 ? 0 : inCommonR / e1FunctionalitiesR;

        String e1e2 = e1ID + "->" + e2ID;
        float e1e2Count = e1e2PairCount.getOrDefault(e1e2, 0f);

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
