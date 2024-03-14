package pt.ist.socialsoftware.cml.converter.domain;

public class Field {
    private String name;
    private DataType type;

    public Field(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public Field() {
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }
}
