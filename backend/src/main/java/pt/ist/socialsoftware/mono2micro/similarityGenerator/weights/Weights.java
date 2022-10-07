package pt.ist.socialsoftware.mono2micro.similarityGenerator.weights;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.AccessesWeights.ACCESSES_WEIGHTS;
import static pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.RepositoryWeights.REPOSITORY_WEIGHTS;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesWeights.class, name = ACCESSES_WEIGHTS),
        @JsonSubTypes.Type(value = RepositoryWeights.class, name = REPOSITORY_WEIGHTS),
})
public abstract class Weights {
    public abstract String getType();

    public abstract int getNumberOfWeights();

    public abstract float[] getWeights();
    public abstract List<String> getWeightsNames();

    public abstract void setWeightsFromArray(float[] weightsArray);

    @Override
    public abstract boolean equals(Object object);
}
