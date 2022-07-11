package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.AccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.dto.RecommendAccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.repository.RecommendAccessesSciPyStrategyRepository;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;

@Service
public class RecommendAccessesSciPyStrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    RecommendAccessesSciPyStrategyRepository recommendAccessesSciPyStrategyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

    @Autowired
    AccessesSciPyStrategyService accessesSciPyStrategyService;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    SourceService sourceService;

    @Autowired
    GridFsService gridFsService;

    public RecommendAccessesSciPyStrategy recommendAccessesSciPy(RecommendAccessesSciPyStrategyDto strategyDto) {
        Codebase codebase = codebaseRepository.findByName(strategyDto.getCodebaseName());
        RecommendAccessesSciPyStrategy existingStrategy = (RecommendAccessesSciPyStrategy) codebase.getStrategies().stream()
                .filter(strategy -> strategy.equalsDto(strategyDto)).findFirst().orElse(null);

        RecommendAccessesSciPyStrategy strategy;
        // Create from scratch
        if (existingStrategy == null) {
            strategy = new RecommendAccessesSciPyStrategy(strategyDto);
            strategy.setCompleted(false);
            strategy.addCombinationsInProduction();
            codebase.addStrategy(strategy);
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
        RecommendAccessesSciPyStrategy strategy = recommendAccessesSciPyStrategyRepository.getRecommendationResultName(strategyName);
        return getRecommendationResult(strategy);
    }

    public void createDecompositions(String strategyName, List<String> decompositionNames) throws Exception {
        RecommendAccessesSciPyStrategy recommendationStrategy = recommendAccessesSciPyStrategyRepository.getRecommendationResultName(strategyName);
        AccessesSource source = (AccessesSource) recommendationStrategy.getCodebase().getSourceByType(ACCESSES);
        Codebase codebase = recommendationStrategy.getCodebase();
        List<Strategy> codebaseStrategies = codebase.getStrategies();

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length != 8)
                continue;

            Constants.TraceType traceType = Constants.TraceType.valueOf(properties[5]);
            String linkageType = properties[6];

            System.out.println("Creating decomposition with name: " + name);

            AccessesSciPyStrategyDto strategyInformation = new AccessesSciPyStrategyDto(recommendationStrategy, traceType, linkageType, name);

            // Get or create the decomposition's strategy
            AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) codebaseStrategies.stream().filter(possibleStrategy -> possibleStrategy.equalsDto(strategyInformation)).findFirst().orElse(null);
            if (strategy == null) {
                strategy = accessesSciPyStrategyService.getNewAccesssesSciPyStrategyForCodebase(codebase, strategyInformation);
                strategy.setDecompositions(new ArrayList<>());

                strategy.setSimilarityMatrixName(strategy.getName() + "_similarityMatrix");
                InputStream inputStream = gridFsService.getFile(name.substring(0, name.lastIndexOf(","))); // Gets the previously produced similarity matrix
                gridFsService.saveFile(inputStream, strategy.getSimilarityMatrixName()); // And saves it with a different name

                // generate dendrogram image
                clusteringAlgorithm.createAccessesSciPyDendrogram(strategy);
                codebase.addStrategy(strategy);
            }

            //Create the decomposition by copying the existing decomposition (new id equals new decomposition)
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) strategy.getDecompositionByName(name);
            decomposition.setName(getDecompositionName(strategy, "N" + properties[7]));
            decomposition.setStrategy(strategy);
            strategy.addDecomposition(decomposition);

            // This is done since previous coupling dependencies mess up the results during 'setupFunctionalities'
            for (Cluster cluster : decomposition.getClusters().values())
                cluster.clearCouplingDependencies();

            // Fill information regarding functionalities and their redesigns
            decomposition.setupFunctionalities(
                    sourceService.getSourceFileAsInputStream(source.getName()),
                    source.getProfile(strategy.getProfile()),
                    strategy.getTracesMaxLimit(),
                    strategy.getTraceType(),
                    true);

            // save strategy and decomposition
            decompositionRepository.save(decomposition);
            strategyRepository.save(strategy);
        }
        codebaseRepository.save(codebase);
    }

    private String getDecompositionName(AccessesSciPyStrategy strategy, String name) {
        List<String> decompositionNames = strategy.getDecompositions().stream().map(Decomposition::getName).collect(Collectors.toList());
        if (decompositionNames.contains(strategy.getName() + " " + name)) {
            int i = 2;
            while (decompositionNames.contains(strategy.getName() + " " + name + "(" + i + ")"))
                i++;
            return strategy.getName() + " " + name + "(" + i + ")";

        } else return strategy.getName() + " " + name;
    }

    public String getRecommendationResult(RecommendAccessesSciPyStrategy strategy) throws IOException {
        return IOUtils.toString(gridFsService.getFile(strategy.getRecommendationResultName()), StandardCharsets.UTF_8);
    }

    public void replaceRecommendationResult(String recommendationResult, String recommendationResultName) {
        gridFsService.replaceFile(new ByteArrayInputStream(recommendationResult.getBytes()), recommendationResultName);
    }

    public void saveSimilarityMatrix(InputStream matrix, String similarityMatrixName) {
        gridFsService.saveFile(matrix, similarityMatrixName);
    }

    public void deleteStrategyProperties(RecommendAccessesSciPyStrategy strategy) { // Used to delete fields that can't otherwise be accessed by abstract Strategy
        gridFsService.deleteFiles(strategy.getSimilarityMatricesNames());
        gridFsService.deleteFile(strategy.getRecommendationResultName());
    }
}
