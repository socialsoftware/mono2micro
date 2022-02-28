package pt.ist.socialsoftware.mono2micro.utils.similarityGenerators;

import org.json.JSONArray;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserDto;
import pt.ist.socialsoftware.mono2micro.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;
import pt.ist.socialsoftware.mono2micro.utils.Constants.TraceType;
import pt.ist.socialsoftware.mono2micro.utils.ClusteringAlgorithmType;
import pt.ist.socialsoftware.mono2micro.utils.ControllerTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.IOException;
import java.util.*;

public class DefaultSimilarityGenerator implements SimilarityGenerator {
    private final Codebase codebase;
    private final String profile;
    private final int tracesMaxLimit;
    private final TraceType traceType;
    private final Dendrogram dendrogram;
    private final ClusteringAlgorithmType clusteringAlgorithm;

    private final Set<Short> entities = new TreeSet<>();
    private final Map<String, Integer> e1e2PairCount = new HashMap<>();
    private final Map<Short, List<Pair<String, Byte>>> entityControllers = new HashMap<>();


    public DefaultSimilarityGenerator(Codebase codebase, Dendrogram dendrogram) {
        this.codebase = codebase;
        this.profile = dendrogram.getProfile();
        this.tracesMaxLimit = dendrogram.getTracesMaxLimit();
        this.traceType = dendrogram.getTraceType();
        this.clusteringAlgorithm = dendrogram.getClusteringAlgorithmType();
        this.dendrogram = dendrogram;
    }

    public DefaultSimilarityGenerator(Codebase codebase, AnalyserDto analyser) {
        this.codebase = codebase;
        this.profile = analyser.getProfile();
        this.tracesMaxLimit = analyser.getTracesMaxLimit();
        this.traceType = analyser.getTraceType();
        this.clusteringAlgorithm = analyser.getClusteringAlgorithmType();
        this.dendrogram = null;
    }

    @Override
    public void buildMatrix()
        throws IOException
    {
        System.out.println("Creating similarity matrix...");

        ControllerTracesIterator iter = new ControllerTracesIterator(
                codebase.getDatafilePath(),
                tracesMaxLimit
        );

        TraceDto t;
        Set<String> profileControllers = codebase.getProfile(profile);

        for (String controllerName : profileControllers) {
            iter.nextControllerWithName(controllerName);

            switch (traceType) {
                case LONGEST:
                    // FIXME return accesses of longest trace instead of the trace itself
                    t = iter.getLongestTrace();

                    if (t != null) {
                        fillEntityDataStructures(
                                entityControllers,
                                e1e2PairCount,
                                t.expand(2),
                                controllerName
                        );
                    }

                    break;

                case WITH_MORE_DIFFERENT_ACCESSES:
                    t = iter.getTraceWithMoreDifferentAccesses();

                    if (t != null) {
                        fillEntityDataStructures(
                                entityControllers,
                                e1e2PairCount,
                                t.expand(2),
                                controllerName
                        );
                    }

                    break;

                // FIXME not going to fix this since time is scarce
//                case REPRESENTATIVE:
//                    Set<String> tracesIds = iter.getRepresentativeTraces();
//                    // FIXME probably here we create a second controllerTracesIterator
//                    iter.reset();
//
//                    while (iter.hasMoreTraces()) {
//                        t = iter.nextTrace();
//
//                        if (tracesIds.contains(String.valueOf(t.getId()))) {
//                            fillEntityDataStructures(
//                                entityControllers,
//                                e1e2PairCount,
//                                t.expand(2),
//                                controllerName
//                            );
//                        }
//                    }
//
//                    break;

                default:
                    while (iter.hasMoreTraces()) {
                        t = iter.nextTrace();

                        fillEntityDataStructures(
                                entityControllers,
                                e1e2PairCount,
                                t.expand(2),
                                controllerName
                        );
                    }
            }

            t = null; // release memory
        }

        iter = null; // release memory

        entities.addAll(entityControllers.keySet());
    }

