package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Document("strategy")
public class Strategy {
    @Id
    private String name;
    private String algorithmType;
    @DBRef
    private Codebase codebase;
    @DBRef(lazy = true)
    private List<Decomposition> decompositions = new ArrayList<>();
    @DBRef(lazy = true)
    private List<Similarity> similarities = new ArrayList<>();
    @DBRef(lazy = true)
    private List<Recommendation> recommendations = new ArrayList<>();
    private List<String> representationInfoTypes;

    public Strategy() {}

    public Strategy(Codebase codebase, String algorithmType, List<String> representationInfoTypes) {
        StringBuilder shortForm = new StringBuilder();
        for (String word : representationInfoTypes.get(0).split(" ")) {
            shortForm.append(word.charAt(0));
        }
        for (int i = 1; i < representationInfoTypes.size(); i++) {
            shortForm.append("+");
            for (String word : representationInfoTypes.get(i).split(" ")) {
                shortForm.append(word.charAt(0));
            }
        }
        this.name = codebase.getName() + " - " + shortForm + " Strategy";
        this.algorithmType = algorithmType;
        this.representationInfoTypes = representationInfoTypes;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public List<String> getRepresentationInfoTypes() {
        return representationInfoTypes;
    }

    public void setRepresentationInfoTypes(List<String> representationInfoTypes) {
        this.representationInfoTypes = representationInfoTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Codebase getCodebase() {
        return codebase;
    }

    public void setCodebase(Codebase codebase) {
        this.codebase = codebase;
    }

    public List<Decomposition> getDecompositions() {
        return decompositions;
    }

    public Decomposition getDecompositionByName(String decompositionName) {
        return this.decompositions.stream().filter(decomposition -> decomposition.getName().equals(decompositionName)).findFirst().orElse(null);
    }

    public void setDecompositions(List<Decomposition> decompositions) {
        this.decompositions = decompositions;
    }

    public synchronized void addDecomposition(Decomposition decomposition) {
        this.decompositions.add(decomposition);
    }

    public synchronized void removeDecomposition(String decompositionName) {
        this.decompositions = this.decompositions.stream().filter(decomposition -> !decomposition.getName().equals(decompositionName)).collect(Collectors.toList());
    }

    public List<Similarity> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(List<Similarity> similarities) {
        this.similarities = similarities;
    }

    public void addSimilarity(Similarity similarity) {
        getSimilarities().add(similarity);
    }

    public void removeSimilarity(String similarityName) {
        setSimilarities(getSimilarities().stream().filter(similarity -> !similarity.getName().equals(similarityName)).collect(Collectors.toList()));
    }

    public boolean containsSimilarityName(String name) {
        return this.similarities.stream().anyMatch(similarity -> similarity.getName().equals(name));
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public boolean containsRecommendationName(String name) {
        return this.recommendations.stream().anyMatch(recommendation -> recommendation.getName().equals(name));
    }

    public void addRecommendation(Recommendation recommendation) {
        getRecommendations().add(recommendation);
    }
}