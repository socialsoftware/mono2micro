package pt.ist.socialsoftware.cml.converter.domain;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    private String name;
    private List<Field> fields;
    private DataType superclass;

    public Entity(String name) {
        this.name = name;
        this.fields = new ArrayList<>();
    }

    public Entity() {
        this.fields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public DataType getSuperclass() {
        return superclass;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entity && getName().equals(((Entity) obj).getName());
    }
}
