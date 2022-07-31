package pt.ist.socialsoftware.mono2micro.strategy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.source.service.SourceService;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.domain.StrategyFactory;
import pt.ist.socialsoftware.mono2micro.strategy.repository.StrategyRepository;

import java.util.List;

@Service
public class StrategyService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    StrategyRepository strategyRepository;

    @Autowired
    SourceService sourceService;

    public void createStrategy(String codebaseName, String strategyType, List<String> sourceTypes, List<Object> sources) throws Exception {
        sourceService.addSources(codebaseName, sourceTypes, sources);

        Codebase codebase = codebaseRepository.getCodebaseStrategies(codebaseName);
        if (codebase.getStrategyByType(strategyType) != null) {
            Strategy strategy = StrategyFactory.getFactory().getStrategy(strategyType);
            strategy.setName(codebaseName + " & " + strategyType);
            if (codebase.getSources().stream().anyMatch(source -> strategy.getSourceTypes().contains(source.getType()))) { // Check if all required sources exist
                codebase.addStrategy(strategy);
                strategyRepository.save(strategy);
                codebaseRepository.save(codebase);
            }
        }
    }
}
