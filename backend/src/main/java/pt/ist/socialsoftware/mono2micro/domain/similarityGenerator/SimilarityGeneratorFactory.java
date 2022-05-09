package pt.ist.socialsoftware.mono2micro.domain.similarityGenerator;

import static pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy.StrategyType.*;

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
            case RECOMMENDATION_ACCESSES_SCIPY: // Uses the same generator since it generates recommendations of the same type
                return new AccessesSimilarityGenerator();
            default:
                throw new RuntimeException("The type \"" + strategyType + "\" is not a valid strategy.");
        }
    }
}