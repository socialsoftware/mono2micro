package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfo;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo.RepresentationInfoFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyDto {
    String codebaseName;
    String name;
    String algorithmType;
    List<String> representationInformationTypes;
    List<String> parameterTypes;

    public StrategyDto(Strategy strategy) {
        this.codebaseName = strategy.getCodebase().getName();
        this.name = strategy.getName();
        this.algorithmType = strategy.getAlgorithmType();
        this.representationInformationTypes = strategy.getRepresentationInfoTypes();
        this.parameterTypes = new ArrayList<>();

        for (String representationType : this.representationInformationTypes) {
            RepresentationInfo representationInfo = RepresentationInfoFactory.getRepresentationInfoFromType(representationType);
            for (String parameterType : representationInfo.getParameters()) {
                if (!this.parameterTypes.contains(parameterType)) {
                    this.parameterTypes.add(parameterType);
                }
            }
        }
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

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
