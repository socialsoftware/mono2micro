package pt.ist.socialsoftware.mono2micro.comparisonTool.domain;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.utils.mojoCalculator.src.main.java.MoJo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.MOJO_RESOURCES_PATH;

public class Purity extends Analysis {
    public static final String PURITY = "PURITY";
    private float purity;


    @Override
    public String getType() {
        return PURITY;
    }

    public Purity(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        analyse(decomposition1, decomposition2);
    }

    public float getPurity() { return purity; }

    public void setPurity(float purity) { this.purity = purity; }

    @Override
    public void analyse(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        Map<String, Set<Short>> decomposition1ClusterEntities = decomposition1.getClusters().values().stream()
                .collect(Collectors.toMap(Cluster::getName, Cluster::getElementsIDs));

        Map<String, Set<Short>> decomposition2ClusterEntities = decomposition2.getClusters().values().stream()
                .collect(Collectors.toMap(Cluster::getName, Cluster::getElementsIDs));

        int totalEntities = 0;
        float totalPurity = 0;

        for (Set<Short> proposedEntities : decomposition2ClusterEntities.values()) {
            int maxOverlap = 0;

            for (Set<Short> expertEntities : decomposition1ClusterEntities.values()) {
                Set<Short> commonEntities = new HashSet<>(proposedEntities);
                commonEntities.retainAll(expertEntities);
                maxOverlap = Math.max(maxOverlap, commonEntities.size());
            }

            totalPurity += maxOverlap;
            totalEntities += proposedEntities.size();
        }

        this.purity = totalEntities == 0 ? 0 : totalPurity / totalEntities;
    }


}
