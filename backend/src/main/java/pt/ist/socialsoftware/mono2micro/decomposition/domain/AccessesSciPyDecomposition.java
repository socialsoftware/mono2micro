package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.AccessesInfo;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepresentationInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.AccessesFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.AccessesMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.AccessesRenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.AccessesSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.AccessesTransferOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.io.IOException;
import java.util.*;

@Document("decomposition")
public class AccessesSciPyDecomposition extends Decomposition {
    public static final String ACCESSES_SCIPY = "Accesses-Based Similarity and SciPy Clustering Algorithm";

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
    }};

    public AccessesSciPyDecomposition() {}

    public AccessesSciPyDecomposition(DecompositionRequest request) {}

    public AccessesSciPyDecomposition(AccessesSciPyDecomposition decomposition, String snapshotName) throws IOException {
        this.name = snapshotName;
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        new AccessesInfo(this, decomposition);
    }

    @Override
    public String getType() {
        return ACCESSES_SCIPY;
    }

    public List<String> getRequiredRepresentations() {
        return requiredRepresentations;
    }

    @Override
    public Clustering getClusteringAlgorithm() {
        if (isExpert())
            return new ExpertClustering();
        else return new SciPyClustering();
    }

    public void calculateMetrics() {
        DecompositionMetric[] metricObjects = new DecompositionMetric[] {
                new CohesionMetric(), new ComplexityMetric(), new CouplingMetric(), new PerformanceMetric()};

        for (DecompositionMetric metric : metricObjects)
            this.metrics.put(metric.getType(), metric.calculateMetric(this));
    }

    @Override
    public void setup() throws Exception {
        new AccessesInfo(this);
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
        AccessesRenameOperation accessesRenameOperation = new AccessesRenameOperation(operation);
        accessesRenameOperation.execute(this);
    }

    @Override
    public void mergeClusters(MergeOperation operation) {
        AccessesMergeOperation accessesMergeOperation = new AccessesMergeOperation(operation);
        accessesMergeOperation.execute(this);
    }

    @Override
    public void splitCluster(SplitOperation operation) {
        AccessesSplitOperation accessesSplitOperation = new AccessesSplitOperation(operation);
        accessesSplitOperation.execute(this);
    }

    @Override
    public void transferEntities(TransferOperation operation) {
        AccessesTransferOperation accessesTransferOperation = new AccessesTransferOperation(operation);
        accessesTransferOperation.execute(this);
    }

    @Override
    public void formCluster(FormClusterOperation operation) {
        AccessesFormClusterOperation accessesFormClusterOperation = new AccessesFormClusterOperation(operation);
        accessesFormClusterOperation.execute(this);
    }

    @Override
    public Decomposition snapshotDecomposition(String snapshotName) throws IOException {
        HistoryService historyService = ContextManager.get().getBean(HistoryService.class);
        PositionHistoryService positionHistoryService = ContextManager.get().getBean(PositionHistoryService.class);

        AccessesSciPyDecomposition snapshotDecomposition = new AccessesSciPyDecomposition(this, snapshotName);

        PositionHistory snapshotHistory = new PositionHistory(snapshotDecomposition);
        snapshotDecomposition.setHistory(snapshotHistory);
        historyService.saveHistory(snapshotHistory);
        positionHistoryService.saveGraphPositions(snapshotDecomposition, positionHistoryService.getGraphPositions(this));

        return snapshotDecomposition;
    }
}