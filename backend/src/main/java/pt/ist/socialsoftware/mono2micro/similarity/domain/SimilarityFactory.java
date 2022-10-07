package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityForSciPyDto;

import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

public class SimilarityFactory {
    private static SimilarityFactory factory = null;

    public static SimilarityFactory getFactory() {
        if (factory == null)
            factory = new SimilarityFactory();
        return factory;
    }

    public Similarity getSimilarity(SimilarityDto similarityDto) {
        if (similarityDto == null)
            return null;
        switch (similarityDto.getType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                return new SimilarityForSciPy((SimilarityForSciPyDto) similarityDto);
            default:
                throw new RuntimeException("The type \"" + similarityDto.getType() + "\" is not a valid similarityDto type.");
        }
    }
}
