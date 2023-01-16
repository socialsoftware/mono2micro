package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;

import java.io.IOException;
import java.util.*;

public class ClassVectorizationInfo extends RepresentationInfo {
    public static final String CLASS_VECTORIZATION_INFO = "Class Vectorization";

    public ClassVectorizationInfo() {}

    @Override
    public String getType() {
        return CLASS_VECTORIZATION_INFO;
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

}
