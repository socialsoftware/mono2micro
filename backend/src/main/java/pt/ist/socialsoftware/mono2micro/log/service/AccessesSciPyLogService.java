package pt.ist.socialsoftware.mono2micro.log.service;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.service.AccessesSciPyDecompositionService;
import pt.ist.socialsoftware.mono2micro.fileManager.GridFsService;
import pt.ist.socialsoftware.mono2micro.log.domain.AccessesSciPyLog;
import pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.log.domain.Log;
import pt.ist.socialsoftware.mono2micro.log.domain.Operation;
import pt.ist.socialsoftware.mono2micro.log.repository.LogRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.FormClusterOperation.ACCESSES_SCIPY_FORM;
import static pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.MergeOperation.ACCESSES_SCIPY_MERGE;
import static pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.RenameOperation.ACCESSES_SCIPY_RENAME;
import static pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.SplitOperation.ACCESSES_SCIPY_SPLIT;
import static pt.ist.socialsoftware.mono2micro.log.domain.accessesSciPyOperations.TransferOperation.ACCESSES_SCIPY_TRANSFER;

@Service
public class AccessesSciPyLogService extends AbstractLogService {

    @Autowired
    AccessesSciPyDecompositionService decompositionService;

    @Autowired
    DecompositionRepository decompositionRepository;

    @Autowired
    LogRepository logRepository;

    @Autowired
    GridFsService gridFsService;

