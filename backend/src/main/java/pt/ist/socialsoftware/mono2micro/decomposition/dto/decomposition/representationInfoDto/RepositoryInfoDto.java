package pt.ist.socialsoftware.mono2micro.decomposition.dto.decomposition.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepositoryInfo;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;

import java.util.ArrayList;
import java.util.Map;

public class RepositoryInfoDto extends RepresentationInfoDto {
    private Map<Short, ArrayList<String>> authors;
    private Map<Short, Map<Short, Integer>> commitsInCommon;
    private Map<Short, Integer> totalCommits;

    public RepositoryInfoDto(RepresentationInfo representationInfo) {
        super(representationInfo.getType());
        RepositoryInfo repositoryInfo = (RepositoryInfo) representationInfo;
        this.authors = repositoryInfo.getAuthors();
        this.commitsInCommon = repositoryInfo.getCommitsInCommon();
        this.totalCommits = repositoryInfo.getTotalCommits();
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
