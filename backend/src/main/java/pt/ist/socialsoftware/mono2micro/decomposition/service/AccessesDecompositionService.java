package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.property.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;

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
}
