package pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces;

import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;

public interface AccessesDecomposition {
    String ACCESSES_DECOMPOSITION = "ACCESSES_DECOMPOSITION";
    void setOutdated(boolean outdated);
    String getName();
    String getType();
    Strategy getStrategy();
    Similarity getSimilarity();
    Map<String, Cluster> getClusters();
    Map<String, Functionality> getFunctionalities();
    void setFunctionalities(Map<String, Functionality> functionalities);
    Map<Short, String> getEntityIDToClusterName();
    Map<String, Object> getMetrics();

    void setMetrics(Map<String, Object> metrics);

    void addCluster(Cluster cluster);
    Cluster removeCluster(String clusterName);

    Cluster getCluster(String clusterName);

    boolean clusterNameExists(String clusterName);

    default Functionality getFunctionality(String functionalityName) {
        Functionality c = getFunctionalities().get(functionalityName.replaceAll("\\.", "_"));

        if (c == null) throw new Error("Functionality with name: " + functionalityName + " not found");

        return c;
    }

    default boolean functionalityExists(String functionalityName) {
        return getFunctionalities().containsKey(functionalityName.replaceAll("\\.", "_"));
    }

    default void addFunctionality(Functionality functionality) {
        getFunctionalities().put(functionality.getName().replaceAll("\\.", "_"), functionality);
    }


    default void setupFunctionalities() throws Exception {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        FunctionalityRepository functionalityRepository = ContextManager.get().getBean(FunctionalityRepository.class);

        AccessesSimilarity similarity = (AccessesSimilarity) getSimilarity();
        AccessesRepresentation accesses = (AccessesRepresentation) getStrategy().getCodebase().getRepresentationByType(ACCESSES);
        InputStream inputStream = gridFsService.getFile(accesses.getName());
        Set<String> profileFunctionalities = accesses.getProfile(similarity.getProfile());

        FunctionalityTracesIterator iter = new FunctionalityTracesIterator(inputStream, similarity.getTracesMaxLimit());
        Map<String, DirectedAcyclicGraph<LocalTransaction, DefaultEdge>> localTransactionsGraphs = new HashMap<>();
        List<Functionality> newFunctionalities = new ArrayList<>();

        Iterator<String> availableFunctionalities = iter.getFunctionalitiesNames();
        while (availableFunctionalities.hasNext()) {
            String functionalityName = availableFunctionalities.next();
            if (!profileFunctionalities.contains(functionalityName) || this.functionalityExists(functionalityName))
                continue;

            iter.getFunctionalityWithName(functionalityName);
            Functionality functionality = new Functionality(getName(), functionalityName);

            // Get traces according to trace type
            List<TraceDto> traceDtos = iter.getTracesByType(similarity.getTraceType());
            functionality.setTraces(traceDtos);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionGraph = functionality.createLocalTransactionGraph(
                    getEntityIDToClusterName()
            );

            localTransactionsGraphs.put(functionality.getName(), localTransactionGraph);

            findClusterDependencies(localTransactionGraph);

            newFunctionalities.add(functionality);
            this.addFunctionality(functionality);
        }

        System.out.println("Calculating functionality metrics...");

        for (Functionality functionality: newFunctionalities) {
            functionality.defineFunctionalityType();
            functionality.calculateMetrics(this);

            // Functionality Redesigns
            if (similarity.getName() != null) { // If it does not have name, it means recommendation is using the similarity as a DTO
                FunctionalityService.createFunctionalityRedesign(
                        gridFsService,
                        this,
                        functionality,
                        Constants.DEFAULT_REDESIGN_NAME,
                        true,
                        localTransactionsGraphs.get(functionality.getName()));
            }
        }
        if (similarity.getName() != null) // If it does not have name, it means recommendation is using the similarity as a DTO
            functionalityRepository.saveAll(newFunctionalities);
    }

    default void findClusterDependencies(DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph) {
        Set<LocalTransaction> allLocalTransactions = localTransactionsGraph.vertexSet();

        for (LocalTransaction lt : allLocalTransactions) {
            // ClusterDependencies
            String clusterName = lt.getClusterName();
            if (!clusterName.equals("-1")) { // not root node
                SciPyCluster fromCluster = (SciPyCluster) getCluster(clusterName);

                List<LocalTransaction> nextLocalTransactions = successorListOf(localTransactionsGraph, lt);

                for (LocalTransaction nextLt : nextLocalTransactions)
                    fromCluster.addCouplingDependencies(nextLt.getClusterName(), nextLt.getFirstAccessedEntityIDs());
            }
        }
    }

    default void deleteAccessesProperties() {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        functionalityService.deleteFunctionalities(getFunctionalities().values());
        gridFsService.deleteFile(getName() + "_refactorization");
    }

