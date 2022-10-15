package pt.ist.socialsoftware.mono2micro.decomposition.domain.views;

import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.cluster.SciPyCluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;
import pt.ist.socialsoftware.mono2micro.operation.*;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewFormClusterOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewMergeOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewSplitOperation;
import pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewTransferOperation;
import pt.ist.socialsoftware.mono2micro.similarity.domain.Similarity;
import pt.ist.socialsoftware.mono2micro.strategy.domain.Strategy;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ist.socialsoftware.mono2micro.operation.RenameOperation.RENAME;
import static pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewFormClusterOperation.CLUSTER_VIEW_FORM;
import static pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewMergeOperation.CLUSTER_VIEW_MERGE;
import static pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewSplitOperation.CLUSTER_VIEW_SPLIT;
import static pt.ist.socialsoftware.mono2micro.operation.clusterView.ClusterViewTransferOperation.CLUSTER_VIEW_TRANSFER;

public interface ClusterViewDecomposition {
    Strategy getStrategy();
    Similarity getSimilarity();
    String getName();
    String getEdgeWeights(String viewType) throws Exception;
    String getSearchItems(String viewType) throws Exception;
    Decomposition snapshotDecomposition(String snapshotName) throws Exception; // This function can be placed in Decomposition if deemed relevant enough

    Map<String, Cluster> getClusters();
    Cluster getCluster(String clusterName);
    void addCluster(Cluster cluster);
    Cluster removeCluster(String clusterName);
    boolean clusterNameExists(String clusterName);

    void renameCluster(RenameOperation operation);
    void mergeClusters(MergeOperation operation);
    void splitCluster(SplitOperation operation);
    void transferEntities(TransferOperation operation);
    void formCluster(FormClusterOperation operation);

    default void transferCouplingDependencies(Set<Short> entities, String currentClusterName, String newClusterName) {
        for (Cluster cluster : getClusters().values())
            ((SciPyCluster) cluster).transferCouplingDependencies(entities, currentClusterName, newClusterName);
    }

    default void clusterViewRename(String clusterName, String newName) {
        if (clusterNameExists(newName) && !clusterName.equals(newName)) throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster oldCluster = removeCluster(clusterName);
        oldCluster.setName(newName);
        addCluster(oldCluster);

        // Change coupling dependencies
        getClusters().forEach((s, c) -> {
            SciPyCluster cluster = (SciPyCluster) c;
            Set<Short> dependencies = cluster.getCouplingDependencies().get(clusterName);
            if (dependencies != null) {cluster.getCouplingDependencies().remove(clusterName); cluster.addCouplingDependencies(newName, dependencies);}
        });
    }

    default void clusterViewMerge(String cluster1Name, String cluster2Name, String newName) {
        Cluster cluster1 = getCluster(cluster1Name);
        Cluster cluster2 = getCluster(cluster2Name);
        if (clusterNameExists(newName) && !cluster1.getName().equals(newName) && !cluster2.getName().equals(newName))
            throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster mergedCluster = new SciPyCluster(newName);

        Set<Element> allEntities = new HashSet<>(cluster1.getElements());
        allEntities.addAll(cluster2.getElements());
        mergedCluster.setElements(allEntities);

        transferCouplingDependencies(cluster1.getElementsIDs(), cluster1.getName(), mergedCluster.getName());
        transferCouplingDependencies(cluster2.getElementsIDs(), cluster2.getName(), mergedCluster.getName());

        removeCluster(cluster1Name);
        removeCluster(cluster2Name);
        addCluster(mergedCluster);
    }

    default void clusterViewSplit(String clusterName, String newName, String entitiesString) {
        String[] entities = entitiesString.split(",");
        if (clusterNameExists(newName)) throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");

        Cluster currentCluster = getCluster(clusterName);
        Cluster newCluster = new SciPyCluster(newName);

        for (String stringifiedEntityID : entities) {
            Element entity = currentCluster.getElementByID(Short.parseShort(stringifiedEntityID));

            if (entity != null) {
                newCluster.addElement(entity);
                currentCluster.removeElement(entity);
            }
        }
        transferCouplingDependencies(newCluster.getElementsIDs(), currentCluster.getName(), newCluster.getName());
        addCluster(newCluster);
    }

    default void clusterViewTransfer(String fromClusterName, String toClusterName, String entitiesString) {
        Cluster fromCluster = getCluster(fromClusterName);
        Cluster toCluster = getCluster(toClusterName);
        Set<Short> entities = Arrays.stream(entitiesString.split(",")).map(Short::valueOf).collect(Collectors.toSet());

        for (Short entityID : entities) {
            Element entity = fromCluster.getElementByID(entityID);
            if (entity != null) {
                toCluster.addElement(entity);
                fromCluster.removeElement(entity);
            }
        }
        transferCouplingDependencies(entities, fromClusterName, toClusterName);
    }

