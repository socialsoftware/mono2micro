package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TSRMetric extends DecompositionMetric {
    public static final String TSR = "Team Size Reduction Ratio";

    public String getType() {
        return TSR;
    }

    public Double calculateMetric(Decomposition decomposition) {
        RepositoryDecomposition d = (RepositoryDecomposition) decomposition;
        double authorsPerClusterSum = 0, cpm;

        for (Cluster cluster : decomposition.getClusters().values()) {
            Set<String> contributorsInThisCluster = new HashSet<>();
            for (Element element : cluster.getElements()) {
                List<String> authorsFromId = d.getAuthorsFromId(element.getId());
                if (authorsFromId != null)
                    contributorsInThisCluster.addAll(authorsFromId);
            }
            authorsPerClusterSum += contributorsInThisCluster.size();
        }
        cpm = authorsPerClusterSum / decomposition.getClusters().size();

        return cpm / d.getTotalAuthors();
    }
}
