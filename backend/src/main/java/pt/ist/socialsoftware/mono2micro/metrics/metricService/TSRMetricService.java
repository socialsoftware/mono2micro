package pt.ist.socialsoftware.mono2micro.metrics.metricService;

import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TSRMetricService {
    public Double calculateMetric(RepositoryDecomposition decomposition) {
        double authorsPerClusterSum = 0, cpm;

        for (Cluster cluster : decomposition.getClusters().values()) {
            Set<String> contributorsInThisCluster = new HashSet<>();
            for (Element element : cluster.getElements()) {
                List<String> authorsFromId = decomposition.getAuthorsFromId(element.getId());
                if (authorsFromId != null)
                    contributorsInThisCluster.addAll(authorsFromId);
            }
            authorsPerClusterSum += contributorsInThisCluster.size();
        }
        cpm = authorsPerClusterSum / decomposition.getClusters().size();

        return cpm / decomposition.getTotalAuthors();
    }
}
