package pt.ist.socialsoftware.mono2micro.dendrogram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.DecompositionService;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.Dendrogram;
import pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.dendrogram.repository.DendrogramRepository;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;

import static pt.ist.socialsoftware.mono2micro.dendrogram.domain.AccessesSciPyDendrogram.ACCESSES_SCIPY;

@Service
public class DendrogramService {
    @Autowired
    StrategyRepository strategyRepository;
    @Autowired
    DendrogramRepository dendrogramRepository;

    @Autowired
    AccessesSciPyDendrogramService accessesSciPyDendrogramService;

    @Autowired
    DecompositionService decompositionService;

    public void deleteSingleDendrogram(String dendrogramName) {
        Dendrogram dendrogram = dendrogramRepository.findByName(dendrogramName);
        Strategy strategy = dendrogram.getStrategy();
        strategy.removeDendrogram(dendrogramName);
        dendrogram.getDecompositions().forEach(decomposition -> decompositionService.deleteDecomposition(decomposition));
        removeSpecificDendrogramProperties(dendrogram);
        strategyRepository.save(strategy);
        dendrogramRepository.deleteByName(dendrogramName);
    }

    public void deleteDendrogram(Dendrogram dendrogram) { // Used when strategy is deleted
        dendrogram.getDecompositions().forEach(decomposition -> decompositionService.deleteDecomposition(decomposition));
        removeSpecificDendrogramProperties(dendrogram);
        dendrogramRepository.deleteByName(dendrogram.getName());
    }

    private void removeSpecificDendrogramProperties(Dendrogram dendrogram) {
        switch (dendrogram.getType()) {
            case ACCESSES_SCIPY:
                accessesSciPyDendrogramService.deleteDendrogramProperties((AccessesSciPyDendrogram) dendrogram);
                break;
        }
    }

    public List<Decomposition> getDecompositions(String dendrogramName) {
        Dendrogram dendrogram = dendrogramRepository.findByName(dendrogramName);
        return dendrogram.getDecompositions();
    }

    public Dendrogram getDendrogram(String dendrogramName) {
        return dendrogramRepository.findByName(dendrogramName);
    }
}
