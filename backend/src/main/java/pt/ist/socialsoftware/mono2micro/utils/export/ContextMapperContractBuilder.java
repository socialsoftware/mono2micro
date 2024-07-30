package pt.ist.socialsoftware.mono2micro.utils.export;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ContextMapperContractBuilder extends DecompositionContractBuilder {

    Map<Short, String> idToEntityMap;

    public ContextMapperContractBuilder(Decomposition decomposition) {
        super(decomposition);
        this.idToEntityMap = new HashMap<>();
    }

    @Override
    public String parseContractData() throws JSONException {
        JSONObject contractAsJSON = new JSONObject();

        contractAsJSON.put("name", decomposition.getStrategy().getCodebase().getName());
        contractAsJSON.put("clusters", createClusterList(decomposition.getClusters().values()));
        contractAsJSON.put("functionalities", createFunctionalitiesList(
                sagaRefactorizationAsJSON.getJSONObject("functionalities")));
        contractAsJSON.put("entities", structureRepresentationAsJSON.getJSONArray("entities"));
        return contractAsJSON.toString(2);
    }

    @Override
    protected boolean isMissingContractData() {
        return structureRepresentationAsJSON == null || sagaRefactorizationAsJSON == null;
    }

    private JSONArray createClusterList(Collection<Cluster> clustersData) throws JSONException {
        JSONArray clusterListAsJSON = new JSONArray();

        for (Cluster cluster : clustersData) {
            JSONObject clusterAsJSON = new JSONObject();
            clusterAsJSON.put("name", cluster.getName());
            clusterAsJSON.put("elements", createClusterElementList(cluster.getElements()));
            clusterListAsJSON.put(clusterAsJSON);
        }
        return clusterListAsJSON;
    }

    private JSONArray createClusterElementList(Collection<Element> elementsData) throws JSONException {
        JSONArray elementListAsJSON = new JSONArray();

        for (Element element : elementsData) {
            JSONObject elementAsJSON = new JSONObject();
            elementAsJSON.put("name", element.getName());
            elementListAsJSON.put(elementAsJSON);
            idToEntityMap.put(element.getId(), element.getName());
        }
        return elementListAsJSON;
    }

    private JSONArray createFunctionalitiesList(JSONObject sagaFunctionalitiesData) throws JSONException {
        JSONArray functionalityListAsJSON = new JSONArray();

        Iterator<?> functionalityIterator = sagaFunctionalitiesData.keys();
        while (functionalityIterator.hasNext()) {
            functionalityListAsJSON.put(createSagaFunctionality(
                    sagaFunctionalitiesData.getJSONObject((String) functionalityIterator.next())));
        }
        return functionalityListAsJSON;
    }

    private JSONObject createSagaFunctionality(JSONObject sagaFunctionalityData) throws JSONException {
        JSONObject functionalityAsJSON = new JSONObject();

        functionalityAsJSON.put("name", sagaFunctionalityData.getString("name"));
        functionalityAsJSON.put("orchestrator", sagaFunctionalityData
                .getJSONObject("refactor").getJSONObject("orchestrator").getString("name"));
        functionalityAsJSON.put("steps", createSagaFunctionalityStepList(sagaFunctionalityData
                .getJSONObject("refactor").getJSONArray("call_graph")));
        return functionalityAsJSON;
    }

    private JSONArray createSagaFunctionalityStepList(JSONArray callGraphListData) throws JSONException {
        JSONArray sagaFunctionalityStepListAsJSON = new JSONArray();

        for (int i = 0; i < callGraphListData.length(); i++) {
            JSONObject sagaFunctionalityStepAsJSON = new JSONObject();
            sagaFunctionalityStepAsJSON.put("cluster", callGraphListData.getJSONObject(i).getString("cluster_name"));
            sagaFunctionalityStepAsJSON.put("accesses", createSagaFunctionalityStepAccessList(callGraphListData
                    .getJSONObject(i).getJSONArray("accesses")));
            sagaFunctionalityStepListAsJSON.put(sagaFunctionalityStepAsJSON);
        }
        return sagaFunctionalityStepListAsJSON;
    }

    private JSONArray createSagaFunctionalityStepAccessList(JSONArray accessListData) throws JSONException {
        JSONArray accessListAsJSON = new JSONArray();

        for (int i = 0; i < accessListData.length(); i++) {
            JSONObject accessAsJSON = new JSONObject();
            accessAsJSON.put("entity", idToEntityMap.get(Short.parseShort(accessListData.getJSONObject(i).getString("entity_id"))));
            accessAsJSON.put("type", accessListData.getJSONObject(i).getString("type"));
            accessListAsJSON.put(accessAsJSON);
        }
        return accessListAsJSON;
    }
}
