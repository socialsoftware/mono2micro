package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.representationsInfo.RepositoryInfo.REPOSITORY_INFO;

public class TSRMetric extends DecompositionMetric {
    public static final String TSR = "Team Size Reduction Ratio";

    @Override
    public String getType() {
        return TSR;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) {
        RepositoryInfo repositoryInfo = (RepositoryInfo) decomposition.getRepresentationInformationByType(REPOSITORY_INFO);
        double authorsPerClusterSum = 0, cpm;

        for (Cluster cluster : decomposition.getClusters().values()) {
            Set<String> contributorsInThisCluster = new HashSet<>();
            for (Element element : cluster.getElements()) {
                List<String> authorsFromId = repositoryInfo.getAuthorsFromId(element.getId());
                if (authorsFromId != null)
                    contributorsInThisCluster.addAll(authorsFromId);
            }
            authorsPerClusterSum += contributorsInThisCluster.size();
        }
        cpm = authorsPerClusterSum / decomposition.getClusters().size();

        return cpm / repositoryInfo.getTotalAuthors();
    }
}
