package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;

public class StrategyDto {
    String codebaseName;
    String name;
    String decompositionType;
    List<String> representationTypes;

    public StrategyDto(Strategy strategy) {
        this.codebaseName = strategy.getCodebase().getName();
        this.name = strategy.getName();
        this.decompositionType = strategy.getDecompositionType();
        this.representationTypes = strategy.getRepresentationTypes();
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDecompositionType() {
        return decompositionType;
    }

    public void setDecompositionType(String decompositionType) {
        this.decompositionType = decompositionType;
    }

    public List<String> getRepresentationTypes() {
        return representationTypes;
    }

    public void setRepresentationTypes(List<String> representationTypes) {
        this.representationTypes = representationTypes;
    }
}
