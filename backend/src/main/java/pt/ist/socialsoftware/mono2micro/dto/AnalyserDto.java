package pt.ist.socialsoftware.mono2micro.dto;

import java.util.List;

import pt.ist.socialsoftware.mono2micro.domain.Constants;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.domain.Graph;

public class AnalyserDto {
    private Graph expert;
    private List<String> profiles;
    private int requestLimit;
    // only used when the codebase "is dynamic" aka !isStatic()
    private int tracesMaxLimit; // default is 0 which means, no limit
    private Constants.TypeOfTraces typeOfTraces = Constants.TypeOfTraces.DEFAULT;

    public Graph getExpert() { return expert; }

    public void setExpert(Graph expert) {
        this.expert = expert;
    }

    public List<String> getProfiles() { return profiles; }

    public void setProfiles(List<String> profiles) { this.profiles = profiles; }

    public int getRequestLimit() { return requestLimit; }

    public void setRequestLimit(int requestLimit) { this.requestLimit = requestLimit; }

    public int getTracesMaxLimit() { return tracesMaxLimit; }

    public void setTracesMaxLimit(int tracesMaxLimit) { this.tracesMaxLimit = tracesMaxLimit; }

    public Constants.TypeOfTraces getTypeOfTraces() { return typeOfTraces; }

    public void setTypeOfTraces(Constants.TypeOfTraces typeOfTraces) { this.typeOfTraces = typeOfTraces; }
}
