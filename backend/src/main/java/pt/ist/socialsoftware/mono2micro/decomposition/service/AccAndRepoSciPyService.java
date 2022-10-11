package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperationsWithPosition;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.repository.DecompositionOperationsRepository;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.service.PositionLogService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityMatrixSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

@Service
public class AccAndRepoSciPyService {
    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SimilarityRepository similarityRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    PositionLogService positionLogService;

    @Autowired
    DecompositionOperationsRepository decompositionOperationsRepository;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    FunctionalityRepository functionalityRepository;

    @Autowired
    AccessesDecompositionService accessesDecompositionService;

    public void snapshotDecomposition(String decompositionName) throws Exception {
        AccAndRepoSciPyDecomposition decomposition = (AccAndRepoSciPyDecomposition) accessesDecompositionService.updateOutdatedFunctionalitiesAndMetrics(decompositionName);
        SimilarityMatrixSciPy similarity = (SimilarityMatrixSciPy) decomposition.getSimilarity();
        String snapshotName = decomposition.getName() + " SNAPSHOT";

        // Find unused name
        if (similarity.getDecompositionByName(snapshotName) != null) {
            int i = 1;
            do {i++;} while (similarity.getDecompositionByName(snapshotName + "(" + i + ")") != null);
            snapshotName = snapshotName + "(" + i + ")";
        }
        AccAndRepoSciPyDecomposition snapshotDecomposition = new AccAndRepoSciPyDecomposition(decomposition);
        snapshotDecomposition.setName(snapshotName);

        // Duplicate functionalities for the new decomposition (also includes duplicating respective redesigns)
        for (Functionality functionality : decomposition.getFunctionalities().values()) {
            Functionality snapshotFunctionality = new Functionality(snapshotDecomposition.getName(), functionality);
            snapshotFunctionality.setFunctionalityRedesignNameUsedForMetrics(functionality.getFunctionalityRedesignNameUsedForMetrics());

            for (String redesignName : functionality.getFunctionalityRedesigns().keySet()) {
                FunctionalityRedesign functionalityRedesign = functionalityService.getFunctionalityRedesign(functionality, redesignName);
                snapshotFunctionality.addFunctionalityRedesign(functionalityRedesign.getName(), snapshotFunctionality.getId() + functionalityRedesign.getName());
                functionalityService.saveFunctionalityRedesign(snapshotFunctionality, functionalityRedesign);
            }
            snapshotDecomposition.addFunctionality(snapshotFunctionality);
            functionalityRepository.save(snapshotFunctionality);
        }

        DecompositionOperationsWithPosition snapshotLog = new DecompositionOperationsWithPosition(snapshotDecomposition);
        snapshotDecomposition.setDecompositionOperations(snapshotLog);
        decompositionOperationsRepository.save(snapshotLog);
        positionLogService.saveGraphPositions(snapshotDecomposition, positionLogService.getGraphPositions(decomposition));

        similarity.addDecomposition(snapshotDecomposition);
        Strategy strategy = strategyRepository.findByName(similarity.getStrategy().getName());
        strategy.addDecomposition(snapshotDecomposition);
        snapshotDecomposition.setStrategy(strategy);
        decompositionRepository.save(snapshotDecomposition);
        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }
}
