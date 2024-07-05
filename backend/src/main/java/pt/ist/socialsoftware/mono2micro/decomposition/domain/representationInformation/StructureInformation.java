package pt.ist.socialsoftware.mono2micro.decomposition.domain.representationInformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.Partition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.fileManager.ContextManager;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.functionality.domain.LocalTransaction;
import pt.ist.socialsoftware.mono2micro.functionality.dto.TraceDto;
import pt.ist.socialsoftware.mono2micro.metrics.decompositionMetrics.*;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipy;
import pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram.Dendrogram;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.SimilarityStructureIterator;

import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyStructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.jgrapht.Graphs.successorListOf;
import static pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation.STRUCTURE;
import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.STRUCTURE_TYPE;

public class StructureInformation extends RepresentationInformation {
    private Map<String, ArrayList<String>> entitiesContained = new HashMap<>();
    private Integer totalEntities;

    public StructureInformation() {}

    @Override
    public void setup(Decomposition decomposition) throws IOException {
        this.decompositionName = decomposition.getName();
        decomposition.addRepresentationInformation(this);
        setupEntities(decomposition);
    }

    @Override
    public void snapshot(Decomposition snapshotDecomposition, Decomposition decomposition) {
        this.decompositionName = snapshotDecomposition.getName();
        snapshotDecomposition.addRepresentationInformation(this);
        StructureInformation structureInformation = (StructureInformation) decomposition.getRepresentationInformationByType(STRUCTURE_TYPE);
        this.entitiesContained = structureInformation.getEntitiesContained();
        this.totalEntities = structureInformation.getTotalEntities();
    }

    @Override
    public String getType() {
        return STRUCTURE_TYPE;
    }

    @Override
    public void update(Decomposition decomposition) throws Exception {}

    @Override
    public List<DecompositionMetricCalculator> getDecompositionMetrics() {
        return new ArrayList<>(Arrays.asList(
                new CohesionMetricCalculator(),
                new ComplexityMetricCalculator(),
                new CouplingMetricCalculator(),
                new PerformanceMetricCalculator()));
    }

    @Override
    public List<String> getParameters() {
        return new ArrayList<>(Arrays.asList(
                RepresentationInformationParameters.PROFILE_PARAMETER.toString(),
                RepresentationInformationParameters.TRACES_MAX_LIMIT_PARAMETER.toString(),
                RepresentationInformationParameters.TRACE_TYPE_PARAMETER.toString()));
    }

    @Override
    public void deleteProperties() {}


    public Map<String, ArrayList<String>> getEntitiesContained() {
        return entitiesContained;
    }

    public void setEntitiesContained(Map<String, ArrayList<String>> entitiesContained) {
        this.entitiesContained = entitiesContained;
    }

    public Integer getTotalEntities() {
        return totalEntities;
    }

    public void setTotalEntities(Integer totalEntities) {this.totalEntities = totalEntities;
    }

    private void setupEntities(Decomposition decomposition) throws IOException{
        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        StructureRepresentation structureRepresentation = (StructureRepresentation) decomposition.getStrategy().getCodebase().getRepresentationByFileType(STRUCTURE);
        Map<String, Object> jsonData = new ObjectMapper().readValue(
                gridFsService.getFileAsString(structureRepresentation.getName()),
                new TypeReference<Map<String, Object>>() {}
        );

        Map<String, ArrayList<String>> entitiesContainedMap = mapEntitiesContained(jsonData);


        /*Filter out all the primitive types*/
        SimilarityScipyStructure s = (SimilarityScipyStructure) decomposition.getSimilarity();
        StructureRepresentation structure = (StructureRepresentation) decomposition.getSimilarity().getStrategy().getCodebase().getRepresentationByFileType(STRUCTURE);
        Set<String> entities = structure.getProfile(s.getProfile());
        filterEntities(entitiesContainedMap, entities);

        setEntitiesContained(entitiesContainedMap);
    }

    public static void filterEntities(Map<String, ArrayList<String>> entitiesContainedMap, Set<String> entities) {
        for (Map.Entry<String, ArrayList<String>> entry : entitiesContainedMap.entrySet()) {
            ArrayList<String> fields = entry.getValue();
            Iterator<String> iterator = fields.iterator();

            while (iterator.hasNext()) {
                String field = iterator.next();
                boolean shouldRemove = true;

                // Check if the entire field is in the entities set
                if (entities.contains(field)) {
                    shouldRemove = false;
                } else {
                    // Check if any part of the field split by space is in the entities set
                    String[] parts = field.split(" ");
                    for (String part : parts) {
                        if (entities.contains(part)) {
                            shouldRemove = false;
                            break;
                        }
                    }
                }

                if (shouldRemove) {
                    iterator.remove();
                }
            }
        }
    }