    @Override
    public JSONObject getSciPyMatrixAsJSONObject()
            throws Exception
    {
        JSONObject matrixData = new JSONObject();
        JSONArray similarityMatrixJSON = new JSONArray();

        int maxNumberOfPairs = getMaxNumberOfPairs(e1e2PairCount);

        for (short e1ID : entities) {
            JSONArray matrixRow = new JSONArray();

            for (short e2ID : entities) {
                JSONArray weightsCell = new JSONArray();
                if (e1ID == e2ID) {
                    weightsCell.put(1);
                    weightsCell.put(1);
                    weightsCell.put(1);
                    weightsCell.put(1);
                    matrixRow.put(weightsCell);
                    continue;
                }

                float[] weights = calculateSimilarityMatrixWeights(
                        entityControllers,
                        e1e2PairCount,
                        e1ID,
                        e2ID,
                        maxNumberOfPairs
                );

                weightsCell.put(weights[0]);
                weightsCell.put(weights[1]);
                weightsCell.put(weights[2]);
                weightsCell.put(weights[3]);

                matrixRow.put(weightsCell);
            }
            similarityMatrixJSON.put(matrixRow);
        }

        if (dendrogram != null) { //Analyser does not have dendrogram
            matrixData.put("access", dendrogram.getAccessMetricWeight());
            matrixData.put("write", dendrogram.getWriteMetricWeight());
            matrixData.put("read", dendrogram.getReadMetricWeight());
            matrixData.put("sequence", dendrogram.getSequenceMetricWeight());
            matrixData.put("linkageType", dendrogram.getLinkageType());
        }
        else matrixData.put("linkageType", "average");

        matrixData.put("matrix", similarityMatrixJSON);
        matrixData.put("entities", entities);

        return matrixData;
    }

    @Override
    public void writeSimilarityMatrix() throws Exception {
        switch (clusteringAlgorithm) {
            case SCIPY:
                CodebaseManager.getInstance().writeDendrogramSimilarityMatrix(
                        codebase.getName(),
                        dendrogram != null ? dendrogram.getName() : "analyser",
                        getSciPyMatrixAsJSONObject()
                );
                break;

            //Add other printing formats here
        }
    }

    public static int getMaxNumberOfPairs(Map<String,Integer> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    public static float[] calculateSimilarityMatrixWeights(
            Map<Short,List<Pair<String, Byte>>> entityControllers, // entityID -> [<controllerName, accessMode>, ...]
            Map<String,Integer> e1e2PairCount,
            short e1ID,
            short e2ID,
            int maxNumberOfPairs
    ) {

        float inCommon = 0;
        float inCommonW = 0;
        float inCommonR = 0;
        float e1ControllersW = 0;
        float e1ControllersR = 0;

        for (Pair<String, Byte> e1Controller : entityControllers.get(e1ID)) {
            for (Pair<String, Byte> e2Controller : entityControllers.get(e2ID)) {
                if (e1Controller.getFirst().equals(e2Controller.getFirst())) {
                    inCommon++;
                    // != 1 == contains("W") -> "W" or "RW"
                    if (e1Controller.getSecond() != 1 && e2Controller.getSecond() != 1)
                        inCommonW++;

                    // != 2 == contains("R") -> "R" or "RW"
                    if (e1Controller.getSecond() != 2 && e2Controller.getSecond() != 2)
                        inCommonR++;
                }
            }

            // != 1 == contains("W") -> "W" or "RW"
            if (e1Controller.getSecond() != 1)
                e1ControllersW++;

            // != 2 == contains("R") -> "R" or "RW"
            if (e1Controller.getSecond() != 2)
                e1ControllersR++;
        }

        float accessWeight = inCommon / entityControllers.get(e1ID).size();
        float writeWeight = e1ControllersW == 0 ? 0 : inCommonW / e1ControllersW;
        float readWeight = e1ControllersR == 0 ? 0 : inCommonR / e1ControllersR;

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

    public static void fillEntityDataStructures(
            Map<Short, List<Pair<String, Byte>>> entityControllers,
            Map<String, Integer> e1e2PairCount,
            List<AccessDto> accessesList,
            String controllerName
    ) {

        for (int i = 0; i < accessesList.size(); i++) {
            AccessDto access = accessesList.get(i);
            short entityID = access.getEntityID();
            byte mode = access.getMode();

            if (entityControllers.containsKey(entityID)) {
                boolean containsController = false;

                for (Pair<String, Byte> controllerPair : entityControllers.get(entityID)) {
                    if (controllerPair.getFirst().equals(controllerName)) {
                        containsController = true;

                        if (controllerPair.getSecond() != 3 && controllerPair.getSecond() != mode)
                            controllerPair.setSecond((byte) 3); // "RW" -> 3

                        break;
                    }
                }

                if (!containsController) {
                    entityControllers.get(entityID).add(
                            new Pair<>(
                                    controllerName,
                                    mode
                            )
                    );
                }

            } else {
                List<Pair<String, Byte>> controllersPairs = new ArrayList<>();
                controllersPairs.add(
                        new Pair<>(
                                controllerName,
                                mode
                        )
                );


                entityControllers.put(entityID, controllersPairs);
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
