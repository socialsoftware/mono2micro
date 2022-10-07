package pt.ist.socialsoftware.mono2micro.metrics.metricService;

import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CohesionMetricService {
    public Double calculateMetric(AccessesDecomposition decomposition, Map<String, List<Functionality>> clustersFunctionalities) {
        double totalCohesion = 0;

        for (Cluster c : decomposition.getClusters().values()) {
            SciPyCluster cluster = (SciPyCluster) c;
            List<Functionality> FunctionalitiesThatAccessThisCluster = clustersFunctionalities.get(cluster.getName());

            double clusterCohesion = 0;

            for (Functionality functionality : FunctionalitiesThatAccessThisCluster) {
                double numberEntitiesTouched = 0;

                Set<Short> functionalityEntities = functionality.getEntities().keySet();

                for (short entityID : functionalityEntities)
                    if (cluster.containsElement(entityID))
                        numberEntitiesTouched++;

                clusterCohesion += numberEntitiesTouched / cluster.getElements().size();
            }

            clusterCohesion /= FunctionalitiesThatAccessThisCluster.size();
            clusterCohesion = BigDecimal.valueOf(clusterCohesion).setScale(2, RoundingMode.HALF_UP).doubleValue();
            cluster.setCohesion(clusterCohesion);
            totalCohesion += clusterCohesion;
        }

        int graphClustersAmount = decomposition.getClusters().size();

        return BigDecimal.valueOf(totalCohesion / graphClustersAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}