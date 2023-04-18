package pt.ist.socialsoftware.mono2micro.similarity.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyClassVectorization.SIMILARITY_SCIPY_CLASS_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyEntityVectorization.SIMILARITY_SCIPY_ENTITY_VECTORIZATION;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyAccessesAndRepository.SIMILARITY_SCIPY_ACCESSES_REPOSITORY;
import static pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityScipyFunctionalityVectorizationByCallGraph.SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimilarityScipyAccessesAndRepositoryDto.class, name = SIMILARITY_SCIPY_ACCESSES_REPOSITORY),
        @JsonSubTypes.Type(value = SimilarityScipyClassVectorizationDto.class, name = SIMILARITY_SCIPY_CLASS_VECTORIZATION),
        @JsonSubTypes.Type(value = SimilarityScipyEntityVectorizationDto.class, name = SIMILARITY_SCIPY_ENTITY_VECTORIZATION),
        @JsonSubTypes.Type(value = SimilarityScipyFunctionalityVectorizationByCallGraphDto.class, name = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH),
        @JsonSubTypes.Type(value = SimilarityScipyFunctionalityVectorizationBySequenceOfAccessesDto.class, name = SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES)
})
public abstract class SimilarityDto {
    String codebaseName;
    String strategyName;
    String name;
    String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getCodebaseName() {
        return codebaseName;
    }

    public void setCodebaseName(String codebaseName) {
        this.codebaseName = codebaseName;
    }
}
