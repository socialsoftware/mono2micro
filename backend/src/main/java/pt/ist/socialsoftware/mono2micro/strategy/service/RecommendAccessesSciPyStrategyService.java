package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.clusteringAlgorithm.SciPyClusteringAlgorithmService;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.similarityGenerator.AccessesSimilarityGeneratorService;
import pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.RecommendAccessesSciPyStrategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.dto.RecommendAccessesSciPyStrategyDto;
import pt.ist.socialsoftware.mono2micro.strategy.repository.RecommendAccessesSciPyStrategyRepository;
import pt.ist.socialsoftware.mono2micro.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static pt.ist.socialsoftware.mono2micro.source.domain.AccessesSource.ACCESSES;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.RECOMMEND_FOLDER;
import static pt.ist.socialsoftware.mono2micro.utils.Constants.STRATEGIES_FOLDER;

@Service
public class RecommendAccessesSciPyStrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    RecommendAccessesSciPyStrategyRepository strategyRepository;

    @Autowired
    AccessesSimilarityGeneratorService similarityGenerator;

    @Autowired
    SciPyClusteringAlgorithmService clusteringAlgorithm;

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
            strategyRepository.save(strategy);
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
                strategyRepository.save(existingStrategy);
                strategy = existingStrategy;
            }
        }

        // Executes the request in a fork to avoid blocking the user
        ForkJoinPool.commonPool().submit(() -> {
            try {
                similarityGenerator.createSimilarityMatricesForSciPy(strategy);
                clusteringAlgorithm.generateMultipleDecompositions(strategy);

                strategy.addProducedCombinations();
                strategyRepository.save(strategy);
                System.out.println("recommendation ended");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return strategy;
    }

    public String getRecommendationResultByStrategyName(String strategyName) throws IOException {
        RecommendAccessesSciPyStrategy strategy = strategyRepository.getRecommendationResultName(strategyName);
        return getRecommendationResult(strategy);
    }

    public void createDecompositions(String strategyName, List<String> decompositionNames) throws Exception {
        RecommendAccessesSciPyStrategy recommendationStrategy = strategyRepository.getRecommendationResultName(strategyName);
        AccessesSource source = (AccessesSource) recommendationStrategy.getCodebase().getSourceByType(ACCESSES);

        for (String name : decompositionNames) {
            String[] properties = name.split(",");

            if (properties.length != 8)
                continue;

            Constants.TraceType traceType = Constants.TraceType.valueOf(properties[5]);
            String linkageType = properties[6];

            System.out.println("Creating decomposition with name: " + name);

            AccessesSciPyStrategy strategyInformation = new AccessesSciPyStrategy(recommendationStrategy, traceType, linkageType, name);

            // Get or create the decomposition's strategy
            List<Strategy> strategies = fileManager.getCodebaseStrategies(codebaseName, STRATEGIES_FOLDER, Collections.singletonList(strategyInformation.getType()));
            //TODO depois disto eliminar o equals das strategies
            AccessesSciPyStrategy strategy = (AccessesSciPyStrategy) strategies.stream().filter(possibleStrategy -> possibleStrategy.equals(strategyInformation)).findFirst().orElse(null);
            if (strategy == null) {
                strategy = (AccessesSciPyStrategy) fileManager.createCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategyInformation);

                fileManager.transferSimilarityMatrixFromRecommendation(
                        codebaseName,
                        recommendationStrategy.getName(),
                        properties[0] + "," + properties[1] + "," + properties[2] + "," + properties[3] + "," + properties[4] + "," + properties[5] + ".json",
                        "similarityMatrix.json",
                        strategy);

                // generate dendrogram image
                //ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithmFactory.getFactory().getClusteringAlgorithm(strategy.getType());
                //clusteringAlgorithm.createDendrogram(strategy);
            }

            //Create the decomposition by copying the existing decomposition
            AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) fileManager
                    .transferDecompositionFromRecommendation(
                            codebaseName,
                            recommendationStrategy.getName(),
                            name,
                            getDecompositionName(strategy, "N" + properties[6]),
                            strategy);

            // This is done since previous coupling dependencies mess up the results during 'setupFunctionalities'
            for (Cluster cluster : decomposition.getClusters().values())
                cluster.clearCouplingDependencies();

            // Fill information regarding functionalities and their redesigns
            decomposition.setupFunctionalities(
                    //source.getSourceFilePath(),
                    new ByteArrayInputStream("TODO".getBytes()),
                    source.getProfile(strategy.getProfile()),
                    strategy.getTracesMaxLimit(),
                    strategy.getTraceType(),
                    true);

            // save strategy and decomposition
            fileManager.writeStrategyDecomposition(codebaseName, STRATEGIES_FOLDER, decomposition.getStrategyName(), decomposition);
            fileManager.writeCodebaseStrategy(codebaseName, STRATEGIES_FOLDER, strategy);
        }
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
