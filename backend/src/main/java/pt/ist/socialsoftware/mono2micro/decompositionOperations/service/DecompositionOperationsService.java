package pt.ist.socialsoftware.mono2micro.decompositionOperations.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperations;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperationsWithPosition;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.repository.DecompositionOperationsRepository;

import static pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperationsWithPosition.DECOMPOSITION_OPERATIONS_POSITION;

@Service
public class DecompositionOperationsService {
    @Autowired
    DecompositionOperationsRepository decompositionOperationsRepository;

    @Autowired
    GridFsService gridFsService;

    public void saveDecompositionOperations(DecompositionOperations operations) {
        decompositionOperationsRepository.save(operations);
    }

    public void deleteDecompositionOperations(DecompositionOperations decompositionOperations) {
        if (decompositionOperations.getType().equals(DECOMPOSITION_OPERATIONS_POSITION)) {
            DecompositionOperationsWithPosition positions = (DecompositionOperationsWithPosition) decompositionOperations;
            positions.getDepthGraphPositions().values().forEach(graphPositions -> gridFsService.deleteFile(graphPositions));
        }
        decompositionOperationsRepository.deleteByName(decompositionOperations.getName());
    }
}
