package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.decomposition.dto.request.SciPyRequestDto;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendForSciPy;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendForSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendationRepository;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityForSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.dto.SimilarityForSciPyDto;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.similarity.service.SimilarityService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.SimilarityMatrixGeneratorService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.Weights;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.weights.WeightsFactory;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.inteface.RecommendationsStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
public class RecommendSciPyService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

    @Autowired
    SimilarityMatrixGeneratorService similarityGenerator;

    @Autowired
    SimilarityService similarityService;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    GridFsService gridFsService;

    //TODO change buttons in frontend to radio bringing tracetype, linkageType and tracesmaxlimit
    public RecommendForSciPy createRecommendation(RecommendForSciPyDto recommendationDto) {
        RecommendationsStrategy strategy = (RecommendationsStrategy) strategyRepository.findByName(recommendationDto.getStrategyName());
        RecommendForSciPy existingRecommendation = (RecommendForSciPy) strategy.getRecommendations().stream()
                .filter(recommendation -> recommendation.equalsDto(recommendationDto)).findFirst().orElse(null);

        RecommendForSciPy recommendation;
        // Create from scratch
        if (existingRecommendation == null) {
            recommendation = new RecommendForSciPy(recommendationDto);
            recommendation.setCompleted(false);
            strategy.addRecommendation(recommendation);
            recommendation.setStrategy((Strategy) strategy);

            // Get new name
            int i = 0;
            String recommendationName;
            do {
                recommendationName = recommendationDto.getStrategyName() + " & " + recommendationDto.getType() + " R" + ++i;
            } while (recommendationRepository.existsByName(recommendationName));
            recommendation.setName(recommendationName);

            recommendationRepository.save(recommendation);
            strategyRepository.save((Strategy) strategy);
        }
        else return existingRecommendation;

        clusteringAlgorithm.prepareAutowire();
        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                similarityGenerator.createSimilarityMatrices(recommendation);
                clusteringAlgorithm.generateMultipleDecompositions(recommendation);

                recommendationRepository.save(recommendation);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return recommendation;
    }

    public void createDecompositions(RecommendForSciPy recommendation, List<String> decompositionNames) throws Exception {
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) strategyRepository.findByName(recommendation.getStrategy().getName());
        List<Similarity> similarities = strategy.getSimilarities();

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length > 2)
                continue;

            System.out.println("Creating decomposition with name: " + name);

            List<Weights> weightsList = WeightsFactory.getFactory().getWeightsList(recommendation.getAllWeightsTypes());

            int i = 1;
            for (Weights weights : weightsList) {
                float[] w = new float[]{weights.getNumberOfWeights()};
                for (int j = 0; j < w.length; j++)
                    w[j] = Float.parseFloat(properties[i++]);

                weights.setWeightsFromArray(w);
            }

            SimilarityForSciPyDto similarityInformation = new SimilarityForSciPyDto(recommendation, weightsList);

            // Get or create the decomposition's strategy
            SimilarityForSciPy similarity = (SimilarityForSciPy) similarities.stream().filter(possibleSimilarity -> possibleSimilarity.equalsDto(similarityInformation)).findFirst().orElse(null);
            if (similarity == null) {
                similarity = (SimilarityForSciPy) similarityService.getNewSimilarityForStrategy(strategy, similarityInformation);

                similarity.setSimilarityMatrixName(similarity.getName() + "_similarityMatrix");
                InputStream inputStream = gridFsService.getFile(name.substring(0, name.lastIndexOf(","))); // Gets the previously produced similarity matrix
                gridFsService.saveFile(inputStream, similarity.getSimilarityMatrixName()); // And saves it with a different name

                // generate dendrogram image
                clusteringAlgorithm.createDendrogramImage(similarity);
            }
            similarityRepository.save(similarity);

            clusteringAlgorithm.createDecomposition(new SciPyRequestDto(similarity.getName(), "N", Float.parseFloat(properties[properties.length - 1])));
        }
    }
}
