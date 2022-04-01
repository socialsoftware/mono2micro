package pt.ist.socialsoftware.mono2micro.domain.clusteringAlgorithm;

import pt.ist.socialsoftware.mono2micro.domain.strategy.Strategy;
import pt.ist.socialsoftware.mono2micro.dto.decompositionDto.RequestDto;

public interface ClusteringAlgorithm {

    // Not all clustering algorithms implement this function, but for simplicity when invoking,
    // this is required. Leave the method empty on the clustering algorithms that don't need it
    void createDendrogram(Strategy strategy) throws Exception;

    void createDecomposition(Strategy strategy, RequestDto requestDto) throws Exception;
}