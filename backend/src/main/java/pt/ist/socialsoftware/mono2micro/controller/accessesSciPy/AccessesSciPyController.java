package pt.ist.socialsoftware.mono2micro.controller.accessesSciPy;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ist.socialsoftware.mono2micro.decomposition.controller.DecompositionController;
import pt.ist.socialsoftware.mono2micro.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.source.domain.Source;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.fileManager.FileManager;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

@RestController
@RequestMapping(value = "/mono2micro/strategy/{strategyName}/decomposition/{decompositionName}")
public class AccessesSciPyController {

    private static final Logger logger = LoggerFactory.getLogger(DecompositionController.class);

    private final FileManager fileManager = FileManager.getInstance();

    @RequestMapping(value = "/updatedAccessesSciPyDecomposition", method = RequestMethod.GET)
    public ResponseEntity<Decomposition> updatedAccessesSciPyDecomposition(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName
    ) {
        logger.debug("updatedAccessesSciPyDecomposition");

        try {
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
                    codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName
            );
            decomposition.updateOutdatedFunctionalitiesAndMetrics();

            return new ResponseEntity<>(decomposition, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/getLocalTransactionsGraphForFunctionality", method = RequestMethod.GET)
    public ResponseEntity<Utils.GetSerializableLocalTransactionsGraphResult> getFunctionalityLocalTransactionsGraph(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName,
            @RequestParam String functionalityName
    ) {
        logger.debug("getFunctionalityLocalTransactionsGraph");

        try {
            AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) fileManager.getCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyName);

            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
                    codebaseName,
                    STRATEGIES_FOLDER,
                    strategyName,
                    decompositionName
            );

            Source source = fileManager.getCodebaseSource(codebaseName, ACCESSES);

            DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
                    .createLocalTransactionGraphFromScratch(
                            //source.getSourceFilePath(),
                            new ByteArrayInputStream("TODO".getBytes()),
                            strategy.getTracesMaxLimit(),
                            strategy.getTraceType(),
                            decomposition.getEntityIDToClusterID());

            return new ResponseEntity<>(
                    Utils.getSerializableLocalTransactionsGraph(functionalityLocalTransactionsGraph),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/getSearchItems", method = RequestMethod.GET)
    public ResponseEntity<ArrayList<HashMap<String, String>>> getSearchItems(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName
    ) {
        logger.debug("getSearchItems");

        try {
            ArrayList<HashMap<String, String>> searchItems = new ArrayList<>();
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(
                    codebaseName,
                    STRATEGIES_FOLDER,
                    strategyName,
                    decompositionName
            );

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

            return new ResponseEntity<>(searchItems, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/getEdgeWeights", method = RequestMethod.GET)
    public ResponseEntity<String> getEdgeWeights(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName
    ) {
        logger.debug("getEdgeWeights");

        try {
            JSONArray copheneticDistances = fileManager.getCopheneticDistances(codebaseName, strategyName);
            JSONArray entities = fileManager.getSimilarityMatrix(codebaseName, strategyName, "similarityMatrix.json").getJSONArray("entities");
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager.getStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, strategyName, decompositionName);

            JSONArray edgesJSON = new JSONArray();
            int k = 0;
            for (int i = 0; i < entities.length(); i++) {
                for (int j = i + 1; j < entities.length(); j++) {
                    int e1ID = entities.getInt(i);
                    int e2ID = entities.getInt(j);

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

            return new ResponseEntity<>(filteredEdgesJSON.toString(), HttpStatus.OK);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String edgeId(int node1, int node2) {
        if (node1 < node2)
            return node1 + "&" + node2;
        return node2 + "&" + node1;
    }

    @RequestMapping(value = "/getGraphPositions", method = RequestMethod.GET)
    public ResponseEntity<String> getGraphPositions(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName
    ) {
        logger.debug("getGraphPositions");

        try {
            String graphPositions = fileManager.getGraphPositions(codebaseName, strategyName, decompositionName);

            if (graphPositions == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            else return new ResponseEntity<>(graphPositions, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Permanently saves clusters and entities' positions
    @RequestMapping(value = "/saveGraphPositions", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> saveGraphPositions(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName,
            @RequestBody String graphPositions
    ) {
        logger.debug("saveGraphPositions");

        try {
            fileManager.saveGraphPositions(codebaseName, strategyName, decompositionName, graphPositions);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/deleteGraphPositions", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteGraphPositions(
            @PathVariable String codebaseName,
            @PathVariable String strategyName,
            @PathVariable String decompositionName
    ) {
        logger.debug("deleteGraphPositions");

        try {
            fileManager.deleteGraphPositions(codebaseName, strategyName, decompositionName);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
