package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.datatypes.DataType;

import java.io.IOException;

public class DataTypeSerializer extends StdSerializer<DataType> {

    public DataTypeSerializer() {
        this(null);
    }

    protected DataTypeSerializer(Class<DataType> t) {
        super(t);
    }

    @Override
    public void serialize(DataType dataType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", dataType.getName());
        if (dataType.isParameterizedType()) {
            jsonGenerator.writeObjectField("parameters", dataType.getParameters());
        }
        jsonGenerator.writeEndObject();
    }
}
