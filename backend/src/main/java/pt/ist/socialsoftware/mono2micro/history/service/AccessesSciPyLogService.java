package pt.ist.socialsoftware.mono2micro.history.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.decomposition.repository.DecompositionRepository;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.accessesSciPy.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionLog;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionOperation;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.FormClusterDecompositionOperation.ACCESSES_SCIPY_FORM;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.MergeDecompositionOperation.ACCESSES_SCIPY_MERGE;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.RenameDecompositionOperation.ACCESSES_SCIPY_RENAME;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.SplitDecompositionOperation.ACCESSES_SCIPY_SPLIT;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.TransferDecompositionOperation.ACCESSES_SCIPY_TRANSFER;

@Service
public class AccessesSciPyLogService extends AbstractLogService {

    @Autowired
    DecompositionRepository decompositionRepository;

    public void addDecompositionOperation(Decomposition d, DecompositionOperation decompositionOperation) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        DecompositionLog decompositionLog = decomposition.getDecompositionHistory();
        if (decompositionLog == null) {
            decompositionLog = new DecompositionLog(decomposition);
            decomposition.setDecompositionHistory(decompositionLog);
        }
        decompositionLog.addLogOperation(decompositionOperation);
        logRepository.save(decompositionLog);
    }

    public void undoOperation(Decomposition d) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        DecompositionLog decompositionLog = decomposition.getDecompositionHistory();
        DecompositionOperation decompositionOperation = decompositionLog.getCurrentLogOperation();

        switch (decompositionOperation.getOperationType()) {
            case ACCESSES_SCIPY_RENAME:
                undoRename(decomposition, (RenameDecompositionOperation) decompositionOperation);
                break;
            case ACCESSES_SCIPY_MERGE:
                undoMerge(decomposition, (MergeDecompositionOperation) decompositionOperation);
                break;
            case ACCESSES_SCIPY_SPLIT:
                undoSplit(decomposition, (SplitDecompositionOperation) decompositionOperation);
                break;
            case ACCESSES_SCIPY_TRANSFER:
                undoTransfer(decomposition, (TransferDecompositionOperation) decompositionOperation);
                break;
            case ACCESSES_SCIPY_FORM:
                undoFormCluster(decomposition, (FormClusterDecompositionOperation) decompositionOperation);
                break;
        }
        decompositionLog.decrementCurrentLogDepth();
        decomposition.setOutdated(true);

        logRepository.save(decompositionLog);
        decompositionRepository.save(decomposition);
    }

    private void undoRename(AccessesSciPyDecomposition decomposition, RenameDecompositionOperation decompositionOperation) {
        Cluster cluster = decomposition.getClusterByName(decompositionOperation.getNewClusterName());
        decomposition.renameCluster(cluster.getID(), decompositionOperation.getPreviousClusterName());
    }

    private void undoMerge(AccessesSciPyDecomposition decomposition, MergeDecompositionOperation decompositionOperation) {
        Cluster cluster = decomposition.getClusterByName(decompositionOperation.getNewCluster());
        List<String> clusterNames = new ArrayList<>(decompositionOperation.getPreviousClusters().keySet());
        String[] cluster2Entities = decompositionOperation.getPreviousClusters().get(clusterNames.get(1)).toArray(new String[0]);
        decomposition.splitCluster(cluster.getID(), clusterNames.get(1), cluster2Entities);
        decomposition.renameCluster(cluster.getID(), clusterNames.get(0));
    }

    private void undoSplit(AccessesSciPyDecomposition decomposition, SplitDecompositionOperation decompositionOperation) {
        Cluster originalCluster = decomposition.getClusterByName(decompositionOperation.getOriginalCluster());
        Cluster newCluster = decomposition.getClusterByName(decompositionOperation.getNewCluster());

        decomposition.mergeClusters(originalCluster.getID(), newCluster.getID(), originalCluster.getName());
    }

    private void undoTransfer(AccessesSciPyDecomposition decomposition, TransferDecompositionOperation decompositionOperation) {
        Cluster fromCluster = decomposition.getClusterByName(decompositionOperation.getFromCluster());
        Cluster toCluster = decomposition.getClusterByName(decompositionOperation.getToCluster());

        decomposition.transferEntities(toCluster.getID(), fromCluster.getID(), decompositionOperation.getEntities().split(","));
    }

    private void undoFormCluster(AccessesSciPyDecomposition decomposition, FormClusterDecompositionOperation decompositionOperation) {
        //TODO form cluster undo
    }
}