package domain.datatypes;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializers.DataTypeSerializer;

import java.util.*;

/**
 * Represents a data type in the source code.
 */
@JsonSerialize(using = DataTypeSerializer.class)
public class DataType {

    private static final Set<String> primitiveTypes = new HashSet<>();

    static {
        primitiveTypes.add("byte");
        primitiveTypes.add("short");
        primitiveTypes.add("int");
        primitiveTypes.add("long");
        primitiveTypes.add("float");
        primitiveTypes.add("double");
        primitiveTypes.add("char");
        primitiveTypes.add("boolean");
    }

    private String name;
    private List<DataType> parameters;

    public DataType(String name) {
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<DataType> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addParameter(DataType dataType) {
        if (!dataType.equals(this)) {
            parameters.add(dataType);
        }
    }

    public boolean isPrimitiveType() {
        return primitiveTypes.contains(name);
    }

    public boolean isParameterizedType() {
        return !parameters.isEmpty();
    }
}
