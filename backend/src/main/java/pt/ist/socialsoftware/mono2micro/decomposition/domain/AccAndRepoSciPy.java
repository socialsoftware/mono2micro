package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.LogDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.log.domain.Log;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY;

@Document("decomposition")
public class AccAndRepoSciPy extends Decomposition implements SciPyDecomposition, AccessesDecomposition, RepositoryDecomposition, LogDecomposition {
    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(ACCESSES_DECOMPOSITION);
        add(REPOSITORY_DECOMPOSITION);
        add(LOG_DECOMPOSITION);
        add(SCIPY_DECOMPOSITION);
    }};

    private boolean outdated;
    private boolean expert;
    private double silhouetteScore;
    @DBRef
    Similarity similarity;
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<Short, String> entityIDToClusterName = new HashMap<>();
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();
    private Integer totalAuthors;
    @DBRef
    private Log log;

    public AccAndRepoSciPy() {}

    public AccAndRepoSciPy(AccAndRepoSciPy decomposition) {
        this.name = decomposition.getName();
        this.similarity = decomposition.getSimilarity();
        this.metrics = decomposition.getMetrics();
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.clusters = decomposition.getClusters();
        this.entityIDToClusterName = decomposition.getEntityIDToClusterName();
        this.authors = decomposition.getAuthors();
        this.commitsInCommon = decomposition.getCommitsInCommon();
        this.totalCommits = decomposition.getTotalCommits();
        this.totalAuthors = decomposition.getTotalAuthors();
    }

    @Override
    public String getStrategyType() {
        return ACC_AND_REPO_SCIPY;
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
