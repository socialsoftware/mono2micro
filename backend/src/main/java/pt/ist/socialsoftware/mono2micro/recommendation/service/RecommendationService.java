package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendForSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.interfaces.SimilarityMatrices;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendForSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.interfaces.SimilarityMatrices.SIMILARITY_MATRICES;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy.ACC_AND_REPO_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy.ACCESSES_SCIPY;
import static pt.ist.socialsoftware.mono2micro.strategy.domain.RepositorySciPyStrategy.REPOSITORY_SCIPY;

@Service
public class RecommendationService {
    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    RecommendSciPyService recommendSciPyService;

    @Autowired
    GridFsService gridFsService;

    public RecommendationDto createRecommendation(RecommendationDto recommendationDto) {
        switch (recommendationDto.getType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                return new RecommendForSciPyDto(recommendSciPyService.createRecommendation((RecommendForSciPyDto) recommendationDto));
            default:
                throw new RuntimeException("No know type " + recommendationDto.getType() + ".");
        }
    }

    public void createDecompositions(String recommendationName, List<String> decompositionNames) throws Exception {
        Recommendation recommendation = recommendationRepository.findByName(recommendationName);
        switch (recommendation.getType()) {
            case ACCESSES_SCIPY:
            case REPOSITORY_SCIPY:
            case ACC_AND_REPO_SCIPY:
                recommendSciPyService.createDecompositions((RecommendForSciPy) recommendation, decompositionNames);
                return;
            default:
                throw new RuntimeException("No know type " + recommendation.getType() + ".");
        }
    }

    public void deleteRecommendation(Recommendation recommendation) {
        if (recommendation.containsImplementation(SIMILARITY_MATRICES))
            gridFsService.deleteFiles(((SimilarityMatrices) recommendation).getSimilarityMatricesNames());

        gridFsService.deleteFile(recommendation.getRecommendationResultName());
        recommendationRepository.delete(recommendation);
    }

    public String getRecommendationResultFromName(String recommendationName) throws IOException {
        Recommendation recommendation = recommendationRepository.getRecommendationResultName(recommendationName);
        return getRecommendationResult(recommendation);
    }

    public String getRecommendationResult(Recommendation recommendation) throws IOException {
        return IOUtils.toString(gridFsService.getFile(recommendation.getRecommendationResultName()), StandardCharsets.UTF_8);
    }

    public void replaceRecommendationResult(String recommendationResult, String recommendationResultName) {
        gridFsService.replaceFile(new ByteArrayInputStream(recommendationResult.getBytes()), recommendationResultName);
    }
}