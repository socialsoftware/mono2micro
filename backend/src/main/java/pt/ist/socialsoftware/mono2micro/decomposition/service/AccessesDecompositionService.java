package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;

@Service
public class AccessesDecompositionService {
    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    GridFsService gridFsService;

    public Utils.GetSerializableLocalTransactionsGraphResult getLocalTransactionGraphForFunctionality(String decompositionName, String functionalityName) throws JSONException, IOException {
        AccessesDecomposition decomposition = (AccessesDecomposition) decompositionRepository.findByName(decompositionName);
        AccessesSimilarity similarity = (AccessesSimilarity) decomposition.getSimilarity();
        AccessesRepresentation representation = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByType(ACCESSES);

        DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = decomposition.getFunctionality(functionalityName)
                .createLocalTransactionGraphFromScratch(
                        gridFsService.getFile(representation.getName()),
                        similarity.getTracesMaxLimit(),
                        similarity.getTraceType(),
                        decomposition.getEntityIDToClusterName());

        return Utils.getSerializableLocalTransactionsGraph(functionalityLocalTransactionsGraph);
    }

    public ArrayList<HashMap<String, String>> getSearchItems(String decompositionName) {
        ArrayList<HashMap<String, String>> searchItems = new ArrayList<>();
        AccessesDecomposition decomposition = (AccessesDecomposition) decompositionRepository.findByName(decompositionName);

        decomposition.getClusters().values().forEach(cluster -> {
            HashMap<String, String> clusterItem = new HashMap<>();
            clusterItem.put("name", cluster.getName());
            clusterItem.put("type", "Cluster");
            clusterItem.put("id", cluster.getName());
            clusterItem.put("entities", Integer.toString(cluster.getElements().size()));
            clusterItem.put("funcType", ""); clusterItem.put("cluster", "");
            searchItems.add(clusterItem);

            cluster.getElements().forEach(entity -> {
                HashMap<String, String> entityItem = new HashMap<>();
                entityItem.put("name", entity.getName());
                entityItem.put("type", "Entity");
                entityItem.put("id", String.valueOf(entity.getId()));
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
}
