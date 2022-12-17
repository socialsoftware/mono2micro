package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.annotation.Transient;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimilarityMatrix extends Matrix {
    private List<Weights> weightsList;

    public List<Weights> getWeightsList() {
        return weightsList;
    }

    public void setWeightsList(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

    public SimilarityMatrix() {}

    public SimilarityMatrix(List<Weights> weightsList) {
        this.weightsList = weightsList;
    }

    public SimilarityMatrix(String name, List<Weights> weightsList) {
        this.name = name;
        this.weightsList = weightsList;
    }

    @Override
    public void generate(GridFsService gridFsService, Similarity similarity) throws Exception {
        throw new NotImplementedException("Must be implemented in sub class");
    }

}
