package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformationFactory;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.formCluster.FormClusterPartitionsOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.merge.MergePartitionsOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.rename.RenamePartitionsOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.split.SplitPartitionsOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferOperation;
import pt.ist.socialsoftware.mono2micro.operation.transfer.TransferPartitionsOperation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.*;

@Document("decomposition")
public class PartitionsDecomposition extends Decomposition {
    public static final String PARTITIONS_DECOMPOSITION = "Partitions Decomposition";
    public PartitionsDecomposition() { this.type = PARTITIONS_DECOMPOSITION; }

    public PartitionsDecomposition(Similarity similarity) {
        super(similarity);
        this.type = PARTITIONS_DECOMPOSITION;
    }

    public PartitionsDecomposition(PartitionsDecomposition decomposition, String snapshotName) throws Exception {
        this.type = PARTITIONS_DECOMPOSITION;
        this.name = snapshotName;
        this.strategy = decomposition.getStrategy();
        decomposition.getStrategy().addDecomposition(this);
        this.similarity = decomposition.getSimilarity();
        decomposition.getSimilarity().addDecomposition(this);
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        List<RepresentationInformation> representationInformations = RepresentationInformationFactory.getStrategyRepresentationInformations(decomposition.getStrategy());
        for (RepresentationInformation representationInformation : representationInformations)
            representationInformation.snapshot(this, decomposition);
    }

    @Override
    public void calculateMetrics() {
        this.representationInformations.stream()
                .map(RepresentationInformation::getDecompositionMetrics)
                .flatMap(Collection::stream)
                .forEach(metric -> this.metrics.put(metric.getType(), metric.calculateMetric(this)));
    }

    @Override
    public void setup() throws Exception {
        List<RepresentationInformation> representationInformations = RepresentationInformationFactory.getStrategyRepresentationInformations(getStrategy());
        for (RepresentationInformation representationInformation : representationInformations)
            representationInformation.setup(this);
        this.history = new PositionHistory(this);
    }

    @Override
    public void update() throws Exception {
        for (RepresentationInformation representationInformation : representationInformations)
            representationInformation.update(this);
    }

    @Override
    public void renameCluster(RenameOperation operation) {
        RenamePartitionsOperation partitionsOperation = new RenamePartitionsOperation(operation);
        partitionsOperation.execute(this);
    }

    @Override
    public void mergeClusters(MergeOperation operation) {
        MergePartitionsOperation partitionsOperation = new MergePartitionsOperation(operation);
        partitionsOperation.execute(this);
    }

    @Override
    public void splitCluster(SplitOperation operation) {
        SplitPartitionsOperation partitionsOperation = new SplitPartitionsOperation(operation);
        partitionsOperation.execute(this);
    }

    @Override
    public void transferEntities(TransferOperation operation) {
        TransferPartitionsOperation partitionsOperation = new TransferPartitionsOperation(operation);
        partitionsOperation.execute(this);
    }

    @Override
    public void formCluster(FormClusterOperation operation) {
        FormClusterPartitionsOperation partitionsOperation = new FormClusterPartitionsOperation(operation);
        partitionsOperation.execute(this);
    }

    @Override
    public void deleteProperties() {
        representationInformations.forEach(RepresentationInformation::deleteProperties);
    }

    @Override
    public Decomposition snapshotDecomposition(String snapshotName) throws Exception {
        HistoryService historyService = ContextManager.get().getBean(HistoryService.class);
        PositionHistoryService positionHistoryService = ContextManager.get().getBean(PositionHistoryService.class);

        PartitionsDecomposition snapshotDecomposition = new PartitionsDecomposition(this, snapshotName);

        PositionHistory snapshotHistory = new PositionHistory(snapshotDecomposition);
        snapshotDecomposition.setHistory(snapshotHistory);
        historyService.saveHistory(snapshotHistory);
        positionHistoryService.saveGraphPositions(snapshotDecomposition, positionHistoryService.getGraphPositions(this));

        return snapshotDecomposition;
    }
}
