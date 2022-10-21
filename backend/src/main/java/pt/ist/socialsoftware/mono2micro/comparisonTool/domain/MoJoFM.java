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

public class MoJoFM extends Analysis {
    public static final String MOJO = "MOJO";
    private int truePositive;
    private int trueNegative;
    private int falsePositive;
    private int falseNegative;
    private List<String[]> falsePairs = new ArrayList<>();
    private float accuracy;
    private float precision;
    private float recall;
    private float specificity;
    private float fmeasure;
    private double mojoCommon;
    private double mojoBiggest;
    private double mojoNew;
    private double mojoSingletons;

    @Override
    public String getType() {
        return MOJO;
    }

    public MoJoFM(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        analyse(decomposition1, decomposition2);
    }

    public float getPrecision() {
        return precision;
    }

    public float getSpecificity() {
        return specificity;
    }

    public void setSpecificity(float specificity) {
        this.specificity = specificity;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public List<String[]> getFalsePairs() {
        return falsePairs;
    }

    public void setFalsePairs(List<String[]> falsePairs) {
        this.falsePairs = falsePairs;
    }

    public void addFalsePair(String[] falsePair) {
        this.falsePairs.add(falsePair);
    }

    public int getFalseNegative() {
        return falseNegative;
    }

    public void setFalseNegative(int falseNegative) {
        this.falseNegative = falseNegative;
    }

    public int getFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(int falsePositive) {
        this.falsePositive = falsePositive;
    }

    public int getTrueNegative() {
        return trueNegative;
    }

    public void setTrueNegative(int trueNegative) {
        this.trueNegative = trueNegative;
    }

    public int getTruePositive() {
        return truePositive;
    }

    public void setTruePositive(int truePositive) {
        this.truePositive = truePositive;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public float getRecall() {
        return recall;
    }

    public void setRecall(float recall) {
        this.recall = recall;
    }

    public float getFmeasure() {
        return fmeasure;
    }

    public void setFmeasure(float fmeasure) {
        this.fmeasure = fmeasure;
    }

    public double getMojoCommon() {
        return mojoCommon;
    }

    public void setMojoCommon(double mojoValue) {
        this.mojoCommon = mojoValue;
    }

    public double getMojoBiggest() {
        return mojoBiggest;
    }

    public void setMojoBiggest(double mojoBiggest) {
        this.mojoBiggest = mojoBiggest;
    }

    public double getMojoNew() {
        return mojoNew;
    }

    public void setMojoNew(double mojoNew) {
        this.mojoNew = mojoNew;
    }

    public double getMojoSingletons() {
        return mojoSingletons;
    }

    public void setMojoSingletons(double mojoSingletons) {
        this.mojoSingletons = mojoSingletons;
    }
    @Override
    public void analyse(Decomposition decomposition1, Decomposition decomposition2) throws IOException {
        Map<String, Set<Short>> decomposition1ClusterEntities = new HashMap<>();
        for (Cluster cluster : decomposition1.getClusters().values()) {
            decomposition1ClusterEntities.put(cluster.getName(), cluster.getElementsIDs());
        }

        Map<String, Set<Short>> decomposition2_CommonEntitiesOnly = new HashMap<>();
        for (Cluster cluster : decomposition2.getClusters().values()) {
            decomposition2_CommonEntitiesOnly.put(cluster.getName(), cluster.getElementsIDs());
        }

        List<Short> entities = new ArrayList<>();
        List<Short> notSharedEntities = new ArrayList<>();

        for (Set<Short> l1 : decomposition1ClusterEntities.values()) {
            for (short e1ID : l1) {
                boolean inBoth = false;

                for (Set<Short> l2 : decomposition2_CommonEntitiesOnly.values()) {
                    if (l2.contains(e1ID)) {
                        inBoth = true;
                        break;
                    }
                }

                if (inBoth)
                    entities.add(e1ID);
                else {
                    notSharedEntities.add(e1ID);
                }
            }
        }

        // ------------------------------------------------------------------------------------------
        Map<String, Set<Short>> decomposition2_UnassignedInBigger = decompositionCopyOf(decomposition2_CommonEntitiesOnly);
        Map.Entry<String, Set<Short>> biggerClusterEntry = null;

        for (Map.Entry<String, Set<Short>> clusterEntry : decomposition2_UnassignedInBigger.entrySet()) {
            if (biggerClusterEntry == null)
                biggerClusterEntry = clusterEntry;

            else if (clusterEntry.getValue().size() > biggerClusterEntry.getValue().size())
                biggerClusterEntry = clusterEntry;
        }

        biggerClusterEntry.getValue().addAll(notSharedEntities);

        // ------------------------------------------------------------------------------------------
        Map<String, Set<Short>> decomposition2_UnassignedInNew = decompositionCopyOf(decomposition2_CommonEntitiesOnly);
        Set<Short> newClusterForUnassignedEntities = new HashSet<>(notSharedEntities);
        decomposition2_UnassignedInNew.put("newClusterForUnnasignedEntities", newClusterForUnassignedEntities);

        // ------------------------------------------------------------------------------------------
        Map<String, Set<Short>> decomposition2_UnassignedInSingletons = decompositionCopyOf(decomposition2_CommonEntitiesOnly);
        for (int i = 0; i < notSharedEntities.size(); i++) {
            Set<Short> clusterSingletonEntity = new HashSet<>();
            clusterSingletonEntity.add(notSharedEntities.get(i));
            decomposition2_UnassignedInSingletons.put("singletonCluster" + i, clusterSingletonEntity);
        }

        for (int i = 0; i < entities.size(); i++) {
            for (int j = i+1; j < entities.size(); j++) {
                short e1ID = entities.get(i);
                short e2ID = entities.get(j);

                String e1ClusterG1 = "";
                String e2ClusterG1 = "";
                String e1ClusterG2 = "";
                String e2ClusterG2 = "";

                for (String cluster : decomposition1ClusterEntities.keySet()) {
                    if (decomposition1ClusterEntities.get(cluster).contains(e1ID)) {
                        e1ClusterG1 = cluster;
                    }
                    if (decomposition1ClusterEntities.get(cluster).contains(e2ID)) {
                        e2ClusterG1 = cluster;
                    }
                }

                for (String cluster : decomposition2_CommonEntitiesOnly.keySet()) {
                    if (decomposition2_CommonEntitiesOnly.get(cluster).contains(e1ID)) {
                        e1ClusterG2 = cluster;
                    }
                    if (decomposition2_CommonEntitiesOnly.get(cluster).contains(e2ID)) {
                        e2ClusterG2 = cluster;
                    }
                }

                boolean sameClusterInGraph1 = false;
                if (e1ClusterG1.equals(e2ClusterG1))
                    sameClusterInGraph1 = true;

                boolean sameClusterInGraph2 = false;
                if (e1ClusterG2.equals(e2ClusterG2))
                    sameClusterInGraph2 = true;

                if (sameClusterInGraph1 && sameClusterInGraph2)
                    truePositive++;
                if (sameClusterInGraph1 && !sameClusterInGraph2)
                    falseNegative++;
                if (!sameClusterInGraph1 && sameClusterInGraph2)
                    falsePositive++;
                if (!sameClusterInGraph1 && !sameClusterInGraph2)
                    trueNegative++;

                if (sameClusterInGraph1 != sameClusterInGraph2) {
                    String[] falsePair = new String[6];
                    falsePair[0] = String.valueOf(e1ID);
                    falsePair[1] = e1ClusterG1;
                    falsePair[2] = e1ClusterG2;
                    falsePair[3] = String.valueOf(e2ID);
                    falsePair[4] = e2ClusterG1;
                    falsePair[5] = e2ClusterG2;

                    addFalsePair(falsePair);
                }
            }
        }

        if (truePositive == 0 && trueNegative == 0 && falsePositive == 0 && falseNegative == 0) { // no ExpertCut submitted
            accuracy = 0;
            precision = 0;
            recall = 0;
            specificity = 0;
            fmeasure = 0;
        }
        else {
            accuracy = (float)(truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative);
            accuracy = BigDecimal.valueOf(accuracy).setScale(2, RoundingMode.HALF_UP).floatValue();

            precision = (float)truePositive / (truePositive + falsePositive);
            precision = Float.isNaN(precision) ? -1 : BigDecimal.valueOf(precision).setScale(2, RoundingMode.HALF_UP).floatValue();

            recall = (float)truePositive / (truePositive + falseNegative);
            recall = BigDecimal.valueOf(recall).setScale(2, RoundingMode.HALF_UP).floatValue();

            specificity = (float)trueNegative / (trueNegative + falsePositive);
            specificity = Float.isNaN(specificity) ? -1 : BigDecimal.valueOf(specificity).setScale(2, RoundingMode.HALF_UP).floatValue();

            fmeasure = 2 * precision * recall / (precision + recall);
            fmeasure = Float.isNaN(precision) ? -1 : BigDecimal.valueOf(fmeasure).setScale(2, RoundingMode.HALF_UP).floatValue();
        }

        /*
         *******************************************
         ************ CALCULATE MOJO ***************
         *******************************************
         */
        mojoCommon = getMojoValue(
                decomposition2_CommonEntitiesOnly,
                decomposition1ClusterEntities,
                decomposition2_CommonEntitiesOnly.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
        );

        mojoBiggest = getMojoValue(
                decomposition2_UnassignedInBigger,
                decomposition1ClusterEntities,
                decomposition2_UnassignedInBigger.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
        );

        mojoNew = getMojoValue(
                decomposition2_UnassignedInNew,
                decomposition1ClusterEntities,
                decomposition2_UnassignedInNew.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
        );

        mojoSingletons = getMojoValue(
                decomposition2_UnassignedInSingletons,
                decomposition1ClusterEntities,
                decomposition2_UnassignedInSingletons.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())
        );
    }

    private static Map<String, Set<Short>> decompositionCopyOf(Map<String, Set<Short>> decomposition) {
        HashMap<String, Set<Short>> copy = new HashMap<>();

        for (Map.Entry<String, Set<Short>> entry : decomposition.entrySet())
            copy.put(
                    entry.getKey(),
                    new HashSet<>(entry.getValue())
            );

        return copy;
    }

    private static double getMojoValue(
            Map<String, Set<Short>> decomposition1ClusterEntities,
            Map<String, Set<Short>> decomposition2,
            Set<Short> entities
    )
            throws IOException
    {
        StringBuilder sbSource = new StringBuilder();
        for (Map.Entry<String, Set<Short>> clusterEntry : decomposition1ClusterEntities.entrySet()) {
            String clusterName = clusterEntry.getKey();
            Set<Short> clusterEntities = clusterEntry.getValue();

            for (short entityID : clusterEntities) {
                if (entities.contains(entityID)) { // entity present in both decompositions
                    sbSource.append("contain ")
                            .append(clusterName)
                            .append(" ")
                            .append(entityID)
                            .append("\n");
                }
            }
        }

        StringBuilder sbTarget = new StringBuilder();
        for (Map.Entry<String, Set<Short>> clusterEntry : decomposition2.entrySet()) {
            String clusterName = clusterEntry.getKey();
            Set<Short> clusterEntities = clusterEntry.getValue();

            for (short entityID : clusterEntities) {
                if (entities.contains(entityID)) { // entity present in both decompositions
                    sbTarget.append("contain ")
                            .append(clusterName)
                            .append(" ")
                            .append(entityID)
                            .append("\n");
                }
            }
        }

        String distrSrcPath = MOJO_RESOURCES_PATH + "distrSrc.rsf";
        String distrTargetPath = MOJO_RESOURCES_PATH + "distrTarget.rsf";

        FileWriter srcFileWriter = new FileWriter(new File(distrSrcPath));
        srcFileWriter.write(sbSource.toString());
        srcFileWriter.close();

        FileWriter targetFileWriter = new FileWriter(new File(distrTargetPath));
        targetFileWriter.write(sbTarget.toString());
        targetFileWriter.close();

        return new MoJo().executeMojo(new String[]{
                distrSrcPath,
                distrTargetPath,
                "-fm"
        });
    }
}
