package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Document("strategy")
public class AccessesSciPyStrategy extends Strategy {
    public static final String ACCESSES_SCIPY = "Accesses SciPy";

    public static final List<String> representationTypes = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
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
    public List<String> getRepresentationTypes() {
        return representationTypes;
    }

    public List<Similarity> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(List<Similarity> similarities) {
        this.similarities = similarities;
    }

    public void addSimilarity(Similarity similarity) {
        this.similarities.add(similarity);
    }

    public void removeSimilarity(String similarityName) {
        this.similarities = this.similarities.stream().filter(similarity -> !similarity.getName().equals(similarityName)).collect(Collectors.toList());
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public void addRecommendation(Recommendation recommendation) {
        this.recommendations.add(recommendation);
    }

}
