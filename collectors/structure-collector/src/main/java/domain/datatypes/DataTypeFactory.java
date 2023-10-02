package domain.datatypes;

import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for DataType objects. DataType objects are Value Objects, so the same instance is used to represent a given data type.
 * Use this class to create DataType objects to avoid large amounts of repeated objects during execution.
 */
public class DataTypeFactory {

    private DataTypeFactory() {
    }

    private static final Map<String, DataType> createdDataTypes = new HashMap<>();

    public static DataType getOrCreateDataType(CtTypeReference<?> dataTypeReference) {
        String dataTypeKey = buildDataTypeKey(dataTypeReference);
        if (createdDataTypes.containsKey(dataTypeKey)) {
            return createdDataTypes.get(dataTypeKey);
        } else {
            return createDataType(dataTypeKey, dataTypeReference);
        }
    }

    private static String buildDataTypeKey(CtTypeReference<?> dataTypeReference) {
        StringBuilder dataTypeKey = new StringBuilder(dataTypeReference.getSimpleName());
        for (CtTypeReference<?> ctParameter : dataTypeReference.getActualTypeArguments()) {
            dataTypeKey.append(buildDataTypeKey(ctParameter));
        }
        return dataTypeKey.toString();
    }

    private static DataType createDataType(String dataTypeKey, CtTypeReference<?> dataTypeReference) {
        DataType dataType = new DataType(dataTypeReference.getSimpleName());
        addParametersToDataType(dataType, dataTypeReference);
        createdDataTypes.put(dataTypeKey, dataType);
        return dataType;
    }

    private static void addParametersToDataType(DataType dataType, CtTypeReference<?> dataTypeReference) {
        for (CtTypeReference<?> ctParameter : dataTypeReference.getActualTypeArguments()) {
            dataType.addParameter(getOrCreateDataType(ctParameter));
        }
    }
}
