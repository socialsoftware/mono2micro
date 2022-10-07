package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccAndRepoSciPy;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityRepository;
import pt.ist.socialsoftware.mono2micro.functionality.FunctionalityService;
import pt.ist.socialsoftware.mono2micro.functionality.domain.Functionality;
import pt.ist.socialsoftware.mono2micro.functionality.domain.FunctionalityRedesign;
import pt.ist.socialsoftware.mono2micro.log.domain.PositionLog;
import pt.ist.socialsoftware.mono2micro.log.repository.LogRepository;
import pt.ist.socialsoftware.mono2micro.log.service.PositionLogService;
import pt.ist.socialsoftware.mono2micro.similarity.domain.SimilarityForSciPy;
import pt.ist.socialsoftware.mono2micro.similarity.repository.SimilarityRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.AccAndRepoSciPyStrategy;
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
    LogRepository logRepository;

    @Autowired
    FunctionalityService functionalityService;

    @Autowired
    FunctionalityRepository functionalityRepository;

    @Autowired
    AccessesDecompositionService accessesDecompositionService;

    public void snapshotDecomposition(String decompositionName) throws Exception {
        AccAndRepoSciPy decomposition = (AccAndRepoSciPy) accessesDecompositionService.updateOutdatedFunctionalitiesAndMetrics(decompositionName);
        SimilarityForSciPy similarity = (SimilarityForSciPy) decomposition.getSimilarity();
        String snapshotName = decomposition.getName() + " SNAPSHOT";

        // Find unused name
        if (similarity.getDecompositionByName(snapshotName) != null) {
            int i = 1;
            do {i++;} while (similarity.getDecompositionByName(snapshotName + "(" + i + ")") != null);
            snapshotName = snapshotName + "(" + i + ")";
        }
        AccAndRepoSciPy snapshotDecomposition = new AccAndRepoSciPy(decomposition);
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

        PositionLog snapshotLog = new PositionLog(snapshotDecomposition);
        snapshotDecomposition.setLog(snapshotLog);
        logRepository.save(snapshotLog);
        positionLogService.saveGraphPositions(snapshotDecomposition, positionLogService.getGraphPositions(decomposition));

        similarity.addDecomposition(snapshotDecomposition);
        AccAndRepoSciPyStrategy strategy = (AccAndRepoSciPyStrategy) strategyRepository.findByName(similarity.getStrategy().getName());
        strategy.addDecomposition(snapshotDecomposition);
        snapshotDecomposition.setStrategy(strategy);
        decompositionRepository.save(snapshotDecomposition);
        similarityRepository.save(similarity);
        strategyRepository.save(strategy);
    }
}
