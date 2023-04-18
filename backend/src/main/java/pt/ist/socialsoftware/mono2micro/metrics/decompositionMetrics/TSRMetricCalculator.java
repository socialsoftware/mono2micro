package pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation.RepositoryInformation;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.REPOSITORY_TYPE;

public class TSRMetricCalculator extends DecompositionMetricCalculator {
    public static final String TSR = "TSR"; //Team Size Reduction Ratio

    @Override
    public String getType() {
        return TSR;
    }

    @Override
    public Double calculateMetric(Decomposition decomposition) {
        RepositoryInformation repositoryInformation = (RepositoryInformation) decomposition.getRepresentationInformationByType(REPOSITORY_TYPE);
        double authorsPerClusterSum = 0;
        double cpm;

        for (Cluster cluster : decomposition.getClusters().values()) {
            Set<String> contributorsInThisCluster = new HashSet<>();
            for (Element element : cluster.getElements()) {
                List<String> authorsFromId = repositoryInformation.getAuthorsFromId(element.getId());
                if (authorsFromId != null)
                    contributorsInThisCluster.addAll(authorsFromId);
            }
            authorsPerClusterSum += contributorsInThisCluster.size();
        }
        cpm = authorsPerClusterSum / decomposition.getClusters().size();

        return BigDecimal.valueOf(cpm / repositoryInformation.getTotalAuthors())
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
