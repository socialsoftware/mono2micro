package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.RecommendationsStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.SimilaritiesStrategy;

import java.util.ArrayList;
import java.util.List;


@Document("strategy")
public class AccessesSciPyStrategy extends Strategy implements SimilaritiesStrategy, RecommendationsStrategy {
    public static final String ACCESSES_SCIPY = "Accesses SciPy";

    private static final List<String> representationTypes = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
    }};

    private static final List<String> implementationTypes = new ArrayList<String>() {{
        add(SimilaritiesStrategy.CONTAINS_SIMILARITIES);
        add(RecommendationsStrategy.CONTAINS_RECOMMENDATIONS);
    }};

    @DBRef(lazy = true)
    private List<Similarity> similarities = new ArrayList<>();
    @DBRef(lazy = true)
    private List<Recommendation> recommendations = new ArrayList<>();

    public AccessesSciPyStrategy() {}

    @Override
    public String getType() {
        return ACCESSES_SCIPY;
    }
    @Override
    public List<String> getImplementations() {
        return implementationTypes;
    }
    @Override
    public List<String> getRepresentationTypes() {
        return representationTypes;
    }

    @Override
    public List<Similarity> getSimilarities() {
        return similarities;
    }

    @Override
    public void setSimilarities(List<Similarity> similarities) {
        this.similarities = similarities;
    }

    @Override
    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    @Override
    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }
}
