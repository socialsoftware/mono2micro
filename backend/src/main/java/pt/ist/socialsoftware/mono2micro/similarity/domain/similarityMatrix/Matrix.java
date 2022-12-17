package pt.ist.socialsoftware.mono2micro.similarity.domain.similarityMatrix;

import org.springframework.data.annotation.Transient;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyEntityVectorization;

public abstract class Matrix { // TODO - Latter this should become the SimilarityMatrix

    @Transient
    public GridFsService gridFsService;

    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGridFsService(GridFsService gridFsService) {
        this.gridFsService = gridFsService;
    }

    public abstract void generate(GridFsService gridFsService, Similarity similarity) throws Exception;
}
