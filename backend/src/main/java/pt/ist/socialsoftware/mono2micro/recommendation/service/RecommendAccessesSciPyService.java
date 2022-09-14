package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.AccessesSciPyDendrogramDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendAccessesSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendAccessesSciPyRepository;
import pt.ist.socialsoftware.mono2micro.dendrogram.service.AccessesSciPyDendrogramService;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static pt.ist.socialsoftware.mono2micro.representation.domain.AccessesRepresentation.ACCESSES;

@Service
public class RecommendAccessesSciPyService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    RecommendAccessesSciPyRepository recommendationRepository;

    @Autowired
    RecommendAccessesSciPyRepository recommendAccessesSciPyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

    @Autowired
    AccessesSciPyDendrogramService accessesSciPyDendrogramService;

    @Autowired
    GridFsService gridFsService;

    public RecommendAccessesSciPy recommendAccessesSciPy(RecommendAccessesSciPyDto recommendationDto) {
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) strategyRepository.findByName(recommendationDto.getStrategyName());
        RecommendAccessesSciPy existingRecommendation = (RecommendAccessesSciPy) strategy.getRecommendations().stream()
                .filter(recommendation -> recommendation.equalsDto(recommendationDto)).findFirst().orElse(null);

        RecommendAccessesSciPy recommendation;
        // Create from scratch
        if (existingRecommendation == null) {
            recommendation = new RecommendAccessesSciPy(recommendationDto);
            recommendation.setCompleted(false);
            recommendation.addCombinationsInProduction();
            strategy.addRecommendation(recommendation);
            recommendation.setStrategy(strategy);

            // Get new name
            int i = 0;
            String recommendationName;
            do {
                recommendationName = recommendationDto.getStrategyName() + " & " + recommendationDto.getType() + ++i;
            } while (recommendationRepository.existsByName(recommendationName));
            recommendation.setName(recommendationName);

            recommendAccessesSciPyRepository.save(recommendation);
            strategyRepository.save(strategy);
        }
        // Already satisfies the requirements
        else {
            existingRecommendation.setTraceTypes(recommendationDto.getTraceTypes());
            existingRecommendation.setLinkageTypes(recommendationDto.getLinkageTypes());

            if (existingRecommendation.containsRequestedCombinations() || !existingRecommendation.isCompleted())
                return existingRecommendation;
            // Adds to the already existing recommendation
            else {
                existingRecommendation.addCombinationsInProduction();
                existingRecommendation.setCompleted(false);
                recommendAccessesSciPyRepository.save(existingRecommendation);
                recommendation = existingRecommendation;
            }
        }

        AccessesRepresentation representation = (AccessesRepresentation) recommendation.getStrategy().getCodebase().getRepresentationByType(ACCESSES);

        clusteringAlgorithm.prepareAutowire();
        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                similarityGenerator.createSimilarityMatricesForSciPy(representation, recommendation);
                clusteringAlgorithm.generateMultipleDecompositions(recommendation);

                recommendation.addProducedCombinations();
                recommendAccessesSciPyRepository.save(recommendation);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return recommendation;
    }

    public String getRecommendationResultByName(String recommendationName) throws IOException {
        RecommendAccessesSciPy recommendation = recommendAccessesSciPyRepository.getRecommendationResultName(recommendationName);
        return getRecommendationResult(recommendation);
    }

    public void createDecompositions(String recommendationName, List<String> decompositionNames) throws Exception {
        RecommendAccessesSciPy recommendation = recommendAccessesSciPyRepository.findByName(recommendationName);
        AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) strategyRepository.findByName(recommendation.getStrategy().getName());
        List<Dendrogram> dendrograms = strategy.getDendrograms();

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length != 8)
                continue;

            Constants.TraceType traceType = Constants.TraceType.valueOf(properties[5]);
            String linkageType = properties[6];

            System.out.println("Creating decomposition with name: " + name);

            AccessesSciPyDendrogramDto dendrogramInformation = new AccessesSciPyDendrogramDto(recommendation, traceType, linkageType, name);

            // Get or create the decomposition's strategy
            AccessesSciPyDendrogram dendrogram = (AccessesSciPyDendrogram) dendrograms.stream().filter(possibleDendrogram -> possibleDendrogram.equalsDto(dendrogramInformation)).findFirst().orElse(null);
            if (dendrogram == null) {
                dendrogram = accessesSciPyDendrogramService.getNewAccessesSciPyDendrogramForStrategy(strategy, dendrogramInformation);

                dendrogram.setSimilarityMatrixName(dendrogram.getName() + "_similarityMatrix");
                InputStream inputStream = gridFsService.getFile(name.substring(0, name.lastIndexOf(","))); // Gets the previously produced similarity matrix
                gridFsService.saveFile(inputStream, dendrogram.getSimilarityMatrixName()); // And saves it with a different name

                // generate dendrogram image
                similarityGenerator.createAccessesSciPyDendrogram(dendrogram);
            }

            clusteringAlgorithm.createDecomposition(strategy, dendrogram, "N", Float.parseFloat(properties[7]));
        }
    }

    public String getRecommendationResult(RecommendAccessesSciPy recommendation) throws IOException {
        return IOUtils.toString(gridFsService.getFile(recommendation.getRecommendationResultName()), StandardCharsets.UTF_8);
    }

    public void replaceRecommendationResult(String recommendationResult, String recommendationResultName) {
        gridFsService.replaceFile(new ByteArrayInputStream(recommendationResult.getBytes()), recommendationResultName);
    }

    public void saveSimilarityMatrix(InputStream matrix, String similarityMatrixName) {
        gridFsService.saveFile(matrix, similarityMatrixName);
    }

    public void deleteStrategyProperties(RecommendAccessesSciPy recommendation) { // Used to delete fields that can't otherwise be accessed by abstract Recommendation
        gridFsService.deleteFiles(recommendation.getSimilarityMatricesNames());
        gridFsService.deleteFile(recommendation.getRecommendationResultName());
    }
}
