package pt.ist.socialsoftware.mono2micro.history.service;

import org.springframework.stereotype.Service;
import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.*;
import pt.ist.socialsoftware.mono2micro.history.model.DecompositionHistory;
import pt.ist.socialsoftware.mono2micro.history.model.HistoryEntry;

import java.util.ArrayList;
import java.util.List;

import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.FormClusterHistoryEntry.ACCESSES_SCIPY_FORM;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.MergeHistoryEntry.ACCESSES_SCIPY_MERGE;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.RenameHistoryEntry.ACCESSES_SCIPY_RENAME;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.SplitHistoryEntry.ACCESSES_SCIPY_SPLIT;
import static pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations.TransferHistoryEntry.ACCESSES_SCIPY_TRANSFER;

@Service
public class AccessesSciPyHistoryService extends AbstractHistoryService {

    public void addHistoryEntry(Decomposition d, HistoryEntry historyEntry) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        DecompositionHistory decompositionHistory = decomposition.getDecompositionHistory();
        if (decompositionHistory == null) {
            decompositionHistory = new DecompositionHistory(decomposition);
            decomposition.setDecompositionHistory(decompositionHistory);
            decompositionHistory.setHistoryEntryList(new ArrayList<>());
        }
        decompositionHistory.addHistoryEntry(historyEntry);
        historyRepository.save(decompositionHistory);
    }

    public void undoOperation(Decomposition d) {
        AccessesSciPyDecomposition decomposition = (AccessesSciPyDecomposition) d;
        DecompositionHistory decompositionHistory = historyRepository.findByName(decomposition.getName());
        HistoryEntry historyEntry = decompositionHistory.getCurrentHistoryEntry();

        switch (historyEntry.getOperationType()) {
            case ACCESSES_SCIPY_RENAME:
                undoRename(decomposition, (RenameHistoryEntry) historyEntry);
                break;
            case ACCESSES_SCIPY_MERGE:
                undoMerge(decomposition, (MergeHistoryEntry) historyEntry);
                break;
            case ACCESSES_SCIPY_SPLIT:
                undoSplit(decomposition, (SplitHistoryEntry) historyEntry);
                break;
            case ACCESSES_SCIPY_TRANSFER:
                undoTransfer(decomposition, (TransferHistoryEntry) historyEntry);
                break;
            case ACCESSES_SCIPY_FORM:
                undoFormCluster(decomposition, (FormClusterHistoryEntry) historyEntry);
                break;
        }
        decompositionHistory.decrementCurrentHistoryEntry();

        //CodebaseManager codebaseManager = CodebaseManager.getInstance();
        //try {
        //    decomposition.setOutdated(true);
        //    codebaseManager.writeStrategyDecomposition(decomposition.getCodebaseName(), STRATEGIES_FOLDER, decomposition.getStrategyName(), decomposition);
        //    historyRepository.save(decompositionHistory);
        //} catch(IOException e) {
        //    System.err.println("Could not save file during undo.");
        //}
    }

    private void undoRename(AccessesSciPyDecomposition decomposition, RenameHistoryEntry historyEntry) {
        Cluster cluster = decomposition.getClusterByName(historyEntry.getNewClusterName());
        decomposition.renameCluster(cluster.getID(), historyEntry.getPreviousClusterName());
    }

    private void undoMerge(AccessesSciPyDecomposition decomposition, MergeHistoryEntry historyEntry) {
        Cluster cluster = decomposition.getClusterByName(historyEntry.getNewCluster());
        List<String> clusterNames = new ArrayList<>(historyEntry.getPreviousClusters().keySet());
        String[] cluster2Entities = historyEntry.getPreviousClusters().get(clusterNames.get(1)).toArray(new String[0]);
        decomposition.splitCluster(cluster.getID(), clusterNames.get(1), cluster2Entities);
        decomposition.renameCluster(cluster.getID(), clusterNames.get(0));
    }

    private void undoSplit(AccessesSciPyDecomposition decomposition, SplitHistoryEntry historyEntry) {
        Cluster originalCluster = decomposition.getClusterByName(historyEntry.getOriginalCluster());
        Cluster newCluster = decomposition.getClusterByName(historyEntry.getNewCluster());

        decomposition.mergeClusters(originalCluster.getID(), newCluster.getID(), originalCluster.getName());
    }

    private void undoTransfer(AccessesSciPyDecomposition decomposition, TransferHistoryEntry historyEntry) {
        Cluster fromCluster = decomposition.getClusterByName(historyEntry.getFromCluster());
        Cluster toCluster = decomposition.getClusterByName(historyEntry.getToCluster());

        decomposition.transferEntities(toCluster.getID(), fromCluster.getID(), historyEntry.getEntities().split(","));
    }

    private void undoFormCluster(AccessesSciPyDecomposition decomposition, FormClusterHistoryEntry historyEntry) {
        //TODO form cluster undo
    }
}