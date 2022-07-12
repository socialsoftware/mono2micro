package pt.ist.socialsoftware.mono2micro.strategy.dto;

import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyDtoFactory {
    private static StrategyDtoFactory factory = null;

    public static StrategyDtoFactory getFactory() {
        if (factory == null)
            factory = new StrategyDtoFactory();
        return factory;
    }

    public StrategyDto getStrategyDto(Strategy strategy) {
        if (strategy == null)
            return null;
        switch (strategy.getType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                return new AccessesSciPyStrategyDto((AccessesSciPyStrategy) strategy);
            case RecommendAccessesSciPyStrategy.RECOMMEND_ACCESSES_SCIPY:
                return new RecommendAccessesSciPyStrategyDto((RecommendAccessesSciPyStrategy) strategy);
            default:
                throw new RuntimeException("The type \"" + strategy.getType() + "\" is not a valid strategy type.");
        }
    }

    public List<StrategyDto> getStrategyDtos(List<Strategy> strategies) {
        if (strategies == null)
            return null;
        List<StrategyDto> strategyDtos = new ArrayList<>();
        for (Strategy strategy : strategies)
            strategyDtos.add(getStrategyDto(strategy));
        return strategyDtos;
    }
}