    public void addOperation(Decomposition d, Operation operation) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        AccessesSciPyLog decompositionLog = decomposition.getLog();

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
        logRepository.save(decompositionLog);
    }

    public void undoOperation(Decomposition d) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        Log log = decomposition.getLog();
        if (log.getCurrentLogOperationDepth() == 0)
            throw new RuntimeException("No more operations to undo");
        Operation operation = log.getCurrentLogOperation();

        switch (operation.getOperationType()) {
            case ACCESSES_SCIPY_RENAME:
                undoRename(decomposition, (RenameOperation) operation);
                break;
            case ACCESSES_SCIPY_MERGE:
                undoMerge(decomposition, (MergeOperation) operation);
                break;
            case ACCESSES_SCIPY_SPLIT:
                undoSplit(decomposition, (SplitOperation) operation);
                break;
            case ACCESSES_SCIPY_TRANSFER:
                undoTransfer(decomposition, (TransferOperation) operation);
                break;
            case ACCESSES_SCIPY_FORM:
                undoFormCluster(decomposition, (FormClusterOperation) operation);
                break;
        }
        log.decrementCurrentLogDepth();
        decomposition.setOutdated(true);

        logRepository.save(log);
        decompositionRepository.save(decomposition);
    }

    private void undoRename(AccessesSciPyDecomposition decomposition, RenameOperation operation) {
        decompositionService.renameCluster(decomposition, operation.getNewClusterName(), operation.getPreviousClusterName());
    }

    private void undoMerge(AccessesSciPyDecomposition decomposition, MergeOperation operation) {
        if (operation.getCluster1Name().equals(operation.getNewCluster())) {
            decompositionService.splitCluster(decomposition, operation.getNewCluster(), operation.getCluster2Name(), operation.getCluster2Entities());
            decompositionService.renameCluster(decomposition, operation.getNewCluster(), operation.getCluster1Name());
        }
        else {
            decompositionService.splitCluster(decomposition, operation.getNewCluster(), operation.getCluster1Name(), operation.getCluster1Entities());
            decompositionService.renameCluster(decomposition, operation.getNewCluster(), operation.getCluster2Name());
        }
    }

    private void undoSplit(AccessesSciPyDecomposition decomposition, SplitOperation operation) {
        decompositionService.mergeClusters(decomposition, operation.getOriginalCluster(), operation.getNewCluster(), operation.getOriginalCluster());
    }

    private void undoTransfer(AccessesSciPyDecomposition decomposition, TransferOperation operation) {
        decompositionService.transferEntities(decomposition, operation.getToCluster(), operation.getFromCluster(), operation.getEntities());
    }

    private void undoFormCluster(AccessesSciPyDecomposition decomposition, FormClusterOperation operation) {
        operation.getEntities().forEach((clusterName, entitiesID) -> {
            Cluster toCluster = decomposition.getClusterByName(clusterName);
            if (toCluster == null) // If there is no cluster, the operation is a split, if there is, it is a transfer
                decompositionService.splitCluster(decomposition, operation.getNewCluster(), clusterName, entitiesID.stream().map(Object::toString).collect(Collectors.joining(",")));
            else
                decompositionService.transferEntities(decomposition, operation.getNewCluster(), toCluster.getName(), entitiesID.stream().map(Object::toString).collect(Collectors.joining(",")));
        });

        decomposition.removeCluster(operation.getNewCluster());
        decompositionRepository.save(decomposition);
    }

    public void redoOperation(Decomposition d) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        Log log = decomposition.getLog();
        if (log.getCurrentLogOperationDepth() == log.getMaxLogDepth())
            throw new RuntimeException("No more operations to redo");
        log.incrementCurrentLogDepth();
        Operation operation = log.getCurrentLogOperation();

        switch (operation.getOperationType()) {
            case ACCESSES_SCIPY_RENAME:
                redoRename(decomposition, (RenameOperation) operation);
                break;
            case ACCESSES_SCIPY_MERGE:
                redoMerge(decomposition, (MergeOperation) operation);
                break;
            case ACCESSES_SCIPY_SPLIT:
                redoSplit(decomposition, (SplitOperation) operation);
                break;
            case ACCESSES_SCIPY_TRANSFER:
                redoTransfer(decomposition, (TransferOperation) operation);
                break;
            case ACCESSES_SCIPY_FORM:
                redoFormCluster(decomposition, (FormClusterOperation) operation);
                break;
        }
        decomposition.setOutdated(true);

        logRepository.save(log);
        decompositionRepository.save(decomposition);
    }

    private void redoRename(AccessesSciPyDecomposition decomposition, RenameOperation operation) {
        decompositionService.renameCluster(decomposition, operation.getPreviousClusterName(), operation.getNewClusterName());
    }

    private void redoMerge(AccessesSciPyDecomposition decomposition, MergeOperation operation) {
        decompositionService.mergeClusters(decomposition, operation.getCluster1Name(), operation.getCluster2Name(), operation.getNewCluster());
    }

    private void redoSplit(AccessesSciPyDecomposition decomposition, SplitOperation operation) {
        decompositionService.splitCluster(decomposition, operation.getOriginalCluster(), operation.getNewCluster(), operation.getEntities());
    }

    private void redoTransfer(AccessesSciPyDecomposition decomposition, TransferOperation operation) {
        decompositionService.transferEntities(decomposition, operation.getFromCluster(), operation.getToCluster(), operation.getEntities());
    }

    private void redoFormCluster(AccessesSciPyDecomposition decomposition, FormClusterOperation operation) {
        decompositionService.formCluster(decomposition, operation.getNewCluster(), operation.getEntities());
    }

    // Called when deleting an Accesses SciPy decomposition
    public void deleteDecompositionLog(AccessesSciPyLog log) {
        if (log != null) {
            log.getDepthGraphPositions().values().forEach(graphPositions -> gridFsService.deleteFile(graphPositions));
            logRepository.deleteByName(log.getName());
        }
    }

    public void saveGraphPositions(AccessesSciPyDecomposition decomposition, String graphPositions) {
        AccessesSciPyLog decompositionLog = decomposition.getLog();
        Long depth = decompositionLog.getCurrentLogOperationDepth();
        String fileName = decompositionLog.getName() + "_depth_" + depth;

        gridFsService.replaceFile(new ByteArrayInputStream(graphPositions.getBytes(StandardCharsets.UTF_8)), fileName);
        decompositionLog.putDepthGraphPosition(depth, fileName);
        logRepository.save(decompositionLog);
    }

    public String getGraphPositions(AccessesSciPyDecomposition decomposition) throws IOException {
        AccessesSciPyLog decompositionLog = decomposition.getLog();
        String fileName = decompositionLog.getDepthGraphPosition(decompositionLog.getCurrentLogOperationDepth());
        if (fileName == null)
            return null;
        InputStream file = gridFsService.getFile(fileName);
        return IOUtils.toString(file, StandardCharsets.UTF_8);
    }

    public void deleteGraphPositions(AccessesSciPyDecomposition decomposition) {
        AccessesSciPyLog decompositionLog = decomposition.getLog();
        String fileName = decompositionLog.getDepthGraphPosition(decompositionLog.getCurrentLogOperationDepth());
        gridFsService.deleteFile(fileName);
        decompositionLog.removeDepthGraphPosition(decompositionLog.getCurrentLogOperationDepth());
        logRepository.save(decompositionLog);
    }

    public Map<String, Boolean> canUndoRedo(AccessesSciPyDecomposition decomposition) {
        AccessesSciPyLog decompositionLog = decomposition.getLog();
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