    public Map<String, ArrayList<String>> mapEntitiesContained(Map<String, Object> jsonData) {
        // Extract the entities list from the map
        ArrayList<Map<String, Object>> entities = (ArrayList<Map<String, Object>>) jsonData.get("entities");

        // Create a map of entity names and their field types
        Map<String, ArrayList<String>> entityFieldTypesMap = new HashMap<>();
        for (Map<String, Object> entity : entities) {
            String entityName = (String) entity.get("name");
            ArrayList<Map<String, Object>> fields = (ArrayList<Map<String, Object>>) entity.get("fields");
            ArrayList<String> fieldTypes = new ArrayList<>();
            for (Map<String, Object> field : fields) {
                Map<String, Object> type = (Map<String, Object>) field.get("type");
                String typeName = (String) type.get("name");
                if (type.containsKey("parameters")) {
                    ArrayList<Map<String, Object>> parameters = (ArrayList<Map<String, Object>>) type.get("parameters");
                    for (Map<String, Object> parameter : parameters) {
                        String parameterName = (String) parameter.get("name") + " " + typeName;
                        fieldTypes.add(parameterName);
                    }
                    continue;
                }
                fieldTypes.add(typeName);
            }
            entityFieldTypesMap.put(entityName, fieldTypes);
        }
        return entityFieldTypesMap;
    }

    @Override
    public String getEdgeWeights(Decomposition decomposition) throws JSONException, IOException {

        GridFsService gridFsService = ContextManager.get().getBean(GridFsService.class);
        Dendrogram dendrogram = ((SimilarityScipy) decomposition.getSimilarity()).getDendrogram();
        JSONArray copheneticDistances = new JSONArray(IOUtils.toString(gridFsService.getFile(dendrogram.getCopheneticDistanceName()), StandardCharsets.UTF_8));

        ArrayList<Short> entitiesShorts = new ArrayList<>(decomposition.getEntityIDToClusterName().keySet());

        JSONArray edgesJSON = new JSONArray();
        Map<Short, String> id2entity = ((SimilarityScipy) decomposition.getSimilarity()).getIDToEntityName();
        ArrayList<String> entitiesStrings = new ArrayList<>();
        for(short id : entitiesShorts) {
            entitiesStrings.add(id2entity.get(id));
        }
        int k = 0;
        for (int i = 0; i < entitiesStrings.size(); i++) {
            String e1ID = entitiesStrings.get(i);
            ArrayList<String> localEntitiesContained = getEntitiesContained().get(e1ID);
            if (localEntitiesContained == null)
                continue;
            for (int j = i + 1; j < entitiesStrings.size(); j++) {
                String e2ID = entitiesStrings.get(j);
                if(e1ID.equals(e2ID)) {
                    continue;
                }
                ArrayList<String> entitiesContainedE2ID = getEntitiesContained().get(e2ID);
                if (entitiesContainedE2ID == null)
                    continue;

                JSONObject edgeJSON = new JSONObject();
                short e1short = entitiesShorts.get(i);
                short e2short = entitiesShorts.get(j);
                if (e1short < e2short) {
                    edgeJSON.put("e1ID", e1short); edgeJSON.put("e2ID", e2short);
                }
                else {
                    edgeJSON.put("e1ID", e2short); edgeJSON.put("e1ID", e2short);
                }
                edgeJSON.put("dist", copheneticDistances.getDouble(k));

                // Addding the entity e1 contained within e2 and e2 contained in e1
                JSONArray enititiesJson = new JSONArray();
                for (String eID : entitiesContainedE2ID){
                    if(eID.equals(e1ID))
                        enititiesJson.put(e1short);
                }
                for (String eID : localEntitiesContained){
                    if(eID.equals(e2ID))
                        enititiesJson.put(e2short);
                }

                edgeJSON.put("appearences", enititiesJson);

                edgesJSON.put(edgeJSON);
                k++;
            }
        }
        return edgesJSON.toString();
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

        return searchItems.toString();
    }
}
