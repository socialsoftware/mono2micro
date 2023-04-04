package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.similarity.domain.*;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization.SIMILARITY_SCIPY_CLASS_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyEntityVectorization.SIMILARITY_SCIPY_ENTITY_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyAccessesAndRepository.SIMILARITY_SCIPY_ACCESSES_REPOSITORY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

public class SimilarityDtoFactory {
    public static SimilarityDto getSimilarityDto(Similarity similarity) {
        switch (similarity.getType()) {
            case SIMILARITY_SCIPY_ACCESSES_REPOSITORY:
                return new SimilarityScipyAccessesAndRepositoryDto((SimilarityScipyAccessesAndRepository) similarity);
            case SIMILARITY_SCIPY_ENTITY_VECTORIZATION:
                return new SimilarityScipyEntityVectorizationDto((SimilarityScipyEntityVectorization) similarity);
            case SIMILARITY_SCIPY_CLASS_VECTORIZATION:
                return new SimilarityScipyClassVectorizationDto((SimilarityScipyClassVectorization) similarity);
            case SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH:
                return new SimilarityScipyFunctionalityVectorizationByCallGraphDto((SimilarityScipyFunctionalityVectorizationByCallGraph) similarity);
            case SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES:
                return new SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto((SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses) similarity);
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
