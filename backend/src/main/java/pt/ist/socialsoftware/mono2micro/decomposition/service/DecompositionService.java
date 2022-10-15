package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.DecompositionFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.views.ClusterViewDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.ExpertRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.operation.*;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;
import java.util.Optional;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

@Service
public class DecompositionService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    HistoryService historyService;

    @Autowired
    GridFsService gridFsService;

    public String[] getDecompositionTypes() {
        return new String[] { // LOG NEW DECOMPOSITION TYPES HERE
                ACCESSES_SCIPY,
                REPOSITORY_SCIPY,
                ACC_AND_REPO_SCIPY,
        };
    }

    public void createDecomposition(DecompositionRequest request) throws Exception {
        Decomposition decomposition = DecompositionFactory.getDecomposition(request);

        Similarity similarity = similarityRepository.findByName(request.getSimilarityName());
        decomposition.setSimilarity(similarity);
        similarity.addDecomposition(decomposition);
        decomposition.setStrategy(similarity.getStrategy());
        similarity.getStrategy().addDecomposition(decomposition);

        Clustering clustering = decomposition.getClusteringAlgorithm();
        clustering.generateClusters(decomposition, request);

        setupDecomposition(decomposition);
    }

    public void createExpertDecomposition(String similarityName, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        Similarity similarity = similarityRepository.findByName(similarityName);
        Decomposition decomposition = DecompositionFactory.getDecomposition(similarity.getStrategy().getDecompositionType());

        decomposition.setExpert(true);
        decomposition.setSimilarity(similarity);
        similarity.addDecomposition(decomposition);
        decomposition.setStrategy(similarity.getStrategy());
        similarity.getStrategy().addDecomposition(decomposition);

        Clustering clustering = decomposition.getClusteringAlgorithm();
        clustering.generateClusters(decomposition, new ExpertRequest(expertName, expertFile));

        setupDecomposition(decomposition);
    }

    public void setupDecomposition(Decomposition decomposition) throws Exception {
        decomposition.setup();
        decomposition.calculateMetrics();

        historyService.saveHistory(decomposition.getHistory());
        decompositionRepository.save(decomposition);
        similarityRepository.save(decomposition.getSimilarity());
        strategyRepository.save(decomposition.getStrategy());
    }

    public Decomposition updateDecomposition(String decompositionName) throws Exception {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        if (decomposition.isOutdated()) {
            decomposition.update();
            decomposition.calculateMetrics();
            decomposition.setOutdated(false);
            decompositionRepository.save(decomposition);
        }
        return decomposition;
    }

    public List<String> getRequiredRepresentations(String decompositionType) {
        return DecompositionFactory.getDecomposition(decompositionType).getRequiredRepresentations();
    }

    public Decomposition getDecomposition(String decompositionName) {
        return decompositionRepository.findByName(decompositionName);
    }

    public void deleteSingleDecomposition(String decompositionName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.deleteProperties();

        Strategy strategy = decomposition.getStrategy();
        strategy.removeDecomposition(decomposition.getName());
        Similarity similarity = decomposition.getSimilarity();
        similarity.removeDecomposition(decomposition.getName());

        similarityRepository.save(similarity);
        historyService.deleteHistory(decomposition.getHistory());
        strategyRepository.save(strategy);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void deleteDecomposition(Decomposition decomposition) {
        decomposition.deleteProperties();
        historyService.deleteHistory(decomposition.getHistory());
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void mergeClustersOperation(String decompositionName, MergeOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.mergeClusters(operation);
        historyService.addOperation(decomposition, operation);
        decompositionRepository.save(decomposition);
    }

    public void renameClusterOperation(String decompositionName, RenameOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.renameCluster(operation);
        historyService.addOperation(decomposition, operation);
        decompositionRepository.save(decomposition);
    }

    public void splitClusterOperation(String decompositionName, SplitOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.splitCluster(operation);
        historyService.addOperation(decomposition, operation);
        decompositionRepository.save(decomposition);
    }

    public void transferEntitiesOperation(String decompositionName, TransferOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.transferEntities(operation);
        historyService.addOperation(decomposition, operation);
        decompositionRepository.save(decomposition);
    }

    public void formClusterOperation(String decompositionName, FormClusterOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.formCluster(operation);
        historyService.addOperation(decomposition, operation);
        decompositionRepository.save(decomposition);
    }

    public String getEdgeWeights(String decompositionName, String view) throws Exception {
        ClusterViewDecomposition clusterView = (ClusterViewDecomposition) decompositionRepository.findByName(decompositionName);
        return clusterView.getEdgeWeights(view);
    }

    public String getSearchItems(String decompositionName, String view) throws Exception {
        ClusterViewDecomposition clusterView = (ClusterViewDecomposition) decompositionRepository.findByName(decompositionName);
        return clusterView.getSearchItems(view);
    }

    public void snapshotDecomposition(String decompositionName) throws Exception {
        ClusterViewDecomposition decomposition = (ClusterViewDecomposition) updateDecomposition(decompositionName);
        Similarity similarity = decomposition.getSimilarity();

        String snapshotName = decomposition.getName() + " SNAPSHOT";
        if (similarity.getDecompositionByName(snapshotName) != null) {
            int i = 1;
            do {i++;} while (similarity.getDecompositionByName(snapshotName + "(" + i + ")") != null);
            snapshotName = snapshotName + "(" + i + ")";
        }

        Decomposition snapshotDecomposition = decomposition.snapshotDecomposition(snapshotName);

        Strategy strategy = decomposition.getStrategy();
        similarity.addDecomposition(snapshotDecomposition);
        snapshotDecomposition.setSimilarity(similarity);
        strategy.addDecomposition(snapshotDecomposition);
        snapshotDecomposition.setStrategy(strategy);
        decompositionRepository.save(snapshotDecomposition);
        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }
}