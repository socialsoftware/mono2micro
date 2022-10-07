package pt.ist.socialsoftware.mono2micro.decomposition.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.SciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

@Document("decomposition")
public class RepositorySciPy extends Decomposition implements SciPyDecomposition, RepositoryDecomposition {
    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(REPOSITORY_DECOMPOSITION);
        add(SCIPY_DECOMPOSITION);
    }};

    private boolean expert;
    private double silhouetteScore;
    @DBRef
    Similarity similarity;
    private Map<Short, String> entityIDToClusterName = new HashMap<>();
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();
    private Integer totalAuthors;

    public RepositorySciPy() {}

    @Override
    public String getStrategyType() {
        return REPOSITORY_SCIPY;
    }

    public List<String> getImplementations() {
        return implementationTypes;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    public double getSilhouetteScore() {
        return silhouetteScore;
    }

    public void setSilhouetteScore(double silhouetteScore) {
        this.silhouetteScore = silhouetteScore;
    }

    public Similarity getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Similarity similarity) {
        this.similarity = similarity;
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
}
