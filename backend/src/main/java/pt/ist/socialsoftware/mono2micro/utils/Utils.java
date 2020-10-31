package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Controller;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
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
//                            Utils.fillEntityDataStructures(
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

    public static class GetLocalTransactionsSequenceAndCalculateTracePerformanceResult {
        public int performance = 0;
        public Decomposition.LocalTransaction lastLocalTransaction = null;
        public List<Decomposition.LocalTransaction> localTransactionsSequence = new ArrayList<>();
        public String firstAccessedClusterName = null;
        Map<Short, Byte> entityIDToMode = new HashMap<>();

        public GetLocalTransactionsSequenceAndCalculateTracePerformanceResult() {}

        public GetLocalTransactionsSequenceAndCalculateTracePerformanceResult(
            int performance,
            Decomposition.LocalTransaction lastLocalTransaction,
            List<Decomposition.LocalTransaction> localTransactionsSequence,
            String firstAccessedClusterName,
            Map<Short, Byte> entityIDToMode
        ) {
            this.performance = performance;
            this.lastLocalTransaction = lastLocalTransaction;
            this.localTransactionsSequence = localTransactionsSequence;
            this.firstAccessedClusterName = firstAccessedClusterName;
            this.entityIDToMode = entityIDToMode;
        }
    }

    public static GetLocalTransactionsSequenceAndCalculateTracePerformanceResult getLocalTransactionsSequenceAndCalculateTracePerformance(
        int lastLocalTransactionID,
        Decomposition.LocalTransaction lastLocalTransaction,
        List<ReducedTraceElementDto> elements,
        Map<Short, String> entityIDToClusterName,
        Map<Short, Byte> entityIDToMode,
        int from,
        int to
    ) {
        int numberOfElements = elements == null ? 0 : elements.size();

        if (numberOfElements == 0) return new GetLocalTransactionsSequenceAndCalculateTracePerformanceResult();

        int performance = 0;
        String firstAccessedClusterName = null;

        Decomposition.LocalTransaction currentLocalTransaction = lastLocalTransaction;
        List<Decomposition.LocalTransaction> localTransactionsSequence = new ArrayList<>();

        int i = from;

        while (i < to) {
            ReducedTraceElementDto element = elements.get(i);

            if (element instanceof RuleDto) {
                RuleDto r = (RuleDto) element;

                GetLocalTransactionsSequenceAndCalculateTracePerformanceResult result = getLocalTransactionsSequenceAndCalculateTracePerformance(
                    lastLocalTransactionID,
                    currentLocalTransaction,
                    elements,
                    entityIDToClusterName,
                    entityIDToMode,
                    i + 1,
                    i + 1 + r.getCount()
                );

                String sequenceFirstAccessedClusterName = result.firstAccessedClusterName;
                int sequencePerformance = result.performance;

                if (firstAccessedClusterName == null)
                    firstAccessedClusterName = sequenceFirstAccessedClusterName;

                // hop between an access (previous cluster if it exists) and the sequence in question
                if (
                    currentLocalTransaction != null && // this currentLT is already outdated that's why it's useful
                    currentLocalTransaction.getClusterID() != Short.parseShort(sequenceFirstAccessedClusterName)
                ) {
                    performance++;
                }

                // performance of the sequence multiplied by the number of times it occurs
                performance += sequencePerformance * r.getOccurrences();

                // update outdated variables
                currentLocalTransaction = result.lastLocalTransaction;
                lastLocalTransactionID = currentLocalTransaction.getId();
                entityIDToMode = result.entityIDToMode;
                localTransactionsSequence.addAll(result.localTransactionsSequence);

                // If the rule has more than 1 occurrence,
                // then we want to consider the hop between the final access and the first one
                if (
                    r.getOccurrences() > 1 &&
                    Short.parseShort(sequenceFirstAccessedClusterName) != currentLocalTransaction.getClusterID()
                ) {
                    performance += r.getOccurrences() - 1;
                }

                i += 1 + r.getCount();

            } else {

                AccessDto access = (AccessDto) element;
                short accessedEntityID = access.getEntityID();
                byte accessMode = access.getMode();

                String currentClusterName = entityIDToClusterName.get(accessedEntityID);

                if (currentClusterName == null) {
                    System.err.println("No assigned entity with ID " + accessedEntityID + " to a cluster.");
                    System.exit(-1);
                }

                short currentClusterID = Short.parseShort(currentClusterName);

                if (firstAccessedClusterName == null)
                    firstAccessedClusterName = currentClusterName;

                if (currentLocalTransaction == null) { // if it's the first element
                    performance++;

                    currentLocalTransaction = new Decomposition.LocalTransaction(
                        ++lastLocalTransactionID,
                        currentClusterID,
                        new HashSet<AccessDto>() {{ add(access); }},
                        accessedEntityID
                    );

                    entityIDToMode.put(accessedEntityID, accessMode);
                }

                else {
                    if (currentClusterID == currentLocalTransaction.getClusterID()) {
                        // check if it is a costly access
                        boolean hasCost = false;
                        Byte savedMode = entityIDToMode.get(accessedEntityID);

                        if (savedMode == null) {
                            hasCost = true;

                        } else {
                            if (savedMode == 1 && accessMode == 2) // "R" -> 1, "W" -> 2
                                hasCost = true;
                        }

                        if (hasCost) {
                            currentLocalTransaction.addClusterAccess(access);
                            entityIDToMode.put(accessedEntityID, accessMode);
                        }

                    } else {
                        performance++;

                        localTransactionsSequence.add(
                            new Decomposition.LocalTransaction(currentLocalTransaction)
                        );

                        currentLocalTransaction = new Decomposition.LocalTransaction(
                            ++lastLocalTransactionID,
                            currentClusterID,
                            new HashSet<AccessDto>() {{ add(access); }},
                            accessedEntityID
                        );

                        entityIDToMode.clear();
                        entityIDToMode.put(accessedEntityID, accessMode);
                    }
                }

                i++;
            }
        }

        // The current LT should be added at the end since there arent more accesses
        // This happens when the "from" is equal to 0 meaning that it's the recursion
        // main/first level of depth

        if (from == 0) {
            if (
                currentLocalTransaction != null &&
                currentLocalTransaction.getClusterAccesses().size() > 0
            )
                localTransactionsSequence.add(currentLocalTransaction);
        }

        return new GetLocalTransactionsSequenceAndCalculateTracePerformanceResult(
            performance,
            currentLocalTransaction,
            localTransactionsSequence,
            firstAccessedClusterName,
            entityIDToMode
        );
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
