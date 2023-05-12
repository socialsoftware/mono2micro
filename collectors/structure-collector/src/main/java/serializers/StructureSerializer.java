package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import processors.ASTCache;

import java.io.IOException;

public class StructureSerializer extends StdSerializer<ASTCache> {

    public StructureSerializer() {
        this(null);
    }

    protected StructureSerializer(Class<ASTCache> t) {
        super(t);
    }

    @Override
    public void serialize(
            ASTCache structure,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectFieldStart("structure");
        jsonGenerator.writeObjectField("entities", structure.getDomainEntities().values());
        jsonGenerator.writeEndObject();
    }
}
