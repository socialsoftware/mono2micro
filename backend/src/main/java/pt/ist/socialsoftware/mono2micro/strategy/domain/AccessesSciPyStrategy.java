package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
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
    private List<Dendrogram> dendrograms = new ArrayList<>();
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

    public List<Dendrogram> getDendrograms() {
        return dendrograms;
    }

    public void setDendrograms(List<Dendrogram> dendrograms) {
        this.dendrograms = dendrograms;
    }

    public void addDendrogram(Dendrogram dendrogram) {
        this.dendrograms.add(dendrogram);
    }

    public void removeDendrogram(String dendrogramName) {
        this.dendrograms = this.dendrograms.stream().filter(dendrogram -> !dendrogram.getName().equals(dendrogramName)).collect(Collectors.toList());
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
