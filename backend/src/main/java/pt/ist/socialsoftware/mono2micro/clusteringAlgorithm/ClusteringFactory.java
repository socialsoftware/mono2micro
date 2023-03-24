package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClustering.SCIPY;

public class ClusteringFactory {
    public static List<String> algorithmTypes = new ArrayList<>(Arrays.asList(SCIPY));

    public static Clustering getClustering(String type) {
        switch (type) {
            case SCIPY:
                return new SciPyClustering();
            default:
                throw new RuntimeException("Algorithm type " + type + "not recognized.");
        }
    }
}
