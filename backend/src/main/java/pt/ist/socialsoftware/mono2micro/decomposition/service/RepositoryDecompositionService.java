package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.RepositoryDecomposition;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RepositoryDecompositionService {
    @Autowired
    GridFsService gridFsService;

    public String getEdgeWeights(RepositoryDecomposition decomposition) throws JSONException, IOException {
        Dendrogram similarity = (Dendrogram) decomposition.getSimilarity();
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(similarity.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entities = new ArrayList<>(decomposition.getEntityIDToClusterName().keySet());

        JSONArray edgesJSON = new JSONArray();
        int k = 0;
        for (int i = 0; i < entities.size(); i++) {
            short e1ID = entities.get(i);
            Map<Short, Integer> commitsInCommon = decomposition.getCommitsInCommon().get(e1ID);
            if (commitsInCommon == null)
                continue;

            for (int j = i + 1; j < entities.size(); j++) {
                short e2ID = entities.get(j);
                Integer numberOfCommits = commitsInCommon.get(e2ID);
                if (numberOfCommits == null)
                    continue;

                JSONObject edgeJSON = new JSONObject();
                if (e1ID < e2ID) {
                    edgeJSON.put("e1ID", e1ID); edgeJSON.put("e2ID", e2ID);
                }
                else {
                    edgeJSON.put("e1ID", e2ID); edgeJSON.put("e1ID", e2ID);
                }
                edgeJSON.put("dist", copheneticDistances.getDouble(k));

                // Since the frontend counts the length of the array, numbers are added to count as independent commits
                JSONArray commitJSON = new JSONArray();
                for (int l = 0; l < numberOfCommits; l++)
                    commitJSON.put(l);
                edgeJSON.put("commits", commitJSON);

                edgesJSON.put(edgeJSON);
                k++;
            }
        }
        return edgesJSON.toString();
    }
}
