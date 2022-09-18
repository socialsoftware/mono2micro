package pt.ist.socialsoftware.mono2micro.element;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Element {
    Short id;
    String name;

    public Short getId() { return id; }

    public void setId(Short id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @JsonIgnore
    public abstract String getType();
}
