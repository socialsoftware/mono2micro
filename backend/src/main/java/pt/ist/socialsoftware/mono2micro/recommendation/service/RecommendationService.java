package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.Recommendation;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendationFactory;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.similarityGenerator.SimilarityMatrices;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendationDtoFactory;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.recommendation.domain.similarityGenerator.SimilarityMatrices.SIMILARITY_MATRICES;

@Service
public class RecommendationService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    DecompositionService decompositionService;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    GridFsService gridFsService;

    public RecommendationDto createRecommendation(RecommendationDto recommendationDto) {
        Strategy strategy = strategyRepository.findByName(recommendationDto.getStrategyName());
        RecommendMatrixSciPy existingRecommendation = (RecommendMatrixSciPy) strategy.getRecommendations().stream()
                .filter(recommendation -> recommendation.equalsDto(recommendationDto)).findFirst().orElse(null);

        Recommendation recommendation;
        // Create from scratch
        if (existingRecommendation == null) {
            recommendation = RecommendationFactory.getRecommendation(strategy, recommendationDto);
            recommendationRepository.save(recommendation);
            strategyRepository.save(strategy);
        }
        else return RecommendationDtoFactory.getRecommendationDto(existingRecommendation);

        recommendation.generateRecommendation(recommendationRepository, gridFsService);
        return RecommendationDtoFactory.getRecommendationDto(recommendation);
    }

    public void createDecompositions(String recommendationName, List<String> decompositionNames) throws Exception {
        Recommendation recommendation = recommendationRepository.findByName(recommendationName);
        recommendation.createDecompositions(decompositionService, gridFsService, decompositionNames);
    }

    public void deleteRecommendation(Recommendation recommendation) {
        if (recommendation.containsImplementation(SIMILARITY_MATRICES))
            gridFsService.deleteFiles(((SimilarityMatrices) recommendation).getSimilarityMatricesNames());

        gridFsService.deleteFile(recommendation.getRecommendationResultName());
        recommendationRepository.delete(recommendation);
    }

    public String getRecommendationResultFromName(String recommendationName) throws IOException {
        Recommendation recommendation = recommendationRepository.getRecommendationResultName(recommendationName);
        return IOUtils.toString(gridFsService.getFile(recommendation.getRecommendationResultName()), StandardCharsets.UTF_8);
    }
}