package pt.ist.socialsoftware.mono2micro.domain.similarityGenerators;

import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;

public interface SimilarityGenerator {

    // Needed to initialize, calculate and print similarity matrix distances
    void createSimilarityMatrix(Strategy strategy) throws Exception;
}