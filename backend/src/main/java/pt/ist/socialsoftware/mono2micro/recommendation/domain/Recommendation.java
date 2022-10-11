package pt.ist.socialsoftware.mono2micro.recommendation.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import java.util.List;

public abstract class Recommendation {
    @Id
    String name;
    String decompositionType;
    @DBRef
    Strategy strategy;
    String recommendationResultName;
    boolean isCompleted; // true when all the decompositions are calculated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public abstract String getType();

    public abstract List<String> getImplementations();

    public boolean containsImplementation(String implementation) {
        return getImplementations().contains(implementation);
    }

    public String getDecompositionType() {
        return decompositionType;
    }

    public void setDecompositionType(String decompositionType) {
        this.decompositionType = decompositionType;
    }

    public abstract boolean equalsDto(RecommendationDto dto);

    public String getRecommendationResultName() {
        return recommendationResultName;
    }

    public void setRecommendationResultName(String recommendationResultName) {
        this.recommendationResultName = recommendationResultName;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    public abstract void generateRecommendation(RecommendationRepository recommendationRepository, GridFsService gridFsService);
    public abstract void createDecompositions(DecompositionService decompositionService, GridFsService gridFsService, List<String> decompositionNames) throws Exception;
}