    default void clusterViewFormCluster(String newName, Map<String, List<Short>> entities) {
        List<Short> entitiesIDs = entities.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        if (clusterNameExists(newName)) {
            Cluster previousCluster = getClusters().values().stream().filter(cluster -> cluster.getName().equals(newName)).findFirst()
                    .orElseThrow(() -> new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists"));
            if (!entitiesIDs.containsAll(previousCluster.getElementsIDs()))
                throw new KeyAlreadyExistsException("Cluster with name: " + newName + " already exists");
        }

        Cluster newCluster = new SciPyCluster(newName);

        for (Short entityID : entitiesIDs) {
            Cluster currentCluster = getClusters().values().stream()
                    .filter(cluster -> cluster.containsElement(entityID)).findFirst()
                    .orElseThrow(() -> new RuntimeException("No cluster contains entity " + entityID));

            Element entity = currentCluster.getElementByID(entityID);
            newCluster.addElement(entity);
            currentCluster.removeElement(entityID);
            transferCouplingDependencies(Collections.singleton(entityID), currentCluster.getName(), newCluster.getName());
            if (currentCluster.getElements().size() == 0)
                removeCluster(currentCluster.getName());
        }
        addCluster(newCluster);
    }

    default void undoClusterViewOperation(Operation operation) {
        if (operation.getOperationType().equals(RENAME)) {
            RenameOperation renameOperation = (RenameOperation) operation;
            renameCluster(new RenameOperation(renameOperation.getNewClusterName(), renameOperation.getClusterName()));
        }
        else if (operation.getOperationType().equals(CLUSTER_VIEW_MERGE)) {
            ClusterViewMergeOperation mergeOperation = (ClusterViewMergeOperation) operation;
            if (mergeOperation.getCluster1Name().equals(mergeOperation.getNewName())) {
                splitCluster(new ClusterViewSplitOperation(mergeOperation.getNewName(), mergeOperation.getCluster2Name(), mergeOperation.getCluster2Entities()));
                renameCluster(new RenameOperation(mergeOperation.getNewName(), mergeOperation.getCluster1Name()));
            } else {
                splitCluster(new ClusterViewSplitOperation(mergeOperation.getNewName(), mergeOperation.getCluster1Name(), mergeOperation.getCluster1Entities()));
                renameCluster(new RenameOperation(mergeOperation.getNewName(), mergeOperation.getCluster2Name()));
            }
        }
        else if (operation.getOperationType().equals(CLUSTER_VIEW_SPLIT)) {
            ClusterViewSplitOperation splitOperation = (ClusterViewSplitOperation) operation;
            mergeClusters(new ClusterViewMergeOperation(splitOperation.getOriginalCluster(), splitOperation.getNewCluster(), splitOperation.getOriginalCluster()));
        }
        else if (operation.getOperationType().equals(CLUSTER_VIEW_TRANSFER)) {
            ClusterViewTransferOperation transferOperation = (ClusterViewTransferOperation) operation;
            transferEntities(new ClusterViewTransferOperation(transferOperation.getToCluster(), transferOperation.getFromCluster(), transferOperation.getEntities()));
        }
        else if (operation.getOperationType().equals(CLUSTER_VIEW_FORM)) {
            ClusterViewFormClusterOperation formClusterOperation = (ClusterViewFormClusterOperation) operation;
            formClusterOperation.getEntities().forEach((clusterName, entitiesID) -> {
                Cluster toCluster = getClusters().get(clusterName);
                if (toCluster == null) // If there is no cluster, the operation is a split, if there is, it is a transfer
                    splitCluster(new ClusterViewSplitOperation(formClusterOperation.getNewCluster(), clusterName, entitiesID.stream().map(Object::toString).collect(Collectors.joining(","))));
                else
                    transferEntities(new ClusterViewTransferOperation(formClusterOperation.getNewCluster(), toCluster.getName(), entitiesID.stream().map(Object::toString).collect(Collectors.joining(","))));
            });

            removeCluster(formClusterOperation.getNewCluster());
        }
    }

    default void redoClusterViewOperation(Operation operation) {
        if (operation.getOperationType().equals(RENAME))
            renameCluster((RenameOperation) operation);
        else if (operation.getOperationType().equals(CLUSTER_VIEW_MERGE))
            mergeClusters((MergeOperation) operation);
        else if (operation.getOperationType().equals(CLUSTER_VIEW_SPLIT))
            splitCluster((SplitOperation) operation);
        else if (operation.getOperationType().equals(CLUSTER_VIEW_TRANSFER))
            transferEntities((TransferOperation) operation);
        else if (operation.getOperationType().equals(CLUSTER_VIEW_FORM))
            formCluster((FormClusterOperation) operation);
    }
}
