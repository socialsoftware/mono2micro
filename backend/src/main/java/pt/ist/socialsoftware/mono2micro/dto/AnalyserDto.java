package pt.ist.socialsoftware.mono2micro.dto;

import java.util.List;

import pt.ist.socialsoftware.mono2micro.domain.Graph;

public class AnalyserDto {
    private Graph expert;
    private List<String> profiles;

    public Graph getExpert() {
        return expert;
    }

    public void setExpert(Graph expert) {
        this.expert = expert;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }
}