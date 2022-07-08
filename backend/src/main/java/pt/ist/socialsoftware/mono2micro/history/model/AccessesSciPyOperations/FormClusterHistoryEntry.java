package pt.ist.socialsoftware.mono2micro.history.model.AccessesSciPyOperations;

import pt.ist.socialsoftware.mono2micro.domain.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.AccessesSciPyDecomposition;
import pt.ist.socialsoftware.mono2micro.history.model.HistoryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormClusterHistoryEntry extends HistoryEntry {
    public static final String ACCESSES_SCIPY_FORM = "AccessesSciPyForm";

    private String newCluster;

    private Map<String, List<Short>> entities;

    public FormClusterHistoryEntry() {}

    public FormClusterHistoryEntry(AccessesSciPyDecomposition decomposition, String newName, Map<Short, List<Short>> entities) {
        this.newCluster = newName;
        this.entities = new HashMap<>();
        entities.forEach((clusterID, entitiesIDs) -> {
            Cluster cluster = decomposition.getCluster(clusterID);
            this.entities.put(cluster.getName(), entitiesIDs);
        });
    }

    @Override
    public String getOperationType() {
        return ACCESSES_SCIPY_FORM;
    }

    public String getNewCluster() {
        return newCluster;
    }

    public void setNewCluster(String newCluster) {
        this.newCluster = newCluster;
    }

    public Map<String, List<Short>> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, List<Short>> entities) {
        this.entities = entities;
    }
}
