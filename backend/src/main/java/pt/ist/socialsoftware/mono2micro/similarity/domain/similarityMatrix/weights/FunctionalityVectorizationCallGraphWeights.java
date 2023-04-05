package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONException;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.io.IOException;
import java.util.*;

public class FunctionalityVectorizationCallGraphWeights extends Weights {
    public static final String FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS = "FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS";
    private float controllersWeight;
    private float servicesWeight;
    private float intermediateMethodsWeight;
    private float entitiesWeight;

    public FunctionalityVectorizationCallGraphWeights() {}

    public FunctionalityVectorizationCallGraphWeights(float controllersWeight, float servicesWeight, float intermediateMethodsWeight, float entitiesWeight) {
        this.controllersWeight = controllersWeight;
        this.servicesWeight = servicesWeight;
        this.intermediateMethodsWeight = intermediateMethodsWeight;
        this.entitiesWeight = entitiesWeight;
    }

    @Override
    public String getType() {
        return FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS;
    }

    @Override
    public int getNumberOfWeights() {
        return 4;
    }

    @Override
    public float[] getWeights() {
        return new float[]{controllersWeight, servicesWeight, intermediateMethodsWeight, entitiesWeight};
    }

    @Override
    public List<String> getWeightsNames() {
        return new ArrayList<>(Arrays.asList("controllersWeight", "servicesWeight", "intermediateMethodsWeight", "entitiesWeight"));
    }

    @Override
    public String getName() {
        StringBuilder result = new StringBuilder("ws(");
        result.append("Co")
                .append(Math.round(getWeights()[0]))
                .append(",")
                .append("Se")
                .append(Math.round(getWeights()[1]))
                .append(",")
                .append("In")
                .append(Math.round(getWeights()[2]))
                .append(",")
                .append("En")
                .append(Math.round(getWeights()[3]))
                .append(")");
        return result.toString();
    }

    @Override
    public void setWeightsFromArray(float[] weightsArray) {
        this.controllersWeight = weightsArray[0];
        this.servicesWeight = weightsArray[1];
        this.intermediateMethodsWeight = weightsArray[2];
        this.entitiesWeight = weightsArray[3];
    }

    public float getControllersWeight() {
        return controllersWeight;
    }
    public void setControllersWeight(float controllersWeight) {
        this.controllersWeight = controllersWeight;
    }
    public float getServicesWeight() {
        return servicesWeight;
    }
    public void setServicesWeight(float servicesWeight) {
        this.servicesWeight = servicesWeight;
    }
    public float getIntermediateMethodsWeight() {
        return intermediateMethodsWeight;
    }
    public void setIntermediateMethodsWeight(float intermediateMethodsWeight) {
        this.intermediateMethodsWeight = intermediateMethodsWeight;
    }
    public float getEntitiesWeight() {
        return entitiesWeight;
    }
    public void setEntitiesWeight(float entitiesWeight) {
        this.entitiesWeight = entitiesWeight;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FunctionalityVectorizationCallGraphWeights))
            return false;
        FunctionalityVectorizationCallGraphWeights callGraphWeights = (FunctionalityVectorizationCallGraphWeights) object;
        return this.controllersWeight == callGraphWeights.getControllersWeight() &&
                this.servicesWeight == callGraphWeights.getServicesWeight() &&
                this.intermediateMethodsWeight == callGraphWeights.getIntermediateMethodsWeight() &&
                this.entitiesWeight == callGraphWeights.getEntitiesWeight();
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        throw new NotImplementedException("Not used");
    }

    @Override
    public void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws IOException, JSONException {
        throw new NotImplementedException("Not used");
    }

}
