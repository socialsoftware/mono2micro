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
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.AccessesSciPyDecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Functionality;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.history.service.AccessesSciPyHistoryService;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.AccessesSciPyStrategyRepository;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import javax.naming.NameAlreadyBoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    AccessesSciPyHistoryService accessesSciPyHistoryService;

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

        decomposition.setupFunctionalities(
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

    public String getGraphPositions(String decompositionName) throws IOException {
        try {
            return IOUtils.toString(gridFsService.getFile(decompositionName + "_graphPositions"), StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void saveGraphPositions(String decompositionName, String graphPositions) {
        gridFsService.saveFile(new ByteArrayInputStream(graphPositions.getBytes(StandardCharsets.UTF_8)), decompositionName + "_graphPositions");
    }

    public void deleteGraphPositions(String decompositionName) {
        gridFsService.deleteFile(decompositionName + "_graphPositions");
    }

    public Map<Short, Cluster> mergeClusters(String decompositionName, Short clusterNameID, Short otherClusterID, String newName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

        MergeDecompositionOperation historyEntry = new MergeDecompositionOperation(decomposition, clusterNameID, otherClusterID, newName);

        decomposition.mergeClusters(clusterNameID, otherClusterID, newName);
        decomposition.setOutdated(true);
        accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
        decompositionRepository.save(decomposition);
        return decomposition.getClusters();
    }
    public Map<Short, Cluster> renameCluster(String decompositionName, Short clusterID, String newName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

        RenameDecompositionOperation historyEntry = new RenameDecompositionOperation(decomposition, clusterID, newName);

        decomposition.renameCluster(clusterID, newName);
        decompositionRepository.save(decomposition);
        accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
        return decomposition.getClusters();
    }

    public Map<Short, Cluster> splitCluster(String decompositionName, Short clusterID, String newName, String entities) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

        SplitDecompositionOperation historyEntry = new SplitDecompositionOperation(decomposition, clusterID, newName, entities);

        decomposition.splitCluster(clusterID, newName, entities.split(","));
        decomposition.setOutdated(true);
        decompositionRepository.save(decomposition);
        accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
        return decomposition.getClusters();
    }

    public Map<Short, Cluster> transferEntities(String decompositionName, Short clusterID, Short toClusterID, String entities) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

        TransferDecompositionOperation historyEntry = new TransferDecompositionOperation(decomposition, clusterID, toClusterID, entities);

        decomposition.transferEntities(clusterID, toClusterID, entities.split(","));
        decomposition.setOutdated(true);
        decompositionRepository.save(decomposition);
        accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
        return decomposition.getClusters();
    }

    public Map<Short, Cluster> formCluster(String decompositionName, String newName, Map<Short, List<Short>> entities) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByNameWithoutFunctionalityRedesigns(decompositionName);

        FormClusterDecompositionOperation historyEntry = new FormClusterDecompositionOperation(decomposition, newName, entities);

        decomposition.formCluster(newName, entities.values().stream().flatMap(Collection::stream).map(Object::toString).toArray(String[]::new));
        decomposition.setOutdated(true);

        decompositionRepository.save(decomposition);
        accessesSciPyHistoryService.addHistoryEntry(decomposition, historyEntry);
        return decomposition.getClusters();
    }

    public Functionality getOrCreateRedesign(String decompositionName, String functionalityName) throws IOException, JSONException {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();

        Functionality functionality = decomposition.getFunctionality(functionalityName);

        Source source = strategy.getCodebase().getSourceByType(ACCESSES);

        if(functionality.getFunctionalityRedesigns()
                .stream()
                .noneMatch(e -> e.getName().equals(Constants.DEFAULT_REDESIGN_NAME))){
            functionality.createFunctionalityRedesign(
                    Constants.DEFAULT_REDESIGN_NAME,
                    true,
                    functionality.createLocalTransactionGraphFromScratch(
                            sourceService.getSourceFileAsInputStream(source.getName()),
                            strategy.getTracesMaxLimit(),
                            strategy.getTraceType(),
                            decomposition.getEntityIDToClusterID())
            );
        };
        decompositionRepository.save(decomposition);
        return functionality;
    }

    public Functionality addCompensating(String decompositionName, String functionalityName, String redesignName, HashMap<String, Object> data) throws Exception {
        int fromID = (Integer) data.get("fromID");
        Short clusterID = ((Integer) data.get("cluster")).shortValue();
        ArrayList<Integer> accesses = (ArrayList<Integer>) data.get("entities");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.addCompensating(clusterID, accesses, fromID);
        // TODO certificar se a proxima linha e' necessaria
        functionalityRedesign.calculateMetrics(decomposition, functionality);
        decompositionRepository.save(decomposition);
        return functionality;
    }

    public Functionality sequenceChange(String decompositionName, String functionalityName, String redesignName, HashMap<String, String> data) throws Exception {
        String localTransactionID = data.get("localTransactionID");
        String newCaller = data.get("newCaller");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.sequenceChange(localTransactionID, newCaller);
        functionalityRedesign.calculateMetrics(decomposition, functionality);

        decompositionRepository.save(decomposition);
        return functionality;
    }

    public Functionality dcgi(String decompositionName, String functionalityName, String redesignName, HashMap<String, String> data) throws Exception {
        Short fromClusterID = Short.parseShort(data.get("fromCluster"));
        Short toClusterID = Short.parseShort(data.get("toCluster"));
        String localTransactions = data.get("localTransactions");

        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.dcgi(fromClusterID, toClusterID, localTransactions);
        functionalityRedesign.calculateMetrics(decomposition, functionality);
        decompositionRepository.save(decomposition);
        return functionality;
    }

    public Functionality pivotTransaction(String decompositionName, String functionalityName, String redesignName, String transactionID, Optional<String> newRedesignName)
            throws Exception
    {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) decomposition.getStrategy();
        Functionality functionality = decomposition.getFunctionality(functionalityName);

        if(newRedesignName.isPresent())
            if(!functionality.checkNameValidity(newRedesignName.get()))
                throw new NameAlreadyBoundException();

        FunctionalityRedesign functionalityRedesign = functionality.getFunctionalityRedesign(redesignName);
        functionalityRedesign.definePivotTransaction(Integer.parseInt(transactionID));
        functionalityRedesign.calculateMetrics(decomposition, functionality);

        if(newRedesignName.isPresent()) {
            functionality.changeFunctionalityRedesignName(redesignName, newRedesignName.get());

            Source source = strategy.getCodebase().getSourceByType(ACCESSES);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
                    .createLocalTransactionGraphFromScratch(
                            sourceService.getSourceFileAsInputStream(source.getName()),
                            strategy.getTracesMaxLimit(),
                            strategy.getTraceType(),
                            decomposition.getEntityIDToClusterID());

            functionality.createFunctionalityRedesign(
                    Constants.DEFAULT_REDESIGN_NAME,
                    false,
                    functionalityLocalTransactionsGraph
            );
        }

        functionalityRedesign = functionality.getFunctionalityRedesign(Constants.DEFAULT_REDESIGN_NAME);
        functionalityRedesign.calculateMetrics(decomposition, functionality);
        decompositionRepository.save(decomposition);
        return functionality;
    }

    public Functionality changeLTName(String decompositionName, String functionalityName, String redesignName, String transactionID, String newName) {
        AccessesSciPyDecomposition decomposition = decompositionRepository.findByName(decompositionName);
        Functionality functionality = decomposition.getFunctionality(functionalityName);
        functionality.getFunctionalityRedesign(redesignName).changeLTName(transactionID, newName);
        decompositionRepository.save(decomposition);
        return functionality;
    }

    public void deleteDecompositionProperties(AccessesSciPyDecomposition decomposition) {
        accessesSciPyHistoryService.deleteDecompositionHistory(decomposition.getDecompositionHistory());
    }
}