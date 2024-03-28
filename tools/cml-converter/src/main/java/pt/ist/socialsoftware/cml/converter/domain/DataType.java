package pt.ist.socialsoftware.cml.converter.domain;

import java.util.ArrayList;
import java.util.List;

public class DataType {
    private String name;
    private List<DataType> parameters;

    public DataType(String name) {
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    public DataType() {
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<DataType> getParameters() {
        return parameters;
    }

    public boolean isParameterizedType() {
        return !parameters.isEmpty();
    }

    public boolean isCollectionType() {
        return isParameterizedType() &&
                (name.equals("List") || name.equals("Set") || name.equals("Collection"));
    }
}
