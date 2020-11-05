package util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import util.Access;
import java.io.IOException;

public class AccessSerializer extends StdSerializer<Access> {
    public AccessSerializer() {
        this(null);
    }

    public AccessSerializer(Class<Access> t) {
        super(t);
    }

    @Override
    public void serialize(
            Access access,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString(access.getMode());
        jsonGenerator.writeNumber(access.getEntityID());
        jsonGenerator.writeEndArray();
    }
}