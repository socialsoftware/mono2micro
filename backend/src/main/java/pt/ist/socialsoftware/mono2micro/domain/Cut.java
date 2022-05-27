package pt.ist.socialsoftware.mono2micro.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pt.ist.socialsoftware.mono2micro.utils.deserializers.CutDeserializer;

import java.util.*;

@JsonDeserialize(using = CutDeserializer.class)
public class Cut {
    private HashMap<Short, Cluster> clusters = new HashMap<Short, Cluster>();

    public void addNewCluster(Cluster c) {
        this.clusters.put(c.getID(), c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cut cut = (Cut) o;
        if (clusters.size() != cut.clusters.size()) return false;
        for (Short i = 0; i < clusters.size(); i++) {
            Cluster cluster1 = clusters.get(i);
            Cluster cluster2 = cut.clusters.get(i);
            if (!cluster1.getEntities().containsAll(cluster2.getEntities()) || !cluster2.getEntities().containsAll(cluster1.getEntities())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (short i = 0; i < clusters.size(); i++) {
            ArrayList<Short> sortedEntities = new ArrayList<>(clusters.get(i).getEntities());
            Collections.sort(sortedEntities);
            result.append(i).append("->").append(sortedEntities).append(";");
        }
        return result.toString();
    }

    public HashMap<Short, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(HashMap<Short, Cluster> clusters) {
        this.clusters = clusters;
    }
}
