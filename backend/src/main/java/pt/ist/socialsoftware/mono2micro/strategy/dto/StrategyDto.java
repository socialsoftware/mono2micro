package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;

public class StrategyDto {
    String codebaseName;
    String name;
    String algorithmType;
    List<String> representationInformationTypes;

    public StrategyDto(Strategy strategy) {
        this.codebaseName = strategy.getCodebase().getName();
        this.name = strategy.getName();
        this.algorithmType = strategy.getAlgorithmType();
        this.representationInformationTypes = strategy.getRepresentationInfoTypes();
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

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public List<String> getRepresentationInformationTypes() {
        return representationInformationTypes;
    }

    public void setRepresentationInformationTypes(List<String> representationInformationTypes) {
        this.representationInformationTypes = representationInformationTypes;
    }
}
