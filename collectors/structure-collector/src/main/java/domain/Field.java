package domain;

import domain.datatypes.DataType;

/**
 * Represents a class field in the source code.
 */
public class Field {

    private String name;
    private DataType type;

    public Field(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }
}
