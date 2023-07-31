package util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import util.ContextReference;
import util.EntityIdentifier;

import java.io.IOException;

public class ContextReferenceSerializer extends StdSerializer<ContextReference> {
    public ContextReferenceSerializer() {
        this(null);
    }

    public ContextReferenceSerializer(Class<ContextReference> t) {
        super(t);
    }

    @Override
    public void serialize(
            ContextReference reference,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeString(EntityIdentifier.CONTEXT_REFERENCE + reference.getContextType());
        jsonGenerator.writeNumber(reference.getEntityID());
        jsonGenerator.writeEndArray();
    }
}