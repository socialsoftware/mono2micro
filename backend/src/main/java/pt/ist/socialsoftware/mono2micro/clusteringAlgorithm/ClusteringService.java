package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusteringService {
    public List<String> getAlgorithmSupportedStrategyTypes(String algorithmType) {
        return ClusteringFactory.getClustering(algorithmType).getAlgorithmSupportedStrategyTypes();
    }
}
