package pt.ist.socialsoftware.mono2micro.strategy.domain;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

public class StrategyFactory {
    private static StrategyFactory factory = null;

    public static StrategyFactory getFactory() {
        if (factory == null)
            factory = new StrategyFactory();
        return factory;
    }

    public Strategy getStrategy(String strategyType) {
        switch (strategyType) {
            case ACCESSES_SCIPY:
                return new AccessesSciPyStrategy();
            case REPOSITORY_SCIPY:
                return new RepositorySciPyStrategy();
            default:
                throw new RuntimeException("The type \"" + strategyType + "\" is not a valid strategy type.");
        }
    }
}
