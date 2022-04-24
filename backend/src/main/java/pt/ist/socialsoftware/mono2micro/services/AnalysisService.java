package pt.ist.socialsoftware.mono2micro.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.dto.AnalyserDto;
import pt.ist.socialsoftware.mono2micro.manager.CodebaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.CODEBASES_PATH;

@Service
public class AnalysisService {

    @Autowired
    DendrogramService dendrogramService;

    @Autowired
    ClusterService clusterService;

    private final CodebaseManager codebaseManager = CodebaseManager.getInstance();

    private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final Integer MIN_DEPTH = 3;
    private final Integer MAX_DEPTH = 4;
    private final Integer MIN_WEIGHT = 0;
    private final Integer MAX_WEIGHT = 100;
    private final Integer WEIGHT_STEP = 50;
    private final Integer DEPTH_STEP = 1;

    public void featureMethodCallsAnalysis(
            String codebaseName,
            AnalyserDto analyserDto
    ) {
        try {

            JSONObject analyserResult = new JSONObject();
            Codebase codebase = codebaseManager.getCodebase(codebaseName);
            Dendrogram dendrogram = new Dendrogram();
            dendrogram.setAnalysisType("feature");
            dendrogram.setFeatureVectorizationStrategy("methodCalls");
            dendrogram.setProfile(analyserDto.getProfile());
            dendrogram.setLinkageType(analyserDto.getLinkageType());

            File analyserFeaturesPath = new File(CODEBASES_PATH + codebaseName + "/analyser/features/");
            if (!analyserFeaturesPath.exists()) {
                analyserFeaturesPath.mkdirs();
            }

            File analyserMethodCallsFeaturesPath = new File(CODEBASES_PATH + codebaseName + "/analyser/features/methodCalls/");
            if (!analyserMethodCallsFeaturesPath.exists()) {
                analyserMethodCallsFeaturesPath.mkdirs();
            }

            File analyserPath = new File(CODEBASES_PATH + codebaseName + "/analyser/features/methodCalls/cuts");
            if (!analyserPath.exists()) {
                analyserPath.mkdirs();
            }

            for (int d = MIN_DEPTH; d <= MAX_DEPTH; d += DEPTH_STEP) {
                dendrogram.setMaxDepth(d);
                for (int cw = MIN_WEIGHT; cw <= MAX_WEIGHT; cw += WEIGHT_STEP) {
                    dendrogram.setControllersWeight(cw);
                    for (int sw = MIN_WEIGHT; sw <= MAX_WEIGHT; sw += WEIGHT_STEP) {
                        dendrogram.setServicesWeight(sw);
                        for (int iw = MIN_WEIGHT; iw <= MAX_WEIGHT; iw += WEIGHT_STEP) {
                            dendrogram.setIntermediateMethodsWeight(iw);
                            for (int ew = MIN_WEIGHT; ew <= MAX_WEIGHT; ew += WEIGHT_STEP) {
                                dendrogram.setEntitiesWeight(ew);
                                dendrogramService.createDendrogramByFeatures(codebaseName, dendrogram);
                                clusterService.executeClusterAnalysis(codebaseName);
                            }
                        }
                    }
                }
            }

            File analyserCutsPath = new File(CODEBASES_PATH + codebaseName + "/analyser/features/methodCalls/cuts/");
            File[] files = analyserCutsPath.listFiles();

            for (File file : files) {

                String filename = FilenameUtils.getBaseName(file.getName());

                Decomposition cutDecomposition = new Decomposition();

                InputStream is = new FileInputStream(analyserCutsPath + "/" + filename + ".json");
                JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
                is.close();

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
                        cutDecomposition.putEntity(entityID, id);
                    }

                    cutDecomposition.addCluster(cluster);
                }

                cutDecomposition.setNextClusterID(Integer.valueOf(clusterIds.size()).shortValue());

                cutDecomposition.setControllers(codebaseManager.getControllersWithCostlyAccesses(
                        codebase,
                        dendrogram.getProfile(),
                        cutDecomposition.getEntityIDToClusterID()
                ));

                cutDecomposition.calculateMetrics(
                        codebase,
                        analyserDto.getTracesMaxLimit(),
                        analyserDto.getTraceType(),
                        true
                );

                String[] weights = filename.split(",");
                Float maxDepth = Float.parseFloat(weights[0]);
                Float controllersWeight = Float.parseFloat(weights[1]);
                Float servicesWeight = Float.parseFloat(weights[2]);
                Float intermediateMethodsWeight = Float.parseFloat(weights[3]);
                Float entitiesWeight = Float.parseFloat(weights[4]);
                Integer clusterSize = Integer.parseInt(weights[5]);
                JSONObject metrics = new JSONObject();
                metrics.put("maxDepth", maxDepth);
                metrics.put("controllersWeight", controllersWeight);
                metrics.put("servicesWeight", servicesWeight);
                metrics.put("intermediateMethodsWeight", intermediateMethodsWeight);
                metrics.put("entitiesWeight", entitiesWeight);
                metrics.put("numberClusters", clusterSize);
                metrics.put("cohesion", cutDecomposition.getCohesion());
                metrics.put("coupling", cutDecomposition.getCoupling());
                metrics.put("complexity", cutDecomposition.getComplexity());
                metrics.put("performance", cutDecomposition.getPerformance());
                metrics.put("accuracy", 0.0);
                metrics.put("precision", 0.0);
                metrics.put("recall", 0.0);
                metrics.put("specificity", 0.0);
                metrics.put("fmeasure", 0.0);
                metrics.put("mojoCommon", 0.0);
                metrics.put("mojoBiggest", 0.0);
                metrics.put("mojoNew", 0.0);
                metrics.put("mojoSingletons", 0.0);
                metrics.put("controllerSpecs", new JSONObject());

                analyserResult.put(filename, metrics);

            }

            FileWriter file = new FileWriter(CODEBASES_PATH + codebaseName + "/analyser/features/methodCalls/" + "analyserResult.json");
            file.write(analyserResult.toString(4));
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
