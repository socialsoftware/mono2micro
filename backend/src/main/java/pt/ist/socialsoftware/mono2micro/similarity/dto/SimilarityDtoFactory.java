package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy.SIMILARITY_MATRIX_SCIPY;

public class SimilarityDtoFactory {
    public static SimilarityDto getSimilarityDto(Similarity similarity) {
        switch (similarity.getType()) {
            case SIMILARITY_MATRIX_SCIPY:
                return new SimilarityMatrixSciPyDto((SimilarityMatrixSciPy) similarity);
            default:
                throw new RuntimeException("The type \"" + similarity.getType() + "\" is not a valid similarity type.");
        }
    }

    public static List<SimilarityDto> getSimilarityDtos(List<Similarity> similarities) {
        List<SimilarityDto> similarityDtos = new ArrayList<>();
        for (Similarity similarity : similarities)
            similarityDtos.add(getSimilarityDto(similarity));
        return similarityDtos;
    }
}
