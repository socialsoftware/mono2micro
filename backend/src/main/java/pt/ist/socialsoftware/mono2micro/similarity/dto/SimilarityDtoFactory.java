package pt.ist.socialsoftware.mono2micro.similarity.dto;

import pt.ist.socialsoftware.mono2micro.similarity.domain.AccessesSciPySimilarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;

import java.util.ArrayList;
import java.util.List;

public class SimilarityDtoFactory {
    private static SimilarityDtoFactory factory = null;

    public static SimilarityDtoFactory getFactory() {
        if (factory == null)
            factory = new SimilarityDtoFactory();
        return factory;
    }

    public SimilarityDto getSimilarityDto(Similarity similarity) {
        if (similarity == null)
            return null;
        switch (similarity.getType()) {
            case AccessesSciPyStrategy.ACCESSES_SCIPY:
                return new AccessesSciPySimilarityDto((AccessesSciPySimilarity) similarity);
            default:
                throw new RuntimeException("The type \"" + similarity.getType() + "\" is not a valid similarity type.");
        }
    }

    public List<SimilarityDto> getSimilarityDtos(List<Similarity> similarities) {
        if (similarities == null)
            return null;
        List<SimilarityDto> similarityDtos = new ArrayList<>();
        for (Similarity similarity : similarities)
            similarityDtos.add(getSimilarityDto(similarity));
        return similarityDtos;
    }
}
