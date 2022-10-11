package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperationsWithPosition;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document("decomposition")
public class AccAndRepoSciPyDecomposition extends Decomposition implements SciPyDecomposition, AccessesDecomposition, RepositoryDecomposition {
    public static final String ACC_AND_REPO_SCIPY = "Accesses and Repository-Based Similarity and SciPy Clustering Algorithm";

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(ACCESSES_DECOMPOSITION);
        add(REPOSITORY_DECOMPOSITION);
        add(SCIPY_DECOMPOSITION);
    }};

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
        add(AuthorRepresentation.AUTHOR);
        add(CommitRepresentation.COMMIT);
    }};

    private boolean outdated;
    private double silhouetteScore;
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();
    private Integer totalAuthors;

    public AccAndRepoSciPyDecomposition() {
        this.decompositionOperations = new DecompositionOperationsWithPosition(this);
    }

    public AccAndRepoSciPyDecomposition(DecompositionRequest decompositionRequest) {
        this.decompositionOperations = new DecompositionOperationsWithPosition(this);
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
        this.decompositionOperations = new DecompositionOperationsWithPosition(this);
    }

    @Override
    public String getType() {
        return ACC_AND_REPO_SCIPY;
    }

    @Override
    public List<String> getImplementations() {
        return implementationTypes;
    }

    @Override
    public List<String> getRequiredRepresentations() {
        return requiredRepresentations;
    }

    @Override
    public boolean isOutdated() {
        return outdated;
    }

    @Override
    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
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
    public Clustering getClusteringAlgorithm(GridFsService gridFsService) {
        if (isExpert())
            return new ExpertClustering(gridFsService);
        else return new SciPyClustering(gridFsService);
    }

    public void calculateMetrics() {
        DecompositionMetric[] metricObjects = new DecompositionMetric[] {
                new CohesionMetric(), new ComplexityMetric(), new CouplingMetric(), new PerformanceMetric(), new TSRMetric()};

        Map<String, Object> newMetrics = new HashMap<>();
        for (DecompositionMetric metric : metricObjects)
            newMetrics.put(metric.getType(), metric.calculateMetric(this));
        metrics = newMetrics;
    }
}
