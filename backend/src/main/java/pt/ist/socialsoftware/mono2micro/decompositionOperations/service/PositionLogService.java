package pt.ist.socialsoftware.mono2micro.decompositionOperations.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.interfaces.AccessesDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesDecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperationsWithPosition;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.domain.DecompositionOperations;
import pt.ist.socialsoftware.mono2micro.decompositionOperations.repository.DecompositionOperationsRepository;
import pt.ist.socialsoftware.mono2micro.operation.Operation;
import pt.ist.socialsoftware.mono2micro.operation.RenameOperation;
import pt.ist.socialsoftware.mono2micro.operation.accesses.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.operation.RenameOperation.RENAME;
import static pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesFormClusterOperation.ACCESSES_FORM;
import static pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesMergeOperation.ACCESSES_MERGE;
import static pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesSplitOperation.ACCESSES_SPLIT;
import static pt.ist.socialsoftware.mono2micro.operation.accesses.AccessesTransferOperation.ACCESSES_TRANSFER;

@Service
public class PositionLogService extends AbstractLogService {

    @Autowired
    AccessesDecompositionService accessesDecompositionService;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    DecompositionOperationsRepository decompositionOperationsRepository;

    @Autowired
    GridFsService gridFsService;

    @Override
    public void addOperation(Decomposition decomposition, Operation operation) {
        DecompositionOperationsWithPosition decompositionLog = (DecompositionOperationsWithPosition) decomposition.getDecompositionOperations();

        Long newLogOperationDepth = decompositionLog.incrementCurrentLogDepth();
        operation.setHistoryDepth(newLogOperationDepth);

        // Remove conflicting Decomposition operations that have been overridden by the new operation
        decompositionLog.setLogOperationsList(decompositionLog.getLogOperationList().stream().filter(op ->
                op.getHistoryDepth() < newLogOperationDepth).collect(Collectors.toList()));

        decompositionLog.getDepthGraphPositions().entrySet().removeIf(entry -> {
            if (entry.getKey() >= newLogOperationDepth) {
                gridFsService.deleteFile(entry.getValue());
                return true;
            }
            else return false;
        });

        decompositionLog.getLogOperationList().add(operation);
        decompositionOperationsRepository.save(decompositionLog);
    }

    @Override
    public void undoOperation(Decomposition d) {
        DecompositionOperations decompositionOperations = d.getDecompositionOperations();
        AccessesDecomposition decomposition = (AccessesDecomposition) d;
        if (decompositionOperations.getCurrentLogOperationDepth() == 0)
            throw new RuntimeException("No more operations to undo");
        Operation operation = decompositionOperations.getCurrentLogOperation();

        switch (operation.getOperationType()) {
            case RENAME:
                undoRename(decomposition, (RenameOperation) operation);
                break;
            case ACCESSES_MERGE:
                undoMerge(decomposition, (AccessesMergeOperation) operation);
                break;
            case ACCESSES_SPLIT:
                undoSplit(decomposition, (AccessesSplitOperation) operation);
                break;
            case ACCESSES_TRANSFER:
                undoTransfer(decomposition, (AccessesTransferOperation) operation);
                break;
            case ACCESSES_FORM:
                undoFormCluster(decomposition, (AccessesFormClusterOperation) operation);
                break;
        }
        decompositionOperations.decrementCurrentLogDepth();
        decomposition.setOutdated(true);

        decompositionOperationsRepository.save(decompositionOperations);
        decompositionRepository.save(d);
    }

    private void undoRename(AccessesDecomposition decomposition, RenameOperation operation) {
        accessesDecompositionService.renameCluster(decomposition, operation.getNewClusterName(), operation.getClusterName());
    }

    private void undoMerge(AccessesDecomposition decomposition, AccessesMergeOperation operation) {
        if (operation.getCluster1Name().equals(operation.getNewName())) {
            accessesDecompositionService.splitCluster(decomposition, operation.getNewName(), operation.getCluster2Name(), operation.getCluster2Entities());
            accessesDecompositionService.renameCluster(decomposition, operation.getNewName(), operation.getCluster1Name());
        }
        else {
            accessesDecompositionService.splitCluster(decomposition, operation.getNewName(), operation.getCluster1Name(), operation.getCluster1Entities());
            accessesDecompositionService.renameCluster(decomposition, operation.getNewName(), operation.getCluster2Name());
        }
    }

    private void undoSplit(AccessesDecomposition decomposition, AccessesSplitOperation operation) {
        accessesDecompositionService.mergeClusters(decomposition, operation.getOriginalCluster(), operation.getNewCluster(), operation.getOriginalCluster());
    }

    private void undoTransfer(AccessesDecomposition decomposition, AccessesTransferOperation operation) {
        accessesDecompositionService.transferEntities(decomposition, operation.getToCluster(), operation.getFromCluster(), operation.getEntities());
    }

