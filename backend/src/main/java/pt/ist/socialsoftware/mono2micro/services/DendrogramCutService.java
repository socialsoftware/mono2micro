package pt.ist.socialsoftware.mono2micro.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

@Service
public class DendrogramCutService {

    @Autowired
    ClusterService clusterService;

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

    public Decomposition cutClassesAnalysis(Dendrogram dendrogram, Decomposition decomposition)
            throws Exception
    {

        String cutValue = Float.valueOf(decomposition.getCutValue()).toString().replaceAll("\\.?0*$", "");
        if (dendrogram.getDecompositionNames().contains(decomposition.getCutType() + cutValue)) {
            int i = 2;
            while (dendrogram.getDecompositionNames().contains(decomposition.getCutType() + cutValue + "(" + i + ")")) {
                i++;
            }
            decomposition.setName(decomposition.getCutType() + cutValue + "(" + i + ")");
        } else {
            decomposition.setName(decomposition.getCutType() + cutValue);
        }

        File decompositionPath = new File(CODEBASES_PATH + dendrogram.getCodebaseName() + "/" + dendrogram.getName() + "/" + decomposition.getName());
        if (!decompositionPath.exists()) {
            decompositionPath.mkdir();
        }

        clusterService.executeCutDendrogram(
            dendrogram.getCodebaseName(),
            dendrogram.getName(),
            decomposition.getName(),
            decomposition.getCutType(),
            Float.toString(decomposition.getCutValue()),
            "/classes"
        );

        JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(
                dendrogram.getCodebaseName(),
                dendrogram.getName(),
                decomposition.getName()
        );

        decomposition.setSilhouetteScore((float) clustersJSON.getDouble("silhouetteScore"));

        Iterator<String> clusters = clustersJSON.getJSONObject("clusters").sortedKeys();
        ArrayList<Short> clusterIds = new ArrayList<>();

        while(clusters.hasNext()) {
            clusterIds.add(Short.parseShort(clusters.next()));
        }

        Collections.sort(clusterIds);

        for (Short id : clusterIds) {
            String clusterName = String.valueOf(id);
            JSONArray entities = clustersJSON.getJSONObject("clusters").getJSONArray(id.toString());
            Cluster cluster = new Cluster(id, clusterName);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addEntity(entityID);
                decomposition.putEntity(entityID, id);
            }

            decomposition.addCluster(cluster);
        }

        decomposition.setNextClusterID(Integer.valueOf(clusterIds.size()).shortValue());

        return decomposition;
    }

    public Decomposition cutEntitiesAnalysis(Dendrogram dendrogram, Decomposition decomposition)
            throws Exception
    {

        String cutValue = Float.valueOf(decomposition.getCutValue()).toString().replaceAll("\\.?0*$", "");
        if (dendrogram.getDecompositionNames().contains(decomposition.getCutType() + cutValue)) {
            int i = 2;
            while (dendrogram.getDecompositionNames().contains(decomposition.getCutType() + cutValue + "(" + i + ")")) {
                i++;
            }
            decomposition.setName(decomposition.getCutType() + cutValue + "(" + i + ")");
        } else {
            decomposition.setName(decomposition.getCutType() + cutValue);
        }

        File decompositionPath = new File(CODEBASES_PATH + dendrogram.getCodebaseName() + "/" + dendrogram.getName() + "/" + decomposition.getName());
        if (!decompositionPath.exists()) {
            decompositionPath.mkdir();
        }

        clusterService.executeCutDendrogram(
                dendrogram.getCodebaseName(),
                dendrogram.getName(),
                decomposition.getName(),
                decomposition.getCutType(),
                Float.toString(decomposition.getCutValue()),
                "/entities"
        );

        JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(
                dendrogram.getCodebaseName(),
                dendrogram.getName(),
                decomposition.getName()
        );

        decomposition.setSilhouetteScore((float) clustersJSON.getDouble("silhouetteScore"));

        Iterator<String> clusters = clustersJSON.getJSONObject("clusters").sortedKeys();
        ArrayList<Short> clusterIds = new ArrayList<>();

        while(clusters.hasNext()) {
            clusterIds.add(Short.parseShort(clusters.next()));
        }

        Collections.sort(clusterIds);

        for (Short id : clusterIds) {
            String clusterName = String.valueOf(id);
            JSONArray entities = clustersJSON.getJSONObject("clusters").getJSONArray(id.toString());
            Cluster cluster = new Cluster(id, clusterName);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addEntity(entityID);
                decomposition.putEntity(entityID, id);
            }

            decomposition.addCluster(cluster);
        }

        decomposition.setNextClusterID(Integer.valueOf(clusterIds.size()).shortValue());

        return decomposition;
    }

    public Decomposition cutFeaturesAnalysis(Dendrogram dendrogram, Decomposition decomposition, String strategy)
            throws Exception
    {

        String cutValue = Float.valueOf(decomposition.getCutValue()).toString().replaceAll("\\.?0*$", "");
        if (dendrogram.getDecompositionNames().contains(decomposition.getCutType() + cutValue)) {
            int i = 2;
            while (dendrogram.getDecompositionNames().contains(decomposition.getCutType() + cutValue + "(" + i + ")")) {
                i++;
            }
            decomposition.setName(decomposition.getCutType() + cutValue + "(" + i + ")");
        } else {
            decomposition.setName(decomposition.getCutType() + cutValue);
        }

        File decompositionPath = new File(CODEBASES_PATH + dendrogram.getCodebaseName() + "/" + dendrogram.getName() + "/" + decomposition.getName());
        if (!decompositionPath.exists()) {
            decompositionPath.mkdir();
        }

        clusterService.executeCutDendrogram(
                dendrogram.getCodebaseName(),
                dendrogram.getName(),
                decomposition.getName(),
                decomposition.getCutType(),
                Float.toString(decomposition.getCutValue()),
                "/features/" + strategy
        );

        JSONObject clustersJSON = CodebaseManager.getInstance().getClusters(
                dendrogram.getCodebaseName(),
                dendrogram.getName(),
                decomposition.getName()
        );

        decomposition.setSilhouetteScore((float) clustersJSON.getDouble("silhouetteScore"));

        Iterator<String> clusters = clustersJSON.getJSONObject("clusters").sortedKeys();
        ArrayList<Short> clusterIds = new ArrayList<>();

        while(clusters.hasNext()) {
            clusterIds.add(Short.parseShort(clusters.next()));
        }

        Collections.sort(clusterIds);

        for (Short id : clusterIds) {
            String clusterName = String.valueOf(id);
            JSONArray entities = clustersJSON.getJSONObject("clusters").getJSONArray(id.toString());
            Cluster cluster = new Cluster(id, clusterName);

            for (int i = 0; i < entities.length(); i++) {
                short entityID = (short) entities.getInt(i);

                cluster.addEntity(entityID);
                decomposition.putEntity(entityID, id);
            }

            decomposition.addCluster(cluster);
        }

        decomposition.setNextClusterID(Integer.valueOf(clusterIds.size()).shortValue());

        return decomposition;
    }

}
