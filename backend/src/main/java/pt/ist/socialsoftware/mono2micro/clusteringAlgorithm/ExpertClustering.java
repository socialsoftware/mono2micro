package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.DefaultCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.DecompositionRequest;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.ExpertRequest;
import pt.ist.socialsoftware.mono2micro.element.DomainEntity;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpertClustering extends Clustering {
    public static final String EXPERT_CLUSTERING = "EXPERT_CLUSTERING";
    private final GridFsService gridFsService;

    public ExpertClustering() {
        this.gridFsService = ContextManager.get().getBean(GridFsService.class);
    }

    public String getType() {
        return EXPERT_CLUSTERING;
    }

    public void generateClusters(Decomposition decomposition, DecompositionRequest request) throws Exception {
        ExpertRequest dto = (ExpertRequest) request;
        Map<Short, String> idToEntity;

        SimilarityMatrixSciPy similarity = (SimilarityMatrixSciPy) decomposition.getSimilarity();
        List<String> decompositionNames = similarity.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(dto.getExpertName()))
            throw new KeyAlreadyExistsException();
        decomposition.setName(similarity.getName() + " " + dto.getExpertName());
        decomposition.setExpert(true);

        idToEntity = similarity.getIDToEntityName(gridFsService);

        if (dto.getExpertFile().isPresent()) { // Expert decomposition with file
            InputStream is = new BufferedInputStream(dto.getExpertFile().get().getInputStream());
            JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8)).getJSONObject("clusters");
            SciPyClustering.addClustersAndEntities(decomposition, clustersJSON, idToEntity);
            is.close();
        }
        else createGenericDecomposition(similarity.getSimilarityMatrix().getName(), decomposition.getClusters(), idToEntity);
    }

    private void createGenericDecomposition(
            String similarityMatrixName,
            Map<String, Cluster> clusters,
            Map<Short, String> idToEntity
    ) throws Exception {
        DefaultCluster cluster = new DefaultCluster("Generic");

        JSONObject similarityMatrixData = new JSONObject(gridFsService.getFile(similarityMatrixName));

        JSONArray entities = similarityMatrixData.getJSONArray("entities");

        for (int i = 0; i < entities.length(); i++) {
            short entityID = (short) entities.getInt(i);

            cluster.addElement(new DomainEntity(entityID, idToEntity.get(entityID)));
        }
        clusters.putIfAbsent(cluster.getName(), cluster);
    }
}
