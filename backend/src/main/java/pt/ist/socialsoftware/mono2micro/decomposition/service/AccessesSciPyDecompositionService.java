package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.apache.commons.io.IOUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.log.service.AccessesSciPyLogService;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.AccessesSciPyStrategyRepository;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;

@Service
public class AccessesSciPyDecompositionService {
    @Autowired
    AccessesSciPyStrategyRepository strategyRepository;

    @Autowired
    SciPyClusteringAlgorithmService clusteringService;

    @Autowired
    AccessesSciPyDecompositionRepository decompositionRepository;

    @Autowired
    AccessesSciPyLogService accessesSciPyLogService;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    SourceService sourceService;

    @Autowired
    GridFsService gridFsService;

    public void createDecomposition(String strategyName, String cutType, float cutValue) throws Exception {
        AccessesSciPyStrategy strategy = strategyRepository.findByName(strategyName);
        clusteringService.createDecomposition(strategy, cutType, cutValue);
    }

    public void createExpertDecomposition(String strategyName, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        AccessesSciPyStrategy strategy = strategyRepository.findByName(strategyName);
        clusteringService.createExpertDecomposition(strategy, expertName, expertFile);
    }

    public Decomposition updateOutdatedFunctionalitiesAndMetrics(String decompositionName) throws Exception {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();
        AccessesSource source = (AccessesSource) strategy.getCodebase().getSourceByType(ACCESSES);
        if (!decomposition.isOutdated())
            return decomposition;

        functionalityService.setupFunctionalities(
                decomposition,
                sourceService.getSourceFileAsInputStream(source.getName()),
                source.getProfile(strategy.getProfile()),
                strategy.getTracesMaxLimit(),
                strategy.getTraceType(),
                true);

        decomposition.calculateMetrics();
        decomposition.setOutdated(false);

        decompositionRepository.save(decomposition);
        return decomposition;
    }

    public Utils.GetSerializableLocalTransactionsGraphResult getLocalTransactionGraphForFunctionality(String decompositionName, String functionalityName) throws JSONException, IOException {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();
        AccessesSource source = (AccessesSource) strategy.getCodebase().getSourceByType(ACCESSES);

        DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
                .createLocalTransactionGraphFromScratch(
                        sourceService.getSourceFileAsInputStream(source.getName()),
                        strategy.getTracesMaxLimit(),
                        strategy.getTraceType(),
                        decomposition.getEntityIDToClusterID());

        return Utils.getSerializableLocalTransactionsGraph(functionalityLocalTransactionsGraph);
    }

    public ArrayList<HashMap<String, String>> getSearchItems(String decompositionName) {
        ArrayList<HashMap<String, String>> searchItems = new ArrayList<>();
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);

        decomposition.getClusters().values().forEach(cluster -> {
            HashMap<String, String> clusterItem = new HashMap<>();
            clusterItem.put("name", cluster.getName());
            clusterItem.put("type", "Cluster");
            clusterItem.put("id", "c" + cluster.getID());
            clusterItem.put("entities", Integer.toString(cluster.getEntities().size()));
            clusterItem.put("funcType", ""); clusterItem.put("cluster", "");
            searchItems.add(clusterItem);

            cluster.getEntities().forEach(entity -> {
                HashMap<String, String> entityItem = new HashMap<>();
                entityItem.put("name", String.valueOf(entity));
                entityItem.put("type", "Entity");
                entityItem.put("id", String.valueOf(entity));
                entityItem.put("entities", ""); entityItem.put("funcType", "");
                entityItem.put("cluster", cluster.getName());
                searchItems.add(entityItem);
            });
        });

        decomposition.getFunctionalities().values().forEach(functionality -> {
            HashMap<String, String> functionalityItem = new HashMap<>();
            functionalityItem.put("name", functionality.getName());
            functionalityItem.put("type", "Functionality");
            functionalityItem.put("id", functionality.getName());
            functionalityItem.put("entities", Integer.toString(functionality.getEntities().size()));
            functionalityItem.put("funcType", functionality.getType().toString()); functionalityItem.put("cluster", "");
            searchItems.add(functionalityItem);
        });

