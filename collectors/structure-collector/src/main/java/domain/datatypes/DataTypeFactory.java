package domain.datatypes;

import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for DataType objects. DataType objects are Value Objects, so the same instance is used to represent a given data type.
 * Use this class to create DataType objects to avoid large amounts of repeated objects during execution.
 */
public class DataTypeFactory {

    private static final Map<String, DataType> createdDataTypes = new HashMap<>();

    private DataTypeFactory() {
    }

    public static DataType getOrCreateDataType(CtTypeReference<?> dataTypeReference) {
        String dataTypeName = dataTypeReference.getSimpleName();

        if (createdDataTypes.containsKey(buildCreatedDataTypeKey(dataTypeName, dataTypeReference))) {
            return createdDataTypes.get(dataTypeName);
        } else {
            return createDataType(dataTypeName, dataTypeReference);
        }
    }

    private static DataType createDataType(String dataTypeName, CtTypeReference<?> dataTypeReference) {
        if (isPrimitiveDataType(dataTypeReference)) {
            return createPrimitiveDataType(dataTypeName);
        } else if (isParametrizedDataType(dataTypeReference)) {
            return createParameterizedDataType(dataTypeName, dataTypeReference);
        } else {
            return createReferenceDataType(dataTypeName);
        }
    }

    private static PrimitiveDataType createPrimitiveDataType(String dataTypeName) {
        PrimitiveDataType primitiveDataType = new PrimitiveDataType(dataTypeName);
        cacheCreatedDataType(primitiveDataType);
        return primitiveDataType;
    }

    // TODO: Enhancement - Consider bounded types and wildcards (List<T extends Type>, List<?>)
    private static ParameterizedDataType createParameterizedDataType(String dataTypeName, CtTypeReference<?> dataTypeReference) {
        ParameterizedDataType parameterizedDataType = new ParameterizedDataType(dataTypeName);
        for (CtTypeReference<?> ctParameter : dataTypeReference.getActualTypeArguments()) {
            parameterizedDataType.addParameter(getOrCreateDataType(ctParameter));
        }
        cacheCreatedDataType(parameterizedDataType);
        return parameterizedDataType;
    }

    private static ReferenceDataType createReferenceDataType(String dataTypeName) {
        ReferenceDataType referenceDataType = new ReferenceDataType(dataTypeName);
        cacheCreatedDataType(referenceDataType);
        return referenceDataType;
    }

    private static boolean isPrimitiveDataType(CtTypeReference<?> dataTypeReference) {
        return dataTypeReference.isPrimitive();
    }

    private static boolean isParametrizedDataType(CtTypeReference<?> dataTypeReference) {
        return dataTypeReference.isParameterized();
    }

    private static String buildCreatedDataTypeKey(String dataTypeName, CtTypeReference<?> dataTypeReference) {
        StringBuilder dataTypeKey = new StringBuilder(dataTypeName);
        if (isParametrizedDataType(dataTypeReference)) {
            for (CtTypeReference<?> ctParameter : dataTypeReference.getActualTypeArguments()) {
                dataTypeKey.append(buildCreatedDataTypeKey(ctParameter.getSimpleName(), ctParameter));
            }
        }
        return dataTypeKey.toString();
    }

    private static void cacheCreatedDataType(DataType dataType) {
        createdDataTypes.put(dataType.toStringKey(), dataType);
    }
}
