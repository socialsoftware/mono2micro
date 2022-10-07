package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.log.domain.Log;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.util.*;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;

@Document("decomposition")
public class AccessesSciPy extends Decomposition implements SciPyDecomposition, AccessesDecomposition, LogDecomposition {
    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(ACCESSES_DECOMPOSITION);
        add(SCIPY_DECOMPOSITION);
        add(LOG_DECOMPOSITION);
    }};

    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;
    @DBRef
    Similarity similarity;
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, String> entityIDToClusterName = new HashMap<>();
    @DBRef
    private Log log;

    public AccessesSciPy() {}

    public AccessesSciPy(AccessesSciPy decomposition) {
        this.name = decomposition.getName();
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.clusters = decomposition.getClusters();
        this.entityIDToClusterName = decomposition.getEntityIDToClusterName();
    }

    @Override
    public String getStrategyType() {
        return ACCESSES_SCIPY;
    }

    public List<String> getImplementations() {
        return implementationTypes;
    }

    public Similarity getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    @Override
    public Map<Short, String> getEntityIDToClusterName() {
        return entityIDToClusterName;
    }

    public void setEntityIDToClusterName(Map<Short, String> entityIDToClusterName) {
        this.entityIDToClusterName = entityIDToClusterName;
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public void setLog(Log log) {
        this.log = log;
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
}
