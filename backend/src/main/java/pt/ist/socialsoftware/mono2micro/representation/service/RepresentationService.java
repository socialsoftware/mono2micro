package pt.ist.socialsoftware.mono2micro.representation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.socialsoftware.mono2micro.codebase.domain.Codebase;
import pt.ist.socialsoftware.mono2micro.codebase.repository.CodebaseRepository;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.representation.domain.Representation;
import pt.ist.socialsoftware.mono2micro.representation.domain.RepresentationFactory;
import pt.ist.socialsoftware.mono2micro.representation.repository.RepresentationRepository;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;
import pt.ist.socialsoftware.mono2micro.strategy.service.StrategyService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RepresentationService {
    @Autowired
    CodebaseRepository codebaseRepository;

    @Autowired
    StrategyService strategyService;

    @Autowired
    RepresentationRepository representationRepository;

    @Autowired
    GridFsService gridFsService;

    public void addRepresentations(String codebaseName, String representationInfoType, List<String> representationTypes, List<Object> representations) throws Exception {
        Codebase codebase = codebaseRepository.findByName(codebaseName);

        if (representationTypes == null && representations == null) { // representations already added
            return;
        }
        if (representationTypes == null || representations == null || representationTypes.size() != representations.size())
            throw new RuntimeException("Number of representations is different from the number of representation types.");

        List<String> availableRepresentationsTypes = codebase.getRepresentations().stream().map(Representation::getType).collect(Collectors.toList());

        for (String representationType : representationTypes)
            if (availableRepresentationsTypes.contains(representationType))
                throw new RuntimeException("Re-sending representations is not allowed.");

        for (int i = 0; i < representationTypes.size(); i++) {
            String representationType = representationTypes.get(i);
            byte[] representationFileStream = ((MultipartFile) representations.get(i)).getBytes();
            Representation representation = RepresentationFactory.getRepresentation(representationType);
            String fileName = representation.init(codebase, representationFileStream);
            codebase.addRepresentation(representation);
            gridFsService.saveFile(new ByteArrayInputStream(representationFileStream), fileName);
            representationRepository.save(representation);
        }
        codebaseRepository.save(codebase);
    }

    public Representation getCodebaseRepresentation(String codebaseName, String representationType) {
        Codebase codebase = codebaseRepository.findByName(codebaseName);
        return codebase.getRepresentations().stream().filter(representation -> representation.getType().equals(representationType)).findFirst()
                .orElseThrow(() -> new RuntimeException("No representation with type" + representationType));
    }

    public Representation getRepresentation(String representationId) {
        return representationRepository.findById(representationId).orElseThrow(() -> new RuntimeException("No representation " + representationId + " found."));
    }

    public void deleteRepresentation(String representationId) {
        Representation representation = representationRepository.findById(representationId).orElseThrow(() -> new RuntimeException("No representation with id " + representationId));
        gridFsService.deleteFile(representation.getName());
        representationRepository.deleteById(representationId);
    }

    public void deleteSingleRepresentation(String representationId) {
        Representation representation = representationRepository.findById(representationId).orElseThrow(() -> new RuntimeException("No representation with id " + representationId));
        Codebase codebase = representation.getCodebase();
        codebase.removeRepresentation(representationId);
        ArrayList<Strategy> strategies = new ArrayList<>();
        for (Strategy strategy: codebase.getStrategies()) {
            boolean deleted = false;
            for (String strategyType: strategy.getStrategyTypes()) {
                if (Strategy.strategiesToRepresentations.get(strategyType).contains(representation.getType())) {
                    strategyService.deleteStrategy(strategy);
                    deleted = true;
                    break;
                }
            }
            if (!deleted)
                strategies.add(strategy);
        }

        codebase.setStrategies(strategies);
        gridFsService.deleteFile(representation.getName());
        codebaseRepository.save(codebase);
        representationRepository.deleteById(representationId);
    }
}