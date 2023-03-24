package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix;

import org.springframework.data.annotation.Transient;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix.weights.Weights;

import java.util.List;

public abstract class SimilarityMatrix {
    @Transient
    public GridFsService gridFsService;

    public String name;
    private List<Weights> weightsList;

    public GridFsService getGridFsService() {
        return gridFsService;
    }

    public void setGridFsService(GridFsService gridFsService) {
        this.gridFsService = gridFsService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public abstract void generate(GridFsService gridFsService, Similarity similarity) throws Exception;

}
