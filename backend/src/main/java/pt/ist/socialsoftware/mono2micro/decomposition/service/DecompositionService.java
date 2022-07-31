package pt.ist.socialsoftware.mono2micro.decomposition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.dendrogram.repository.DendrogramRepository;

import static pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram.ACCESSES_SCIPY;

@Service
public class DecompositionService {
    @Autowired
    DendrogramRepository dendrogramRepository;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    AccessesSciPyDecompositionService accessesSciPyDecompositionService;

    public Decomposition getDecomposition(String decompositionName) {
        return decompositionRepository.findByName(decompositionName);
    }

    public void deleteSingleDecomposition(String decompositionName) {
        Decomposition decomposition = decompositionRepository.findByName(decompositionName);
        removeSpecificDecompositionProperties(decomposition);
        Strategy strategy = decomposition.getStrategy();
        strategy.removeDecomposition(decomposition.getName());
        dendrogramRepository.save(strategy);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    public void deleteDecomposition(Decomposition decomposition) {
        removeSpecificDecompositionProperties(decomposition);
        decompositionRepository.deleteByName(decomposition.getName());
    }

    private void removeSpecificDecompositionProperties(Decomposition decomposition) {
        switch (decomposition.getStrategyType()) {
            case ACCESSES_SCIPY:
                accessesSciPyDecompositionService.deleteDecompositionProperties((AccessesSciPyDecomposition) decomposition);
                break;
        }
    }
}
