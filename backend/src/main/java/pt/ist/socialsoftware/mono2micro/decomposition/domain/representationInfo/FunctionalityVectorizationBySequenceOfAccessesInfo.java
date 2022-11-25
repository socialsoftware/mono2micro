package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInfo;

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
    @DBRef(lazy = true)
    private Map<String, Functionality> functionalities = new HashMap<>(); // <functionalityName, Functionality>

    public FunctionalityVectorizationBySequenceOfAccessesInfo() {}

    public Map<String, Functionality> getFunctionalities() {
        return functionalities;
    }

    public void setFunctionalities(Map<String, Functionality> functionalities) {
        this.functionalities = functionalities;
    }

    public Functionality getFunctionality(String functionalityName) {
        Functionality c = getFunctionalities().get(functionalityName.replaceAll("\\.", "_"));

        if (c == null) throw new Error("Functionality with name: " + functionalityName + " not found");

        return c;
    }

    public boolean functionalityExists(String functionalityName) {
        return getFunctionalities().containsKey(functionalityName.replaceAll("\\.", "_"));
    }

    public void addFunctionality(Functionality functionality) {
        getFunctionalities().put(functionality.getName().replaceAll("\\.", "_"), functionality);
    }

    @Override
    public void setup(Decomposition decomposition) throws Exception {
        this.decompositionName = decomposition.getName();
        decomposition.addRepresentationInformation(this);
    }

    @Override
    public void snapshot(Decomposition snapshotDecomposition, Decomposition decomposition) throws IOException {
        this.decompositionName = snapshotDecomposition.getName();
        snapshotDecomposition.addRepresentationInformation(this);
        FunctionalityVectorizationBySequenceOfAccessesInfo accessesInfo = (FunctionalityVectorizationBySequenceOfAccessesInfo) decomposition.getRepresentationInformationByType(FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO);
    }

    @Override
    public String getType() {
        return FUNCTIONALITY_VECTORIZATION_ACCESSES_INFO;
    }

    @Override
    public void update(Decomposition decomposition) throws Exception {
    }

    @Override
    public List<DecompositionMetric> getDecompositionMetrics() {
        return new ArrayList<DecompositionMetric>() {{
            add(new CohesionMetric());
            add(new ComplexityMetric());
            add(new CouplingMetric());
            add(new PerformanceMetric());
        }};
    }

    @Override
    public List<String> getParameters() {
        return new ArrayList<String>() {{}};
    }

    @Override
    public void deleteProperties() {
    }

    @Override
    public String getEdgeWeights(Decomposition decomposition) throws JSONException, IOException {

        JSONArray filteredEdgesJSON = new JSONArray();

        return filteredEdgesJSON.toString();
    }

    public String edgeId(int node1, int node2) {
        if (node1 < node2)
            return node1 + "&" + node2;
        return node2 + "&" + node1;
    }

    @Override
    public String getSearchItems(Decomposition decomposition) throws JSONException {
        JSONArray searchItems = new JSONArray();

        for (Cluster cluster : decomposition.getClusters().values()) {
            JSONObject clusterItem = new JSONObject();
            clusterItem.put("name", cluster.getName());
            clusterItem.put("type", "Cluster");
            clusterItem.put("id", cluster.getName());
            clusterItem.put("entities", Integer.toString(cluster.getElements().size()));
            searchItems.put(clusterItem);

            for (Element element : cluster.getElements()) {
                JSONObject entityItem = new JSONObject();
                entityItem.put("name", element.getName());
                entityItem.put("type", "Entity");
                entityItem.put("id", String.valueOf(element.getId()));
                entityItem.put("cluster", cluster.getName());
                searchItems.put(entityItem);
            }
        }

        for (Functionality functionality : getFunctionalities().values()) {
            JSONObject functionalityItem = new JSONObject();
            functionalityItem.put("name", functionality.getName());
            functionalityItem.put("type", "Functionality");
            functionalityItem.put("id", functionality.getName());
            functionalityItem.put("entities", Integer.toString(functionality.getEntities().size()));
            functionalityItem.put("funcType", functionality.getType().toString());
            searchItems.put(functionalityItem);
        }

        return searchItems.toString();
    }

    @Override
    public void renameClusterInFunctionalities(String clusterName, String newName) {

    }

    @Override
    public void removeFunctionalitiesWithEntityIDs(Decomposition decomposition, Set<Short> elements) {

    }

}
