package pt.ist.socialsoftware.mono2micro.domain.similarityGenerators;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.ACCESSES_SCIPY;

public class SimilarityGeneratorFactory {
    private static SimilarityGeneratorFactory factory = null;

    public static SimilarityGeneratorFactory getFactory() {
        if (factory == null)
            factory = new SimilarityGeneratorFactory();
        return factory;
    }

    public SimilarityGenerator getSimilarityGenerator(String strategyType) {
        switch (strategyType) {
            case ACCESSES_SCIPY:
                return new AccessesSimilarityGenerator();
            default:
                throw new RuntimeException("The type \"" + strategyType + "\" is not a valid strategy.");
        }
    }
}