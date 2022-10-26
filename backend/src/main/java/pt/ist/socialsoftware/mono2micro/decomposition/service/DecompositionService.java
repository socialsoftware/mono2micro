package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.DecompositionFactory;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.ExpertRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.Optional;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition.DecompositionType.*;

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

    public String[] getDecompositionTypes() {
        return new String[] { // LOG NEW DECOMPOSITION TYPES HERE
                ACCESSES_DECOMPOSITION,
                REPOSITORY_DECOMPOSITION,
                ACC_AND_REPO_DECOMPOSITION,
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

    public Set<String> getRequiredRepresentations(String decompositionType) {
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
        historyService.saveHistory(decomposition.getHistory());
        decompositionRepository.save(decomposition);
    }

    public void renameClusterOperation(String decompositionName, RenameOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.renameCluster(operation);
        historyService.saveHistory(decomposition.getHistory());
        decompositionRepository.save(decomposition);
    }

    public void splitClusterOperation(String decompositionName, SplitOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.splitCluster(operation);
        historyService.saveHistory(decomposition.getHistory());
        decompositionRepository.save(decomposition);
    }

    public void transferEntitiesOperation(String decompositionName, TransferOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.transferEntities(operation);
        historyService.saveHistory(decomposition.getHistory());
        decompositionRepository.save(decomposition);
    }

    public void formClusterOperation(String decompositionName, FormClusterOperation operation) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        decomposition.formCluster(operation);
        historyService.saveHistory(decomposition.getHistory());
        decompositionRepository.save(decomposition);
    }

    public String getEdgeWeights(String decompositionName, String representationInfo) throws Exception {
        Decomposition clusterView = decompositionRepository.findByName(decompositionName);
        return clusterView.getEdgeWeights(representationInfo);
    }

    public String getSearchItems(String decompositionName, String representationInfo) throws Exception {
        Decomposition clusterView = decompositionRepository.findByName(decompositionName);
        return clusterView.getSearchItems(representationInfo);
    }

    public void snapshotDecomposition(String decompositionName) throws Exception {
        Decomposition decomposition = updateDecomposition(decompositionName);
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