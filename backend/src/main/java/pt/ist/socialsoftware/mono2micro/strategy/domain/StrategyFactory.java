package pt.ist.socialsoftware.mono2micro.strategy.domain;

import pt.ist.socialsoftware.mono2micro.strategy.dto.AccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.RecommendAccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.StrategyDto;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy.RECOMMEND_ACCESSES_SCIPY;

public class StrategyFactory {
    private static StrategyFactory factory = null;

    public static StrategyFactory getFactory() {
        if (factory == null)
            factory = new StrategyFactory();
        return factory;
    }

    public Strategy getStrategy(StrategyDto strategyDto) {
        switch (strategyDto.getType()) {
            case ACCESSES_SCIPY:
                return new AccessesSciPyStrategy((AccessesSciPyStrategyDto) strategyDto);
            case RECOMMEND_ACCESSES_SCIPY:
                return new RecommendAccessesSciPyStrategy((RecommendAccessesSciPyStrategyDto) strategyDto);
            default:
                throw new RuntimeException("The type \"" + strategyDto.getType() + "\" is not a valid source type.");
        }
    }
}
