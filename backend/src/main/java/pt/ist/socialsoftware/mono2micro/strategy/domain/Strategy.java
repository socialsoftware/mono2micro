package pt.ist.socialsoftware.mono2micro.strategy.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.representation.domain.AuthorRepresentation.AUTHOR;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CodeEmbeddingsRepresentation.CODE_EMBEDDINGS;
import static pt.ist.socialsoftware.mono2micro.representation.domain.CommitRepresentation.COMMIT;
import static pt.ist.socialsoftware.mono2micro.representation.domain.EntityToIDRepresentation.ENTITY_TO_ID;
import static pt.ist.socialsoftware.mono2micro.representation.domain.IDToEntityRepresentation.ID_TO_ENTITY;
import static pt.ist.socialsoftware.mono2micro.representation.domain.Representation.*;

@Document("strategy")
public class Strategy {
    public static final String ACCESSES_STRATEGY = "Accesses";
    public static final String REPOSITORY_STRATEGY = "Repository";
    public static final String CLASS_VECTORIZATION_STRATEGY = "Class Vectorization";
    public static final String ENTITY_VECTORIZATION_STRATEGY = "Entity Vectorization";
    public static final String FUNCTIONALITY_VECTORIZATION_CALLGRAPH_STRATEGY = "Functionality Vectorization Call Graph";
    public static final String FUNCTIONALITY_VECTORIZATION_ACCESSES_STRATEGY = "Functionality Vectorization Sequence Accesses";

    public static final Map<String, List<String>> strategiesToRepresentations = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(ACCESSES_STRATEGY, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ACCESSES))),
            new AbstractMap.SimpleImmutableEntry<>(REPOSITORY_STRATEGY, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ACCESSES, AUTHOR, COMMIT))),
            new AbstractMap.SimpleImmutableEntry<>(CLASS_VECTORIZATION_STRATEGY, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ENTITY_TO_ID, ACCESSES, CODE_EMBEDDINGS))),
            new AbstractMap.SimpleImmutableEntry<>(ENTITY_VECTORIZATION_STRATEGY, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ENTITY_TO_ID, ACCESSES, CODE_EMBEDDINGS))),
            new AbstractMap.SimpleImmutableEntry<>(FUNCTIONALITY_VECTORIZATION_CALLGRAPH_STRATEGY, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ENTITY_TO_ID, ACCESSES, CODE_EMBEDDINGS))),
            new AbstractMap.SimpleImmutableEntry<>(FUNCTIONALITY_VECTORIZATION_ACCESSES_STRATEGY, new ArrayList<>(Arrays.asList(ID_TO_ENTITY, ENTITY_TO_ID, ACCESSES, CODE_EMBEDDINGS)))
    ).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));

    public static final Map<String, List<String>> strategiesToRepresentationTypes = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(ACCESSES_STRATEGY, new ArrayList<>(Arrays.asList(ACCESSES_TYPE))),
            new AbstractMap.SimpleImmutableEntry<>(REPOSITORY_STRATEGY, new ArrayList<>(Arrays.asList(ACCESSES_TYPE, REPOSITORY_TYPE))),
            new AbstractMap.SimpleImmutableEntry<>(CLASS_VECTORIZATION_STRATEGY, new ArrayList<>(Arrays.asList(ACCESSES_TYPE, CODE_EMBEDDINGS_TYPE))),
            new AbstractMap.SimpleImmutableEntry<>(ENTITY_VECTORIZATION_STRATEGY, new ArrayList<>(Arrays.asList(ACCESSES_TYPE, CODE_EMBEDDINGS_TYPE))),
            new AbstractMap.SimpleImmutableEntry<>(FUNCTIONALITY_VECTORIZATION_CALLGRAPH_STRATEGY, new ArrayList<>(Arrays.asList(ACCESSES_TYPE, CODE_EMBEDDINGS_TYPE))),
            new AbstractMap.SimpleImmutableEntry<>(FUNCTIONALITY_VECTORIZATION_ACCESSES_STRATEGY, new ArrayList<>(Arrays.asList(ACCESSES_TYPE, CODE_EMBEDDINGS_TYPE)))
    ).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
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
    private List<String> strategyTypes;

    public Strategy() {}

    public Strategy(Codebase codebase, String algorithmType, List<String> strategyTypes) {
        StringBuilder shortForm = new StringBuilder();
        for (String word : strategyTypes.get(0).split(" ")) {
            shortForm.append(word.charAt(0));
        }
        for (int i = 1; i < strategyTypes.size(); i++) {
            shortForm.append("+");
            for (String word : strategyTypes.get(i).split(" ")) {
                shortForm.append(word.charAt(0));
            }
        }
        this.name = codebase.getName() + " - " + shortForm + " Strategy";
        this.algorithmType = algorithmType;
        this.strategyTypes = strategyTypes;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
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

    public Similarity getSimilarityByName(String name) {
        return this.similarities.stream()
                .filter(similarity -> similarity.getName().equals(name))
                .findAny()
                .orElse(null);
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

    public List<String> getStrategyTypes() {
        return strategyTypes;
    }

    public void setStrategyTypes(List<String> strategyTypes) {
        this.strategyTypes = strategyTypes;
    }
}