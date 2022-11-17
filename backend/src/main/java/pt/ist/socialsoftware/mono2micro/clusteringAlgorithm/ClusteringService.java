package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusteringService {
    public List<String> getSupportedRepresentationInfoTypes(String algorithmType) {
        return ClusteringFactory.getClustering(algorithmType).getSupportedRepresentationInfoTypes();
    }
}
