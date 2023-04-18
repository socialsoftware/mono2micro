package pt.ist.socialsoftware.mono2micro.clusteringAlgorithm;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.PartitionsDecomposition;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Expert {
    public static final String EXPERT = "Expert Clustering";

    public Expert() {}

    public String getType() {
        return EXPERT;
    }

    public Decomposition generateClusters(Similarity similarity, String expertName, Optional<MultipartFile> expertFile) throws Exception {
        Decomposition decomposition = new PartitionsDecomposition(similarity);
        decomposition.setExpert(true);

        Map<Short, String> idToEntity;

        List<String> decompositionNames = similarity.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());

        if (decompositionNames.contains(expertName))
            throw new KeyAlreadyExistsException();
        decomposition.setName(similarity.getName() + " " + expertName);
        decomposition.setExpert(true);

        idToEntity = similarity.getIDToEntityName();

        InputStream is = new BufferedInputStream(expertFile.get().getInputStream());
        JSONObject clustersJSON = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8)).getJSONObject("clusters");
        SciPyClustering.addClustersAndEntities(decomposition, clustersJSON, idToEntity);
        is.close();

        return decomposition;
    }
}
