package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.SciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.log.service.LogService;
import pt.ist.socialsoftware.mono2micro.operation.Operation;
import pt.ist.socialsoftware.mono2micro.operation.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesTransferOperation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityForSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.IOException;
import java.util.Optional;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition.ACCESSES_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition.LOG_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition.REPOSITORY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition.SCIPY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

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
    SciPyClusteringAlgorithmService clusteringService;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    LogService logService;

    @Autowired
    GridFsService gridFsService;

    public void createDecomposition(DecompositionRequest request) throws Exception {
        switch (request.getType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                clusteringService.createDecomposition((SciPyRequestDto) request);
                return;
            default:
                throw new RuntimeException("No implementation type given in DecompositionRequest");
        }
    }
    public void createExpertDecomposition(String similarityName, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        Similarity similarity = similarityRepository.findByName(similarityName);
        switch (similarity.getType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                clusteringService.createExpertDecomposition((SimilarityForSciPy) similarity, expertName, expertFile);
                return;
            default:
                throw new RuntimeException("No expert decomposition available for this type");
        }
    }

    private void removeSpecificDecompositionProperties(Decomposition decomposition) {
        if (decomposition.containsImplementation(ACCESSES_DECOMPOSITION)) {
            functionalityService.deleteFunctionalities(((AccessesDecomposition)decomposition).getFunctionalities().values());
            gridFsService.deleteFile(decomposition.getName() + "_refactorization");
        }
        // NO PROPERTY TO BE DELETED
        // if (decomposition.containsImplementation(REPOSITORY_DECOMPOSITION)) {
        // }
        if (decomposition.containsImplementation(LOG_DECOMPOSITION)) {
            logService.deleteDecompositionLog(((LogDecomposition) decomposition).getLog());
        }
        if (decomposition.containsImplementation(SCIPY_DECOMPOSITION)) {
            Similarity similarity = ((SciPyDecomposition) decomposition).getSimilarity();
            similarity.removeDecomposition(decomposition.getName());
            similarityRepository.save(similarity);
        }
    }

    public Decomposition getDecomposition(String decompositionName) {
        return decompositionRepository.findByName(decompositionName);
    }

    public void deleteSingleDecomposition(String decompositionName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        removeSpecificDecompositionProperties(decomposition);
        Strategy strategy = strategyRepository.findByName(decomposition.getStrategy().getName());
        strategy.removeDecomposition(decomposition.getName());
        strategyRepository.save(strategy);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void deleteDecomposition(Decomposition decomposition) {
        removeSpecificDecompositionProperties(decomposition);
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
        switch (decomposition.getStrategyType()) {
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