package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

public class StrategyDto {
    String codebaseName;
    String name;
    String type;

    public StrategyDto(Strategy strategy) {
        this.codebaseName = strategy.getCodebase().getName();
        this.name = strategy.getName();
        this.type = strategy.getType();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
