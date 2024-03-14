package pt.ist.socialsoftware.cml.converter.domain;

import java.util.HashSet;
import java.util.Set;

public class Cluster {
    private String name;
    private Set<Entity> elements;

    public Cluster(String name) {
        this.name = name;
        this.elements = new HashSet<>();
    }

    public Cluster() {
        this.elements = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Entity> getElements() {
        return elements;
    }

    public void addElement(Entity m2mEntity) {
        this.elements.add(m2mEntity);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Cluster && getName().equals(((Cluster) obj).getName());
    }
}
