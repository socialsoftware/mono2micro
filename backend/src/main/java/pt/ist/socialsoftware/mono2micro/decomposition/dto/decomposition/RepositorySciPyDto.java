package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

public class RepositorySciPyDto extends DecompositionDto {
    private boolean expert;
    private double silhouetteScore;
    private Map<String, Short> entityIDToClusterName = new HashMap<>();
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();


    public RepositorySciPyDto() {this.type = REPOSITORY_SCIPY;}

    public RepositorySciPyDto(RepositorySciPy decomposition) {
        this.setCodebaseName(decomposition.getSimilarity().getStrategy().getCodebase().getName());
        this.setStrategyName(decomposition.getStrategy().getName());
        this.setName(decomposition.getName());
        this.type = REPOSITORY_SCIPY;
        this.setMetrics(decomposition.getMetrics());
        this.silhouetteScore = decomposition.getSilhouetteScore();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        this.authors = decomposition.getAuthors();
        this.commitsInCommon = decomposition.getCommitsInCommon();
        this.totalCommits = decomposition.getTotalCommits();
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

    public Map<String, Short> getEntityIDToClusterName() {
        return entityIDToClusterName;
    }

    public void setEntityIDToClusterName(Map<String, Short> entityIDToClusterName) {
        this.entityIDToClusterName = entityIDToClusterName;
    }

    public Map<Short, ArrayList<String>> getAuthors() {
        return authors;
    }

    public void setAuthors(Map<Short, ArrayList<String>> authors) {
        this.authors = authors;
    }

    public Map<Short, Map<Short, Integer>> getCommitsInCommon() {
        return commitsInCommon;
    }

    public void setCommitsInCommon(Map<Short, Map<Short, Integer>> commitsInCommon) {
        this.commitsInCommon = commitsInCommon;
    }

    public Map<Short, Integer> getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(Map<Short, Integer> totalCommits) {
        this.totalCommits = totalCommits;
    }
}