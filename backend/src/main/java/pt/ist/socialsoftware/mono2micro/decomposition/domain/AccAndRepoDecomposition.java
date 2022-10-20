package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.AccessesInfo;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepresentationInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.AccAndRepoFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.AccAndRepoMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.AccAndRepoRenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.AccAndRepoSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.AccAndRepoTransferOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.io.IOException;
import java.util.*;

@Document("decomposition")
public class AccAndRepoDecomposition extends Decomposition {
    public static final String ACC_AND_REPO_DECOMPOSITION = "Accesses and Repository Decomposition";

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
        add(AuthorRepresentation.AUTHOR);
        add(CommitRepresentation.COMMIT);
    }};
    public AccAndRepoDecomposition() {}

    public AccAndRepoDecomposition(DecompositionRequest decompositionRequest) {}

    public AccAndRepoDecomposition(AccAndRepoDecomposition decomposition, String snapshotName) throws IOException {
        this.name = snapshotName;
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        new AccessesInfo(this, decomposition);
        new RepositoryInfo(this, decomposition);
    }

    @Override
    public String getType() {
        return ACC_AND_REPO_DECOMPOSITION;
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
        DecompositionMetric[] metricObjects = new DecompositionMetric[] {
                new CohesionMetric(), new ComplexityMetric(), new CouplingMetric(), new PerformanceMetric(), new TSRMetric()};

        for (DecompositionMetric metric : metricObjects)
            this.metrics.put(metric.getType(), metric.calculateMetric(this));
    }

    @Override
    public void setup() throws Exception {
        new AccessesInfo(this);
        new RepositoryInfo(this);
        this.history = new PositionHistory(this);
    }

    @Override
    public void update() throws Exception {
        for (RepresentationInformation representationInformation : representationInformations)
            representationInformation.update(this);
    }

    @Override
    public void deleteProperties() {
        representationInformations.forEach(RepresentationInformation::deleteProperties);
    }

    @Override
    public void renameCluster(RenameOperation operation) {
        AccAndRepoRenameOperation accAndRepoRenameOperation = new AccAndRepoRenameOperation(operation);
        accAndRepoRenameOperation.execute(this);
    }

    @Override
    public void mergeClusters(MergeOperation operation) {
        AccAndRepoMergeOperation accAndRepoMergeOperation = new AccAndRepoMergeOperation(operation);
        accAndRepoMergeOperation.execute(this);
    }

    @Override
    public void splitCluster(SplitOperation operation) {
        AccAndRepoSplitOperation accAndRepoSplitOperation = new AccAndRepoSplitOperation(operation);
        accAndRepoSplitOperation.execute(this);
    }

    @Override
    public void transferEntities(TransferOperation operation) {
        AccAndRepoTransferOperation accAndRepoTransferOperation = new AccAndRepoTransferOperation(operation);
        accAndRepoTransferOperation.execute(this);
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

        AccAndRepoDecomposition snapshotDecomposition = new AccAndRepoDecomposition(this, snapshotName);

        PositionHistory snapshotHistory = new PositionHistory(snapshotDecomposition);
        snapshotDecomposition.setHistory(snapshotHistory);
        historyService.saveHistory(snapshotHistory);
        positionHistoryService.saveGraphPositions(snapshotDecomposition, positionHistoryService.getGraphPositions(this));

        return snapshotDecomposition;
    }
}
