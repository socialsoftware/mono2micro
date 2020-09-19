package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import domain.CompactedTrace;

import java.io.IOException;

public class CompactedTraceSerializer extends StdSerializer<CompactedTrace> {
	public CompactedTraceSerializer() {
		this(null);
	}

	public CompactedTraceSerializer(Class<CompactedTrace> t) {
		super(t);
	}

	@Override
	public void serialize(
		CompactedTrace compactedTrace,
		JsonGenerator jsonGenerator,
		SerializerProvider serializerProvider
	) throws IOException {
		jsonGenerator.writeStartObject();

		jsonGenerator.writeNumberField("id", compactedTrace.getId());

		if (compactedTrace.getFrequency() > 1) {
			jsonGenerator.writeNumberField("f", compactedTrace.getFrequency());
		}

		if (compactedTrace.getElements().size() > 0) {
			jsonGenerator.writeObjectField("a", compactedTrace.getElements());
		}

		jsonGenerator.writeEndObject();
	}
}
