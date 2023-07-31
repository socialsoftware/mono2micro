package domain.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterizedDataType extends DataType {

    private List<DataType> parameters;

    public ParameterizedDataType(String name) {
        super(name);
        parameters = new ArrayList<>();
    }

    public List<DataType> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addParameter(DataType dataType) {
        this.parameters.add(dataType);
    }
}
