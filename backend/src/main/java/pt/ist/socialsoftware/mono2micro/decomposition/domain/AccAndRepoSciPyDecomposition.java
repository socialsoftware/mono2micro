package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.views.ClusterViewDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.history.domain.PositionHistory;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.history.service.HistoryService;
import pt.ist.socialsoftware.mono2micro.history.service.PositionHistoryService;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
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
import java.util.stream.Collectors;

@Document("decomposition")
public class AccAndRepoSciPyDecomposition extends Decomposition implements SciPyDecomposition, AccessesDecomposition, RepositoryDecomposition, ClusterViewDecomposition {
    public static final String ACC_AND_REPO_SCIPY = "Accesses and Repository-Based Similarity and SciPy Clustering Algorithm";

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
        add(AuthorRepresentation.AUTHOR);
        add(CommitRepresentation.COMMIT);
    }};

    private double silhouetteScore;
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();
    private Integer totalAuthors;

    public AccAndRepoSciPyDecomposition() {
        this.history = new PositionHistory(this);
    }

    public AccAndRepoSciPyDecomposition(DecompositionRequest decompositionRequest) {
        this.history = new PositionHistory(this);
    }

    public AccAndRepoSciPyDecomposition(AccAndRepoSciPyDecomposition decomposition) {
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
        return ACC_AND_REPO_SCIPY;
    }

    @Override
    public List<String> getRequiredRepresentations() {
        return requiredRepresentations;
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

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    @Override
    public Map<String, Functionality> getFunctionalities() { return functionalities; }

    public void setFunctionalities(Map<String, Functionality> functionalities) { this.functionalities = functionalities; }

    @Override
    public Clustering getClusteringAlgorithm() {
        if (isExpert())
            return new ExpertClustering();
        else return new SciPyClustering();
    }

    public void calculateMetrics() {
        DecompositionMetric[] metricObjects = new DecompositionMetric[] {
                new CohesionMetric(), new ComplexityMetric(), new CouplingMetric(), new PerformanceMetric(), new TSRMetric()};

        Map<String, Object> newMetrics = new HashMap<>();
        for (DecompositionMetric metric : metricObjects)
            newMetrics.put(metric.getType(), metric.calculateMetric(this));
        metrics = newMetrics;
    }

    public String getEdgeWeights(GridFsService gridFsService, String viewType) throws Exception {
        if (viewType.equals(ACCESSES_DECOMPOSITION))
            return getEdgeWeightsFromAccesses(gridFsService);
        else if (viewType.equals(REPOSITORY_DECOMPOSITION))
            return getEdgeWeightsFromRepository(gridFsService);
        else throw new RuntimeException("View type not supported by this decomposition");
    }

    public void setup() throws Exception {
        setupFunctionalities();
        setupAuthorsAndCommits();
    }

    public void update() throws Exception {
        setupFunctionalities();
    }

    public void deleteProperties() {
        deleteAccessesProperties();
    }

    public void renameCluster(RenameOperation operation) {
        clusterViewRename(operation.getClusterName(), operation.getNewClusterName());
        renameClusterInFunctionalities(operation.getClusterName(), operation.getNewClusterName());
    }

    public void mergeClusters(MergeOperation operation) {
        ClusterViewMergeOperation mergeOperation = (ClusterViewMergeOperation) operation;
        mergeOperation.addEntities(this); // needed so that when doing undo, the original entities are restored
        clusterViewMerge(operation.getCluster1Name(), operation.getCluster2Name(), operation.getNewName());
        removeFunctionalitiesWithEntities(getCluster(operation.getNewName()).getElements());
        setOutdated(true);
    }

    public void splitCluster(SplitOperation operation) {
        ClusterViewSplitOperation mergeOperation = (ClusterViewSplitOperation) operation;
        clusterViewSplit(mergeOperation.getOriginalCluster(), mergeOperation.getNewCluster(), mergeOperation.getEntities());
        removeFunctionalitiesWithEntities(getCluster(mergeOperation.getNewCluster()).getElements());
        setOutdated(true);
    }

    public void transferEntities(TransferOperation operation) {
        ClusterViewTransferOperation transferOperation = (ClusterViewTransferOperation) operation;
        clusterViewTransfer(transferOperation.getFromCluster(), transferOperation.getToCluster(), transferOperation.getEntities());
        Set<Short> entitiesIDs = Arrays.stream(transferOperation.getEntities().split(",")).map(Short::valueOf).collect(Collectors.toSet());
        removeFunctionalitiesWithEntityIDs(entitiesIDs);
        setOutdated(true);
    }

    public void formCluster(FormClusterOperation operation) {
        ClusterViewFormClusterOperation formClusterOperation = (ClusterViewFormClusterOperation) operation;
        clusterViewFormCluster(formClusterOperation.getNewCluster(), formClusterOperation.getEntities());
        Set<Short> entitiesIDs = formClusterOperation.getEntities().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        removeFunctionalitiesWithEntityIDs(entitiesIDs);
        setOutdated(true);
    }

    public void undoOperation(Operation operation) {
        undoClusterViewOperation(operation);
    }

    public void redoOperation(Operation operation) {
        redoClusterViewOperation(operation);
    }

    public Decomposition snapshotDecomposition(String snapshotName) throws IOException {
        FunctionalityService functionalityService = ContextManager.get().getBean(FunctionalityService.class);
        HistoryService historyService = ContextManager.get().getBean(HistoryService.class);
        PositionHistoryService positionHistoryService = ContextManager.get().getBean(PositionHistoryService.class);

        AccAndRepoSciPyDecomposition snapshotDecomposition = new AccAndRepoSciPyDecomposition(this);
        snapshotDecomposition.setName(snapshotName);

        // Duplicate functionalities for the new decomposition (also includes duplicating respective redesigns)
        copyFunctionalities(functionalityService, snapshotDecomposition);

        PositionHistory snapshotHistory = new PositionHistory(snapshotDecomposition);
        snapshotDecomposition.setHistory(snapshotHistory);
        historyService.saveHistory(snapshotHistory);
        positionHistoryService.saveGraphPositions(snapshotDecomposition, positionHistoryService.getGraphPositions(this));

        return snapshotDecomposition;
    }
}
