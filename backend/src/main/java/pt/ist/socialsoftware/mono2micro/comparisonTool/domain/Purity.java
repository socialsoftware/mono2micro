package pt.ist.socialsoftware.mono2micro.comparisonTool.domain;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Purity extends Analysis {
    public static final String PURITY = "PURITY";
    private float purity;
    private Map<String, Float> clusterPurityMap;
    private Map<String, String> clusterMapping;

    @Override
    public String getType() {
        return PURITY;
    }

    public Purity(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        analyse(decomposition1, decomposition2);
    }

    public float getPurity() { return purity; }

    public Map<String, Float> getClusterPurityMap() { return clusterPurityMap; }

    public Map<String, String> getClusterMapping() { return clusterMapping; }

    @Override
    public void analyse(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        Map<String, Set<Short>> expertDecompositionClusterEntities = decomposition1.getClusters().values().stream()
                .collect(Collectors.toMap(Cluster::getName, Cluster::getElementsIDs));

        Map<String, Set<Short>> proposedDecompositionClusterEntities = decomposition2.getClusters().values().stream()
                .collect(Collectors.toMap(Cluster::getName, Cluster::getElementsIDs));

        int totalEntities = 0;
        float totalPurity = 0;
        clusterPurityMap = new HashMap<>();
        clusterMapping = new HashMap<>();

        for (Map.Entry<String, Set<Short>> proposedEntry : proposedDecompositionClusterEntities.entrySet()) {
            String proposedClusterName = proposedEntry.getKey();
            Set<Short> proposedEntities = proposedEntry.getValue();
            int maxOverlap = 0;
            String bestMatchingExpertCluster = null;

            for (Map.Entry<String, Set<Short>> expertEntry : expertDecompositionClusterEntities.entrySet()) {
                Set<Short> expertEntities = expertEntry.getValue();
                Set<Short> commonEntities = new HashSet<>(proposedEntities);
                commonEntities.retainAll(expertEntities);
                int overlapSize = commonEntities.size();

                if (overlapSize > maxOverlap) {
                    maxOverlap = overlapSize;
                    bestMatchingExpertCluster = expertEntry.getKey();
                }
            }

            float clusterPurity = proposedEntities.size() == 0 ? 0 : (float) maxOverlap / proposedEntities.size();
            clusterPurityMap.put(proposedClusterName, clusterPurity);
            clusterMapping.put(proposedClusterName, bestMatchingExpertCluster);

            totalPurity += maxOverlap;
            totalEntities += proposedEntities.size();
        }

        this.purity = totalEntities == 0 ? 0 : totalPurity / totalEntities;
    }
}
