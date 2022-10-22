package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.ClustersDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition.DecompositionType.REPOSITORY_DECOMPOSITION;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo.REPOSITORY_INFO;

public class RepositoryDecompositionDto extends DecompositionDto {
    private boolean expert;
    private Map<String, Short> entityIDToClusterName = new HashMap<>();
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();


    public RepositoryDecompositionDto() {this.type = REPOSITORY_DECOMPOSITION;}

    public RepositoryDecompositionDto(ClustersDecomposition decomposition) {
        this.setCodebaseName(decomposition.getSimilarity().getStrategy().getCodebase().getName());
        this.setStrategyName(decomposition.getStrategy().getName());
        this.setName(decomposition.getName());
        this.type = REPOSITORY_DECOMPOSITION;
        this.setMetrics(decomposition.getMetrics());
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        RepositoryInfo repositoryInfo = (RepositoryInfo) decomposition.getRepresentationInformationByType(REPOSITORY_INFO);
        this.authors = repositoryInfo.getAuthors();
        this.commitsInCommon = repositoryInfo.getCommitsInCommon();
        this.totalCommits = repositoryInfo.getTotalCommits();
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
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