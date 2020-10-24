package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.dto.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Utils {

    public static Integer lineno() { return new Throwable().getStackTrace()[1].getLineNumber(); }

    public static void print(String message, Integer lineNumber) { System.out.println("[" + lineNumber + "] " + message); }

    public static Set<String> getJsonFileKeys(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonfactory = mapper.getFactory();

        JsonParser jsonParser = jsonfactory.createParser(is);
        JsonToken jsonToken = jsonParser.nextValue(); // JsonToken.START_OBJECT

        if (jsonToken != JsonToken.START_OBJECT) {
            System.err.println("Json must start with a left curly brace");
            System.exit(-1);
        }

        Set<String> keys = new HashSet<>();

        jsonParser.nextValue();

        while (jsonToken != JsonToken.END_OBJECT) {
            if (jsonParser.getCurrentName() != null) {
                String keyName = jsonParser.getCurrentName();
                System.out.println("Key name: " + keyName);
                keys.add(keyName);
                jsonParser.skipChildren();
            }

            jsonToken = jsonParser.nextValue();
        }

        is.close();

        return keys;
    }

    // FIXME better name for this function pls
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

    public static int getMaxNumberOfPairs(Map<String,Integer> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    public static class GetDataToBuildSimilarityMatrixResult {
        public Set<Short> entities;
        public Map<String, Integer> e1e2PairCount;
        public Map<Short, List<Pair<String, Byte>>> entityControllers;

        public GetDataToBuildSimilarityMatrixResult() {}

        public GetDataToBuildSimilarityMatrixResult(
            Set<Short> entities,
            Map<String, Integer> e1e2PairCount, Map<Short,
            List<Pair<String, Byte>>> entityControllers
        ) {
            this.entities = entities;
            this.e1e2PairCount = e1e2PairCount;
            this.entityControllers = entityControllers;
        }
    }

    public static GetDataToBuildSimilarityMatrixResult getDataToBuildSimilarityMatrix(
        Codebase codebase,
        String profile,
        int tracesMaxLimit,
        Constants.TraceType traceType
    )
        throws IOException
    {
        System.out.println("Creating similarity matrix...");

        Map<Short, List<Pair<String, Byte>>> entityControllers = new HashMap<>();
        Map<String, Integer> e1e2PairCount = new HashMap<>();

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
                        Utils.fillEntityDataStructures(
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
                        Utils.fillEntityDataStructures(
                            entityControllers,
                            e1e2PairCount,
                            t.expand(2),
                            controllerName
                        );
                    }

                    break;

                case REPRESENTATIVE:
                    Set<String> tracesIds = iter.getRepresentativeTraces();
                    // FIXME probably here we create a second controllerTracesIterator
                    iter.reset();

                    while (iter.hasMoreTraces()) {
                        t = iter.nextTrace();

                        if (tracesIds.contains(String.valueOf(t.getId()))) {
                            Utils.fillEntityDataStructures(
                                entityControllers,
                                e1e2PairCount,
                                t.expand(2),
                                controllerName
                            );
                        }
                    }

                    break;

                default:
                    while (iter.hasMoreTraces()) {
                        t = iter.nextTrace();

                        Utils.fillEntityDataStructures(
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

        Set<Short> entities = new TreeSet<>(entityControllers.keySet());

        return new GetDataToBuildSimilarityMatrixResult(
            entities,
            e1e2PairCount,
            entityControllers
        );
    }

//    private static SimilarityMatrixDto getMatrixData(
//        Set<Short> entityIDs,
//        Map<String,Integer> e1e2PairCount,
//        Map<Short, List<Pair<String, Byte>>> entityControllers,
//        String linkageType
//    ) {
//
//        SimilarityMatrixDto matrixData = new SimilarityMatrixDto();
//
//        List<List<List<Float>>> similarityMatrix = new ArrayList<>();
//
//        int maxNumberOfPairs = Utils.getMaxNumberOfPairs(e1e2PairCount);
//
//        for (short e1ID : entityIDs) {
//            List<List<Float>> matrixRow = new ArrayList<>();
//
//            for (short e2ID : entityIDs) {
//                List<Float> metric = new ArrayList<>();
//
//                if (e1ID == e2ID) {
//                    metric.add((float) 1);
//                    metric.add((float) 1);
//                    metric.add((float) 1);
//                    metric.add((float) 1);
//
//                    matrixRow.add(metric);
//                    continue;
//                }
//
//                float[] metrics = Utils.calculateSimilarityMatrixMetrics(
//                    entityControllers,
//                    e1e2PairCount,
//                    e1ID,
//                    e2ID,
//                    maxNumberOfPairs
//                );
//
//                metric.add(metrics[0]);
//                metric.add(metrics[1]);
//                metric.add(metrics[2]);
//                metric.add(metrics[3]);
//
//                matrixRow.add(metric);
//            }
//            similarityMatrix.add(matrixRow);
//        }
//        matrixData.setMatrix(similarityMatrix);
//        matrixData.setEntities(entityIDs);
//        matrixData.setLinkageType(linkageType);
//
//        return matrixData;
//    }

    public static float[] calculateSimilarityMatrixMetrics(
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

        float accessMetric = inCommon / entityControllers.get(e1ID).size();
        float writeMetric = e1ControllersW == 0 ? 0 : inCommonW / e1ControllersW;
        float readMetric = e1ControllersR == 0 ? 0 : inCommonR / e1ControllersR;

        String e1e2 = e1ID + "->" + e2ID;
        float e1e2Count = e1e2PairCount.getOrDefault(e1e2, 0);

        float sequenceMetric;

        if (maxNumberOfPairs != 0)
            sequenceMetric = e1e2Count / maxNumberOfPairs;
        else // nao ha controladores a aceder a mais do que uma entidade
            sequenceMetric = 0;

        return new float[] {
            accessMetric,
            writeMetric,
            readMetric,
            sequenceMetric
        };
    }

    public static class CalculateTracePerformanceResult {
        public int performance = 0;
        public String firstAccessedClusterName = null;

        public CalculateTracePerformanceResult() {}

        public CalculateTracePerformanceResult(int performance, String firstAccessedClusterName) {
            this.performance = performance;
            this.firstAccessedClusterName = firstAccessedClusterName;
        }
    }

    public static CalculateTracePerformanceResult calculateTracePerformance(
        List<ReducedTraceElementDto> elements,
        Map<Short, String> entityIDToClusterName,
        int from,
        int to
    ) {
        int numberOfElements = elements == null ? 0 : elements.size();

        if (numberOfElements == 0) return new CalculateTracePerformanceResult();

        if (numberOfElements == 1) return new CalculateTracePerformanceResult(
            1,
            entityIDToClusterName.get(((AccessDto) elements.get(0)).getEntityID())
        );

        int performance = 0;
        String previousClusterName = null;
        String firstAccessedClusterName = null;

        int i = from;

        while (i < to) {
            ReducedTraceElementDto element = elements.get(i);

            if (element instanceof RuleDto) {
                RuleDto r = (RuleDto) element;

                CalculateTracePerformanceResult result = calculateTracePerformance(
                    elements,
                    entityIDToClusterName,
                    i + 1,
                    i + 1 + r.getCount()
                );

                String sequenceFirstAccessedClusterName = result.firstAccessedClusterName;
                int sequencePerformance = result.performance;

                if (firstAccessedClusterName == null)
                    firstAccessedClusterName = sequenceFirstAccessedClusterName;

                // hop between an access (previous cluster if it exists) and the sequence in question
                if (previousClusterName != null && !previousClusterName.equals(sequenceFirstAccessedClusterName))
                    performance++;

                // performance of the sequence multiplied by the number of times it occurs
                performance += sequencePerformance * r.getOccurrences();

                // Here we assume that a sequence will always have an access as its last element
                short sequenceLastAccessedEntityID = ((AccessDto) elements.get(i + r.getCount())).getEntityID();
                String sequenceLastAccessedClusterName = entityIDToClusterName.get(sequenceLastAccessedEntityID);

                previousClusterName = sequenceLastAccessedClusterName;

                // If the rule has more than 1 occurrence, then we want to consider the hop between the final access and the first one
                if (r.getOccurrences() > 1 && !sequenceFirstAccessedClusterName.equals(sequenceLastAccessedClusterName))
                    performance += r.getOccurrences() - 1;

                i += 1 + r.getCount();

            } else {

                AccessDto a = (AccessDto) element;
                short entityID = a.getEntityID();

                try {
                    String currentClusterName = entityIDToClusterName.get(entityID);

                    if (firstAccessedClusterName == null)
                        firstAccessedClusterName = currentClusterName;

                    if (previousClusterName == null)
                        previousClusterName = currentClusterName;

                    else if (!currentClusterName.equals(previousClusterName)) {
                        performance++;
                        previousClusterName = currentClusterName;
                    }

                    i++;
                }

                catch (Exception e) {
                    System.err.println("No assigned entity with ID " + entityID + " to a cluster.");
                    throw e;
                }

            }
        }

        return new CalculateTracePerformanceResult(performance, firstAccessedClusterName);
    }

    public static Map<String, List<Controller>> getClusterControllers(
        Set<String> profileControllers,
        List<Cluster> clusters,
        Map<String, Controller> controllers
    )
        throws Exception
    {
        Map<String, List<Controller>> clusterControllers = new HashMap<>();

        for (Cluster cluster : clusters) {
            List<Controller> touchedControllers = new ArrayList<>();

            for (String controllerName : profileControllers) {
                Controller controller = controllers.get(controllerName);

                if (controller == null)
                    throw new Exception("Controller: " + controllerName + " not found");

                if (!controller.getEntities().isEmpty()) {
                    for (short entityID : controller.getEntities().keySet()) {
                        if (cluster.containsEntity(entityID)) {
                            touchedControllers.add(controller);
                            break;
                        }
                    }
                }
            }

            clusterControllers.put(cluster.getName(), touchedControllers);
        }
        return clusterControllers;
    }

    public static Map<String, List<Cluster>> getControllerClusters(
        Set<String> profileControllers,
        List<Cluster> clusters,
        Map<String, Controller> controllers
    )
        throws Exception
    {
        Map<String, List<Cluster>> controllerClusters = new HashMap<>();

        for (String controllerName : profileControllers) {
            Controller controller = controllers.get(controllerName);

            if (controller == null)
                throw new Exception("Controller: " + controllerName + " not found");

            if (!controller.getEntities().isEmpty()) {
                List<Cluster> touchedClusters = new ArrayList<>();

                for (Cluster cluster : clusters) {

                    for (short entityID : cluster.getEntities()) {
                        if (controller.containsEntity(entityID)) {
                            touchedClusters.add(cluster);
                            break;
                        }
                    }
                }

                controllerClusters.put(controller.getName(), touchedClusters);
            }
        }

        return controllerClusters;
    }

    public static class GetControllersClustersAndClustersControllersResult {
        public Map<String, Set<Cluster>> controllersClusters;
        public Map<String, Set<Controller>> clustersControllers;

        public GetControllersClustersAndClustersControllersResult(
            Map<String, Set<Cluster>> controllersClusters,
            Map<String, Set<Controller>> clustersControllers
        ) {
            this.controllersClusters = controllersClusters;
            this.clustersControllers = clustersControllers;
        }

        public Map<String, Set<Cluster>> getControllersClusters() { return controllersClusters; }
        public Map<String, Set<Controller>> getClustersControllers() { return clustersControllers; }
    }

    public static GetControllersClustersAndClustersControllersResult getControllersClustersAndClustersControllers(
        Set<String> profileControllers,
        List<Cluster> clusters,
        Map<String, Controller> controllers
    )
        throws Exception
    {
        Map<String, Set<Cluster>> controllersClusters = new HashMap<>();
        Map<String, Set<Controller>> clustersControllers = new HashMap<>();

        for (Cluster cluster : clusters) {

            Set<Controller> touchedControllers = new HashSet<>();

            for (String controllerName : profileControllers) {
                Controller controller = controllers.get(controllerName);

                if (controller == null)
                    throw new Exception("Controller: " + controllerName + " not found");

                if (!controller.getEntities().isEmpty()) {
                    for (short entityID : controller.getEntities().keySet()) {
                        if (cluster.containsEntity(entityID)) {
                            touchedControllers.add(controller);

                            Set<Cluster> controllerClusters = controllersClusters.getOrDefault(
                                controllerName,
                                new HashSet<>()
                            );

                            if (!controllerClusters.contains(cluster)) {
                                controllerClusters.add(cluster);

                                controllersClusters.put(
                                    controllerName,
                                    controllerClusters
                                );
                            }

                            break;
                        }
                    }
                }
            }

            clustersControllers.put(
                cluster.getName(),
                touchedControllers
            );
        }

        return new GetControllersClustersAndClustersControllersResult(
            controllersClusters,
            clustersControllers
        );
    }

}
