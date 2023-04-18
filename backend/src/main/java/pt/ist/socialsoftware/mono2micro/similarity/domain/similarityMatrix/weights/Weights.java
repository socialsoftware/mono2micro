package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.List;
import java.util.Set;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.AccessesWeights.ACCESSES_WEIGHTS;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationCallGraphWeights.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.FunctionalityVectorizationSequenceOfAccessesWeights.FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.RepositoryWeights.REPOSITORY_WEIGHTS;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccessesWeights.class, name = ACCESSES_WEIGHTS),
        @JsonSubTypes.Type(value = RepositoryWeights.class, name = REPOSITORY_WEIGHTS),
        @JsonSubTypes.Type(value = FunctionalityVectorizationCallGraphWeights.class, name = FUNCTIONALITY_VECTORIZATION_CALLGRAPH_WEIGHTS),
        @JsonSubTypes.Type(value = FunctionalityVectorizationSequenceOfAccessesWeights.class, name = FUNCTIONALITY_VECTORIZATION_ACCESSES_WEIGHTS)
})
public abstract class Weights {
    public abstract String getType();
    public abstract int getNumberOfWeights();
    public abstract float[] getWeights();
    public abstract List<String> getWeightsNames();
    public abstract String getName();
    public abstract void setWeightsFromArray(float[] weightsArray);
    public abstract void fillMatrix(GridFsService gridFsService, Similarity similarity, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws Exception;
    public abstract void fillMatrix(GridFsService gridFsService, Recommendation recommendation, float[][][] rawMatrix, Set<Short> elements, int fillFromIndex) throws Exception;
    @Override
    public abstract boolean equals(Object object);
}
