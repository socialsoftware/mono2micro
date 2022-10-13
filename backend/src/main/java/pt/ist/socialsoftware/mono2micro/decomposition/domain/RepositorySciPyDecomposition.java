package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.views.ClusterViewDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.DecompositionMetric;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.TSRMetric;
import pt.ist.socialsoftware.mono2micro.operation.*;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewTransferOperation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.io.IOException;
import java.util.*;

@Document("decomposition")
public class RepositorySciPyDecomposition extends Decomposition implements SciPyDecomposition, RepositoryDecomposition, ClusterViewDecomposition {
    public static final String REPOSITORY_SCIPY = "Repository-Based Similarity and SciPy Clustering Algorithm";

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
        add(AuthorRepresentation.AUTHOR);
        add(CommitRepresentation.COMMIT);
    }};

    private double silhouetteScore;
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();
    private Integer totalAuthors;

    public RepositorySciPyDecomposition() {
        this.history = new PositionHistory(this);
    }

    public RepositorySciPyDecomposition(DecompositionRequest request) {
        this.history = new PositionHistory(this);
    }

    public RepositorySciPyDecomposition(RepositorySciPyDecomposition decomposition) {
        this.name = decomposition.getName();
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.clusters = decomposition.getClusters();
        this.authors = decomposition.getAuthors();
        this.commitsInCommon = decomposition.getCommitsInCommon();
        this.totalCommits = decomposition.getTotalCommits();
        this.totalAuthors = decomposition.getTotalAuthors();
        this.history = new PositionHistory(this);
    }

    @Override
    public String getType() {
        return REPOSITORY_SCIPY;
    }

    public List<String> getRequiredRepresentations() {
        return requiredRepresentations;
    }

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    @Override
    public Map<Short, ArrayList<String>> getAuthors() {
        return authors;
    }

    public void setAuthors(Map<Short, ArrayList<String>> authors) {
        this.authors = authors;
    }

    @Override
    public Map<Short, Map<Short, Integer>> getCommitsInCommon() {
        return commitsInCommon;
    }

    public void setCommitsInCommon(Map<Short, Map<Short, Integer>> commitsInCommon) {
        this.commitsInCommon = commitsInCommon;
    }

    @Override
    public Map<Short, Integer> getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(Map<Short, Integer> totalCommits) {
        this.totalCommits = totalCommits;
    }

    @Override
    public Integer getTotalAuthors() {
        return totalAuthors;
    }

    @Override
    public void setTotalAuthors(Integer totalAuthors) {
        this.totalAuthors = totalAuthors;
    }

    @Override
    public Clustering getClusteringAlgorithm() {
        if (isExpert())
            return new ExpertClustering();
        else return new SciPyClustering();
    }

    public void calculateMetrics() {
        DecompositionMetric[] metricObjects = new DecompositionMetric[] {new TSRMetric()};

        Map<String, Object> newMetrics = new HashMap<>();
        for (DecompositionMetric metric : metricObjects)
            newMetrics.put(metric.getType(), metric.calculateMetric(this));
        metrics = newMetrics;
    }

    public String getEdgeWeights(GridFsService gridFsService, String viewType) throws Exception {
        if (viewType.equals(REPOSITORY_DECOMPOSITION))
            return getEdgeWeightsFromRepository(gridFsService);
        else throw new RuntimeException("View type not supported by this decomposition");
    }

    public void setup() throws IOException {
        setupAuthorsAndCommits();
    }

    public void update() {}

    public void deleteProperties() {}

    public void renameCluster(RenameOperation operation) {
        clusterViewRename(operation.getClusterName(), operation.getNewClusterName());
    }

    public void mergeClusters(MergeOperation operation) {
        ClusterViewMergeOperation mergeOperation = (ClusterViewMergeOperation) operation;
        mergeOperation.addEntities(this); // needed so that when doing undo, the original entities are restored
        clusterViewMerge(operation.getCluster1Name(), operation.getCluster2Name(), operation.getNewName());
        setOutdated(true);
    }

    public void splitCluster(SplitOperation operation) {
        ClusterViewSplitOperation mergeOperation = (ClusterViewSplitOperation) operation;
        clusterViewSplit(mergeOperation.getOriginalCluster(), mergeOperation.getNewCluster(), mergeOperation.getEntities());
        setOutdated(true);
    }

    public void transferEntities(TransferOperation operation) {
        ClusterViewTransferOperation transferOperation = (ClusterViewTransferOperation) operation;
        clusterViewTransfer(transferOperation.getFromCluster(), transferOperation.getToCluster(), transferOperation.getEntities());
        setOutdated(true);
    }

    public void formCluster(FormClusterOperation operation) {
        ClusterViewFormClusterOperation formClusterOperation = (ClusterViewFormClusterOperation) operation;
        clusterViewFormCluster(formClusterOperation.getNewCluster(), formClusterOperation.getEntities());
        setOutdated(true);
    }

    public void undoOperation(Operation operation) {
        undoClusterViewOperation(operation);
    }

    public void redoOperation(Operation operation) {
        redoClusterViewOperation(operation);
    }

    public Decomposition snapshotDecomposition(String snapshotName) throws IOException {
        HistoryService historyService = ContextManager.get().getBean(HistoryService.class);
        PositionHistoryService positionHistoryService = ContextManager.get().getBean(PositionHistoryService.class);
        RepositorySciPyDecomposition snapshotDecomposition = new RepositorySciPyDecomposition(this);
        snapshotDecomposition.setName(snapshotName);

        PositionHistory snapshotHistory = new PositionHistory(snapshotDecomposition);
        snapshotDecomposition.setHistory(snapshotHistory);
        historyService.saveHistory(snapshotHistory);
        positionHistoryService.saveGraphPositions(snapshotDecomposition, positionHistoryService.getGraphPositions(this));

        return snapshotDecomposition;
    }
}