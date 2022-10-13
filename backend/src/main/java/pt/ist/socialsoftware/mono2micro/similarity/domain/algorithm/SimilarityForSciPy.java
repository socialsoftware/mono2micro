package pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm;

import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;

import java.util.Map;

public interface SimilarityForSciPy {
    String getLinkageType();
    String getSimilarityMatrixName();
    Map<Short, String> getIDToEntityName(GridFsService gridFsService) throws Exception;
}
