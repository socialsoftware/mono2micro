package pt.ist.socialsoftware.mono2micro.recommendation.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.recommendation.domain.RecommendAccessesSciPy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.dendrogram.dto.AccessesSciPyDendrogramDto;
import pt.ist.socialsoftware.mono2micro.recommendation.dto.RecommendAccessesSciPyDto;
import pt.ist.socialsoftware.mono2micro.recommendation.repository.RecommendAccessesSciPyStrategyRepository;
import pt.ist.socialsoftware.mono2micro.dendrogram.repository.DendrogramRepository;
import pt.ist.socialsoftware.mono2micro.dendrogram.service.AccessesSciPyDendrogramService;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class RecommendAccessesSciPyStrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    DendrogramRepository dendrogramRepository;

    @Autowired
    RecommendAccessesSciPyStrategyRepository recommendAccessesSciPyStrategyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

    @Autowired
    AccessesSciPyDendrogramService accessesSciPyDendrogramService;

    @Autowired
    GridFsService gridFsService;

    public RecommendAccessesSciPy recommendAccessesSciPy(RecommendAccessesSciPyDto strategyDto) {
        Codebase codebase = codebaseRepository.findByName(strategyDto.getStrategyName());
        RecommendAccessesSciPy existingStrategy = (RecommendAccessesSciPy) codebase.getStrategies().stream()
                .filter(strategy -> strategy.equalsDto(strategyDto)).findFirst().orElse(null);

        RecommendAccessesSciPy strategy;
        // Create from scratch
        if (existingStrategy == null) {
            strategy = new RecommendAccessesSciPy(strategyDto);
            strategy.setCompleted(false);
            strategy.addCombinationsInProduction();
            codebase.addStrategy(strategy);
            strategy.setCodebase(codebase);

            // Get new name
            int i = 0;
            String strategyName;
            do {
                strategyName = strategyDto.getStrategyName() + " & " + strategyDto.getType() + ++i;
            } while (dendrogramRepository.existsByName(strategyName));
            strategy.setName(strategyName);

            recommendAccessesSciPyStrategyRepository.save(strategy);
            codebaseRepository.save(codebase);
        }
        // Already satisfies the requirements
        else {
            existingStrategy.setTraceTypes(strategyDto.getTraceTypes());
            existingStrategy.setLinkageTypes(strategyDto.getLinkageTypes());

            if (existingStrategy.containsRequestedCombinations() || !existingStrategy.isCompleted())
                return existingStrategy;
            // Adds to the already existing strategy
            else {
                existingStrategy.addCombinationsInProduction();
                existingStrategy.setCompleted(false);
                recommendAccessesSciPyStrategyRepository.save(existingStrategy);
                strategy = existingStrategy;
            }
        }

        clusteringAlgorithm.prepareAutowire();
        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                similarityGenerator.createSimilarityMatricesForSciPy(strategy);
                clusteringAlgorithm.generateMultipleDecompositions(strategy);

                strategy.addProducedCombinations();
                recommendAccessesSciPyStrategyRepository.save(strategy);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return strategy;
    }

    public String getRecommendationResultByStrategyName(String strategyName) throws IOException {
        RecommendAccessesSciPy strategy = recommendAccessesSciPyStrategyRepository.getRecommendationResultName(strategyName);
        return getRecommendationResult(strategy);
    }

    public void createDecompositions(String strategyName, List<String> decompositionNames) throws Exception {
        RecommendAccessesSciPy recommendationStrategy = recommendAccessesSciPyStrategyRepository.findByName(strategyName);
        Codebase codebase = recommendationStrategy.getCodebase();
        List<Strategy> codebaseStrategies = codebase.getStrategies();

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length != 8)
                continue;

            Constants.TraceType traceType = Constants.TraceType.valueOf(properties[5]);
            String linkageType = properties[6];

            System.out.println("Creating decomposition with name: " + name);

            AccessesSciPyDendrogramDto strategyInformation = new AccessesSciPyDendrogramDto(recommendationStrategy, traceType, linkageType, name);

            // Get or create the decomposition's strategy
            AccessesSciPyDendrogram strategy = (AccessesSciPyDendrogram) codebaseStrategies.stream().filter(possibleStrategy -> possibleStrategy.equalsDto(strategyInformation)).findFirst().orElse(null);
            if (strategy == null) {
                strategy = accessesSciPyDendrogramService.getNewAccessesSciPyDendrogramForStrategy(codebase, strategyInformation);

                strategy.setSimilarityMatrixName(strategy.getName() + "_similarityMatrix");
                InputStream inputStream = gridFsService.getFile(name.substring(0, name.lastIndexOf(","))); // Gets the previously produced similarity matrix
                gridFsService.saveFile(inputStream, strategy.getSimilarityMatrixName()); // And saves it with a different name

                // generate dendrogram image
                clusteringAlgorithm.createAccessesSciPyDendrogram(strategy);
            }

            clusteringAlgorithm.createDecomposition(strategy, "N", Float.parseFloat(properties[7]));
        }
        codebaseRepository.save(codebase);
    }

    private String getDecompositionName(AccessesSciPyDendrogram strategy, String name) {
        List<String> decompositionNames = strategy.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());
        if (decompositionNames.contains(strategy.getName() + " " + name)) {
            int i = 2;
            while (decompositionNames.contains(strategy.getName() + " " + name + "(" + i + ")"))
                i++;
            return strategy.getName() + " " + name + "(" + i + ")";

        } else return strategy.getName() + " " + name;
    }

    public String getRecommendationResult(RecommendAccessesSciPy strategy) throws IOException {
        return IOUtils.toString(gridFsService.getFile(strategy.getRecommendationResultName()), StandardCharsets.UTF_8);
    }

    public void replaceRecommendationResult(String recommendationResult, String recommendationResultName) {
        gridFsService.replaceFile(new ByteArrayInputStream(recommendationResult.getBytes()), recommendationResultName);
    }

    public void saveSimilarityMatrix(InputStream matrix, String similarityMatrixName) {
        gridFsService.saveFile(matrix, similarityMatrixName);
    }

    public void deleteStrategyProperties(RecommendAccessesSciPy strategy) { // Used to delete fields that can't otherwise be accessed by abstract Strategy
        gridFsService.deleteFiles(strategy.getSimilarityMatricesNames());
        gridFsService.deleteFile(strategy.getRecommendationResultName());
    }
}
