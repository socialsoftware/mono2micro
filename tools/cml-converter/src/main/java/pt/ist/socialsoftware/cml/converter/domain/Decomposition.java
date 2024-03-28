package pt.ist.socialsoftware.cml.converter.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Decomposition {
    private String name;
    private Set<Cluster> clusters;
    private Set<Entity> entities;
    private Set<Functionality> functionalities;

    public Decomposition(String name) {
        this.name = name;
        this.clusters = new HashSet<>();
        this.entities = new HashSet<>();
    }

    public Decomposition() {
        this.clusters = new HashSet<>();
        this.entities = new HashSet<>();
        this.functionalities = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Cluster> getClusters() {
        return clusters;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public Set<Functionality> getFunctionalities() {
        return functionalities;
    }

    public Set<Functionality> getFunctionalities(String clusterName) {
        return getFunctionalities().stream()
                .filter(f -> f.getOrchestrator().equals(clusterName))
                .collect(Collectors.toSet());
    }
}
