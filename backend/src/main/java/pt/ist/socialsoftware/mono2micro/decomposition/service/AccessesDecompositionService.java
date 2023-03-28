package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.AccessesInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.FunctionalityDto;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipy;
import pt.ist.socialsoftware.mono2micro.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.ACCESSES_TYPE;

@Service
public class AccessesDecompositionService {
    @Autowired
    DecompositionService decompositionService;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    GridFsService gridFsService;
    public Map<String, Object> getFunctionalitiesAndFunctionalitiesClusters(String decompositionName) throws Exception {
        Decomposition decomposition = decompositionService.updateDecomposition(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);

        Map<String, Set<Cluster>> functionalitiesClusters = Utils.getFunctionalitiesClusters(
                decomposition.getEntityIDToClusterName(),
                decomposition.getClusters(),
                accessesInformation.getFunctionalities().values()
        );

        Map<String, Object> response = new HashMap<>();
        Map<String, Functionality> functionalities = accessesInformation.getFunctionalities();
        Map<String, List<FunctionalityRedesign>> functionalityRedesignsPerFunctionality = functionalities.values().stream()
                .collect(Collectors.toMap(Functionality::getName, functionalityService::getFunctionalityRedesigns));
        Map<String, FunctionalityDto> functionalitiesWithRedesigns = functionalities.values().stream()
                .map(functionality -> new FunctionalityDto(functionality, functionalityRedesignsPerFunctionality.get(functionality.getName())))
                .collect(Collectors.toMap(FunctionalityDto::getName, functionalityDto -> functionalityDto));
        response.put("functionalities", functionalitiesWithRedesigns);
        response.put("functionalitiesClusters", functionalitiesClusters);

        return response;
    }

    public Map<String, Object> getClustersAndClustersFunctionalities(String decompositionName) throws Exception {
        Decomposition decomposition = decompositionService.updateDecomposition(decompositionName);

        Map<String, List<Functionality>> clustersFunctionalities = Utils.getClustersFunctionalities(decomposition);

        Map<String, Object> response = new HashMap<>();
        response.put("clusters", decomposition.getClusters());
        response.put("clustersFunctionalities", clustersFunctionalities);

        return response;
    }

    public Utils.GetSerializableLocalTransactionsGraphResult getLocalTransactionGraphForFunctionality(String decompositionName, String functionalityName) throws JSONException, IOException {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        AccessesInformation accessesInformation = (AccessesInformation) decomposition.getRepresentationInformationByType(ACCESSES_TYPE);
        SimilarityScipy similarity = (SimilarityScipy) decomposition.getSimilarity();
        AccessesRepresentation representation = (AccessesRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(ACCESSES);

        DirectedAcyclicGraph<LocalTransaction, DefaultEdge> functionalityLocalTransactionsGraph = accessesInformation.getFunctionality(functionalityName)
                .createLocalTransactionGraphFromScratch(
                        gridFsService.getFile(representation.getName()),
                        similarity.getTracesMaxLimit(),
                        similarity.getTraceType(),
                        decomposition.getEntityIDToClusterName());

        return Utils.getSerializableLocalTransactionsGraph(functionalityLocalTransactionsGraph);
    }
}
