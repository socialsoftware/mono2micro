package pt.ist.socialsoftware.mono2micro.strategy.domain.inteface;

import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;

import java.util.List;
import java.util.stream.Collectors;

public interface SimilaritiesStrategy {
    String CONTAINS_SIMILARITIES = "CONTAINS_SIMILARITIES";
    String getName();
    List<Similarity> getSimilarities();
    void setSimilarities(List<Similarity> similarities);
    default void addSimilarity(Similarity similarity) {
        getSimilarities().add(similarity);
    }
    default void removeSimilarity(String similarityName) {
        setSimilarities(getSimilarities().stream().filter(similarity -> !similarity.getName().equals(similarityName)).collect(Collectors.toList()));
    }
}
