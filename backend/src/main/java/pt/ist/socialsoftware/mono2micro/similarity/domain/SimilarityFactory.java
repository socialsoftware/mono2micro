package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyClassVectorizationDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyEntityVectorizationDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization.SIMILARITY_SCIPY_CLASS_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyEntityVectorization.SIMILARITY_SCIPY_ENTITY_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyWeights.SIMILARITY_SCIPY_WEIGHTS;

public class SimilarityFactory {

    public static Similarity getSimilarity(SimilarityDto similarityDto) {
        if (similarityDto == null)
            return null;
        switch (similarityDto.getType()) {
            case SIMILARITY_SCIPY_WEIGHTS:
                return new SimilarityScipyWeights((SimilarityMatrixSciPyDto) similarityDto);
            case SIMILARITY_SCIPY_ENTITY_VECTORIZATION:
                return new SimilarityScipyEntityVectorization((SimilarityMatrixSciPyEntityVectorizationDto) similarityDto);
            case SIMILARITY_SCIPY_CLASS_VECTORIZATION:
                return new SimilarityScipyClassVectorization((SimilarityMatrixSciPyClassVectorizationDto) similarityDto);
            default:
                throw new RuntimeException("The type \"" + similarityDto.getType() + "\" is not a valid similarityDto type.");
        }
    }

    public static Similarity getSimilarity(Strategy strategy, SimilarityDto similarityDto) {
        Similarity similarity = getSimilarity(similarityDto);
        int i = 0;
        String similarityName;
        do {
            similarityName = strategy.getName() + " - Similarity " + ++i;
        } while (strategy.containsSimilarityName(similarityName));
        similarity.setName(similarityName);

        similarity.setStrategy(strategy);
        strategy.addSimilarity(similarity);
        return similarity;
    }
}
