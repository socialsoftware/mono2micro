package pt.ist.socialsoftware.mono2micro.similarityGenerator.utils;

import java.util.*;

public class RepositoryUtils {

    public static void fillRawMatrix(
            float[][][] rawMatrix,
            Set<Short> entities,
            int fillFromIndex,
            HashMap<String, Map<String, Integer>> commitChanges,
            HashMap<Short, ArrayList<String>> authorChanges
    ) {
        int i = 0;
        for (short e1ID : entities) {
            int j = 0;

            for (short e2ID : entities) {
                if (e1ID == e2ID) {
                    for (int k = fillFromIndex; k < fillFromIndex + 2; k++)
                        rawMatrix[i][j][k] = 1;
                    j++;
                    continue;
                }

                float[] metrics = calculateSimilarityMatrixCommitMetrics(e1ID, e2ID, commitChanges, authorChanges);

                for (int k = fillFromIndex, l = 0; k < fillFromIndex + 2; k++, l++)
                    rawMatrix[i][j][k] = metrics[l];
                j++;
            }
            i++;
        }
    }

    public static float[] calculateSimilarityMatrixCommitMetrics(
            short e1ID, short e2ID,
            HashMap<String, Map<String, Integer>> commitChanges,
            HashMap<Short, ArrayList<String>> authorChanges
    ) {

        float commitMetricValue = 0;
        if (commitChanges.containsKey(String.valueOf(e1ID)))
            if (commitChanges.get(String.valueOf(e1ID)).containsKey(String.valueOf(e2ID)))
                commitMetricValue = (float) commitChanges.get(String.valueOf(e1ID)).get(String.valueOf(e2ID)) /
                        commitChanges.get(String.valueOf(e1ID)).get("total_commits");

        float authorMetricValue;
        try {
            authorMetricValue = (float) authorChanges.get(e1ID).stream().filter(authorChanges.get(e2ID)::contains).count() / (long) authorChanges.get(e1ID).size();
        } catch (NullPointerException e) {
            authorMetricValue = 0;
        }
        return new float[] { authorMetricValue, commitMetricValue };
    }
}