        return searchItems;
    }

    public String getEdgeWeights(String decompositionName) throws JSONException, IOException {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(strategy.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entities = new ArrayList<>(decomposition.getEntityIDToClusterID().keySet());

        JSONArray edgesJSON = new JSONArray();
        int k = 0;
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                short e1ID = entities.get(i);
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
        for (Functionality functionality : decomposition.getFunctionalities().values()) {
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

    private String edgeId(int node1, int node2) {
        if (node1 < node2)
            return node1 + "&" + node2;
        return node2 + "&" + node1;
    }

    public Map<Short, Cluster> mergeClustersOperation(String decompositionName, Short clusterNameID, Short otherClusterID, String newName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        MergeOperation operation = new MergeOperation(decomposition, clusterNameID, otherClusterID, newName);

        mergeClusters(decomposition, clusterNameID, otherClusterID, newName);
        accessesSciPyLogService.addOperation(decomposition, operation);
        return decomposition.getClusters();
    }

    public void mergeClusters(AccessesSciPyDecomposition decomposition, Short clusterNameID, Short otherClusterID, String newName) {
        Cluster cluster1 = decomposition.getCluster(clusterNameID);
        Cluster cluster2 = decomposition.getCluster(otherClusterID);
        if (decomposition.clusterNameExists(newName) && !cluster1.getName().equals(newName) && !cluster2.getName().equals(newName))
            throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster mergedCluster = new Cluster(decomposition.getNewClusterID(), newName);

        for(short entityID : cluster1.getEntities()) {
            decomposition.getEntityIDToClusterID().replace(entityID, mergedCluster.getID());
            functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(decomposition, entityID));
        }

        for(short entityID : cluster2.getEntities()) {
            decomposition.getEntityIDToClusterID().replace(entityID, mergedCluster.getID());
            functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(decomposition, entityID));
        }

        Set<Short> allEntities = new HashSet<>(cluster1.getEntities());
        allEntities.addAll(cluster2.getEntities());
        mergedCluster.setEntities(allEntities);

        decomposition.transferCouplingDependencies(cluster1.getEntities(), cluster1.getID(), mergedCluster.getID());
        decomposition.transferCouplingDependencies(cluster2.getEntities(), cluster2.getID(), mergedCluster.getID());

        decomposition.removeCluster(clusterNameID);
        decomposition.removeCluster(otherClusterID);

        decomposition.addCluster(mergedCluster);
        decomposition.setOutdated(true);
        decompositionRepository.save(decomposition);
    }

    public Map<Short, Cluster> renameClusterOperation(String decompositionName, Short clusterID, String newName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        RenameOperation operation = new RenameOperation(decomposition, clusterID, newName);

        renameCluster(decomposition, clusterID, newName);
        accessesSciPyLogService.addOperation(decomposition, operation);
        return decomposition.getClusters();
    }

    public void renameCluster(AccessesSciPyDecomposition decomposition, Short clusterID, String newName) {
        if (decomposition.clusterNameExists(newName)) throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        decomposition.getCluster(clusterID).setName(newName);
        decompositionRepository.save(decomposition);
    }

    public Map<Short, Cluster> splitClusterOperation(String decompositionName, Short clusterID, String newName, String entitiesString) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        SplitOperation operation = new SplitOperation(decomposition, clusterID, newName, entitiesString);

        splitCluster(decomposition, clusterID, newName, entitiesString);
        accessesSciPyLogService.addOperation(decomposition, operation);
        return decomposition.getClusters();
    }

    public void splitCluster(AccessesSciPyDecomposition decomposition, Short clusterID, String newName, String entitiesString) {
        String[] entities = entitiesString.split(",");
        if (decomposition.clusterNameExists(newName)) throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster currentCluster = decomposition.getCluster(clusterID);
        Cluster newCluster = new Cluster(decomposition.getNewClusterID(), newName);

        for (String stringifiedEntityID : entities) {
            short entityID = Short.parseShort(stringifiedEntityID);

            if (currentCluster.containsEntity(entityID)) {
                newCluster.addEntity(entityID);
                currentCluster.removeEntity(entityID);
                decomposition.getEntityIDToClusterID().replace(entityID, newCluster.getID());
                functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(decomposition, entityID));
            }
        }
        decomposition.transferCouplingDependencies(newCluster.getEntities(), currentCluster.getID(), newCluster.getID());
        decomposition.addCluster(newCluster);
        decomposition.setOutdated(true);
        decompositionRepository.save(decomposition);
    }

    public Map<Short, Cluster> transferEntitiesOperation(String decompositionName, Short fromClusterID, Short toClusterID, String entitiesString) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        TransferOperation operation = new TransferOperation(decomposition, fromClusterID, toClusterID, entitiesString);

        transferEntities(decomposition, fromClusterID, toClusterID, entitiesString);
        accessesSciPyLogService.addOperation(decomposition, operation);
        return decomposition.getClusters();
    }

    public void transferEntities(AccessesSciPyDecomposition decomposition, Short fromClusterID, Short toClusterID, String entitiesString) {
        Cluster fromCluster = decomposition.getCluster(fromClusterID);
        Cluster toCluster = decomposition.getCluster(toClusterID);
        Set<Short> entities = Arrays.stream(entitiesString.split(",")).map(Short::valueOf).collect(Collectors.toSet());

        for (Short entityID : entities) {
            if (fromCluster.containsEntity(entityID)) {
                toCluster.addEntity(entityID);
                fromCluster.removeEntity(entityID);
                decomposition.getEntityIDToClusterID().replace(entityID, toCluster.getID());
                functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(decomposition, entityID));
            }
        }
        decomposition.transferCouplingDependencies(entities, fromClusterID, toClusterID);
        decomposition.setOutdated(true);
        decompositionRepository.save(decomposition);
    }

    public Map<Short, Cluster> formClusterOperation(String decompositionName, String newName, Map<Short, List<Short>> entities) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        FormClusterOperation operation = new FormClusterOperation(decomposition, newName, entities);

        formCluster(decomposition, newName, entities);
        accessesSciPyLogService.addOperation(decomposition, operation);
        return decomposition.getClusters();
    }
    public void formCluster(AccessesSciPyDecomposition decomposition, String newName, Map<Short, List<Short>> entities) {
        List<Short> entitiesIDs = entities.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        if (decomposition.clusterNameExists(newName)) {
            Cluster previousCluster = decomposition.getClusters().values().stream().filter(cluster -> cluster.getName().equals(newName)).findFirst()
                    .orElseThrow(() -> new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists"));
            if (!entitiesIDs.containsAll(previousCluster.getEntities()))
                throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");
        }

        Cluster newCluster = new Cluster(decomposition.getNewClusterID(), newName);

        for (Short entityID : entitiesIDs) {
            Cluster currentCluster = decomposition.getClusters().values().stream()
                    .filter(cluster -> cluster.getEntities().contains(entityID)).findFirst()
                    .orElseThrow(() -> new RuntimeException("No cluster constains entity " + entityID));

            newCluster.addEntity(entityID);
            currentCluster.removeEntity(entityID);
            decomposition.getEntityIDToClusterID().replace(entityID, newCluster.getID());
            functionalityService.deleteFunctionalities(removeFunctionalityWithEntity(decomposition, entityID));
            decomposition.transferCouplingDependencies(Collections.singleton(entityID), currentCluster.getID(), newCluster.getID());
            if (currentCluster.getEntities().size() == 0)
                decomposition.removeCluster(currentCluster.getID());
        }
        decomposition.addCluster(newCluster);
        decomposition.setOutdated(true);
        decompositionRepository.save(decomposition);
    }

    public List<Functionality> removeFunctionalityWithEntity(AccessesSciPyDecomposition decomposition, short entityID) {
        Map<String, Functionality> newFunctionalities = new HashMap<>();
        List<Functionality> toDelete = new ArrayList<>();
        decomposition.getFunctionalities().forEach((name, functionality) -> {
            if (functionality.containsEntity(entityID))
                toDelete.add(functionality);
            else newFunctionalities.put(name, functionality);
        });
        decomposition.setFunctionalities(newFunctionalities);
        return toDelete;
    }

    public void deleteDecompositionProperties(AccessesSciPyDecomposition decomposition) {
        accessesSciPyLogService.deleteDecompositionLog(decomposition.getLog());
        functionalityService.deleteFunctionalities(decomposition.getFunctionalities().values());
    }
}