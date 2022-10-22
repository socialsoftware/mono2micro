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

import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition.DecompositionType.*;

@Document("decomposition")
public class PartitionsDecomposition extends Decomposition {
    public PartitionsDecomposition() {}

    public PartitionsDecomposition(String type) {this.type = type;}

    public PartitionsDecomposition(DecompositionRequest decompositionRequest) {this.type = decompositionRequest.getDecompositionType();}

    public PartitionsDecomposition(PartitionsDecomposition decomposition, String snapshotName) throws Exception {
        this.type = decomposition.type;
        this.name = snapshotName;
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        List<RepresentationInformation> representationInformations = getRepresentationInformationsByDecompositionType(this.type);
        for (RepresentationInformation representationInformation : representationInformations)
            representationInformation.snapshot(this, decomposition);
    }

    @Override
    public List<RepresentationInformation> getRepresentationInformationsByDecompositionType(String type) {
        List<RepresentationInformation> representationInformations = new ArrayList<>();

        switch (type) {
            case ACC_AND_REPO_DECOMPOSITION:
                representationInformations.add(new AccessesInfo());
                representationInformations.add(new RepositoryInfo());
                break;
            case ACCESSES_DECOMPOSITION:
                representationInformations.add(new AccessesInfo());
                break;
            case REPOSITORY_DECOMPOSITION:
                representationInformations.add(new RepositoryInfo());
                break;
        }
        return representationInformations;
    }

    @Override
    public Clustering getClusteringAlgorithm() {
        if (isExpert())
            return new ExpertClustering();
        else return new SciPyClustering();
    }

    @Override
    public Set<String> getRequiredRepresentations() {
        return getRepresentationInformationsByDecompositionType(type).stream()
                .map(RepresentationInformation::getRequiredRepresentations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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
        List<RepresentationInformation> representationInformations = getRepresentationInformationsByDecompositionType(this.type);
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
