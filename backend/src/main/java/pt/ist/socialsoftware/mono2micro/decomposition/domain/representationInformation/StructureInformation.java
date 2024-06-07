package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation;

import org.apache.commons.io.IOUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.FunctionalityTracesIterator;
import pt.ist.socialsoftware.mono2micro.utils.SimilarityStructureIterator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;
import static pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation.STRUCTURE;
import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.STRUCTURE_TYPE;

public class StructureInformation extends RepresentationInformation {
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>

    public StructureInformation() {}

    @Override
    public void setup(Decomposition decomposition) throws Exception {
        this.decompositionName = decomposition.getName();
        decomposition.addRepresentationInformation(this);
        //setupFunctionalities(decomposition);
    }

    @Override
    public void snapshot(Decomposition snapshotDecomposition, Decomposition decomposition) throws IOException {
        this.decompositionName = snapshotDecomposition.getName();
        snapshotDecomposition.addRepresentationInformation(this);
        StructureInformation structureInformation = (StructureInformation) snapshotDecomposition.getRepresentationInformationByType(STRUCTURE_TYPE);
        //copyFunctionalities(structureInformation);
    }

    @Override
    public String getType() {
        return STRUCTURE_TYPE;
    }

    @Override
    public void update(Decomposition decomposition) throws Exception {
        //setupFunctionalities(decomposition);
    }

    @Override
    public List<DecompositionMetricCalculator> getDecompositionMetrics() {
        return new ArrayList<>(Arrays.asList(
                new CohesionMetricCalculator(),
                new ComplexityMetricCalculator(),
                new CouplingMetricCalculator(),
                new PerformanceMetricCalculator()));
    }

    @Override
    public List<String> getParameters() {
        return new ArrayList<>(Arrays.asList(
            RepresentationInformationParameters.PROFILE_PARAMETER.toString(),
            RepresentationInformationParameters.TRACES_MAX_LIMIT_PARAMETER.toString(),
            RepresentationInformationParameters.TRACE_TYPE_PARAMETER.toString()));
    }

    @Override
    public void deleteProperties() {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);

        functionalityService.deleteFunctionalities(getFunctionalities().values());
        gridFsService.deleteFile(getDecompositionName() + "_refactorization");
    }

    /* public Map<String, Functionality> getFunctionalities() {
        return functionalities;
    } */

    /* public void setFunctionalities(Map<String, Functionality> functionalities) {
        this.functionalities = functionalities;
    }

    public Functionality getFunctionality(String functionalityName) {
        Functionality c = getFunctionalities().get(functionalityName.replace(".", "_"));

        if (c == null) throw new Error("Functionality with name: " + functionalityName + " not found");

        return c;
    }

    public boolean functionalityExists(String functionalityName) {
        return getFunctionalities().containsKey(functionalityName.replace(".", "_"));
    }

    public void addFunctionality(Functionality functionality) {
        getFunctionalities().put(functionality.getName().replace(".", "_"), functionality);
    }
    */

    public void setupEntities(Decomposition decomposition) throws Exception {
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        // Ligar aqui o repositorio de entidades
        //EntityRepository entityRepository = ContextManager.get().getBean(EntityRepository.class);

        SimilarityScipy similarity = (SimilarityScipy) decomposition.getSimilarity();
        StructureRepresentation structure = (StructureRepresentation) decomposition.getStrategy().getCodebase().getRepresentationByFileType(STRUCTURE_TYPE);
        InputStream inputStream = gridFsService.getFile(structure.getName());
        
        SimilarityStructureIterator iter = new SimilarityStructureIterator(inputStream);

    }

    /*
    public void findClusterDependencies(Decomposition decomposition, DirectedAcyclicGraph<LocalTransaction, DefaultEdge> localTransactionsGraph) {
        Set<LocalTransaction> allLocalTransactions = localTransactionsGraph.vertexSet();

        for (LocalTransaction lt : allLocalTransactions) {
            // ClusterDependencies
            String clusterName = lt.getClusterName();
            if (!clusterName.equals("-1")) { // not root node
                Partition fromCluster = (Partition) decomposition.getCluster(clusterName);

                List<LocalTransaction> nextLocalTransactions = successorListOf(localTransactionsGraph, lt);

                for (LocalTransaction nextLt : nextLocalTransactions)
                    fromCluster.addCouplingDependencies(nextLt.getClusterName(), nextLt.getFirstAccessedEntityIDs());
            }
        }
    } */

    @Override
    public String getEdgeWeights(Decomposition decomposition) throws JSONException, IOException {
        /* GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        Dendrogram dendrogram = ((SimilarityScipy) decomposition.getSimilarity()).getDendrogram();
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(dendrogram.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entities = new ArrayList<>(decomposition.getEntityIDToClusterName().keySet());

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

        return filteredEdgesJSON.toString(); */
        return "ola";
    }

    /* public String edgeId(int node1, int node2) {
        if (node1 < node2)
            return node1 + "&" + node2;
        return node2 + "&" + node1;
    } */

    @Override
    public String getSearchItems(Decomposition decomposition) throws JSONException {
        /* JSONArray searchItems = new JSONArray();

        for (Cluster cluster : decomposition.getClusters().values()) {
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

        return searchItems.toString(); */
        return "ola2";
    }

    /* public void copyFunctionalities(StructureInformation structureInformation) throws IOException {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        for (Functionality functionality : structureInformation.getFunctionalities().values()) {
            Functionality snapshotFunctionality = new Functionality(getDecompositionName(), functionality);
            snapshotFunctionality.setFunctionalityRedesignNameUsedForMetrics(functionality.getFunctionalityRedesignNameUsedForMetrics());

            for (String redesignName : functionality.getFunctionalityRedesigns().keySet()) {
                FunctionalityRedesign functionalityRedesign = functionalityService.getFunctionalityRedesign(functionality, redesignName);
                snapshotFunctionality.addFunctionalityRedesign(functionalityRedesign.getName(), snapshotFunctionality.getId() + functionalityRedesign.getName());
                functionalityService.saveFunctionalityRedesign(snapshotFunctionality, functionalityRedesign);
            }
            addFunctionality(snapshotFunctionality);
            functionalityService.saveFunctionality(snapshotFunctionality);
        }
    } */

    @Override
    public void renameClusterInFunctionalities(String clusterName, String newName) {
        /* FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);

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
        }); */
    }

    @Override
    public void removeFunctionalitiesWithEntityIDs(Decomposition decomposition, Set<Short> elements) {
        /* FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        for(Short entityId : elements)
            functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(entityId));
        decomposition.setOutdated(true); */
    }

    /* private List<Functionality> removeFunctionalityWithEntity(short entityID) {
        Map<String, Functionality> newFunctionalities = new HashMap<>();
        List<Functionality> toDelete = new ArrayList<>();
        getFunctionalities().forEach((name, functionality) -> {
            if (functionality.containsEntity(entityID))
                toDelete.add(functionality);
            else newFunctionalities.put(name, functionality);
        });
        setFunctionalities(newFunctionalities);
        return toDelete;
    } */
}
