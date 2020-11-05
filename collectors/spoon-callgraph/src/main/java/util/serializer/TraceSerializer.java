package util.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import util.Trace;
import java.io.IOException;

public class TraceSerializer extends StdSerializer<Trace> {
    public TraceSerializer() {
        this(null);
    }

    public TraceSerializer(Class<Trace> t) {
        super(t);
    }

    @Override
    public void serialize(
            Trace trace,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", trace.getId());

        if (trace.getA() != null && trace.getA().size() > 0) {
            jsonGenerator.writeObjectField("a", trace.getA());
        }

        jsonGenerator.writeEndObject();
    }
}