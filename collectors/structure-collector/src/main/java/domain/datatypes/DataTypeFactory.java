package domain.datatypes;

import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.Map;

public class DataTypeFactory {

    private static final Map<String, DataType> createdDataTypes = new HashMap<>();

    private DataTypeFactory() {
    }

    public static DataType getOrCreateDataType(CtTypeReference<?> dataTypeReference) {
        String dataTypeName = dataTypeReference.getSimpleName();

        if (createdDataTypes.containsKey(dataTypeName)) {
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
        createdDataTypes.put(dataTypeName, primitiveDataType);
        return primitiveDataType;
    }

    private static ParameterizedDataType createParameterizedDataType(String dataTypeName, CtTypeReference<?> dataTypeReference) {
        ParameterizedDataType parameterizedDataType = new ParameterizedDataType(dataTypeName);
        for (CtTypeReference<?> ctParameter : dataTypeReference.getActualTypeArguments()) {
            parameterizedDataType.addParameter(getOrCreateDataType(ctParameter));
        }
        createdDataTypes.put(dataTypeName, parameterizedDataType);
        return parameterizedDataType;
    }

    private static ReferenceDataType createReferenceDataType(String dataTypeName) {
        ReferenceDataType referenceDataType = new ReferenceDataType(dataTypeName);
        createdDataTypes.put(dataTypeName, referenceDataType);
        return referenceDataType;
    }

    private static boolean isPrimitiveDataType(CtTypeReference<?> dataTypeReference) {
        return dataTypeReference.isPrimitive();
    }

    private static boolean isParametrizedDataType(CtTypeReference<?> dataTypeReference) {
        return dataTypeReference.isParameterized();
    }
}
