package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.functionality.dto.AccessDto;
import pt.ist.socialsoftware.mono2micro.functionality.dto.FieldDto;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyStructure;
import pt.ist.socialsoftware.mono2micro.utils.Constants;
import pt.ist.socialsoftware.mono2micro.utils.SimilarityStructureIterator;
import pt.ist.socialsoftware.mono2micro.utils.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static pt.ist.socialsoftware.mono2micro.representation.domain.StructureRepresentation.STRUCTURE;

public class StructureWeights extends Weights {
    public static final String STRUCTURE_WEIGHTS = "STRUCTURE_WEIGHTS";
    private float oneToOneWeight;
    private float oneToManyWeight;

    public StructureWeights() {}

    public StructureWeights(float oneToOneWeight, float oneToManyWeight) {
        this.oneToOneWeight = oneToOneWeight;
        this.oneToManyWeight = oneToManyWeight;
    }

    @Override
    public String getType() {
        return STRUCTURE_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 2;
    }

    @Override
    public float[] getWeights() {
        return new float[]{oneToOneWeight, oneToManyWeight};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<>(Arrays.asList("oneToOneWeight", "oneToManyWeight"));
    }

    @Override
    public String getName() {
        StringBuilder result = new StringBuilder("ws(");
        result.append("o2o")
                .append(Math.round(getWeights()[0]))
                .append(",")
                .append("o2m")
                .append(Math.round(getWeights()[1]))
                .append(")");
        return result.toString();
    }

    @Override
    public void setWeightsFromArray(float[] weightsArray) {
        this.oneToOneWeight = weightsArray[0];
        this.oneToManyWeight = weightsArray[1];
    }

    public float getOneToManyWeight() {
        return oneToManyWeight;
    }
    public void setOneToManyWeight(float oneToManyWeight) {
        this.oneToManyWeight = oneToManyWeight;
    }
    public float getOneToOneWeight() {
        return oneToOneWeight;
    }
    public void setOneToOneWeight(float oneToOneWeight) {
        this.oneToOneWeight = oneToOneWeight;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StructureWeights))
            return false;
        StructureWeights structureWeights = (StructureWeights) object;
        return this.oneToOneWeight == structureWeights.getOneToOneWeight() &&
                this.oneToManyWeight == structureWeights.getOneToManyWeight();
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        SimilarityScipyStructure s = (SimilarityScipyStructure) similarity;
        StructureRepresentation structure = (StructureRepresentation) similarity.getStrategy().getCodebase().getRepresentationByFileType(STRUCTURE);
        fillRawMatrixFromStructures(rawMatrix, fillFromIndex, gridFsService.getFile(structure.getName()), structure.getProfile(s.getProfile()), getWeights());
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        RecommendMatrixSciPy r = (RecommendMatrixSciPy) recommendation;
        StructureRepresentation structure = (StructureRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByFileType(STRUCTURE);
        fillRawMatrixFromStructures(rawMatrix, fillFromIndex, gridFsService.getFile(structure.getName()), structure.getProfile(r.getProfile()), getWeights());
    }

    public static void fillRawMatrixFromStructures(
            float[][][] rawMatrix,
            int fillFromIndex,
            InputStream structureFile,
            Set<String> profileEntities,
            float[] weights
    ) throws JSONException, IOException {
        Set<String> entities = new TreeSet<>();
        Map<String, List<FieldDto>> entityFields = new HashMap<>(); // Map<entityID, List<Fields>>
        SimilarityStructureIterator iterador = new SimilarityStructureIterator(structureFile);
        fillDataStructures(entities, entityFields, iterador, profileEntities);
        filterEntities(entityFields, entities);
        fillRawMatrix(rawMatrix, entities, entityFields, fillFromIndex, weights);
    }

    /*Filter out the primitive types and leave only the entities*/
    public static void filterEntities(Map<String, List<FieldDto>> entityFields, Set<String> entities) {
        for (Map.Entry<String, List<FieldDto>> entry : entityFields.entrySet()) {
            List<FieldDto> fields = entry.getValue();
            Iterator<FieldDto> iterator = fields.iterator();

            while (iterator.hasNext()) {
                FieldDto field = iterator.next();
                if (!entities.contains(field.getType())) {
                    iterator.remove();
                }
            }
        }
    }

    public static void fillDataStructures(
            Set<String> entities,
            Map<String, List<FieldDto>> entityFields,
            SimilarityStructureIterator iter,
            Set<String> profileEntities
    )
            throws JSONException {
        System.out.println("Creating similarity matrix...");

        for (String entityName : profileEntities) {
            ArrayList<String> tempList = new ArrayList<String>();
            iter.getEntityWithName(entityName);

            List<FieldDto> fieldDtos = iter.getAllFields();
            entityFields.put(entityName, fieldDtos);
        }

        entities.addAll(entityFields.keySet());
    }

    public static void fillRawMatrix(
            float[][][] rawMatrix,
            Set<String> entities,
            Map<String, List<FieldDto>> entityFields,
            int fillFromIndex,
            float[] weights
    ) {
        int i = 0;
        for (String e1ID : entities) {
            int j = 0;

            for (String e2ID : entities) {
                if (e1ID.equals(e2ID)) {
                    for (int k = fillFromIndex; k < fillFromIndex + 2; k++)
                        rawMatrix[i][j][k] = 1;
                    j++;
                    continue;
                }

                float[] distances = calculateSimilarityMatrixDistances(e1ID, e2ID, entityFields, weights);

                for (int k = fillFromIndex, l = 0; k < fillFromIndex + 2; k++, l++)
                    rawMatrix[i][j][k] = distances[l];
                j++;
            }
            i++;
        }
    }

    private static int getMaxNumberOfPairs(Map<String,Integer> e1e2PairCount) {
        if (!e1e2PairCount.values().isEmpty())
            return Collections.max(e1e2PairCount.values());
        else
            return 0;
    }

    private static float[] calculateSimilarityMatrixDistances(
        String e1ID,
        String e2ID,
        Map<String, List<FieldDto>> entityFields,
        float[] weights
    ) {

            float o2o = weights[0];
            float o2m = weights[1];
            List<FieldDto> fields = entityFields.get(e1ID);
            float e2Weight[] = new float[] {0,0}; // e2Weight[0] -> pesos o2o e2Weight[1] -> pesos o2m
            float totalWeight = 0;
            float res[] = new float[2];

            for (FieldDto e1Field : fields) {
                if(e1Field.getIsList()){
                    totalWeight += o2m;
                    if(e1Field.getType().equals(e2ID)){
                        e2Weight[1] += o2m;
                    }
                }else{
                    totalWeight += o2o;
                    if(e1Field.getType().equals(e2ID)){
                        e2Weight[0] += o2o;
                    }
                }
            }

            if (fields.size() == 0) {
                res[0] = 0;
                res[1] = 0;
            }
            else {
                res[0] = e2Weight[0] / totalWeight;
                res[1] = e2Weight[1] / totalWeight;
            }


            return res;
        }
}
