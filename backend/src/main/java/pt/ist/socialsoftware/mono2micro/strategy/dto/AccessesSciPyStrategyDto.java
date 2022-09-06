package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

public class AccessesSciPyStrategyDto extends StrategyDto {
    public AccessesSciPyStrategyDto(AccessesSciPyStrategy strategy) {
        this.codebaseName = strategy.getCodebase().getName();
        this.name = strategy.getName();
        this.type = strategy.getType();
    }
}
