package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo.REPOSITORY_INFO;

public class AccAndRepoSciPyDecompositionDto extends DecompositionDto {
    private boolean outdated;
    private boolean expert;
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>
    private Map<String, Short> entityIDToClusterName = new HashMap<>();
    private Map<Short, ArrayList<String>> authors = new HashMap<>();
    private Map<Short, Map<Short, Integer>> commitsInCommon = new HashMap<>();
    private Map<Short, Integer> totalCommits = new HashMap<>();

    public AccAndRepoSciPyDecompositionDto() {this.type = ACC_AND_REPO_SCIPY;}

    public AccAndRepoSciPyDecompositionDto(AccAndRepoSciPyDecomposition decomposition) {
        this.setCodebaseName(decomposition.getSimilarity().getStrategy().getCodebase().getName());
        this.setStrategyName(decomposition.getStrategy().getName());
        this.setName(decomposition.getName());
        this.type = ACC_AND_REPO_SCIPY;
        this.setMetrics(decomposition.getMetrics());
        this.outdated = decomposition.isOutdated();
        this.expert = decomposition.isExpert();
        this.clusters = decomposition.getClusters();
        RepositoryInfo repositoryInfo = (RepositoryInfo) decomposition.getRepresentationInformationByType(REPOSITORY_INFO);
        this.authors = repositoryInfo.getAuthors();
        this.commitsInCommon = repositoryInfo.getCommitsInCommon();
        this.totalCommits = repositoryInfo.getTotalCommits();
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

    public Map<String, Functionality> getFunctionalities() {
        return functionalities;
    }

    public void setFunctionalities(Map<String, Functionality> functionalities) {
        this.functionalities = functionalities;
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
