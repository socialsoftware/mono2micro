package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;

import java.io.IOException;
import java.util.*;

public class FunctionalityVectorizationBySequenceOfAccessesInfo extends RepresentationInfo {
    public static final String FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO = "Functionality Vectorization by Sequence of Accesses";

    public FunctionalityVectorizationBySequenceOfAccessesInfo() {}

    @Override
    public String getType() {
        return FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO;
    }

    @Override
    public List<String> getParameters() {
        return new ArrayList<String>() {{}};
    }

    @Override
    public void deleteProperties() {}

    /* ------------------------- NOT USED -------------------------------- */

    @Override
    public void setup(Decomposition decomposition) throws Exception {
        this.decompositionName = decomposition.getName();
        decomposition.addRepresentationInformation(this);
    }

    @Override
    public List<DecompositionMetric> getDecompositionMetrics() {
        return new ArrayList<DecompositionMetric>() {{}};
    }

    @Override
    public void snapshot(Decomposition snapshotDecomposition, Decomposition decomposition) throws IOException {
        throw new NotImplementedException("Not used");
    }

    @Override
    public void update(Decomposition decomposition) throws Exception {
        throw new NotImplementedException("Not used");
    }

    @Override
    public String getEdgeWeights(Decomposition decomposition) throws JSONException, IOException {
        throw new NotImplementedException("Not used");
    }

    @Override
    public String getSearchItems(Decomposition decomposition) throws JSONException {
        throw new NotImplementedException("Not used");
    }

    @Override
    public void renameClusterInFunctionalities(String clusterName, String newName) {
        throw new NotImplementedException("Not used");
    }

    @Override
    public void removeFunctionalitiesWithEntityIDs(Decomposition decomposition, Set<Short> elements) {
        throw new NotImplementedException("Not used");
    }

}
