package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation;
import pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.RecommendationsStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.SimilaritiesStrategy;

import java.util.ArrayList;
import java.util.List;

@Document("strategy")
public class AccAndRepoSciPyStrategy extends Strategy implements SimilaritiesStrategy, RecommendationsStrategy {
    public static final String ACC_AND_REPO_SCIPY = "Accesses and Repository SciPy";

    public static final List<String> representationTypes = new ArrayList<String>() {{
        add(AccessesRepresentation.ACCESSES);
        add(IDToEntityRepresentation.ID_TO_ENTITY);
        add(AuthorRepresentation.AUTHOR);
        add(CommitRepresentation.COMMIT);
    }};

    public static final List<String> implementationTypes = new ArrayList<String>() {{
        add(SimilaritiesStrategy.CONTAINS_SIMILARITIES);
        add(RecommendationsStrategy.CONTAINS_RECOMMENDATIONS);
    }};

    @DBRef(lazy = true)
    private List<Similarity> similarities = new ArrayList<>();
    @DBRef(lazy = true)
    private List<Recommendation> recommendations = new ArrayList<>();

    public AccAndRepoSciPyStrategy() {}

    @Override
    public String getType() {
        return ACC_AND_REPO_SCIPY;
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
