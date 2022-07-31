package pt.ist.socialsoftware.mono2micro.strategy.dto;

import java.util.List;

public class StrategyDto {
    String type;
    List<String> sourceTypes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(List<String> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }
}
