package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

public class AccessesSciPyStrategyDto extends StrategyDto {
    public AccessesSciPyStrategyDto(AccessesSciPyStrategy strategy) {
        this.type = strategy.getType();
        this.sourceTypes = strategy.getSourceTypes();
    }
}