    default String getEdgeWeightsFromAccesses() throws JSONException, IOException {
        Dendrogram similarity = (Dendrogram) getSimilarity();
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(similarity.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entities = new ArrayList<>(getEntityIDToClusterName().keySet());

        JSONArray edgesJSON = new JSONArray();
        int k = 0;
        for (int i = 0; i < entities.size(); i++) {
            short e1ID = entities.get(i);
            for (int j = i + 1; j < entities.size(); j++) {
                short e2ID = entities.get(j);

                JSONObject edgeJSON = new JSONObject();
                if (e1ID < e2ID) {
                    edgeJSON.put("e1ID", e1ID); edgeJSON.put("e2ID", e2ID);
                }
                else {
                    edgeJSON.put("e1ID", e2ID); edgeJSON.put("e1ID", e2ID);
                }
                edgeJSON.put("dist", copheneticDistances.getDouble(k));
                edgesJSON.put(edgeJSON);
                k++;
            }
        }

        // Get functionalities in common
        HashMap<String, JSONArray> entityRelations = new HashMap<>();
        for (Functionality functionality : getFunctionalities().values()) {
            List<Short> functionalityEntities = new ArrayList<>(functionality.getEntities().keySet());
            for (int i = 0; i < functionalityEntities.size(); i++)
                for (int j = i + 1; j < functionalityEntities.size(); j++) {
                    JSONArray relatedFunctionalities = entityRelations.get(edgeId(functionalityEntities.get(i), functionalityEntities.get(j)));
                    if (relatedFunctionalities == null) {
                        relatedFunctionalities = new JSONArray();
                        relatedFunctionalities.put(functionality.getName());
                        entityRelations.put(edgeId(functionalityEntities.get(i), functionalityEntities.get(j)), relatedFunctionalities);
                    }
                    else relatedFunctionalities.put(functionality.getName());
                }
        }

        JSONArray filteredEdgesJSON = new JSONArray();
        for (int i = 0; i < edgesJSON.length(); i++) {
            JSONObject edgeJSON = edgesJSON.getJSONObject(i);
            JSONArray relatedFunctionalities = entityRelations.get(edgeId(edgeJSON.getInt("e1ID"), edgeJSON.getInt("e2ID")));

            if (relatedFunctionalities != null) {
                edgeJSON.put("functionalities", relatedFunctionalities);
                filteredEdgesJSON.put(edgeJSON);
            }
        }

        return filteredEdgesJSON.toString();
    }

    default String edgeId(int node1, int node2) {
        if (node1 < node2)
            return node1 + "&" + node2;
        return node2 + "&" + node1;
    }

    default String getSearchItemsFromAccesses() throws JSONException {
        JSONArray searchItems = new JSONArray();

        for (Cluster cluster : getClusters().values()) {
            JSONObject clusterItem = new JSONObject();
            clusterItem.put("name", cluster.getName());
            clusterItem.put("type", "Cluster");
            clusterItem.put("id", cluster.getName());
            clusterItem.put("entities", Integer.toString(cluster.getElements().size()));
            searchItems.put(clusterItem);

            for (Element element : cluster.getElements()) {
                JSONObject entityItem = new JSONObject();
                entityItem.put("name", element.getName());
                entityItem.put("type", "Entity");
                entityItem.put("id", String.valueOf(element.getId()));
                entityItem.put("cluster", cluster.getName());
                searchItems.put(entityItem);
            }
        }

        for (Functionality functionality : getFunctionalities().values()) {
            JSONObject functionalityItem = new JSONObject();
            functionalityItem.put("name", functionality.getName());
            functionalityItem.put("type", "Functionality");
            functionalityItem.put("id", functionality.getName());
            functionalityItem.put("entities", Integer.toString(functionality.getEntities().size()));
            functionalityItem.put("funcType", functionality.getType().toString());
            searchItems.put(functionalityItem);
        }

        return searchItems.toString();
    }

    default void copyFunctionalities(FunctionalityService functionalityService, AccessesDecomposition snapshotDecomposition) throws IOException {
        for (Functionality functionality : getFunctionalities().values()) {
            Functionality snapshotFunctionality = new Functionality(snapshotDecomposition.getName(), functionality);
            snapshotFunctionality.setFunctionalityRedesignNameUsedForMetrics(functionality.getFunctionalityRedesignNameUsedForMetrics());

            for (String redesignName : functionality.getFunctionalityRedesigns().keySet()) {
                FunctionalityRedesign functionalityRedesign = functionalityService.getFunctionalityRedesign(functionality, redesignName);
                snapshotFunctionality.addFunctionalityRedesign(functionalityRedesign.getName(), snapshotFunctionality.getId() + functionalityRedesign.getName());
                functionalityService.saveFunctionalityRedesign(snapshotFunctionality, functionalityRedesign);
            }
            snapshotDecomposition.addFunctionality(snapshotFunctionality);
            functionalityService.saveFunctionality(snapshotFunctionality);
        }
    }

    default void renameClusterInFunctionalities(String clusterName, String newName) {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);

        // Change functionalities
        getFunctionalities().forEach((s, functionality) -> {
            Set<Short> entities = functionality.getEntitiesPerCluster().get(clusterName);
            if (entities != null) {
                functionality.getEntitiesPerCluster().remove(clusterName); functionality.getEntitiesPerCluster().put(newName, entities);

                functionality.getFunctionalityRedesigns().keySet().forEach(functionalityRedesignName -> {
                    try {
                        FunctionalityRedesign functionalityRedesign = functionalityService.getFunctionalityRedesign(functionality, functionalityRedesignName);
                        functionalityRedesign.getRedesign().forEach(localTransaction -> {
                            if (localTransaction.getClusterName().equals(clusterName)) {
                                localTransaction.setClusterName(newName);
                                localTransaction.setName(localTransaction.getId() + ": " + newName);
                            }
                        });
                        functionalityService.updateFunctionalityRedesign(functionality, functionalityRedesign);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            functionalityService.saveFunctionality(functionality);
        });
    }

    default void removeFunctionalitiesWithEntityIDs(Set<Short> elements) {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        for(Short entityId : elements)
            functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(entityId));
    }

    default void removeFunctionalitiesWithEntities(Set<Element> elements) {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        for(Element entity : elements)
            functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(entity.getId()));
    }

    default List<Functionality> removeFunctionalityWithEntity(short entityID) {
        Map<String, Functionality> newFunctionalities = new HashMap<>();
        List<Functionality> toDelete = new ArrayList<>();
        getFunctionalities().forEach((name, functionality) -> {
            if (functionality.containsEntity(entityID))
                toDelete.add(functionality);
            else newFunctionalities.put(name, functionality);
        });
        setFunctionalities(newFunctionalities);
        return toDelete;
    }
}