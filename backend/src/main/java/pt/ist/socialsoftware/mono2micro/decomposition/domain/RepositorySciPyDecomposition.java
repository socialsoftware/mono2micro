package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.DecompositionMetric;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.TSRMetric;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.AccAndRepoFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.RepositoryMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RepositoryRenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.RepositorySplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.RepositoryTransferOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.io.IOException;
import java.util.*;

@Document("decomposition")
public class RepositorySciPyDecomposition extends Decomposition {
    public static final String REPOSITORY_SCIPY = "Repository-Based Similarity and SciPy Clustering Algorithm";

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
        add(AuthorRepresentation.AUTHOR);
        add(CommitRepresentation.COMMIT);
    }};

    public RepositorySciPyDecomposition() {}

    public RepositorySciPyDecomposition(DecompositionRequest request) {}

    public RepositorySciPyDecomposition(RepositorySciPyDecomposition decomposition, String snapshotName) {
        this.name = snapshotName;
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        new RepositoryInfo(this, decomposition);
    }

    @Override
    public String getType() {
        return REPOSITORY_SCIPY;
    }

    @Override
    public List<String> getRequiredRepresentations() {
        return requiredRepresentations;
    }

    @Override
    public Clustering getClusteringAlgorithm() {
        if (isExpert())
            return new ExpertClustering();
        else return new SciPyClustering();
    }

    @Override
    public void calculateMetrics() {
        DecompositionMetric[] metricObjects = new DecompositionMetric[] {new TSRMetric()};

        for (DecompositionMetric metric : metricObjects)
            this.metrics.put(metric.getType(), metric.calculateMetric(this));
    }

    @Override
    public void setup() throws IOException {
        new RepositoryInfo(this);
        this.history = new PositionHistory(this);
    }

    @Override
    public void update() {}

    @Override
    public void deleteProperties() {}

    @Override
    public void renameCluster(RenameOperation operation) {
        RepositoryRenameOperation repositoryRenameOperation = new RepositoryRenameOperation(operation);
        repositoryRenameOperation.execute(this);
    }

    @Override
    public void mergeClusters(MergeOperation operation) {
        RepositoryMergeOperation repositoryMergeOperation = new RepositoryMergeOperation(operation);
        repositoryMergeOperation.execute(this);
    }

    @Override
    public void splitCluster(SplitOperation operation) {
        RepositorySplitOperation repositorySplitOperation = new RepositorySplitOperation(operation);
        repositorySplitOperation.execute(this);
    }

    @Override
    public void transferEntities(TransferOperation operation) {
        RepositoryTransferOperation repositoryTransferOperation = new RepositoryTransferOperation(operation);
        repositoryTransferOperation.execute(this);
    }

    @Override
    public void formCluster(FormClusterOperation operation) {
        AccAndRepoFormClusterOperation accAndRepoFormClusterOperation = new AccAndRepoFormClusterOperation(operation);
        accAndRepoFormClusterOperation.execute(this);
    }

    @Override
    public Decomposition snapshotDecomposition(String snapshotName) throws IOException {
        HistoryService historyService = ContextManager.get().getBean(HistoryService.class);
        PositionHistoryService positionHistoryService = ContextManager.get().getBean(PositionHistoryService.class);
        RepositorySciPyDecomposition snapshotDecomposition = new RepositorySciPyDecomposition(this, snapshotName);

        PositionHistory snapshotHistory = new PositionHistory(snapshotDecomposition);
        snapshotDecomposition.setHistory(snapshotHistory);
        historyService.saveHistory(snapshotHistory);
        positionHistoryService.saveGraphPositions(snapshotDecomposition, positionHistoryService.getGraphPositions(this));

        return snapshotDecomposition;
    }
}