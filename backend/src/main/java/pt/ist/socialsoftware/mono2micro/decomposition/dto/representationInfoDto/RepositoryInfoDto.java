package pt.ist.socialsoftware.mono2micro.decomposition.dto.representationInfoDto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepositoryInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;

import java.util.ArrayList;
import java.util.Map;

public class RepositoryInfoDto extends RepresentationInfoDto {
    private Map<Short, ArrayList<String>> authors;
    private Map<Short, Map<Short, Integer>> commitsInCommon;
    private Map<Short, Integer> totalCommits;

    public RepositoryInfoDto(RepresentationInformation representationInformation) {
        super(representationInformation.getType());
        RepositoryInformation repositoryInformation = (RepositoryInformation) representationInformation;
        this.authors = repositoryInformation.getAuthors();
        this.commitsInCommon = repositoryInformation.getCommitsInCommon();
        this.totalCommits = repositoryInformation.getTotalCommits();
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
