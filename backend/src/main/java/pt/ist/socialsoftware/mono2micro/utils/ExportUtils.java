package pt.ist.socialsoftware.mono2micro.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportUtils {

    public static void zipSerializedData(String entryName, String serializedData, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        zipOutputStream.write(serializedData.getBytes());
        zipOutputStream.closeEntry();
    }

    public static String serializeDecompositionDataForContextMapper(
            Decomposition decomposition,
            byte[] refactorizationData,
            byte[] structureData) throws JSONException
    {
        Map<Short, String> idToEntityMap = new HashMap<>();

        JSONObject refactorizationObject = new JSONObject(new String(refactorizationData));
        JSONObject structureObject = new JSONObject(new String(structureData));
        JSONArray clustersList = serializeClusterData(decomposition.getClusters().values(), idToEntityMap);
        JSONArray functionalityList = serializeFunctionalityData(refactorizationObject
                .getJSONObject("functionalities"), idToEntityMap);
        JSONArray entitiesList = serializeStructureData(structureObject
                .getJSONArray("entities"));


        JSONObject decompositionObject = new JSONObject();
        decompositionObject.put("name", decomposition.getStrategy().getCodebase().getName());
        decompositionObject.put("clusters", clustersList);
        decompositionObject.put("functionalities", functionalityList);
        decompositionObject.put("entities", entitiesList);
        return decompositionObject.toString(2);
    }

    protected static JSONArray serializeClusterData(
            Collection<Cluster> clusters,
            Map<Short, String> idToEntityMap) throws JSONException
    {
        JSONArray clustersList = new JSONArray();
        for (Cluster cluster : clusters) {
            JSONObject clusterObject = new JSONObject();
            clusterObject.put("name", cluster.getName());

            JSONArray elementsList = new JSONArray();
            for (Element element : cluster.getElements()) {
                JSONObject elementObject = new JSONObject();
                elementObject.put("name", element.getName());
                elementsList.put(elementObject);
                idToEntityMap.put(element.getId(), element.getName());
            }

            clusterObject.put("elements", elementsList);
            clustersList.put(clusterObject);
        }

        return clustersList;
    }

    protected static JSONArray serializeFunctionalityData(
            JSONObject refFunctionalitiesObject,
            Map<Short, String> idToEntityMap) throws JSONException
    {
        JSONArray functionalitiesList = new JSONArray();

        Iterator functionalitiesObjectKeysIterator = refFunctionalitiesObject.keys();
        while (functionalitiesObjectKeysIterator.hasNext()) {
            JSONObject functionalityObject = new JSONObject();
            JSONObject refFunctionalityObject = refFunctionalitiesObject
                    .getJSONObject((String) functionalitiesObjectKeysIterator.next());

            functionalityObject.put("name", refFunctionalityObject.getString("name"));
            functionalityObject.put("orchestrator", refFunctionalityObject
                    .getJSONObject("refactor").getJSONObject("orchestrator").getString("name"));
            functionalityObject.put("steps", serializeSagaStepsData(refFunctionalityObject
                    .getJSONObject("refactor").getJSONArray("call_graph"), idToEntityMap));
            functionalitiesList.put(functionalityObject);
        }

        return functionalitiesList;
    }

    private static JSONArray serializeSagaStepsData(
            JSONArray refCallGraphList,
            Map<Short, String> idToEntityMap) throws JSONException
    {
        JSONArray sagaStepsList = new JSONArray();
        for (int i = 0; i < refCallGraphList.length(); i++) {
            JSONObject sagaStepObject = new JSONObject();
            sagaStepObject.put("cluster", refCallGraphList.getJSONObject(i).getString("cluster_name"));
            sagaStepObject.put("accesses", serializeSagaStepAccessesData(refCallGraphList
                    .getJSONObject(i).getJSONArray("accesses"), idToEntityMap));
            sagaStepsList.put(sagaStepObject);
        }

        return sagaStepsList;
    }

    private static JSONArray serializeSagaStepAccessesData(
            JSONArray refAccessesList,
            Map<Short, String> idToEntityMap) throws JSONException
    {
        JSONArray accessesList = new JSONArray();
        for (int i = 0; i < refAccessesList.length(); i++) {
            JSONObject accessObject = new JSONObject();
            accessObject.put("entity", idToEntityMap.get(Short.parseShort(refAccessesList.getJSONObject(i).getString("entity_id"))));
            accessObject.put("type", refAccessesList.getJSONObject(i).getString("type"));
            accessesList.put(accessObject);
        }

        return accessesList;
    }

    protected static JSONArray serializeStructureData(JSONArray refEntitiesObject) {
        return refEntitiesObject;
    }
}
