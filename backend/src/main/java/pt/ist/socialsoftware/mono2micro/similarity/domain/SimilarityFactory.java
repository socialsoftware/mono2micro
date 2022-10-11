package pt.ist.socialsoftware.mono2micro.similarity.domain;

import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityDto;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityMatrixSciPyDto;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.decomposition.domain.RepositorySciPyDecomposition.REPOSITORY_SCIPY;

public class SimilarityFactory {

    public static Similarity getSimilarity(SimilarityDto similarityDto) {
        if (similarityDto == null)
            return null;
        switch (similarityDto.getDecompositionType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                return new SimilarityMatrixSciPy((SimilarityMatrixSciPyDto) similarityDto);
            default:
                throw new RuntimeException("The type \"" + similarityDto.getDecompositionType() + "\" is not a valid similarityDto type.");
        }
    }
    public static Similarity getSimilarity(Strategy strategy, SimilarityDto similarityDto) {
        Similarity similarity = getSimilarity(similarityDto);
        int i = 0;
        String similarityName;
        do {
            similarityName = strategy.getName() + " - Similarity " + ++i;
        } while (strategy.containsSimilarityName(similarityName));
        similarity.setName(similarityName);

        similarity.setDecompositionType(strategy.getDecompositionType());
        similarity.setStrategy(strategy);
        strategy.addSimilarity(similarity);
        return similarity;
    }
}
