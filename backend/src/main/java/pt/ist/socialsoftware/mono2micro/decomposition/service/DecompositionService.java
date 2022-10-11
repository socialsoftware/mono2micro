package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.DecompositionFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.ExpertRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.service.DecompositionOperationsService;
import pt.ist.socialsoftware.mono2micro.operation.Operation;
import pt.ist.socialsoftware.mono2micro.operation.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesTransferOperation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.AccessesSimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;

@Service
public class DecompositionService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    AccessesDecompositionService accessesDecompositionService;

    @Autowired
    AccessesSciPyService accessesSciPyService;

    @Autowired
    RepositoryDecompositionService repositoryDecompositionService;

    @Autowired
    AccAndRepoSciPyService accAndRepoSciPyService;

    @Autowired
    FunctionalityRepository functionalityRepository;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    DecompositionOperationsService decompositionOperationsService;

    @Autowired
    GridFsService gridFsService;

    public String[] getDecompositionTypes() {
        return new String[] { // LOG NEW DECOMPOSITION TYPES HERE
                ACCESSES_SCIPY,
                REPOSITORY_SCIPY,
                ACC_AND_REPO_SCIPY,
        };
    }

    public void setupDecomposition(Decomposition decomposition) throws Exception {
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION)) {
            AccessesSimilarity similarity = (AccessesSimilarity) decomposition.getSimilarity();
            InputStream inputStream = gridFsService.getFile(decomposition.getStrategy().getCodebase().getRepresentationByType(ACCESSES).getName());
            ((AccessesDecomposition) decomposition).setupFunctionalities(gridFsService, functionalityRepository, inputStream,
                    similarity.getProfile(), similarity.getTracesMaxLimit(), similarity.getTraceType());
        }
        if (decomposition.containsImplementation(REPOSITORY_DECOMPOSITION)) {
            ((RepositoryDecomposition) decomposition).setupAuthorsAndCommits(gridFsService);
        }
        // FILL DECOMPOSITION WITH SPECIFIC PROPERTIES HERE

        decomposition.calculateMetrics();

        decompositionOperationsService.saveDecompositionOperations(decomposition.getDecompositionOperations());
        decompositionRepository.save(decomposition);
        similarityRepository.save(decomposition.getSimilarity());
        strategyRepository.save(decomposition.getStrategy());
    }

    public void createDecomposition(DecompositionRequest request) throws Exception {
        Decomposition decomposition = DecompositionFactory.getDecomposition(request);

        Similarity similarity = similarityRepository.findByName(request.getSimilarityName());
        decomposition.setSimilarity(similarity);
        similarity.addDecomposition(decomposition);
        decomposition.setStrategy(similarity.getStrategy());
        similarity.getStrategy().addDecomposition(decomposition);

        Clustering clustering = decomposition.getClusteringAlgorithm(gridFsService);
        clustering.generateClusters(decomposition, request);

        setupDecomposition(decomposition);
    }

    public void createExpertDecomposition(String similarityName, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        Similarity similarity = similarityRepository.findByName(similarityName);
        Decomposition decomposition = DecompositionFactory.getDecomposition(similarity.getDecompositionType());

        decomposition.setExpert(true);
        decomposition.setSimilarity(similarity);
        similarity.addDecomposition(decomposition);
        decomposition.setStrategy(similarity.getStrategy());
        similarity.getStrategy().addDecomposition(decomposition);

        Clustering clustering = decomposition.getClusteringAlgorithm(gridFsService);
        clustering.generateClusters(decomposition, new ExpertRequest(expertName, expertFile));

        setupDecomposition(decomposition);
    }

    private void removeSpecificDecompositionProperties(Decomposition decomposition) {
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION)) {
            functionalityService.deleteFunctionalities(((AccessesDecomposition)decomposition).getFunctionalities().values());
            gridFsService.deleteFile(decomposition.getName() + "_refactorization");
        }
        Similarity similarity = decomposition.getSimilarity();
        similarity.removeDecomposition(decomposition.getName());
        similarityRepository.save(similarity);
    }

    public List<String> getRequiredRepresentations(String decompositionType) {
        return DecompositionFactory.getDecomposition(decompositionType).getRequiredRepresentations();
    }

    public Decomposition getDecomposition(String decompositionName) {
        return decompositionRepository.findByName(decompositionName);
    }

    public void deleteSingleDecomposition(String decompositionName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        removeSpecificDecompositionProperties(decomposition);
        Strategy strategy = strategyRepository.findByName(decomposition.getStrategy().getName());
        strategy.removeDecomposition(decomposition.getName());
        decompositionOperationsService.deleteDecompositionOperations(decomposition.getDecompositionOperations());
        strategyRepository.save(strategy);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void deleteDecomposition(Decomposition decomposition) {
        removeSpecificDecompositionProperties(decomposition);
        decompositionOperationsService.deleteDecompositionOperations(decomposition.getDecompositionOperations());
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void mergeClusters(String decompositionName, Operation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION))
            accessesDecompositionService.mergeClustersOperation(decomposition, (AccessesMergeOperation) operation);
        else throw new RuntimeException("Could not get the strategy type");
    }

    public void renameCluster(String decompositionName, Operation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION))
            accessesDecompositionService.renameClusterOperation(decomposition, (RenameOperation) operation);
        else throw new RuntimeException("Could not get the strategy type");
    }

    public void splitCluster(String decompositionName, Operation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION))
            accessesDecompositionService.splitClusterOperation(decomposition, (AccessesSplitOperation) operation);
        else throw new RuntimeException("Could not get the strategy type");
    }

    public void transferEntities(String decompositionName, Operation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION))
            accessesDecompositionService.transferEntitiesOperation(decomposition, (AccessesTransferOperation) operation);
        else throw new RuntimeException("Could not get the strategy type");
    }

    public void formCluster(String decompositionName, Operation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION))
            accessesDecompositionService.formClusterOperation(decomposition, (AccessesFormClusterOperation) operation);
        else throw new RuntimeException("Could not get the strategy type");
    }

    public void snapshotDecomposition(String decompositionName) throws Exception {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        switch (decomposition.getType()) {
            case ACCESSES_SCIPY:
                accessesSciPyService.snapshotDecomposition(decomposition.getName());
                break;
            case ACC_AND_REPO_SCIPY:
                accAndRepoSciPyService.snapshotDecomposition(decomposition.getName());
                break;
            default:
                throw new RuntimeException("Could not get the strategy type");
        }
    }

    public String getEdgeWeights(String decompositionName, String view) throws JSONException, IOException {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        switch (view) {
            case ACCESSES_DECOMPOSITION:
                return accessesDecompositionService.getEdgeWeights((AccessesDecomposition) decomposition);
            case REPOSITORY_DECOMPOSITION:
                return repositoryDecompositionService.getEdgeWeights((RepositoryDecomposition) decomposition);
            default:
                throw new RuntimeException("Could not get the strategy type");
        }
    }
}