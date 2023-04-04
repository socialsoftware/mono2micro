package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.similarity.dto.*;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization.SIMILARITY_SCIPY_CLASS_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyEntityVectorization.SIMILARITY_SCIPY_ENTITY_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyAccessesAndRepository.SIMILARITY_SCIPY_ACCESSES_REPOSITORY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

public class SimilarityFactory {

    public static SimilarityScipy createSimilarity(Strategy strategy, SimilarityDto similarityDto) {
        if (similarityDto == null)
            return null;
        switch (similarityDto.getType()) {
            case SIMILARITY_SCIPY_ACCESSES_REPOSITORY:
                return new SimilarityScipyAccessesAndRepository(strategy, similarityDto.getName(), (SimilarityScipyAccessesAndRepositoryDto) similarityDto);
            case SIMILARITY_SCIPY_ENTITY_VECTORIZATION:
                return new SimilarityScipyEntityVectorization(strategy, similarityDto.getName(), (SimilarityScipyEntityVectorizationDto) similarityDto);
            case SIMILARITY_SCIPY_CLASS_VECTORIZATION:
                return new SimilarityScipyClassVectorization(strategy, similarityDto.getName(), (SimilarityScipyClassVectorizationDto) similarityDto);
            case SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
                return new SimilarityScipyFunctionalityVectorizationByCallGraph(strategy, similarityDto.getName(), (SimilarityScipyFunctionalityVectorizationByCallGraphDto) similarityDto);
            case SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses(strategy, similarityDto.getName(), (SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto) similarityDto);
            default:
                throw new RuntimeException("The type \"" + similarityDto.getType() + "\" is not a valid similarityDto type.");
        }
    }

    public static SimilarityScipy getSimilarity(Strategy strategy, SimilarityDto similarityDto) {
        SimilarityScipy similarity = (SimilarityScipy) strategy.getSimilarityByName(similarityDto.getName());

        if (similarity == null) {
            similarity = createSimilarity(strategy, similarityDto);
        }

        return similarity;
    }
}
