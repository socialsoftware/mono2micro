package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformation;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepresentationInformationFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyDto {
    String codebaseName;
    String name;
    String algorithmType;
    List<String> strategyTypes;
    List<String> parameterTypes;

    public StrategyDto(Strategy strategy) {
        this.codebaseName = strategy.getCodebase().getName();
        this.name = strategy.getName();
        this.algorithmType = strategy.getAlgorithmType();
        this.strategyTypes = strategy.getStrategyTypes();
        this.parameterTypes = new ArrayList<>();

        for (RepresentationInformation representationInformation : RepresentationInformationFactory.getStrategyRepresentationInformations(strategy)) {
            for (String parameterType : representationInformation.getParameters()) {
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

    public List<String> getStrategyTypes() {
        return strategyTypes;
    }

    public void setStrategyTypes(List<String> strategyTypes) {
        this.strategyTypes = strategyTypes;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