    private void undoFormCluster(AccessesDecomposition decomposition, AccessesFormClusterOperation operation) {
        operation.getEntities().forEach((clusterName, entitiesID) -> {
            Cluster toCluster = decomposition.getCluster(clusterName);
            if (toCluster == null) // If there is no cluster, the operation is a split, if there is, it is a transfer
                accessesDecompositionService.splitCluster(decomposition, operation.getNewCluster(), clusterName, entitiesID.stream().map(Object::toString).collect(Collectors.joining(",")));
            else
                accessesDecompositionService.transferEntities(decomposition, operation.getNewCluster(), toCluster.getName(), entitiesID.stream().map(Object::toString).collect(Collectors.joining(",")));
        });

        decomposition.removeCluster(operation.getNewCluster());
        decompositionRepository.save((Decomposition) decomposition);
    }

    @Override
    public void redoOperation(Decomposition d) {
        AccessesDecomposition decomposition = (AccessesDecomposition) d;
        DecompositionOperations decompositionOperations = d.getDecompositionOperations();
        if (decompositionOperations.getCurrentLogOperationDepth() == decompositionOperations.getMaxLogDepth())
            throw new RuntimeException("No more operations to redo");
        decompositionOperations.incrementCurrentLogDepth();
        Operation operation = decompositionOperations.getCurrentLogOperation();

        switch (operation.getOperationType()) {
            case RENAME:
                redoRename(decomposition, (RenameOperation) operation);
                break;
            case ACCESSES_MERGE:
                redoMerge(decomposition, (AccessesMergeOperation) operation);
                break;
            case ACCESSES_SPLIT:
                redoSplit(decomposition, (AccessesSplitOperation) operation);
                break;
            case ACCESSES_TRANSFER:
                redoTransfer(decomposition, (AccessesTransferOperation) operation);
                break;
            case ACCESSES_FORM:
                redoFormCluster(decomposition, (AccessesFormClusterOperation) operation);
                break;
        }
        decomposition.setOutdated(true);

        decompositionOperationsRepository.save(decompositionOperations);
        decompositionRepository.save(d);
    }

    private void redoRename(AccessesDecomposition decomposition, RenameOperation operation) {
        accessesDecompositionService.renameCluster(decomposition, operation.getClusterName(), operation.getNewClusterName());
    }

    private void redoMerge(AccessesDecomposition decomposition, AccessesMergeOperation operation) {
        accessesDecompositionService.mergeClusters(decomposition, operation.getCluster1Name(), operation.getCluster2Name(), operation.getNewName());
    }

    private void redoSplit(AccessesDecomposition decomposition, AccessesSplitOperation operation) {
        accessesDecompositionService.splitCluster(decomposition, operation.getOriginalCluster(), operation.getNewCluster(), operation.getEntities());
    }

    private void redoTransfer(AccessesDecomposition decomposition, AccessesTransferOperation operation) {
        accessesDecompositionService.transferEntities(decomposition, operation.getFromCluster(), operation.getToCluster(), operation.getEntities());
    }

    private void redoFormCluster(AccessesDecomposition decomposition, AccessesFormClusterOperation operation) {
        accessesDecompositionService.formCluster(decomposition, operation.getNewCluster(), operation.getEntities());
    }

    public void saveGraphPositions(Decomposition decomposition, String graphPositions) {
        DecompositionOperationsWithPosition decompositionLog = (DecompositionOperationsWithPosition) decomposition.getDecompositionOperations();
        Long depth = decompositionLog.getCurrentLogOperationDepth();
        String fileName = decompositionLog.getName() + "_depth_" + depth;

        gridFsService.replaceFile(new ByteArrayInputStream(graphPositions.getBytes(StandardCharsets.UTF_8)), fileName);
        decompositionLog.putDepthGraphPosition(depth, fileName);
        decompositionOperationsRepository.save(decompositionLog);
    }

    public String getGraphPositions(Decomposition decomposition) throws IOException {
        DecompositionOperationsWithPosition decompositionLog = (DecompositionOperationsWithPosition) decomposition.getDecompositionOperations();
        String fileName = decompositionLog.getDepthGraphPosition(decompositionLog.getCurrentLogOperationDepth());
        if (fileName == null)
            return null;
        InputStream file = gridFsService.getFile(fileName);
        return IOUtils.toString(file, StandardCharsets.UTF_8);
    }

    public void deleteGraphPositions(Decomposition decomposition) {
        DecompositionOperationsWithPosition decompositionLog = (DecompositionOperationsWithPosition) decomposition.getDecompositionOperations();
        String fileName = decompositionLog.getDepthGraphPosition(decompositionLog.getCurrentLogOperationDepth());
        gridFsService.deleteFile(fileName);
        decompositionLog.removeDepthGraphPosition(decompositionLog.getCurrentLogOperationDepth());
        decompositionOperationsRepository.save(decompositionLog);
    }

    public Map<String, Boolean> canUndoRedo(Decomposition decomposition) {
        DecompositionOperationsWithPosition decompositionLog = (DecompositionOperationsWithPosition) decomposition.getDecompositionOperations();
        Map<String, Boolean> canUndoRedo = new HashMap<>();
        if (decompositionLog.getMaxLogDepth() == decompositionLog.getCurrentLogOperationDepth())
            canUndoRedo.put("redo", false);
        else canUndoRedo.put("redo", true);
        if (decompositionLog.getCurrentLogOperationDepth() == 0)
            canUndoRedo.put("undo", false);
        else canUndoRedo.put("undo", true);
        return canUndoRedo;
    }
}