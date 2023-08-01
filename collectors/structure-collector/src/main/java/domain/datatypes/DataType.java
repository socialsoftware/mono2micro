package domain.datatypes;

/**
 * Represents a data type in the source code.
 */
public abstract class DataType {

    private String name;

    public DataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String toStringKey();
}
