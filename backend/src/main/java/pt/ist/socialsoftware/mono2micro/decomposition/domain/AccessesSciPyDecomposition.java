package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.Clustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.ExpertClustering;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperationsWithPosition;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.util.*;

@Document("decomposition")
public class AccessesSciPyDecomposition extends Decomposition implements SciPyDecomposition, AccessesDecomposition {
    public static final String ACCESSES_SCIPY = "Accesses-Based Similarity and SciPy Clustering Algorithm";

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(ACCESSES_DECOMPOSITION);
        add(SCIPY_DECOMPOSITION);
    }};

    private static final List<String> requiredRepresentations = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
    }};

    private boolean outdated;
    private double silhouetteScore;
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>

    public AccessesSciPyDecomposition() {
        this.decompositionOperations = new DecompositionOperationsWithPosition(this);
    }

    public AccessesSciPyDecomposition(DecompositionRequest request) {
        this.decompositionOperations = new DecompositionOperationsWithPosition(this);
    }

    public AccessesSciPyDecomposition(AccessesSciPyDecomposition decomposition) {
        this.name = decomposition.getName();
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.clusters = decomposition.getClusters();
        this.decompositionOperations = new DecompositionOperationsWithPosition(this);
    }

    @Override
    public String getType() {
        return ACCESSES_SCIPY;
    }

    public List<String> getImplementations() {
        return implementationTypes;
    }

    public List<String> getRequiredRepresentations() {
        return requiredRepresentations;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
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
                new CohesionMetric(), new ComplexityMetric(), new CouplingMetric(), new PerformanceMetric()};

        Map<String, Object> newMetrics = new HashMap<>();
        for (DecompositionMetric metric : metricObjects)
            newMetrics.put(metric.getType(), metric.calculateMetric(this));
        metrics = newMetrics;
    }
}
