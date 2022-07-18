package pt.ist.socialsoftware.mono2micro.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.ReducedTraceElementDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.RuleDto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;

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

    public static class GetLocalTransactionsSequenceAndCalculateTracePerformanceResult {
        public int performance = 0;
        public LocalTransaction lastLocalTransaction = null;
        public List<LocalTransaction> localTransactionsSequence = new ArrayList<>();
        public String firstAccessedClusterName = null;
        Map<Short, Byte> entityIDToMode = new HashMap<>();

        public GetLocalTransactionsSequenceAndCalculateTracePerformanceResult() {}

        public GetLocalTransactionsSequenceAndCalculateTracePerformanceResult(
            int performance,
            LocalTransaction lastLocalTransaction,
            List<LocalTransaction> localTransactionsSequence,
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
        LocalTransaction lastLocalTransaction,
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

        LocalTransaction currentLocalTransaction = lastLocalTransaction;
        List<LocalTransaction> localTransactionsSequence = new ArrayList<>();

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
                    !currentLocalTransaction.getClusterName().equals(sequenceFirstAccessedClusterName)
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
                    !sequenceFirstAccessedClusterName.equals(currentLocalTransaction.getClusterName())
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

                if (firstAccessedClusterName == null)
                    firstAccessedClusterName = currentClusterName;

                if (currentLocalTransaction == null) { // if it's the first element
                    performance++;

                    currentLocalTransaction = new LocalTransaction(
                        ++lastLocalTransactionID,
                        currentClusterName,
                        new HashSet<AccessDto>() {{ add(access); }},
                        accessedEntityID
                    );

                    entityIDToMode.put(accessedEntityID, accessMode);
                }

                else {
                    if (currentClusterName.equals(currentLocalTransaction.getClusterName())) {
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
                            new LocalTransaction(currentLocalTransaction)
                        );

                        currentLocalTransaction = new LocalTransaction(
                            ++lastLocalTransactionID,
                            currentClusterName,
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

    public static Map<String, Set<Cluster>> getFunctionalitiesClusters(
            Map<Short, String> entityIDToClusterName,
            Map<String, Cluster> clusters,
            Collection<Functionality> functionalities
    ) {
        Map<String, Set<Cluster>> functionalitiesClusters = new HashMap<>();

        for (Functionality functionality : functionalities) {
            String functionalityName = functionality.getName();

            Set<Cluster> functionalityClusters = new HashSet<>();

            for (short entityID : functionality.getEntities().keySet()) {
                Cluster cluster = clusters.get(entityIDToClusterName.get(entityID));
                functionalityClusters.add(cluster);
            }

            functionalitiesClusters.put(
                    functionalityName,
                    functionalityClusters
            );
        }

        return functionalitiesClusters;
    }

    public static Map<String, List<Functionality>> getClustersFunctionalities(
            Map<Short, String> entityIDToClusterName,
            Map<String, Cluster> clusters,
            Collection<Functionality> functionalities
    ) {
        Map<String, List<Functionality>> clustersFunctionalities = new HashMap<>();

        for (Functionality functionality : functionalities) {
            for (short entityID : functionality.getEntities().keySet()) {
                Cluster cluster = clusters.get(entityIDToClusterName.get(entityID));

                List<Functionality> clusterFunctionalities = clustersFunctionalities.getOrDefault(cluster.getName(), new ArrayList<>());
                if (clusterFunctionalities.size() == 0)
                    clustersFunctionalities.put(cluster.getName(), clusterFunctionalities);

                if (clusterFunctionalities.stream().noneMatch(prevFunctionality -> prevFunctionality.getName().equals(functionality.getName())))
                    clusterFunctionalities.add(new Functionality(functionality));
            }
        }

        return clustersFunctionalities;
    }

    public static class GetSerializableLocalTransactionsGraphResult {
        public List<LocalTransaction> nodes;
        public List<String> links;

        public GetSerializableLocalTransactionsGraphResult(
            List<LocalTransaction> nodes,
            List<String> links
        ) {
            this.nodes = nodes;
            this.links = links;
        }

        public List<LocalTransaction> getNodes() { return nodes; }
        public List<String> getLinks() { return links; }
    }

    public static GetSerializableLocalTransactionsGraphResult getSerializableLocalTransactionsGraph(
        DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph
    ) {
        List<LocalTransaction> nodes = new ArrayList<>();
        List<String> links = new ArrayList<>();
        Iterator<LocalTransaction> iterator = localTransactionsGraph.iterator();

        while (iterator.hasNext()) {
            LocalTransaction lt = iterator.next();

            List<LocalTransaction> ltChildren = successorListOf(localTransactionsGraph, lt);
            for (LocalTransaction ltC : ltChildren)
                links.add(lt.getId() + "->" + ltC.getId());

            nodes.add(lt);
        }

        return new GetSerializableLocalTransactionsGraphResult(
            nodes,
            links
        );
    }
}
